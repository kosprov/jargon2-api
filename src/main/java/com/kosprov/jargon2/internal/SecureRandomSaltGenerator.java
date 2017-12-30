package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2Exception;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;

class SecureRandomSaltGenerator implements Jargon2.SaltGenerator {

    private static final String INSTANTIATION_FAILURE = "Failed to instantiate internal salt generator";

    static final Jargon2.SaltGenerator DEFAULT = new SecureRandomSaltGenerator();

    final SecureRandom random;

    SecureRandomSaltGenerator() {
        random = createSecureRandom();
    }

    SecureRandomSaltGenerator(String algorithm) {
        random = createSecureRandom(algorithm);
    }

    SecureRandomSaltGenerator(String algorithm, Provider provider) {
        random = createSecureRandom(algorithm, provider);
    }

    SecureRandomSaltGenerator(String algorithm, String provider) {
        random = createSecureRandom(algorithm, provider);
    }

    @Override
    public void generate(byte[] salt) {
        random.nextBytes(salt);
    }

    private static SecureRandom createSecureRandom() {
        return new SecureRandom();
    }

    private static SecureRandom createSecureRandom(String algorithm) {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new Jargon2Exception(INSTANTIATION_FAILURE, e);
        }
        return random;
    }

    private static SecureRandom createSecureRandom(String algorithm, String provider) {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new Jargon2Exception(INSTANTIATION_FAILURE, e);
        }
        return random;
    }

    private static SecureRandom createSecureRandom(String algorithm, Provider provider) {
        SecureRandom random;
        try {
            random = SecureRandom.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new Jargon2Exception(INSTANTIATION_FAILURE, e);
        }
        return random;
    }
}
