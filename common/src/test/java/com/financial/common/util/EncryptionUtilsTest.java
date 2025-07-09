package com.financial.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para EncryptionUtils
 */
@DisplayName("Encryption Utils Tests")
class EncryptionUtilsTest {

    @Test
    @DisplayName("Should generate random key of valid lengths")
    void shouldGenerateRandomKeyOfValidLengths() {
        // Given & When
        String key16 = EncryptionUtils.generateRandomKey(16);
        String key24 = EncryptionUtils.generateRandomKey(24);
        String key32 = EncryptionUtils.generateRandomKey(32);

        // Then
        assertNotNull(key16);
        assertNotNull(key24);
        assertNotNull(key32);

        // Verificar que son diferentes
        assertNotEquals(key16, key24);
        assertNotEquals(key16, key32);
        assertNotEquals(key24, key32);

        // Verificar que son válidos en base64
        assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(key16));
        assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(key24));
        assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(key32));
    }

    @Test
    @DisplayName("Should throw exception for invalid key lengths")
    void shouldThrowExceptionForInvalidKeyLengths() {
        // Given
        int[] invalidLengths = {8, 12, 15, 17, 20, 25, 30, 33, 40};

        // When & Then
        for (int length : invalidLengths) {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                EncryptionUtils.generateRandomKey(length);
            });

            assertEquals("Key length must be 16, 24, or 32 bytes for AES", exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should validate base64 strings correctly")
    void shouldValidateBase64StringsCorrectly() {
        // Given
        String[] validBase64 = {
                "dGVzdA==",
                "SGVsbG8gV29ybGQ=",
                "QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
        };

        String[] invalidBase64 = {
                "this-is-not-base64!@#",
                "contains spaces",
                "contains\nnewlines",
                "invalid=characters",
                null
        };

        // When & Then
        for (String valid : validBase64) {
            assertTrue(EncryptionUtils.isValidBase64(valid),
                    "Should be valid: " + valid);
        }

        for (String invalid : invalidBase64) {
            assertFalse(EncryptionUtils.isValidBase64(invalid),
                    "Should be invalid: " + invalid);
        }
    }

    @Test
    @DisplayName("Should validate AES key lengths correctly")
    void shouldValidateAESKeyLengthsCorrectly() {
        // Given
        String key16 = "1234567890123456"; // 16 bytes
        String key24 = "123456789012345678901234"; // 24 bytes
        String key32 = "12345678901234567890123456789012"; // 32 bytes
        String keyInvalid = "short"; // 5 bytes
        String keyTooLong = "123456789012345678901234567890123"; // 33 bytes

        // When & Then
        assertTrue(EncryptionUtils.isValidAESKeyLength(key16));
        assertTrue(EncryptionUtils.isValidAESKeyLength(key24));
        assertTrue(EncryptionUtils.isValidAESKeyLength(key32));
        assertFalse(EncryptionUtils.isValidAESKeyLength(keyInvalid));
        assertFalse(EncryptionUtils.isValidAESKeyLength(keyTooLong));
        assertFalse(false);
    }

    @Test
    @DisplayName("Should sanitize strings for logging correctly")
    void shouldSanitizeStringsForLoggingCorrectly() {
        // Given & When & Then
        assertEquals("***", EncryptionUtils.sanitizeForLogging(null));
        assertEquals("***", EncryptionUtils.sanitizeForLogging(""));
        assertEquals("***", EncryptionUtils.sanitizeForLogging("short"));
        assertEquals("***", EncryptionUtils.sanitizeForLogging("12345678"));
        assertEquals("1234***5678", EncryptionUtils.sanitizeForLogging("123456785678"));
        assertEquals("test***data", EncryptionUtils.sanitizeForLogging("testSensitivedata"));
        assertEquals("CUST***R001", EncryptionUtils.sanitizeForLogging("CUSTOMER001"));

    }

    @Test
    @DisplayName("Should handle unicode characters in sanitization")
    void shouldHandleUnicodeCharactersInSanitization() {
        // Given
        String unicodeString = "TËST1234ÑÁÉÍ";

        // When
        String sanitized = EncryptionUtils.sanitizeForLogging(unicodeString);

        // Then
        assertEquals("TËST***ÑÁÉÍ", sanitized);
    }

    @Test
    @DisplayName("Should handle edge cases in base64 validation")
    void shouldHandleEdgeCasesInBase64Validation() {
        // Given
        String urlSafeBase64 = "dGVzdA"; // URL-safe sin padding
        String standardBase64 = "dGVzdA=="; // Estándar con padding
        String whitespaceString = "   ";

        // When & Then
        assertTrue(EncryptionUtils.isValidBase64(urlSafeBase64));
        assertTrue(EncryptionUtils.isValidBase64(standardBase64));
        assertFalse(EncryptionUtils.isValidBase64(whitespaceString));
    }

    @Test
    @DisplayName("Should handle multibyte characters in key length validation")
    void shouldHandleMultibyteCharactersInKeyLengthValidation() {
        // Given
        String unicodeKey = "TËST1234ÑÁÉÍ5678"; // Contiene caracteres multibyte

        // When & Then
        // El método cuenta bytes, no caracteres, así que esto debería ser inválido
        // porque los caracteres unicode ocupan más de 1 byte cada uno
        assertFalse(EncryptionUtils.isValidAESKeyLength(unicodeKey));
    }
}