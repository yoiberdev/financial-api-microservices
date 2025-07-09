package com.financial.common.service;

import com.financial.common.exception.EncryptionException;
import com.financial.common.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service for validating inputs before encryption/decryption operations
 * Following Single Responsibility Principle - only handles validation
 */
@Service
@Slf4j
public class ValidationService {

    /**
     * Validates input data before encryption
     * @param data the data to validate
     * @throws EncryptionException if validation fails
     */
    public void validateEncryptionInput(String data) {
        if (!StringUtils.hasText(data)) {
            log.warn("Attempted to encrypt null or empty data");
            throw new EncryptionException("Data to encrypt cannot be null or empty");
        }

        // Additional validation rules can be added here
        if (data.length() > 10000) { // Example: limit data size
            log.warn("Attempted to encrypt data exceeding maximum length");
            throw new EncryptionException("Data exceeds maximum allowed length for encryption");
        }

        log.debug("Encryption input validation passed for data of length: {}", data.length());
    }

    /**
     * Validates encrypted data before decryption
     * @param encryptedData the encrypted data to validate
     * @throws EncryptionException if validation fails
     */
    public void validateDecryptionInput(String encryptedData) {
        if (!StringUtils.hasText(encryptedData)) {
            log.warn("Attempted to decrypt null or empty data");
            throw new EncryptionException("Encrypted data cannot be null or empty");
        }

        if (!EncryptionUtils.isValidBase64(encryptedData)) {
            log.warn("Attempted to decrypt invalid Base64 data: {}",
                    EncryptionUtils.sanitizeForLogging(encryptedData));
            throw new EncryptionException("Encrypted data must be valid Base64 format");
        }

        log.debug("Decryption input validation passed for data: {}",
                EncryptionUtils.sanitizeForLogging(encryptedData));
    }

    /**
     * Validates codigo unico format
     * @param codigoUnico the codigo unico to validate
     * @throws EncryptionException if validation fails
     */
    public void validateCodigoUnico(String codigoUnico) {
        if (!StringUtils.hasText(codigoUnico)) {
            log.warn("Attempted to process null or empty codigo unico");
            throw new EncryptionException("Codigo unico cannot be null or empty");
        }

        // Example: validate format (alphanumeric, specific length, etc.)
        if (!codigoUnico.matches("^[A-Z0-9]{6,20}$")) {
            log.warn("Invalid codigo unico format: {}",
                    EncryptionUtils.sanitizeForLogging(codigoUnico));
            throw new EncryptionException("Codigo unico must be alphanumeric and between 6-20 characters");
        }

        log.debug("Codigo unico validation passed: {}",
                EncryptionUtils.sanitizeForLogging(codigoUnico));
    }
}