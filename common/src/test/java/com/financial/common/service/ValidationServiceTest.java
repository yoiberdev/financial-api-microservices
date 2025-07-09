package com.financial.common.service;

import com.financial.common.exception.EncryptionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para ValidationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Validation Service Tests")
class ValidationServiceTest {

    @InjectMocks
    private ValidationService validationService;

    @Test
    @DisplayName("Should validate encryption input successfully")
    void shouldValidateEncryptionInputSuccessfully() {
        // Given
        String validInput = "CUSTOMER001";

        // When & Then
        assertDoesNotThrow(() -> {
            validationService.validateEncryptionInput(validInput);
        });
    }

    @Test
    @DisplayName("Should throw exception for null encryption input")
    void shouldThrowExceptionForNullEncryptionInput() {
        // When & Then
        EncryptionException exception = assertThrows(EncryptionException.class, () -> {
            validationService.validateEncryptionInput(null);
        });

        assertEquals("Data to encrypt cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for empty encryption input")
    void shouldThrowExceptionForEmptyEncryptionInput() {
        // When & Then
        EncryptionException exception = assertThrows(EncryptionException.class, () -> {
            validationService.validateEncryptionInput("");
        });

        assertEquals("Data to encrypt cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for whitespace-only encryption input")
    void shouldThrowExceptionForWhitespaceOnlyEncryptionInput() {
        // When & Then
        EncryptionException exception = assertThrows(EncryptionException.class, () -> {
            validationService.validateEncryptionInput("   ");
        });

        assertEquals("Data to encrypt cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for data exceeding maximum length")
    void shouldThrowExceptionForDataExceedingMaximumLength() {
        // Given
        String tooLongData = "a".repeat(10001); // Excede el límite de 10000

        // When & Then
        EncryptionException exception = assertThrows(EncryptionException.class, () -> {
            validationService.validateEncryptionInput(tooLongData);
        });

        assertEquals("Data exceeds maximum allowed length for encryption", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate decryption input successfully")
    void shouldValidateDecryptionInputSuccessfully() {
        // Given
        String validBase64 = "dGVzdERhdGE"; // "testData" en base64

        // When & Then
        assertDoesNotThrow(() -> {
            validationService.validateDecryptionInput(validBase64);
        });
    }

    @Test
    @DisplayName("Should throw exception for null decryption input")
    void shouldThrowExceptionForNullDecryptionInput() {
        // When & Then
        EncryptionException exception = assertThrows(EncryptionException.class, () -> {
            validationService.validateDecryptionInput(null);
        });

        assertEquals("Encrypted data cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid base64 decryption input")
    void shouldThrowExceptionForInvalidBase64DecryptionInput() {
        // Given
        String invalidBase64 = "this-is-not-valid-base64!@#";

        // When & Then
        EncryptionException exception = assertThrows(EncryptionException.class, () -> {
            validationService.validateDecryptionInput(invalidBase64);
        });

        assertEquals("Encrypted data must be valid Base64 format", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate codigo unico successfully")
    void shouldValidateCodigoUnicoSuccessfully() {
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
                validationService.validateCodigoUnico(codigo);
            }, "Should validate: " + codigo);
        }
    }

    @Test
    @DisplayName("Should throw exception for null codigo unico")
    void shouldThrowExceptionForNullCodigoUnico() {
        // When & Then
        EncryptionException exception = assertThrows(EncryptionException.class, () -> {
            validationService.validateCodigoUnico(null);
        });

        assertEquals("Codigo unico cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for invalid codigo unico format")
    void shouldThrowExceptionForInvalidCodigoUnicoFormat() {
        // Given
        String[] invalidCodigos = {
                "short", // Muy corto
                "toolongcodigomaximumcharacterslimit", // Muy largo
                "invalid-characters!@#", // Caracteres especiales
                "lower123", // Contiene minúsculas
                "SPACES IN CODIGO" // Contiene espacios
        };

        // When & Then
        for (String codigo : invalidCodigos) {
            EncryptionException exception = assertThrows(EncryptionException.class, () -> {
                validationService.validateCodigoUnico(codigo);
            }, "Should reject: " + codigo);

            assertEquals("Codigo unico must be alphanumeric and between 6-20 characters",
                    exception.getMessage());
        }
    }

    @Test
    @DisplayName("Should validate edge case codigo unico lengths")
    void shouldValidateEdgeCaseCodigoUnicoLengths() {
        // Given
        String minLength = "ABCD12"; // 6 caracteres
        String maxLength = "ABCDEFGHIJ1234567890"; // 20 caracteres

        // When & Then
        assertDoesNotThrow(() -> {
            validationService.validateCodigoUnico(minLength);
        });

        assertDoesNotThrow(() -> {
            validationService.validateCodigoUnico(maxLength);
        });
    }
}