package com.kosprov.jargon2.api;

import com.kosprov.jargon2.spi.Jargon2Backend;

import java.util.Map;

public class NonConstructableJargon2Backend implements Jargon2Backend {

    public NonConstructableJargon2Backend(String dummy) {
    }

    @Override
    public byte[] rawHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        return null;
    }

    @Override
    public String encodedHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        return null;
    }

    @Override
    public boolean verifyRaw(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, byte[] rawHash, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        return false;
    }

    @Override
    public boolean verifyEncoded(String encodedHash, int threads, byte[] secret, byte[] ad, byte[] password, Map<String, Object> options) {
        return false;
    }
}
