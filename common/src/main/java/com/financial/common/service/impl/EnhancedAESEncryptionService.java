package com.financial.common.service.impl;

import com.financial.common.exception.EncryptionException;
import com.financial.common.service.EncryptionService;
import com.financial.common.service.ValidationService;
import com.financial.common.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Enhanced AES implementation with URL-safe Base64 encoding
 */
@Service
@Primary
@Slf4j
public class EnhancedAESEncryptionService implements EncryptionService {

    private final String secretKey;
    private final String algorithm;
    private final ValidationService validationService;

    public EnhancedAESEncryptionService(
            @Value("${encryption.aes.secret-key}") String secretKey,
            @Value("${encryption.aes.algorithm:AES/ECB/PKCS5Padding}") String algorithm,
            ValidationService validationService) {
        this.secretKey = secretKey;
        this.algorithm = algorithm;
        this.validationService = validationService;
        validateConfiguration();
    }

    @Override
    public String encrypt(String data) {
        log.info("Starting encryption process (URL-safe)");

        try {
            // Pre-validation
            validationService.validateEncryptionInput(data);

            // Encryption logic
            Cipher cipher = Cipher.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 🔥 URL-safe Base64 sin padding
            String result = Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);

            log.info("Data encrypted successfully (URL-safe), result length: {}", result.length());
            log.debug("Encrypted data preview: {}", EncryptionUtils.sanitizeForLogging(result));

            return result;

        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during encryption: {}", e.getMessage());
            throw new EncryptionException("Failed to encrypt data: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        log.info("Starting decryption process (URL-safe)");

        try {
            // Pre-validation
            validationService.validateDecryptionInput(encryptedData);

            // Decryption logic
            Cipher cipher = Cipher.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // 🔥 URL-safe Base64 decoder
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            String result = new String(decrypted, StandardCharsets.UTF_8);

            // Post-validation
            validationService.validateCodigoUnico(result);

            log.info("Data decrypted successfully (URL-safe), result length: {}", result.length());
            log.debug("Decrypted data preview: {}", EncryptionUtils.sanitizeForLogging(result));

            return result;

        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during decryption: {}", e.getMessage());
            throw new EncryptionException("Failed to decrypt data: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the encryption configuration on startup
     */
    private void validateConfiguration() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new EncryptionException("Secret key cannot be null or empty");
        }

        if (!EncryptionUtils.isValidAESKeyLength(secretKey)) {
            int keyLength = secretKey.getBytes(StandardCharsets.UTF_8).length;
            throw new EncryptionException(
                    "AES key must be 16, 24, or 32 bytes long. Current length: " + keyLength);
        }

        if (algorithm == null || algorithm.trim().isEmpty()) {
            throw new EncryptionException("Algorithm cannot be null or empty");
        }

        // Test encryption/decryption to validate configuration
        try {
            String testData = "TEST123";
            String encrypted = encryptWithoutValidation(testData);
            String decrypted = decryptWithoutValidation(encrypted);

            if (!testData.equals(decrypted)) {
                throw new EncryptionException("Encryption configuration test failed");
            }

        } catch (Exception e) {
            throw new EncryptionException("Encryption configuration validation failed", e);
        }

        int keyLength = secretKey.getBytes(StandardCharsets.UTF_8).length;
        log.info("Enhanced AES encryption service initialized successfully with {}-bit key (URL-safe)", keyLength * 8);
    }

    /**
     * Internal encryption without validation (for configuration testing)
     */
    private String encryptWithoutValidation(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
    }

    /**
     * Internal decryption without validation (for configuration testing)
     */
    private String decryptWithoutValidation(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getUrlDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}