package com.kosprov.jargon2.api;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kosprov.jargon2.api.Jargon2.*;
import static org.junit.Assert.*;

public class Jargon2Test {

    @Test
    public void typicalFluentApiTest() throws Exception {
        char[] reference = new char[] {'P', '@', 's', 's', 'W', '0', 'r', 'd'};

        String hash;

        {
            char[] password = Arrays.copyOf(reference, reference.length);

            Hasher hasher = jargon2Hasher()
                    .type(Type.ARGON2id)
                    .memoryCost(4096)
                    .timeCost(3)
                    .parallelism(2)
                    .saltLength(16)
                    .hashLength(16);

            try (ByteArray passwordByteArray = toByteArray(password).clearSource()) {

                hash = hasher.password(passwordByteArray).encodedHash();

                assertNotNull(hash);

                assertTrue(Arrays.equals(reference, password));
            }

            for (char c : password) {
                assertEquals(0, c);
            }
        }

        {
            char[] password = Arrays.copyOf(reference, reference.length);

            boolean matches;

            try (ByteArray passwordByteArray = toByteArray(password).clearSource()) {
                matches = jargon2Verifier()
                        .hash(hash)
                        .password(passwordByteArray)
                        .verifyEncoded();

                assertTrue(matches);

                assertTrue(Arrays.equals(reference, password));
            }

            for (char c : password) {
                assertEquals(0, c);
            }
        }
    }

    @Test
    public void allParamsPassedForEncodedHashing() throws Exception {
        Type type = Type.ARGON2id;
        Version version = Version.V10;
        int memoryCost = 2048;
        int timeCost = 5;
        int lanes = 4;
        int threads = 2;
        int saltLength = 8;
        int hashLength = 8;

        byte[] password = "this is a password".getBytes(StandardCharsets.UTF_8);
        byte[] secret = "secret key".getBytes(StandardCharsets.UTF_8);
        byte[] ad = "some additional data".getBytes(StandardCharsets.UTF_8);

        Map<String, Object> options = new HashMap<>();

        CapturingDummyJargon2Backend backend = new CapturingDummyJargon2Backend();

        String hash = jargon2Hasher()
                .backend(backend)
                .options(options)
                .type(type)
                .version(version)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(lanes, threads)
                .saltLength(saltLength)
                .hashLength(hashLength)
                .secret(secret)
                .ad(ad)
                .password(password)
                .encodedHash();

        assertEquals(type, backend.captured.type);
        assertEquals(version, backend.captured.version);
        assertEquals(memoryCost, backend.captured.memoryCost);
        assertEquals(timeCost, backend.captured.timeCost);
        assertEquals(lanes, backend.captured.lanes);
        assertEquals(threads, backend.captured.threads);
        assertEquals(hashLength, backend.captured.hashLength);
        assertSame(secret, backend.captured.secret);
        assertSame(ad, backend.captured.ad);
        assertSame(saltLength, backend.captured.salt.length);
        assertSame(password, backend.captured.password);
        assertSame(options, backend.captured.options);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$argon2id$m=" + memoryCost + ",t=" + timeCost + ",p=" + lanes + "$"));

        threads = 1;

        boolean matches = jargon2Verifier()
                .backend(backend)
                .options(options)
                .hash(hash)
                .threads(threads)
                .secret(secret)
                .ad(ad)
                .password(password)
                .verifyEncoded();

        assertSame(hash, backend.captured.encodedHash);
        assertEquals(threads, backend.captured.threads);
        assertSame(secret, backend.captured.secret);
        assertSame(ad, backend.captured.ad);
        assertSame(password, backend.captured.password);
        assertSame(options, backend.captured.options);

        assertTrue(matches);
    }

    @Test
    public void customSaltGeneratorTest() throws Exception {
        int saltLength = 8;
        int hashLength = 8;

        byte[] password = "this is a password".getBytes(StandardCharsets.UTF_8);

        CapturingDummyJargon2Backend backend = new CapturingDummyJargon2Backend();

        Hasher hasher = jargon2Hasher()
                .backend(backend)
                .saltLength(saltLength)
                .hashLength(hashLength)
                .password(password);

        {
            String hash = hasher.saltGenerator("SHA1PRNG").encodedHash();
            assertSame(saltLength, backend.captured.salt.length);
            assertNotNull(hash);
        }

        {
            String hash = hasher.saltGenerator("SHA1PRNG", DummyProvider.getInstance()).encodedHash();
            assertEquals(saltLength, backend.captured.salt.length);
            for (byte b : backend.captured.salt) {
                assertEquals(DummyProvider.DummySha1PrngSecureRandomSpi.DUMMY_BYTE, b);
            }
            assertNotNull(hash);
        }

        {
            String hash = hasher.saltGenerator("NativePRNG", DummyProvider.getInstance()).encodedHash();
            assertEquals(saltLength, backend.captured.salt.length);
            for (byte b : backend.captured.salt) {
                assertEquals(DummyProvider.DummyNativePrngSecureRandomSpi.DUMMY_BYTE, b);
            }
            assertNotNull(hash);
        }

        {
            String hash = hasher.saltGenerator(DummySaltGenerator.getInstance()).encodedHash();
            assertEquals(saltLength, backend.captured.salt.length);
            for (byte b : backend.captured.salt) {
                assertEquals(DummySaltGenerator.DUMMY_BYTE, b);
            }
            assertNotNull(hash);
        }
    }

