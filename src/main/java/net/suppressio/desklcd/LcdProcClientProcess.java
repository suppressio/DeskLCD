package net.suppressio.desklcd;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Starts/stops the "lcdproc" client binary (the sysinfo client that actually
 * shows CPU/Memory/etc. screens on the LCD, connecting to LCDd). It is not a
 * systemd service, so it is managed here as a plain child process.
 */
public class LcdProcClientProcess {

    private static final String BIN = "/usr/bin/lcdproc";

    private Process process;

    public synchronized boolean isRunningHere() {
        return process != null && process.isAlive();
    }

    public synchronized void start() throws IOException {
        if (isRunningHere()) {
            return;
        }
        ProcessBuilder pb = new ProcessBuilder(BIN, "-f");
        pb.redirectErrorStream(true);
        pb.redirectOutput(new File("/dev/null"));
        process = pb.start();
    }

    public synchronized void stop() {
        if (process == null) {
            return;
        }
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        process = null;
    }

    /** Detects any running lcdproc client, including one started outside this GUI. */
    public static boolean isRunningAnywhere() {
        try {
            Process p = new ProcessBuilder("pgrep", "-x", "lcdproc").start();
            return p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
