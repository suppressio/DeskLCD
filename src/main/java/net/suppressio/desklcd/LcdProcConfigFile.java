package net.suppressio.desklcd;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads and writes the "Active" flag of each screen section in
 * /etc/lcdproc.conf, which controls which information screens the lcdproc
 * client shows on the LCD. Writing requires root and is done via pkexec.
 */
public class LcdProcConfigFile {

    public static final String PATH = "/etc/lcdproc.conf";

    /** Screen section names, in the same order lcdproc --help lists them. */
    public static final String[] SCREENS = {
            "CPU", "SMP-CPU", "CPUGraph", "Load", "Memory", "ProcSize",
            "Disk", "Iface", "Battery", "TimeDate", "OldTime", "Uptime",
            "BigClock", "MiniClock", "About"
    };

    private static final Pattern SECTION = Pattern.compile("^\\s*\\[([^\\]]+)\\]\\s*$");
    private static final Pattern ACTIVE = Pattern.compile("^(\\s*Active\\s*=\\s*)(\\S+)(.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_VALUE = Pattern.compile("^([A-Za-z]+)\\s*=(.*)$");

    /** The [lcdproc] section's connection/runtime settings (not which screens are shown). */
    public static class ConnectionSettings {
        public String server = "localhost";
        public String port = "13666";
        public String reportLevel = "2";
        public boolean reportToSyslog;
        public boolean foreground;
        public String pidFile = "";
        public String displayName = "";
    }

    public static Map<String, Boolean> readScreenStates() throws IOException {
        return parseScreenStates(readLines());
    }

    /**
     * Writes the given screen Active flags back to /etc/lcdproc.conf,
     * preserving everything else in the file. Prompts for authentication via
     * pkexec.
     */
    public static void writeScreenStates(Map<String, Boolean> states) throws IOException, InterruptedException {
        writeLines(applyScreenStates(readLines(), states));
    }

    public static ConnectionSettings readConnectionSettings() throws IOException {
        return parseConnectionSettings(readLines());
    }

    /**
     * Writes the [lcdproc] connection settings back to /etc/lcdproc.conf,
     * preserving everything else in the file. Prompts for authentication via
     * pkexec.
     */
    public static void writeConnectionSettings(ConnectionSettings settings) throws IOException, InterruptedException {
        writeLines(applyConnectionSettings(readLines(), settings));
    }

    private static List<String> readLines() throws IOException {
        return Files.readAllLines(Paths.get(PATH), StandardCharsets.UTF_8);
    }

    private static void writeLines(List<String> lines) throws IOException, InterruptedException {
        Path tmp = Files.createTempFile("lcdproc-conf-", ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder("pkexec", "tee", PATH);
            pb.redirectInput(tmp.toFile());
            pb.redirectOutput(new File("/dev/null"));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            if (!p.waitFor(60, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new IOException(Messages.get("config.error.timeout"));
            }
            if (p.exitValue() != 0) {
                throw new IOException(Messages.get("config.error.saveFailed", p.exitValue()));
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    /**
     * Parses the Active flag of each known screen section out of the given
     * file content. Package-private: pure logic, exercised directly by
     * tests instead of only through the real /etc/lcdproc.conf.
     */
    static Map<String, Boolean> parseScreenStates(List<String> lines) {
        Map<String, Boolean> states = new LinkedHashMap<>();
        for (String s : SCREENS) {
            states.put(s, Boolean.FALSE);
        }

        String currentSection = null;
        for (String line : lines) {
            Matcher sm = SECTION.matcher(line);
            if (sm.matches()) {
                currentSection = sm.group(1).trim();
                continue;
            }
            if (currentSection != null && states.containsKey(currentSection)) {
                Matcher am = ACTIVE.matcher(line);
                if (am.matches()) {
                    states.put(currentSection, Boolean.parseBoolean(am.group(2).trim()));
                }
            }
        }
        return states;
    }

    /**
     * Returns the given file content with each known screen section's
     * Active flag rewritten to match states, leaving everything else
     * untouched. Package-private: pure logic, exercised directly by tests.
     */
    static List<String> applyScreenStates(List<String> lines, Map<String, Boolean> states) {
        List<String> result = new ArrayList<>(lines.size());
        Set<String> pending = new HashSet<>(states.keySet());

        String currentSection = null;
        for (String line : lines) {
            Matcher sm = SECTION.matcher(line);
            if (sm.matches()) {
                currentSection = sm.group(1).trim();
                result.add(line);
                continue;
            }
            if (currentSection != null && states.containsKey(currentSection)) {
                Matcher am = ACTIVE.matcher(line);
                if (am.matches()) {
                    boolean value = states.get(currentSection);
                    result.add(am.group(1) + (value ? "True" : "False") + am.group(3));
                    pending.remove(currentSection);
                    continue;
                }
            }
            result.add(line);
        }

        if (pending.isEmpty()) {
            return result;
        }

        List<String> withInserts = new ArrayList<>(result.size() + pending.size());
        for (String line : result) {
            withInserts.add(line);
            Matcher sm = SECTION.matcher(line);
            if (sm.matches()) {
                String section = sm.group(1).trim();
                if (pending.remove(section)) {
                    withInserts.add("Active=" + (states.get(section) ? "True" : "False"));
                }
            }
        }
        return withInserts;
    }

    /**
     * Parses the [lcdproc] section's connection/runtime settings out of the
     * given file content, falling back to ConnectionSettings' defaults for
     * anything absent (including commented-out lines). Package-private:
     * pure logic, exercised directly by tests.
     */
    static ConnectionSettings parseConnectionSettings(List<String> lines) {
        Map<String, String> raw = new LinkedHashMap<>();
        String currentSection = null;
        for (String line : lines) {
            Matcher sm = SECTION.matcher(line);
            if (sm.matches()) {
                currentSection = sm.group(1).trim();
                continue;
            }
            if ("lcdproc".equals(currentSection)) {
                Matcher km = KEY_VALUE.matcher(line);
                if (km.matches()) {
                    raw.put(km.group(1), km.group(2).trim());
                }
            }
        }

        ConnectionSettings settings = new ConnectionSettings();
        settings.server = raw.getOrDefault("Server", settings.server);
        settings.port = raw.getOrDefault("Port", settings.port);
        settings.reportLevel = raw.getOrDefault("ReportLevel", settings.reportLevel);
        settings.reportToSyslog = Boolean.parseBoolean(raw.getOrDefault("ReportToSyslog", "false"));
        settings.foreground = Boolean.parseBoolean(raw.getOrDefault("Foreground", "false"));
        settings.pidFile = raw.getOrDefault("PidFile", settings.pidFile);
        settings.displayName = raw.getOrDefault("DisplayName", settings.displayName);
        return settings;
    }

    /**
     * Returns the given file content with the [lcdproc] section's
     * connection settings rewritten to match settings, leaving everything
     * else untouched. Server/Port/ReportLevel/ReportToSyslog are always
     * written explicitly; Foreground/PidFile/DisplayName are optional and
     * their line is removed entirely (falling back to the compiled-in
     * default) when left at "off"/empty. Package-private: pure logic,
     * exercised directly by tests.
     */
    static List<String> applyConnectionSettings(List<String> lines, ConnectionSettings settings) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("Server", settings.server);
        values.put("Port", settings.port);
        values.put("ReportLevel", settings.reportLevel);
        values.put("ReportToSyslog", String.valueOf(settings.reportToSyslog));
        values.put("Foreground", settings.foreground ? "true" : null);
        values.put("PidFile", settings.pidFile.isEmpty() ? null : settings.pidFile);
        values.put("DisplayName", settings.displayName.isEmpty() ? null : settings.displayName);
        return applySectionValues(lines, "lcdproc", values);
    }

    /**
     * Rewrites, within targetSection only, each line matching a key in
     * values: a null value removes the line entirely, a non-null value
     * replaces it (or is inserted right after the section header if the
     * key was absent). Lines outside targetSection, and lines for keys not
     * in values, are left untouched.
     */
    private static List<String> applySectionValues(List<String> lines, String targetSection, Map<String, String> values) {
        Set<String> pending = new LinkedHashSet<>();
        for (Map.Entry<String, String> e : values.entrySet()) {
            if (e.getValue() != null) {
                pending.add(e.getKey());
            }
        }

        List<String> result = new ArrayList<>(lines.size());
        String currentSection = null;
        boolean inTarget = false;
        for (String line : lines) {
            Matcher sm = SECTION.matcher(line);
            if (sm.matches()) {
                currentSection = sm.group(1).trim();
                inTarget = targetSection.equals(currentSection);
                result.add(line);
                continue;
            }
            if (inTarget) {
                Matcher km = KEY_VALUE.matcher(line);
                if (km.matches() && values.containsKey(km.group(1))) {
                    String key = km.group(1);
                    String newValue = values.get(key);
                    pending.remove(key);
                    if (newValue != null) {
                        result.add(key + "=" + newValue);
                    }
                    continue;
                }
            }
            result.add(line);
        }

        if (pending.isEmpty()) {
            return result;
        }

        List<String> withInserts = new ArrayList<>(result.size() + pending.size());
        boolean inserted = false;
        for (String line : result) {
            withInserts.add(line);
            Matcher sm = SECTION.matcher(line);
            if (sm.matches() && !inserted && targetSection.equals(sm.group(1).trim())) {
                for (String key : pending) {
                    withInserts.add(key + "=" + values.get(key));
                }
                inserted = true;
            }
        }
        return withInserts;
    }
}
