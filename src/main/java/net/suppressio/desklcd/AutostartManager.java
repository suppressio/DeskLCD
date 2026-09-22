package net.suppressio.desklcd;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Enables/disables launching DeskLCD automatically at login, via a standard
 * XDG autostart .desktop entry in ~/.config/autostart. The Exec= line reuses
 * the command line this very process was started with, so it works whether
 * DeskLCD is run as a jar, via Maven, or as an installed native launcher.
 */
public class AutostartManager {

    private static final Path AUTOSTART_DIR =
            Paths.get(System.getProperty("user.home"), ".config", "autostart");
    private static final Path DESKTOP_FILE = AUTOSTART_DIR.resolve("desklcd.desktop");

    public static boolean isEnabled() {
        return Files.exists(DESKTOP_FILE);
    }

    public static void enable() throws IOException {
        Files.createDirectories(AUTOSTART_DIR);
        String content = "[Desktop Entry]\n"
                + "Type=Application\n"
                + "Name=DeskLCD\n"
                + "Comment=GUI per LCDd/LCDproc\n"
                + "Exec=" + currentCommandLine() + "\n"
                + "X-GNOME-Autostart-enabled=true\n";
        Files.write(DESKTOP_FILE, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void disable() throws IOException {
        Files.deleteIfExists(DESKTOP_FILE);
    }

    private static String currentCommandLine() {
        ProcessHandle.Info info = ProcessHandle.current().info();
        return info.commandLine().orElseGet(() -> {
            String cmd = info.command().orElse("java");
            String[] args = info.arguments().orElse(new String[0]);
            return args.length == 0 ? cmd : cmd + " " + String.join(" ", args);
        });
    }
}
