package com.financial.common.config;

import com.financial.common.service.EncryptionService;
import com.financial.common.service.impl.AESEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration class for encryption services
 * Following Open/Closed Principle - extensible for other encryption algorithms
 */
@Configuration
@Slf4j
public class EncryptionConfiguration {

    @Bean
    @ConditionalOnMissingBean(EncryptionService.class)
    public EncryptionService encryptionService(
            @Value("${encryption.aes.secret-key}") String secretKey,
            @Value("${encryption.aes.algorithm:AES/ECB/PKCS5Padding}") String algorithm) {

        log.info("Configuring AES encryption service");
        return new AESEncryptionService(secretKey, algorithm);
    }
}