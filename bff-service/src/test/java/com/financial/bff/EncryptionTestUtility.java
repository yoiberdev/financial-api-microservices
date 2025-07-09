package com.financial.bff;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionTestUtility {

    private static final String SECRET_KEY = "MySecretKey12345"; // Misma clave del application.yml
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public static void main(String[] args) {
        System.out.println("🔐 Financial API - Encryption Test Utility");
        System.out.println("==========================================");

        // Códigos de prueba
        String[] testCodes = {
                "CUST001",
                "CUST002",
                "CUST003",
                "CUSTOMER001",
                "USER123456"
        };

        System.out.println("📝 Generating encrypted codes for testing:");
        System.out.println();

        for (String code : testCodes) {
            try {
                String encrypted = encrypt(code);
                System.out.println("Original: " + code);
                System.out.println("Encrypted: " + encrypted);
                System.out.println("Test URL: http://localhost:8080/api/customer-info/" + encrypted);
                System.out.println("---");

                // Verificar que se puede desencriptar
                String decrypted = decrypt(encrypted);
                if (code.equals(decrypted)) {
                    System.out.println("✅ Encryption/Decryption: OK");
                } else {
                    System.out.println("❌ Encryption/Decryption: FAILED");
                }
                System.out.println();

            } catch (Exception e) {
                System.err.println("❌ Error encrypting " + code + ": " + e.getMessage());
            }
        }

        System.out.println("🚀 Test these URLs in your browser or Postman:");
        System.out.println("   http://localhost:8080/health");
        System.out.println("   http://localhost:8080/webjars/swagger-ui/index.html");
        System.out.println("   http://localhost:8080/api/customer-info/{encrypted_code}");
    }

    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}