package com.financial.common.service.impl;


/**
 * Script temporal para generar códigos únicos encriptados para testing
 * Usar este script para obtener versiones encriptadas de códigos únicos
 */
public class EncryptionTestScript {

    public static void main(String[] args) {
        // Usar la misma configuración que en application.yml
        String secretKey = "MySecretKey12345"; // 16 bytes
        String algorithm = "AES/ECB/PKCS5Padding";

        AESEncryptionService encryptionService = new AESEncryptionService(secretKey, algorithm);

        // Códigos únicos de ejemplo para testing
        String[] codigosUnicos = {
                "CUST001",
                "CUST002",
                "CUST003",
                "CUST004",
                "CUST005"
        };

        System.out.println("=== CÓDIGOS ÚNICOS ENCRIPTADOS PARA TESTING ===");
        System.out.println();

        for (String codigo : codigosUnicos) {
            try {
                String encrypted = encryptionService.encrypt(codigo);
                String decrypted = encryptionService.decrypt(encrypted);

                System.out.println("Original:   " + codigo);
                System.out.println("Encriptado: " + encrypted);
                System.out.println("Verificado: " + decrypted);
                System.out.println("Match:      " + codigo.equals(decrypted));
                System.out.println("---");

            } catch (Exception e) {
                System.err.println("Error procesando " + codigo + ": " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("=== EJEMPLO PARA POSTMAN ===");
        String testCodigo = "CUST001";
        String encrypted = encryptionService.encrypt(testCodigo);
        System.out.println("Para probar en Postman, usar este código encriptado:");
        System.out.println("{{bff_base_url}}/api/customer-info/" + encrypted);

        System.out.println();
        System.out.println("=== CONFIGURACIÓN ===");
        System.out.println("Secret Key: " + secretKey);
        System.out.println("Algorithm:  " + algorithm);
        System.out.println("Key Length: " + secretKey.getBytes().length + " bytes");
    }
}