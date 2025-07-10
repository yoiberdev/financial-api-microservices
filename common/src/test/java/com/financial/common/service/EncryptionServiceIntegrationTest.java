package com.financial.common.service;

import com.financial.common.config.EncryptionConfiguration;
import com.financial.common.exception.EncryptionException;
import com.financial.common.service.impl.EncryptionService;
import com.financial.common.service.impl.EnhancedAESEncryptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete encryption service setup
 * Testing the full Spring context and configuration
 */
@SpringBootTest(classes = {
        EncryptionConfiguration.class,
        EnhancedAESEncryptionService.class,
        ValidationService.class
})
@TestPropertySource(properties = {
        "encryption.aes.secret-key=MySecretKey12345",
        "encryption.aes.algorithm=AES/ECB/PKCS5Padding"
})
@DisplayName("Encryption Service Integration Tests")
class EncryptionServiceIntegrationTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    @DisplayName("Should autowire encryption service successfully")
    void shouldAutowireEncryptionServiceSuccessfully() {
        assertNotNull(encryptionService);
        assertInstanceOf(EnhancedAESEncryptionService.class, encryptionService);
    }

    @Test
    @DisplayName("Should perform full encryption/decryption cycle with validation")
    void shouldPerformFullEncryptionDecryptionCycleWithValidation() {
        // Given
        String originalCodigoUnico = "CUSTOMER001";

        // When
        String encrypted = encryptionService.encrypt(originalCodigoUnico);
        String decrypted = encryptionService.decrypt(encrypted);

        // Then
        assertNotNull(encrypted);
        assertNotEquals(originalCodigoUnico, encrypted);
        assertEquals(originalCodigoUnico, decrypted);
    }

    @Test
    @DisplayName("Should validate codigo unico format during decryption")
    void shouldValidateCodigoUnicoFormatDuringDecryption() {
        // Given - First encrypt a valid codigo unico
        String validCodigoUnico = "CUSTOMER001";
        String encrypted = encryptionService.encrypt(validCodigoUnico);

        // When & Then - Should decrypt successfully
        assertDoesNotThrow(() -> {
            String decrypted = encryptionService.decrypt(encrypted);
            assertEquals(validCodigoUnico, decrypted);
        });
    }

    @Test
    @DisplayName("Should reject invalid input during encryption")
    void shouldRejectInvalidInputDuringEncryption() {
        // When & Then
        assertThrows(EncryptionException.class, () -> {
            encryptionService.encrypt(null);
        });

        assertThrows(EncryptionException.class, () -> {
            encryptionService.encrypt("");
        });

        assertThrows(EncryptionException.class, () -> {
            encryptionService.encrypt("   ");
        });
    }

    @Test
    @DisplayName("Should reject invalid encrypted data during decryption")
    void shouldRejectInvalidEncryptedDataDuringDecryption() {
        // When & Then
        assertThrows(EncryptionException.class, () -> {
            encryptionService.decrypt("invalid-base64-data");
        });

        assertThrows(EncryptionException.class, () -> {
            encryptionService.decrypt("");
        });

        assertThrows(EncryptionException.class, () -> {
            encryptionService.decrypt(null);
        });
    }

    @Test
    @DisplayName("Should handle multiple valid codigo unico formats")
    void shouldHandleMultipleValidCodigoUnicoFormats() {
        // Given
        String[] validCodigos = {
                "CUSTOMER001",
                "USER123456",
                "ACCOUNT789",
                "CLIENT000001",
                "ABCD1234567890"
        };

        // When & Then
        for (String codigo : validCodigos) {
            assertDoesNotThrow(() -> {
                String encrypted = encryptionService.encrypt(codigo);
                String decrypted = encryptionService.decrypt(encrypted);
                assertEquals(codigo, decrypted);
            }, "Failed for codigo: " + codigo);
        }
    }

    @Test
    @DisplayName("Should maintain consistency across multiple operations")
    void shouldMaintainConsistencyAcrossMultipleOperations() {
        // Given
        String codigoUnico = "CUSTOMER001";

        // When - Encrypt and decrypt multiple times
        String encrypted1 = encryptionService.encrypt(codigoUnico);
        String decrypted1 = encryptionService.decrypt(encrypted1);

        String encrypted2 = encryptionService.encrypt(codigoUnico);
        String decrypted2 = encryptionService.decrypt(encrypted2);

        // Then
        assertEquals(codigoUnico, decrypted1);
        assertEquals(codigoUnico, decrypted2);
        assertEquals(encrypted1, encrypted2); // ECB mode produces same output for same input
    }
}