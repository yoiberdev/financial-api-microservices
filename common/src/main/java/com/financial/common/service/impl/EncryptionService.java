package com.financial.common.service.impl;

import com.financial.common.exception.EncryptionException;

/**
 * Service interface for encryption and decryption operations
 * Following SOLID principles - Interface Segregation and Dependency Inversion
 */
public interface EncryptionService {

    /**
     * Encrypts the given data using AES algorithm
     * @param data the plain text to encrypt
     * @return encrypted string in Base64 format
     * @throws EncryptionException if encryption fails
     */
    String encrypt(String data);

    /**
     * Decrypts the given encrypted data
     * @param encryptedData the encrypted string in Base64 format
     * @return decrypted plain text
     * @throws EncryptionException if decryption fails
     */
    String decrypt(String encryptedData);
}