package com.kosprov.jargon2.api;

import java.security.Provider;
import java.security.SecureRandomSpi;
import java.util.*;

public class DummyProvider extends Provider {

    private static final DummyProvider INSTANCE = new DummyProvider();

    public static DummyProvider getInstance() {
        return INSTANCE;
    }

    private Map<String, Service> secureRandomServices = new HashMap<String, Service>() {
        {
            put("SHA1PRNG", new Service(DummyProvider.this, "SecureRandom", "SHA1PRNG", DummySha1PrngSecureRandomSpi.class.getName(), null, null));
            put("NativePRNG", new Service(DummyProvider.this, "SecureRandom", "NativePRNG", DummyNativePrngSecureRandomSpi.class.getName(), null, null));
        }
    };

    private DummyProvider() {
        super("DUMMY", 1.0, "Dummy impl");
    }

    @Override
    public synchronized Service getService(String type, String algorithm) {
        if ("SecureRandom".equals(type)) {
            return secureRandomServices.get(algorithm);
        } else {
            return null;
        }
    }

    @Override
    public synchronized Set<Service> getServices() {
        return new HashSet<>(secureRandomServices.values());
    }

    public static class DummySha1PrngSecureRandomSpi extends SecureRandomSpi {
        static byte DUMMY_BYTE = (byte) 0b00000001;

        @Override
        protected void engineSetSeed(byte[] seed) { }

        @Override
        protected void engineNextBytes(byte[] bytes) {
            Arrays.fill(bytes, DUMMY_BYTE);
        }

        @Override
        protected byte[] engineGenerateSeed(int numBytes) {
            return new byte[numBytes];
        }
    }

    public static class DummyNativePrngSecureRandomSpi extends SecureRandomSpi {
        static byte DUMMY_BYTE = (byte) 0b00000010;

        @Override
        protected void engineSetSeed(byte[] seed) { }

        @Override
        protected void engineNextBytes(byte[] bytes) {
            Arrays.fill(bytes, DUMMY_BYTE);
        }

        @Override
        protected byte[] engineGenerateSeed(int numBytes) {
            return new byte[numBytes];
        }
    }
}
