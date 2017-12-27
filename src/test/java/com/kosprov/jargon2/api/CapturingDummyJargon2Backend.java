package com.kosprov.jargon2.api;

import java.util.Map;

public class CapturingDummyJargon2Backend extends DummyJargon2Backend {

    public static class CapturedSet {
        public Jargon2.Type type;
        public Jargon2.Version version;
        public int memoryCost;
        public int timeCost;
        public int lanes;
        public int threads;
        public byte[] rawHash;
        public String encodedHash;
        public int hashLength;
        public byte[] secret;
        public byte[] ad;
        public byte[] salt;
        public byte[] password;
        public Map<String, Object> options;
    }

    public CapturedSet captured;

    @Override
    public byte[] rawHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        captured = new CapturedSet();
        captured.type = type;
        captured.version = version;
        captured.memoryCost = memoryCost;
        captured.timeCost = timeCost;
        captured.lanes = lanes;
        captured.threads = threads;
        captured.hashLength = hashLength;
        captured.secret = secret;
        captured.ad = ad;
        captured.salt = salt;
        captured.password = password;
        captured.options = options;

        return super.rawHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
    }

    @Override
    public String encodedHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        captured = new CapturedSet();
        captured.type = type;
        captured.version = version;
        captured.memoryCost = memoryCost;
        captured.timeCost = timeCost;
        captured.lanes = lanes;
        captured.threads = threads;
        captured.hashLength = hashLength;
        captured.secret = secret;
        captured.ad = ad;
        captured.salt = salt;
        captured.password = password;
        captured.options = options;
        
        return super.encodedHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
    }

    @Override
    public boolean verifyRaw(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, byte[] rawHash, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        captured = new CapturedSet();
        captured.type = type;
        captured.version = version;
        captured.memoryCost = memoryCost;
        captured.timeCost = timeCost;
        captured.lanes = lanes;
        captured.threads = threads;
        captured.rawHash = rawHash;
        captured.secret = secret;
        captured.ad = ad;
        captured.salt = salt;
        captured.password = password;
        captured.options = options;
        
        return super.verifyRaw(type, version, memoryCost, timeCost, lanes, threads, rawHash, secret, ad, salt, password, options);
    }

    @Override
    public boolean verifyEncoded(String encodedHash, int threads, byte[] secret, byte[] ad, byte[] password, Map<String, Object> options) {
        captured = new CapturedSet();
        captured.encodedHash = encodedHash;
        captured.threads = threads;
        captured.secret = secret;
        captured.ad = ad;
        captured.password = password;
        captured.options = options;
        
        return super.verifyEncoded(encodedHash, threads, secret, ad, password, options);
    }
}
