package net.suppressio.desklcd;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Queries and controls the "lcdproc.service" systemd unit, which runs LCDd
 * (the LCDproc server / hardware driver process).
 */
public class LcdProcSystemService {

    public static final String UNIT = "lcdproc.service";

    public static class Status {
        public final boolean active;
        public final boolean enabled;
        public final String since;

        Status(boolean active, boolean enabled, String since) {
            this.active = active;
            this.enabled = enabled;
            this.since = since;
        }
    }

    public static Status queryStatus() {
        boolean active = runQuiet("systemctl", "is-active", "--quiet", UNIT) == 0;
        boolean enabled = runQuiet("systemctl", "is-enabled", "--quiet", UNIT) == 0;
        String since = runCapture("systemctl", "show", UNIT, "--property=ActiveEnterTimestamp", "--value").trim();
        if (since.isEmpty()) {
            since = "-";
        }
        return new Status(active, enabled, since);
    }

    public static void start() throws IOException, InterruptedException {
        control("start");
    }

    public static void stop() throws IOException, InterruptedException {
        control("stop");
    }

    public static void restart() throws IOException, InterruptedException {
        control("restart");
    }

    private static void control(String action) throws IOException, InterruptedException {
        int code = run("pkexec", "systemctl", action, UNIT);
        if (code != 0) {
            throw new IOException(Messages.get("service.error.exitCode", action, UNIT, code));
        }
    }

    private static int run(String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.redirectOutput(new File("/dev/null"));
        Process p = pb.start();
        if (!p.waitFor(30, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            return -1;
        }
        return p.exitValue();
    }

    private static int runQuiet(String... cmd) {
        try {
            return run(cmd);
        } catch (Exception e) {
            return -1;
        }
    }

    private static String runCapture(String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String out = readAll(p.getInputStream());
            p.waitFor(10, TimeUnit.SECONDS);
            return out;
        } catch (Exception e) {
            return "";
        }
    }

    private static String readAll(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[512];
        int n;
        while ((n = in.read(buf)) != -1) {
            sb.append(new String(buf, 0, n, "UTF-8"));
        }
        return sb.toString();
    }
}