    @Test(expected = Jargon2Exception.class)
    public void erroneousSaltGeneratorTest() {
        jargon2Hasher().saltGenerator("WRONG");
    }

    @Test(expected = Jargon2Exception.class)
    public void erroneousSaltGeneratorTest2() {
        jargon2Hasher().saltGenerator("WRONG", "WRONG");
    }

    @Test(expected = Jargon2Exception.class)
    public void erroneousSaltGeneratorTest3() {
        jargon2Hasher().saltGenerator("WRONG", DummyProvider.getInstance());
    }

    @Test
    public void allParamsPassedForRawHashing() throws Exception {

        Type type = Type.ARGON2id;
        Version version = Version.V10;
        int memoryCost = 2048;
        int timeCost = 5;
        int lanes = 4;
        int threads = 2;
        int hashLength = 8;

        byte[] password = "this is a password".getBytes(StandardCharsets.UTF_8);
        byte[] salt = "some salt".getBytes(StandardCharsets.UTF_8);
        byte[] secret = "secret key".getBytes(StandardCharsets.UTF_8);
        byte[] ad = "some additional data".getBytes(StandardCharsets.UTF_8);

        Map<String, Object> options = new HashMap<>();

        CapturingDummyJargon2Backend backend = new CapturingDummyJargon2Backend();

        byte[] hash = jargon2Hasher()
                .backend(backend)
                .options(options)
                .type(type)
                .version(version)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(lanes, threads)
                .hashLength(hashLength)
                .salt(salt)
                .secret(secret)
                .ad(ad)
                .password(password)
                .rawHash();

        assertEquals(type, backend.captured.type);
        assertEquals(version, backend.captured.version);
        assertEquals(memoryCost, backend.captured.memoryCost);
        assertEquals(timeCost, backend.captured.timeCost);
        assertEquals(lanes, backend.captured.lanes);
        assertEquals(threads, backend.captured.threads);
        assertEquals(hashLength, backend.captured.hashLength);
        assertSame(secret, backend.captured.secret);
        assertSame(ad, backend.captured.ad);
        assertSame(salt, backend.captured.salt);
        assertSame(password, backend.captured.password);
        assertSame(options, backend.captured.options);

        assertNotNull(hash);

        threads = 1;

        boolean matches = jargon2Verifier()
                .backend(backend)
                .options(options)
                .type(type)
                .version(version)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(lanes)
                .parallelism(lanes, threads) // this should override threads to 1
                .hash(hash)
                .secret(secret)
                .ad(ad)
                .salt(salt)
                .password(password)
                .verifyRaw();

        assertEquals(type, backend.captured.type);
        assertEquals(version, backend.captured.version);
        assertEquals(memoryCost, backend.captured.memoryCost);
        assertEquals(timeCost, backend.captured.timeCost);
        assertEquals(lanes, backend.captured.lanes);
        assertEquals(threads, backend.captured.threads);
        assertSame(hash, backend.captured.rawHash);
        assertSame(secret, backend.captured.secret);
        assertSame(ad, backend.captured.ad);
        assertSame(salt, backend.captured.salt);
        assertSame(password, backend.captured.password);
        assertSame(options, backend.captured.options);

        assertTrue(matches);
    }

    @Test(expected = Jargon2Exception.class)
    public void noSaltForRawHashingTest() throws Exception {
        byte[] password = "this is a password".getBytes(StandardCharsets.UTF_8);

        jargon2Hasher()
            .password(password)
            .rawHash();
    }

    @Test
    public void lowLevelApiEncodedTest() throws Exception {
        String password = "this is a password";
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        String salt = "some salt";
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

        String encoded = jargon2LowLevelApi(DummyJargon2Backend.class.getName()).encodedHash(
                Type.ARGON2id,
                Version.V13,
                4096,
                3,
                2,
                16,
                saltBytes,
                passwordBytes
        );

        boolean matches = jargon2LowLevelApi().verifyEncoded(
                encoded,
                passwordBytes
        );

        assertTrue(matches);
    }

