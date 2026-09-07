package com.financial.common.service;

import com.financial.common.exception.EncryptionException;
import com.financial.common.service.impl.EncryptionService;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Implementacion AES basica (sin ValidationService).
 *
 * NO es un bean de Spring: antes llevaba {@code @Service @Primary} igual que
 * {@link com.financial.common.service.impl.EnhancedAESEncryptionService}, lo que dejaba dos
 * candidatos @Primary del mismo tipo y hacia fallar el arranque con
 * NoUniqueBeanDefinitionException. Se registra a demanda desde
 * {@link com.financial.common.config.EncryptionConfiguration} con
 * encryption.aes.implementation=basic.
 */
@Slf4j
public class AESEncryptionService implements EncryptionService {

    private final String secretKey;
    private final String algorithm;

    public AESEncryptionService(String secretKey, String algorithm) {
        this.secretKey = secretKey;
        this.algorithm = algorithm;
        validateKey();
    }

    @Override
    public String encrypt(String data) {
        try {
            log.debug("Encrypting data with AES algorithm");

            Cipher cipher = Cipher.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 🔥 CAMBIO PRINCIPAL: Usar Base64 URL-safe
            String result = Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);

            log.debug("Data encrypted successfully (URL-safe)");
            return result;

        } catch (Exception e) {
            log.error("Error encrypting data: {}", e.getMessage());
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        try {
            log.debug("Decrypting data with AES algorithm");

            Cipher cipher = Cipher.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            // 🔥 CAMBIO PRINCIPAL: Usar Base64 URL-safe decoder
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            String result = new String(decrypted, StandardCharsets.UTF_8);

            log.debug("Data decrypted successfully (URL-safe)");
            return result;

        } catch (Exception e) {
            log.error("Error decrypting data: {}", e.getMessage());
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    private void validateKey() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new EncryptionException("Secret key cannot be null or empty");
        }

        int keyLength = secretKey.getBytes(StandardCharsets.UTF_8).length;
        if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
            throw new EncryptionException(
                    "AES key must be 16, 24, or 32 bytes long. Current length: " + keyLength);
        }

        log.info("AES encryption service initialized with {}-bit key (URL-safe)", keyLength * 8);
    }
}