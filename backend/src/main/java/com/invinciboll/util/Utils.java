package com.invinciboll.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils {

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

        /**
     * Computes the MD5 hash of the given file using the specified hash algorithm.
     * @param inputFile Path to the input file.
     * @return Hexadecimal string representation of the file hash.
     * @throws IOException If an I/O error occurs while reading the file.
     * @throws IllegalArgumentException If the specified hash algorithm is not supported.
     * @throws RuntimeException If the hash algorithm is not found (should never happen for MD5).
     * */
    public static String computeFileHash(Path inputFile) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            try (InputStream is = Files.newInputStream(inputFile);
                DigestInputStream dis = new DigestInputStream(is, md)) {
                while (dis.read() != -1) {
                    // No need to process the data, just read to update the digest
                }
            }

            byte[] digest = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // should never happen
            throw new RuntimeException("Algorithm MD5 not found: " + e.getMessage());
        } catch (IOException e) {
           throw new IOException("Error reading file: " + e.getMessage());
        }
    }
}
