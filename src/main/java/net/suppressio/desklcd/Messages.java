package net.suppressio.desklcd;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Thin wrapper around a {@link ResourceBundle} for UI strings, resolved
 * from {@code messages.properties} (fallback/English) and
 * {@code messages_it.properties} (Italian) based on the JVM default locale.
 */
public final class Messages {

    private static final ResourceBundle BUNDLE =
            ResourceBundle.getBundle("net.suppressio.desklcd.messages", Locale.getDefault());

    private Messages() {
    }

    /** Plain lookup, no formatting — safe for strings containing literal apostrophes. */
    public static String get(String key) {
        return BUNDLE.getString(key);
    }

    /** Lookup with {@link MessageFormat} substitution for {0}, {1}, ... placeholders. */
    public static String get(String key, Object... args) {
        return MessageFormat.format(BUNDLE.getString(key), args);
    }
}
