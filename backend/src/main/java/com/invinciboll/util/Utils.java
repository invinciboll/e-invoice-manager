package com.invinciboll.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

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
