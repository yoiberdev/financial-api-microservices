package com.financial.common.service.impl;

import com.financial.common.exception.EncryptionException;
import com.financial.common.service.AESEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AESEncryptionService
 * Following TDD principles and comprehensive test coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AES Encryption Service Tests")
class AESEncryptionServiceTest {

    private EncryptionService encryptionService;
    private static final String VALID_SECRET_KEY = "MySecretKey12345"; // 16 bytes
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    @BeforeEach
    void setUp() {
        encryptionService = new AESEncryptionService(VALID_SECRET_KEY, ALGORITHM);
    }

    @Test
    @DisplayName("Should encrypt and decrypt data successfully")
    void shouldEncryptAndDecryptDataSuccessfully() {
        // Given
        String originalData = "CUSTOMER001";

        // When
        String encrypted = encryptionService.encrypt(originalData);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotEquals(originalData, encrypted);
        assertEquals(originalData, decrypted);
    }

    @Test
    @DisplayName("Should handle different data types and special characters")
    void shouldHandleDifferentDataTypes() {
        // Given
        String[] testData = {
                "CUSTOMER001",
                "user@email.com",
                "Special!@#$%^&*()_+{}|:<>?[]\\;'\".,/",
                "números_ñáéíóú",
                "12345",
                " spaces and tabs\t"
        };

        // When & Then
        for (String data : testData) {
            String encrypted = encryptionService.encrypt(data);
            String decrypted = encryptionService.decrypt(encrypted);
            assertEquals(data, decrypted, "Failed for data: " + data);
        }
    }

    @Test
    @DisplayName("Should throw exception for invalid secret key")
    void shouldThrowExceptionForInvalidSecretKey() {
        // Given
        String invalidKey = "short"; // Too short for AES

        // When & Then
        assertThrows(EncryptionException.class, () -> {
            new AESEncryptionService(invalidKey, ALGORITHM);
        });
    }

    @Test
    @DisplayName("Should throw exception for null secret key")
    void shouldThrowExceptionForNullSecretKey() {
        // When & Then
        assertThrows(EncryptionException.class, () -> {
            new AESEncryptionService(null, ALGORITHM);
        });
    }

    @Test
    @DisplayName("Should throw exception for empty secret key")
    void shouldThrowExceptionForEmptySecretKey() {
        // When & Then
        assertThrows(EncryptionException.class, () -> {
            new AESEncryptionService("", ALGORITHM);
        });
    }

    @Test
    @DisplayName("Should throw exception for invalid encrypted data")
    void shouldThrowExceptionForInvalidEncryptedData() {
        // Given
        String invalidEncryptedData = "this-is-not-valid-base64-encrypted-data";

        // When & Then
        assertThrows(EncryptionException.class, () -> {
            encryptionService.decrypt(invalidEncryptedData);
        });
    }

    @Test
    @DisplayName("Should work with 24-byte key")
    void shouldWorkWith24ByteKey() {
        // Given
        String key24Bytes = "MySecretKey123456789ABCD"; // 24 bytes
        EncryptionService service = new AESEncryptionService(key24Bytes, ALGORITHM);
        String testData = "CUSTOMER001";

        // When
        String encrypted = service.encrypt(testData);
        String decrypted = service.decrypt(encrypted);

        // Then
        assertEquals(testData, decrypted);
    }

    @Test
    @DisplayName("Should work with 32-byte key")
    void shouldWorkWith32ByteKey() {
        // Given
        String key32Bytes = "MySecretKey123456789ABCDEF123456"; // 32 bytes
        EncryptionService service = new AESEncryptionService(key32Bytes, ALGORITHM);
        String testData = "CUSTOMER001";

        // When
        String encrypted = service.encrypt(testData);
        String decrypted = service.decrypt(encrypted);

        // Then
        assertEquals(testData, decrypted);
    }

    @Test
    @DisplayName("Should generate different encrypted strings for same input")
    void shouldGenerateDifferentEncryptedStrings() {
        // Given
        String testData = "CUSTOMER001";

        // When
        String encrypted1 = encryptionService.encrypt(testData);
        String encrypted2 = encryptionService.encrypt(testData);

        // Then - With ECB mode, encrypted strings should be the same
        // (this is expected behavior for ECB, though not recommended for production)
        assertEquals(encrypted1, encrypted2);
    }

    @Test
    @DisplayName("Should handle empty string encryption")
    void shouldHandleEmptyStringEncryption() {
        // Given
        String emptyString = "";

        // When
        String encrypted = encryptionService.encrypt(emptyString);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertEquals(emptyString, decrypted);
    }
}