    @Test
    public void lowLevelApiEncodedAllParamsTest() throws Exception {
        String secret = "this is a secret";
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        String ad = "this is additional data";
        byte[] adBytes = ad.getBytes(StandardCharsets.UTF_8);

        String password = "this is a password";
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        String salt = "some salt";
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

        String encoded = jargon2LowLevelApi(DummyJargon2Backend.class.getName()).encodedHash(
                Type.ARGON2id,
                Version.V13,
                4096,
                3,
                2,
                2,
                16,
                secretBytes,
                adBytes,
                saltBytes,
                passwordBytes,
                null
        );

        boolean matches = jargon2LowLevelApi().verifyEncoded(
                encoded,
                2,
                secretBytes,
                adBytes,
                passwordBytes,
                null
        );

        assertTrue(matches);
    }

    @Test
    public void lowLevelApiRawTest() throws Exception {
        String password = "this is a password";
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        String salt = "some salt";
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

        byte[] rawHash = jargon2LowLevelApi(DummyJargon2Backend.class.getName()).rawHash(
                Type.ARGON2id,
                Version.V13,
                4096,
                3,
                2,
                16,
                saltBytes,
                passwordBytes
        );

        boolean matches = jargon2LowLevelApi().verifyRaw(
                Type.ARGON2id,
                Version.V13,
                4096,
                3,
                2,
                rawHash,
                saltBytes,
                passwordBytes
        );

        assertTrue(matches);
    }

    @Test
    public void lowLevelApiRawAllParamsTest() throws Exception {
        String secret = "this is a secret";
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        String ad = "this is additional data";
        byte[] adBytes = ad.getBytes(StandardCharsets.UTF_8);

        String password = "this is a password";
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

        String salt = "some salt";
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

        byte[] rawHash = jargon2LowLevelApi(DummyJargon2Backend.class.getName()).rawHash(
                Type.ARGON2id,
                Version.V13,
                4096,
                3,
                2,
                2,
                16,
                secretBytes,
                adBytes,
                saltBytes,
                passwordBytes,
                null
        );

        boolean matches = jargon2LowLevelApi().verifyRaw(
                Type.ARGON2id,
                Version.V13,
                4096,
                3,
                2,
                2,
                rawHash,
                secretBytes,
                adBytes,
                saltBytes,
                passwordBytes,
                null
        );

        assertTrue(matches);
    }

    @Test(expected = Jargon2Exception.class)
    public void invalidBackendClassNameOnLowLevelApiTest() {
        jargon2LowLevelApi("invalid.class.Name");
    }

    @Test(expected = Jargon2Exception.class)
    public void nonConstructableBackendClassNameOnLowLevelApiTest() {
        jargon2LowLevelApi(NonConstructableJargon2Backend.class.getName());
    }

    @Test(expected = Jargon2Exception.class)
    public void nonConstructableBackendClassOnLowLevelApiTest() {
        jargon2LowLevelApi(NonConstructableJargon2Backend.class);
    }

    @Test(expected = Jargon2Exception.class)
    public void invalidBackendClassNameOnHasherTest() {
        jargon2Hasher().backend("invalid.class.Name");
    }

    @Test(expected = Jargon2Exception.class)
    public void nonConstructableBackendClassNameOnHasherTest() {
        jargon2Hasher().backend(NonConstructableJargon2Backend.class.getName());
    }

    @Test(expected = Jargon2Exception.class)
    public void nonConstructableBackendClassOnHasherTest() {
        jargon2Hasher().backend(NonConstructableJargon2Backend.class);
    }

    @Test(expected = Jargon2Exception.class)
    public void invalidBackendClassNameOnVerifierTest() {
        jargon2Verifier().backend("invalid.class.Name");
    }

    @Test(expected = Jargon2Exception.class)
    public void nonConstructableBackendClassNameOnVerifierTest() {
        jargon2Verifier().backend(NonConstructableJargon2Backend.class.getName());
    }

    @Test(expected = Jargon2Exception.class)
    public void nonConstructableBackendClassOnVerifierTest() {
        jargon2Verifier().backend(NonConstructableJargon2Backend.class);
    }

    @Test
    public void validBackendsTest() {
        jargon2LowLevelApi(DummyJargon2Backend.class.getName());
        jargon2LowLevelApi(DummyJargon2Backend.class);
        jargon2LowLevelApi(new DummyJargon2Backend());
        jargon2Hasher().backend(DummyJargon2Backend.class.getName());
        jargon2Hasher().backend(DummyJargon2Backend.class);
        jargon2Hasher().backend(new DummyJargon2Backend());
        jargon2Verifier().backend(DummyJargon2Backend.class.getName());
        jargon2Verifier().backend(DummyJargon2Backend.class);
        jargon2Verifier().backend(new DummyJargon2Backend());
    }

