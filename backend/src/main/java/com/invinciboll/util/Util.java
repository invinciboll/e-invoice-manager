package com.invinciboll.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Util {

    // Optional: Windows reserved file names (case-insensitive)
    private static final Set<String> WINDOWS_RESERVED_NAMES = new HashSet<>(Arrays.asList(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    ));

    public static String sanitizeSellerName(String sellerName) {
        if (sellerName == null) {
            return null;
        }
        
        // 1. Trim leading and trailing whitespace.
        String sanitized = sellerName.trim();
        
        // 2. Remove any character that is NOT one of:
        //    - Letters (A-Z, a-z)
        //    - Digits (0-9)
        //    - Hyphen (-), Underscore (_), Period (.), Tilde (~)
        //    - Space ( )
        //
        // This allows spaces to be present in the string.
        sanitized = sanitized.replaceAll("[^A-Za-z0-9\\-._~ ]", "");
        
        // 3. Remove any leading or trailing dots (Windows file systems may have issues with these).
        //    Note: Leading/trailing spaces are already removed by trim().
        sanitized = sanitized.replaceAll("^[.]+|[.]+$", "");
        
        // 4. (Optional) If the sanitized name matches a reserved Windows file name, prefix it with an underscore.
        if (WINDOWS_RESERVED_NAMES.contains(sanitized.toUpperCase())) {
            sanitized = "_" + sanitized;
        }
        
        // 5. (Optional) If the resulting string is empty, provide a default value.
        if (sanitized.isEmpty()) {
            sanitized = "_";
        }
        
        return sanitized;
    }

    /**
     * Sanitizes a reference string by replacing any disallowed character
     * with a hyphen (-). Allowed characters: letters, digits, hyphen (-),
     * underscore (_), period (.), tilde (~), and space.
     */
    public static String sanitizeReference(String reference) {
        if (reference == null) {
            return null;
        }
        
        // 1. Trim the input.
        String sanitized = reference.trim();
        
        // 2. Replace any character that is NOT in the allowed set with a hyphen.
        // Allowed characters: A-Z, a-z, 0-9, hyphen (-), underscore (_), period (.),
        // tilde (~), and space.
        sanitized = sanitized.replaceAll("[^A-Za-z0-9\\-._~ ]", "-");
        
        // 3. Remove any leading or trailing dots, which can cause issues on Windows.
        sanitized = sanitized.replaceAll("^[.]+|[.]+$", "");
        
        // 4. If the resulting string is empty, provide a default value.
        if (sanitized.isEmpty()) {
            sanitized = "_na_ref_";
        }
        
        // 5. (Optional) If the sanitized name is a reserved Windows file name, prefix it with an underscore.
        if (WINDOWS_RESERVED_NAMES.contains(sanitized.toUpperCase())) {
            sanitized = "_" + sanitized;
        }
        
        return sanitized;
    }
}
