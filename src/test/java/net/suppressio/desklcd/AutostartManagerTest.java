package net.suppressio.desklcd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AutostartManagerTest {

    @Test
    void buildDesktopEntry_includesRequiredXdgFieldsAndExecLine() {
        String entry = AutostartManager.buildDesktopEntry("java -jar /opt/desklcd/desklcd.jar");

        assertTrue(entry.startsWith("[Desktop Entry]\n"));
        assertTrue(entry.contains("Type=Application\n"));
        assertTrue(entry.contains("Name=DeskLCD\n"));
        assertTrue(entry.contains("Exec=java -jar /opt/desklcd/desklcd.jar\n"));
        assertTrue(entry.contains("X-GNOME-Autostart-enabled=true\n"));
    }
}