    @Test
    public void byteArraysTest() throws Exception {
        byte[] secret = "superSecret".getBytes(StandardCharsets.UTF_8);

        try (ByteArray secretByteArray = toByteArray(secret).clearSource()) {
            Hasher hasher = jargon2Hasher()
                    .type(Type.ARGON2id)
                    .memoryCost(8)
                    .timeCost(1)
                    .parallelism(1)
                    .secret(secretByteArray)
                    .hashLength(16);

            Verifier verifier = jargon2Verifier()
                    .secret(secretByteArray);

            char[] ad = "additional data".toCharArray();
            InputStreamReader salt = new InputStreamReader(new ByteArrayInputStream("this is a salt".getBytes()));
            InputStream password = new ByteArrayInputStream("this is a password".getBytes());

            boolean matches;

            try (ByteArray adByteArray = toByteArray(ad).encoding("ASCII");
                 ByteArray saltByteArray = toByteArray(salt).encoding("ASCII");
                 ByteArray passwordByteArray = toByteArray(password)) {

                String hash = hasher
                        .ad(adByteArray)
                        .salt(saltByteArray)
                        .password(passwordByteArray)
                        .encodedHash();

                matches = verifier
                        .hash(hash)
                        .ad(adByteArray)
                        .password(passwordByteArray)
                        .verifyEncoded();
            }
            assertTrue(matches);
        }

        byte[] zeros = new byte[secret.length];
        assertTrue(Arrays.equals(zeros, secret));
    }

    @Test
    public void byteArrayEncodingTest() throws Exception {
        String str = "Φούμπαρ";

        {
            CharSeqByteArray byteArray1 = toByteArray(str);
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = byteArray1.encoding("ISO8859_7");
            byte[] bytes2 = byteArray2.getBytes();

            assertFalse(Arrays.equals(bytes1, bytes2));
            assertTrue(Arrays.equals(str.getBytes("ISO8859_7"), bytes2));
        }

        {
            char[] chars = str.toCharArray();
            CharSeqByteArray byteArray1 = toByteArray(chars);
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = byteArray1.encoding("ISO8859_7");
            byte[] bytes2 = byteArray2.getBytes();

            assertFalse(Arrays.equals(bytes1, bytes2));
            assertTrue(Arrays.equals(new String(chars).getBytes("ISO8859_7"), bytes2));
        }

        {
            char[] chars = str.toCharArray();

            ByteArray byteArray1 = toByteArray(new CharArrayReader(chars));
            byte[] bytes1 = byteArray1.getBytes();

            ByteArray byteArray2 = toByteArray(new CharArrayReader(chars)).encoding("ISO8859_7");
            byte[] bytes2 = byteArray2.getBytes();

            assertFalse(Arrays.equals(bytes1, bytes2));
            assertTrue(Arrays.equals(new String(chars).getBytes("ISO8859_7"), bytes2));
        }
    }

    @Test
    public void byteArrayNormalizationTest() {

        Hasher hasher = jargon2Hasher()
                .type(Type.ARGON2id)
                .memoryCost(8)
                .timeCost(1)
                .parallelism(1)
                .saltLength(8)
                .hashLength(16);

        Verifier verifier = jargon2Verifier();

        {
            String password1 = "\u00C1";
            String password2 = "\u0041\u0301";

            {
                String hash = hasher.password(toByteArray(password1)).encodedHash();

                boolean matches = verifier.hash(hash).password(toByteArray(password2)).verifyEncoded();
                assertFalse(matches);
            }

            {
                String hash = hasher.password(toByteArray(password1).normalize()).encodedHash();

                boolean matches = verifier.hash(hash).password(toByteArray(password2).normalize()).verifyEncoded();
                assertTrue(matches);
            }
        }

        {
            char[] password1 = new char[] { '\u00C1' };
            char[] password2 = new char[] { '\u0041', '\u0301' };
            {

                String hash = hasher.password(toByteArray(password1)).encodedHash();

                boolean matches = verifier.hash(hash).password(toByteArray(password2)).verifyEncoded();
                assertFalse(matches);
            }

            {
                String hash = hasher.password(toByteArray(password1).normalize()).encodedHash();

                boolean matches = verifier.hash(hash).password(toByteArray(password2).normalize()).verifyEncoded();
                assertTrue(matches);
            }
        }

        {
            char[] password1 = new char[] { '\u00C1' };
            char[] password2 = new char[] { '\u0041', '\u0301' };
            {

                String hash = hasher.password(toByteArray(new CharArrayReader(password1))).encodedHash();

                boolean matches = verifier.hash(hash).password(toByteArray(new CharArrayReader(password2))).verifyEncoded();
                assertFalse(matches);
            }

            {
                String hash = hasher.password(toByteArray(new CharArrayReader(password1)).normalize()).encodedHash();

                boolean matches = verifier.hash(hash).password(toByteArray(new CharArrayReader(password2)).normalize()).verifyEncoded();
                assertTrue(matches);
            }
        }
    }
}