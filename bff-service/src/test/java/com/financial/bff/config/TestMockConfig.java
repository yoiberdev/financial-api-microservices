package com.financial.bff.config;

import com.financial.common.service.impl.EncryptionService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMockConfig {

    @Bean
    public EncryptionService encryptionService() {
        return Mockito.mock(EncryptionService.class);
    }
}
