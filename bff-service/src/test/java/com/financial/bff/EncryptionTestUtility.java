package com.financial.bff;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionTestUtility {

    private static final String SECRET_KEY = "MySecretKey12345"; // 16 bytes
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public static void main(String[] args) {
        System.out.println("🔐 Financial API - Encryption Test Utility");
        System.out.println("==========================================");

        // Códigos de prueba disponibles en la BD
        String[] testCodes = {
                "CUST001",
                "CUST002",
                "CUST003",
                "CUST004",
                "CUST005"
        };

        System.out.println("📝 Generating encrypted codes for testing:");
        System.out.println();

        for (String code : testCodes) {
            try {
                String encrypted = encrypt(code);
                System.out.println("✅ Original: " + code);
                System.out.println("   Encrypted: " + encrypted);
                System.out.println("   Test URL: http://localhost:8080/api/customer-info/" + encrypted);
                System.out.println("   Postman Variable: " + code.toLowerCase() + "_encrypted = " + encrypted);
                System.out.println();

                // Verificar que se puede desencriptar
                String decrypted = decrypt(encrypted);
                if (code.equals(decrypted)) {
                    System.out.println("   ✅ Encryption/Decryption: OK");
                } else {
                    System.out.println("   ❌ Encryption/Decryption: FAILED");
                }
                System.out.println("   ---");

            } catch (Exception e) {
                System.err.println("❌ Error encrypting " + code + ": " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("🚀 POSTMAN COLLECTION VARIABLES:");
        System.out.println("Copy these to your Postman environment:");
        System.out.println();

        for (String code : testCodes) {
            try {
                String encrypted = encrypt(code);
                System.out.println("{");
                System.out.println("  \"key\": \"" + code.toLowerCase() + "_encrypted\",");
                System.out.println("  \"value\": \"" + encrypted + "\",");
                System.out.println("  \"type\": \"string\"");
                System.out.println("},");
            } catch (Exception e) {
                System.err.println("Error generating " + code);
            }
        }

        System.out.println();
        System.out.println("🧪 CURL COMMANDS FOR TESTING:");
        System.out.println();

        for (String code : testCodes) {
            try {
                String encrypted = encrypt(code);
                System.out.println("# Test " + code + ":");
                System.out.println("curl -X GET \\");
                System.out.println("  -H \"Correlation-ID: test-" + System.currentTimeMillis() + "\" \\");
                System.out.println("  \"http://localhost:8080/api/customer-info/" + encrypted + "\"");
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error generating curl for " + code);
            }
        }

        System.out.println("🌐 SWAGGER UI:");
        System.out.println("   http://localhost:8080/swagger-ui.html");
        System.out.println();
        System.out.println("🏥 HEALTH CHECKS:");
        System.out.println("   http://localhost:8080/health");
        System.out.println("   http://localhost:8081/api/customers/health");
        System.out.println("   http://localhost:8082/api/financial-products/health");
    }

    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getUrlDecoder().decode(encryptedData);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}