package com.financial.common.config;

import com.financial.common.service.AESEncryptionService;
import com.financial.common.service.impl.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seleccion de la implementacion de cifrado.
 *
 * <p>Por defecto se usa {@code EnhancedAESEncryptionService} (registrado con {@code @Service}).
 * Con {@code encryption.aes.implementation=basic} se registra en su lugar la implementacion
 * basica {@link AESEncryptionService}.</p>
 *
 * <p>Antes este bean se declaraba con {@code @ConditionalOnMissingBean}, que fuera de una
 * autoconfiguracion se evalua segun el orden de registro de definiciones: podia crear un tercer
 * candidato de {@link EncryptionService} de forma no determinista. La condicion por propiedad
 * garantiza que siempre exista exactamente un bean de este tipo.</p>
 */
@Configuration
@Slf4j
public class EncryptionConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "encryption.aes", name = "implementation", havingValue = "basic")
    public EncryptionService encryptionService(
            @Value("${encryption.aes.secret-key}") String secretKey,
            @Value("${encryption.aes.algorithm:AES/ECB/PKCS5Padding}") String algorithm) {

        log.info("Configuring basic AES encryption service (encryption.aes.implementation=basic)");
        return new AESEncryptionService(secretKey, algorithm);
    }
}
