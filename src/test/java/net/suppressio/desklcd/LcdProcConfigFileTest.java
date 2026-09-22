package net.suppressio.desklcd;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LcdProcConfigFileTest {

    @Test
    void parseScreenStates_readsActiveFlagsPerSection() {
        List<String> lines = Arrays.asList(
                "[lcdproc]",
                "Server=localhost",
                "",
                "[CPU]",
                "# Show screen",
                "Active=True",
                "OnTime=1",
                "",
                "[Memory]",
                "Active=false"
        );

        Map<String, Boolean> states = LcdProcConfigFile.parseScreenStates(lines);

        assertTrue(states.get("CPU"));
        assertFalse(states.get("Memory"));
    }

    @Test
    void parseScreenStates_defaultsMissingScreensToFalse() {
        List<String> lines = Arrays.asList("[CPU]", "Active=True");

        Map<String, Boolean> states = LcdProcConfigFile.parseScreenStates(lines);

        for (String screen : LcdProcConfigFile.SCREENS) {
            if (!screen.equals("CPU")) {
                assertFalse(states.get(screen), screen + " should default to false");
            }
        }
    }

    @Test
    void applyScreenStates_rewritesOnlyTargetedActiveLines() {
        List<String> lines = Arrays.asList(
                "[lcdproc]",
                "Server=localhost",
                "",
                "[CPU]",
                "# Show screen",
                "Active=True",
                "OnTime=1",
                "",
                "[Memory]",
                "Active=false"
        );
        Map<String, Boolean> states = new LinkedHashMap<>();
        states.put("CPU", false);
        states.put("Memory", true);

        List<String> result = LcdProcConfigFile.applyScreenStates(lines, states);

        assertEquals(lines.size(), result.size(), "no lines should be added or removed here");
        assertEquals("Active=False", result.get(5));
        assertEquals("Active=True", result.get(9));
        // Everything else must be untouched, in order.
        assertEquals("[lcdproc]", result.get(0));
        assertEquals("Server=localhost", result.get(1));
        assertEquals("OnTime=1", result.get(6));
    }

    @Test
    void applyScreenStates_insertsActiveLineWhenSectionHasNone() {
        List<String> lines = Arrays.asList(
                "[CPU]",
                "OnTime=1",
                "[Memory]",
                "Foo=bar"
        );
        Map<String, Boolean> states = new LinkedHashMap<>();
        states.put("CPU", true);
        states.put("Memory", false);

        List<String> result = LcdProcConfigFile.applyScreenStates(lines, states);

        assertEquals(Arrays.asList(
                "[CPU]",
                "Active=True",
                "OnTime=1",
                "[Memory]",
                "Active=False",
                "Foo=bar"
        ), result);
    }

    @Test
    void applyScreenStates_leavesUnknownSectionsAlone() {
        List<String> lines = Arrays.asList(
                "[menu]",
                "Active=whatever",
                "[CPU]",
                "Active=False"
        );
        Map<String, Boolean> states = new LinkedHashMap<>();
        states.put("CPU", true);

        List<String> result = LcdProcConfigFile.applyScreenStates(lines, states);

        assertEquals("Active=whatever", result.get(1));
        assertEquals("Active=True", result.get(3));
    }
}
