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

    public static Map<String, Boolean> readScreenStates() throws IOException {
        return parseScreenStates(Files.readAllLines(Paths.get(PATH), StandardCharsets.UTF_8));
    }

    /**
     * Writes the given screen Active flags back to /etc/lcdproc.conf,
     * preserving everything else in the file. Prompts for authentication via
     * pkexec.
     */
    public static void writeScreenStates(Map<String, Boolean> states) throws IOException, InterruptedException {
        List<String> lines = Files.readAllLines(Paths.get(PATH), StandardCharsets.UTF_8);
        List<String> result = applyScreenStates(lines, states);

        Path tmp = Files.createTempFile("lcdproc-conf-", ".tmp");
        try {
            Files.write(tmp, result, StandardCharsets.UTF_8);

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
}
