package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2Exception;
import com.kosprov.jargon2.spi.Jargon2Backend;

import java.util.Map;

import static com.kosprov.jargon2.api.Jargon2.*;

public class Jargon2BackendAdapter implements LowLevelApi {
    private Jargon2Backend backend;

    public Jargon2BackendAdapter(Jargon2Backend backend) {
        this.backend = backend;
    }

    @Override
    public byte[] rawHash(Type type, Version version, int memoryCost, int timeCost, int parallelism, int hashLength, byte[] salt, byte[] password) throws Jargon2Exception {
        return backend.rawHash(type, version, memoryCost, timeCost, parallelism, parallelism, hashLength, null, null, salt, password, null);
    }

    @Override
    public byte[] rawHash(Type type, Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) throws Jargon2Exception {
        return backend.rawHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
    }

    @Override
    public String encodedHash(Type type, Version version, int memoryCost, int timeCost, int parallelism, int hashLength, byte[] salt, byte[] password) throws Jargon2Exception {
        return backend.encodedHash(type, version, memoryCost, timeCost, parallelism, parallelism, hashLength, null, null, salt, password, null);
    }

    @Override
    public String encodedHash(Type type, Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) throws Jargon2Exception {
        return backend.encodedHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
    }

    @Override
    public boolean verifyRaw(Type type, Version version, int memoryCost, int timeCost, int parallelism, byte[] rawHash, byte[] salt, byte[] password) throws Jargon2Exception {
        return backend.verifyRaw(type, version, memoryCost, timeCost, parallelism, parallelism, rawHash, null, null, salt, password, null);
    }

    @Override
    public boolean verifyRaw(Type type, Version version, int memoryCost, int timeCost, int lanes, int threads, byte[] rawHash, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) throws Jargon2Exception {
        return backend.verifyRaw(type, version, memoryCost, timeCost, lanes, threads, rawHash, secret, ad, salt, password, options);
    }

    @Override
    public boolean verifyEncoded(String encodedHash, byte[] password) throws Jargon2Exception {
        return backend.verifyEncoded(encodedHash, -1, null, null, password, null);
    }

    @Override
    public boolean verifyEncoded(String encodedHash, byte[] secret, byte[] ad, byte[] password, Map<String, Object> options) throws Jargon2Exception {
        return backend.verifyEncoded(encodedHash, -1, secret, ad, password, options);
    }

    @Override
    public boolean verifyEncoded(String encodedHash, int threads, byte[] password) throws Jargon2Exception {
        return backend.verifyEncoded(encodedHash, threads, null, null, password, null);
    }

    @Override
    public boolean verifyEncoded(String encodedHash, int threads, byte[] secret, byte[] ad, byte[] password, Map<String, Object> options) throws Jargon2Exception {
        return backend.verifyEncoded(encodedHash, threads, secret, ad, password, options);
    }
}