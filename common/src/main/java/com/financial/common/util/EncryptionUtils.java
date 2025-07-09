package com.financial.common.util;

import com.financial.common.exception.EncryptionException;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encryption-related operations
 * Following utility pattern and static helper methods
 */
@Slf4j
public final class EncryptionUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Private constructor to prevent instantiation
    private EncryptionUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a random AES key of specified length
     * @param keyLength the length in bytes (16, 24, or 32)
     * @return Base64 encoded random key
     */
    public static String generateRandomKey(int keyLength) {
        if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
            throw new IllegalArgumentException(
                    "Key length must be 16, 24, or 32 bytes for AES");
        }

        byte[] keyBytes = new byte[keyLength];
        SECURE_RANDOM.nextBytes(keyBytes);

        log.debug("Generated random AES key of {} bytes", keyLength);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * Validates if a string is a valid Base64 encoded value
     * @param input the string to validate
     * @return true if valid Base64, false otherwise
     */
    public static boolean isValidBase64(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        try {
            Base64.getDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            log.debug("Invalid Base64 format: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates if the provided key has a valid length for AES encryption
     * @param key the key to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAESKeyLength(String key) {
        if (key == null) {
            return false;
        }

        int length = key.getBytes().length;
        return length == 16 || length == 24 || length == 32;
    }

    /**
     * Sanitizes input for logging purposes (removes sensitive data)
     * @param input the input to sanitize
     * @return sanitized string for safe logging
     */
    public static String sanitizeForLogging(String input) {
        if (input == null || input.length() <= 8) {
            return "***";
        }

        return input.substring(0, 4) + "***" + input.substring(input.length() - 4);
    }
}