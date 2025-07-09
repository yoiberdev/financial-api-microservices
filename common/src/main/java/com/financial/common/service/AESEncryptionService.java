package com.financial.common.service;

import org.springframework.stereotype.Service;

@Service
public class AESEncryptionService implements EncryptionService {

    @Override
    public String decrypt(String encryptedData) {
        // Lógica real o mock
        return encryptedData;
    }

    @Override
    public String encrypt(String data) {
        return data;
    }
}
