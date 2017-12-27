package com.kosprov.jargon2.internal.discovery;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2Exception;
import com.kosprov.jargon2.spi.Jargon2Backend;
import org.junit.Test;

import java.util.Map;

public class Jargon2BackendDiscoveryErrorTest {

    @Test(expected = Jargon2BackendDiscoveryException.class)
    public void errorOnMoreThanOneBackendTest() {
        System.setProperty("com.kosprov.jargon2.spi.backend", AnotherJargon2Backend.class.getName());
        try {
            Jargon2BackendDiscovery.INSTANCE.getJargon2Backend();
        } finally {
            System.setProperty("com.kosprov.jargon2.spi.backend", "");
        }
    }

    public static class AnotherJargon2Backend implements Jargon2Backend {
        @Override
        public byte[] rawHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) throws Jargon2Exception {
            return new byte[0];
        }

        @Override
        public String encodedHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) throws Jargon2Exception {
            return null;
        }

        @Override
        public boolean verifyRaw(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, byte[] rawHash, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) throws Jargon2Exception {
            return false;
        }

        @Override
        public boolean verifyEncoded(String encodedHash, int threads, byte[] secret, byte[] ad, byte[] password, Map<String, Object> options) throws Jargon2Exception {
            return false;
        }
    }

}