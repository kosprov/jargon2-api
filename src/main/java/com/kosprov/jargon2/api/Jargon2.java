package com.kosprov.jargon2.api;

import com.kosprov.jargon2.internal.Jargon2BackendAdapter;
import com.kosprov.jargon2.internal.discovery.Jargon2BackendDiscovery;
import com.kosprov.jargon2.spi.Jargon2Backend;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Map;

/**
 * Fluent Java API for Argon2 hashing.
 */
public class Jargon2 {

    /**
     * The default character encoding for converting char sequences to byte arrays
     */
    public static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");

    /**
     * The default buffer size for consuming bytes from streams or readers during byte arrays conversion
     */
    public static final int STREAM_BUFFER_SIZE = 64;

    /**
     * The default normalization form to apply before converting char sequences to byte arrays
     */
    public static final Normalization DEFAULT_NORMALIZED_FORM = Normalization.NFC;

    /**
     * Argon2 hash types
     */
    public enum Type {
        /**
         * Data-dependent hashing
         */
        ARGON2d,
        /**
         * Data-independent hashing
         */
        ARGON2i,
        /**
         * Mixed-mode hashing.
         */
        ARGON2id;

        private String value;
        private String valueCapitalized;

        Type() {
            value = name().toLowerCase();
            valueCapitalized = Character.toUpperCase(value.charAt(0)) + value.substring(1);
        }

        /**
         * Get the Argon2 type as it appears on the encoded hash
         *
         * @return The Argon2 type lower-case
         */
        public String getValue() {
            return value;
        }

        /**
         * Get the Argon2 type capitalized (e.g. Argon2id)
         *
         * @return The Argon2 type capitalized
         */
        public String getValueCapitalized() {
            return valueCapitalized;
        }
    }

    /**
     * Argon2 version
     */
    public enum Version {
        /**
         * Version 1.0
         */
        V10(0x10),
        /**
         * Version 1.3
         */
        V13(0x13);

        private int value;

        Version(int value) {
            this.value = value;
        }

        /**
         * Get the numeric value of the version as it appears on the encoded hash
         *
         * @return The integer value of the version
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Low-level API for Argon2
     */
    public interface LowLevelApi {

        /**
         * Calculate a raw hash for the given parameters.
         *
         * <p>
         *     Same as {@link #rawHash(Type, Version, int, int, int, int, int, byte[], byte[], byte[], byte[], Map)} with
         *     same lanes and threads and no secret and additional data.
         * </p>
         *
         * @param type The Argon2 {@link Type}
         * @param version The Argon2 {@link Version}
         * @param memoryCost The memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         * @param timeCost The number of passes through memory
         * @param parallelism The number of memory lanes and threads to be used
         * @param hashLength The number of output bytes of the hash value
         * @param salt The salt value to be used during hashing
         * @param password The password to be hashed
         * @return A byte array of length hashLength with the hash value
         * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
         */
        byte[] rawHash(
                // Configuration
                Type type,
                Version version,
                int memoryCost,
                int timeCost,
                int parallelism,
                int hashLength,
                // Data
                byte[] salt,
                byte[] password
        ) throws Jargon2Exception;

        /**
         * Calculate a raw hash for the given parameters.
         *
         * <p>
         *     Lanes and threads can be specified independently. For example:
         *
         *     If lanes=4 and threads=1, the hash will be calculated based on 4 lanes by using a single thread.
         *     If lanes=4 and threads=8, the hash will be calculated based on 4 lanes by using 4 threads (threads are
         *     capped to lanes).
         *
         *     In Argon2 terminology, "lanes" is equivalent to "parallelism".
         * </p>
         *
         * @param type The Argon2 {@link Type}
         * @param version The Argon2 {@link Version}
         * @param memoryCost The memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         * @param timeCost The number of passes through memory
         * @param lanes The number of memory lanes
         * @param threads The maximum number of threads to process lanes
         * @param hashLength The number of output bytes of the hash value
         * @param secret A secret for keyed hashing. Can be null
         * @param ad Additional authentication data to include into the hash. Can be null
         * @param salt The salt value to be used during hashing
         * @param password The password to be hashed
         * @param options A map of options to be passed to the backend
         * @return A byte array of length hashLength with the hash value. Can be null
         * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
         */
        byte[] rawHash(
                // Configuration
                Type type,
                Version version,
                int memoryCost,
                int timeCost,
                int lanes,
                int threads,
                int hashLength,
                // Data
                byte[] secret,
                byte[] ad,
                byte[] salt,
                byte[] password,
                Map<String, Object> options
        ) throws Jargon2Exception;

        /**
         * Calculate an encoded hash for the given parameters.
         *
         * <p>
         *     Same as {@link #encodedHash(Type, Version, int, int, int, int, int, byte[], byte[], byte[], byte[], Map)} with
         *     same lanes and threads and no secret and additional data.
         * </p>
         *
         * @param type The Argon2 {@link Type}
         * @param version The Argon2 {@link Version}
         * @param memoryCost The memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         * @param timeCost The number of passes through memory
         * @param parallelism The number of memory lanes and threads to be used
         * @param hashLength The number of output bytes of the hash value
         * @param salt The salt value to be used during hashing
         * @param password The password to be hashed
         * @return A string containing the encoded hash value
         * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
         */
        String encodedHash(
                // Configuration
                Type type,
                Version version,
                int memoryCost,
                int timeCost,
                int parallelism,
                int hashLength,
                // Data
                byte[] salt,
                byte[] password
        ) throws Jargon2Exception;

        /**
         * Calculate an encoded hash for the given parameters.
         *
         * <p>
         *     Output string has the following format:
         *
         *     <pre>
         * $argon2&lt;type&gt;[$v=&lt;version&gt;]$m=&lt;memoryCost&gt;,t=&lt;timeCost&gt;,p=&lt;lanes&gt;$&lt;salt&gt;$&lt;hash&gt;
         *     </pre>
         *
         *     The version param ($v=num) is not present for V10. Salt and hash are Base64 encoded without any padding,
         *     new lines or spaces.
         *
         * <p>
         *     Lanes and threads can be specified independently. For example:
         *
         *     If lanes=4 and threads=1, the hash will be calculated based on 4 lanes by using a single thread.
         *     If lanes=4 and threads=8, the hash will be calculated based on 4 lanes by using 4 threads (threads are
         *     capped to lanes).
         *
         *     In Argon2 terminology, "lanes" is equivalent to "parallelism".
         *
         * @param type The Argon2 {@link Type}
         * @param version The Argon2 {@link Version}
         * @param memoryCost The memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         * @param timeCost The number of passes through memory
         * @param lanes The number of memory lanes
         * @param threads The maximum number of threads to process lanes
         * @param hashLength The number of output bytes of the hash value
         * @param secret A secret for keyed hashing. Can be null
         * @param ad Additional authentication data to include into the hash. Can be null
         * @param salt The salt value to be used during hashing
         * @param password The password to be hashed
         * @param options A map of options to be passed to the backend. Can be null
         * @return A string containing the encoded hash value
         * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
         */
        String encodedHash(
                // Configuration
                Type type,
                Version version,
                int memoryCost,
                int timeCost,
                int lanes,
                int threads,
                int hashLength,
                // Data
                byte[] secret,
                byte[] ad,
                byte[] salt,
                byte[] password,
                Map<String, Object> options
        ) throws Jargon2Exception;

        /**
         * Verify a raw hash value for the given parameters.
         *
         * <p>
         *     Same as {@link #verifyRaw(Type, Version, int, int, int, int, byte[], byte[], byte[], byte[], byte[], Map)} with
         *     same lanes and threads and no secret and additional data.
         * </p>
         *
         * @param type The Argon2 {@link Type}
         * @param version The Argon2 {@link Version}
         * @param memoryCost The memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         * @param timeCost The number of passes through memory
         * @param parallelism The number of memory lanes and threads to be used
         * @param rawHash The raw hash bytes
         * @param salt The salt value to be used during hashing
         * @param password The password to be hashed
         * @return true if recalculating the hash matches the given value
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyRaw(
                // Configuration
                Type type,
                Version version,
                int memoryCost,
                int timeCost,
                int parallelism,
                // Data
                byte[] rawHash,
                byte[] salt,
                byte[] password
        ) throws Jargon2Exception;

        /**
         * Verify a raw hash value for the given parameters
         *
         * <p>
         *     Lanes and threads can be specified independently. For example:
         *
         *     If lanes=4 and threads=1, the hash will be recalculated based on 4 lanes by using a single thread.
         *     If lanes=4 and threads=8, the hash will be recalculated based on 4 lanes by using 4 threads (threads are
         *     capped to lanes).
         *
         *     In Argon2 terminology, "lanes" is equivalent to "parallelism".
         * </p>
         *
         * @param type The Argon2 {@link Type}
         * @param version The Argon2 {@link Version}
         * @param memoryCost The memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         * @param timeCost The number of passes through memory
         * @param lanes The number of memory lanes
         * @param threads The maximum number of threads to process lanes
         * @param rawHash The raw hash bytes
         * @param secret A secret for keyed hashing. Can be null
         * @param ad Additional authentication data to include into the hash. Can be null
         * @param salt The salt value to be used during hashing
         * @param password The password to be hashed
         * @param options A map of options to be passed to the backend. Can be null
         * @return true if recalculating the hash matches the given value
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyRaw(
                // Configuration
                Type type,
                Version version,
                int memoryCost,
                int timeCost,
                int lanes,
                int threads,
                // Data
                byte[] rawHash,
                byte[] secret,
                byte[] ad,
                byte[] salt,
                byte[] password,
                Map<String, Object> options
        ) throws Jargon2Exception;

        /**
         * Verify an encoded hash value for the given parameters.
         *
         * <p>The number of threads used in hash recalculation is derived from the encoded hash.</p>
         *
         * @param encodedHash The encoded hash
         * @param password The password to verify
         * @return true if recalculating the hash matches the given value
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyEncoded(
                // Encoded hash encapsulates configuration
                String encodedHash,
                // Data
                byte[] password
        ) throws Jargon2Exception;

        /**
         * Verify an encoded hash value for the given parameters.
         *
         * <p>The number of threads used in hash recalculation is derived from the encoded hash.</p>
         *
         * @param encodedHash The encoded hash
         * @param secret The secret (keyed hashing) used during hashing. Can be null
         * @param ad Additional authentication data to included during hashing. Can be null
         * @param password The password to verify
         * @param options A map of options to be passed to the backend. Can be null
         * @return true if recalculating the hash matches the given value
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyEncoded(
                // Encoded hash encapsulates configuration
                String encodedHash,
                // Data
                byte[] secret,
                byte[] ad,
                byte[] password,
                Map<String, Object> options
        ) throws Jargon2Exception;

        /**
         * Verify an encoded hash value for the given parameters.
         *
         * <p>
         *     The number of threads specified is the maximum number that can be used for hash recalculation,
         *     based on the parallelism property (p=&lt;lanes&gt;).
         *
         *     If p=4 and threads=1, a single thread will be used to process all 4 lanes.
         *     If p=4 and threads=8, 4 threads will be used (threads are capped to lanes).
         * </p>
         *
         * @param encodedHash The encoded hash
         * @param threads The maximum number of threads it be used during hash recalculation.
         * @param password The password to verify
         * @return true if recalculating the hash matches the given value
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyEncoded(
                // Encoded hash encapsulates configuration
                String encodedHash,
                int threads,
                // Data
                byte[] password
        ) throws Jargon2Exception;

        /**
         * Verify an encoded hash value for the given parameters.
         *
         * <p>
         *     The number of threads specified is the maximum number that can be used for hash recalculation,
         *     based on the parallelism property (p=&lt;lanes&gt;).
         *
         *     If p=4 and threads=1, a single thread will be used to process all 4 lanes.
         *     If p=4 and threads=8, 4 threads will be used (threads are capped to lanes).
         * </p>
         *
         * @param encodedHash The encoded hash
         * @param threads The maximum number of threads it be used during hash recalculation.
         * @param secret The secret (keyed hashing) used during hashing. Can be null
         * @param ad Additional authentication data to included during hashing. Can be null
         * @param password The password to verify
         * @param options A map of options to be passed to the backend. Can be null
         * @return true if recalculating the hash matches the given value
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyEncoded(
                // Encoded hash encapsulates configuration
                String encodedHash,
                int threads,
                // Data
                byte[] secret,
                byte[] ad,
                byte[] password,
                Map<String, Object> options
        ) throws Jargon2Exception;
    }

    /**
     * API to override the internal random number generator used for salt values.
     */
    public interface SaltGenerator {
        /**
         * Fill the given array with the generated salt value
         * @param salt The array to be filled
         */
        void generate(byte[] salt);
    }

    /**
     * Entry-point for the fluent API for password hashing.
     *
     * @return A builder to configure and use an Argon2 hashing backend
     */
    public static Hasher jargon2Hasher() {
        return new com.kosprov.jargon2.internal.Hasher();
    }

    /**
     * Entry-point for the fluent API for password verification.
     *
     * @return A builder to configure and use an Argon2 verification backend
     */
    public static Verifier jargon2Verifier() {
        return new com.kosprov.jargon2.internal.Verifier();
    }

    /**
     * Get the low-level API with the automatically discovered backend implementation
     *
     * @return The low-level Argon2 API
     */
    public static LowLevelApi jargon2LowLevelApi() {
        return jargon2LowLevelApi(Jargon2BackendDiscovery.INSTANCE.getJargon2Backend());
    }

    /**
     * Get the low-level API with the given backend implementation
     *
     * @param backend The {@link Jargon2Backend} implementation
     * @return The low-level Argon2 API
     */
    public static LowLevelApi jargon2LowLevelApi(Jargon2Backend backend) {
        return new Jargon2BackendAdapter(backend);
    }

    /**
     * Get the low-level API with the given backend implementation class name
     *
     * @param backendClass The {@link Jargon2Backend} implementation class
     * @return The low-level Argon2 API
     */
    public static LowLevelApi jargon2LowLevelApi(String backendClass) {
        try {
            return jargon2LowLevelApi(Class.forName(backendClass).asSubclass(Jargon2Backend.class));
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    /**
     * Get the low-level API with the given backend implementation class
     *
     * @param backendClass The {@link Jargon2Backend} implementation class
     * @return The low-level Argon2 API
     */
    public static LowLevelApi jargon2LowLevelApi(Class<? extends Jargon2Backend> backendClass) {
        try {
            return jargon2LowLevelApi(backendClass.newInstance());
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    /**
     * Convert a string to a {@link CharSeqByteArray} using the default encoding.
     *
     * @param value The string to convert
     * @return A new {@link CharSeqByteArray} instance.
     */
    public static CharSeqByteArray toByteArray(String value) {
        return new com.kosprov.jargon2.internal.ByteArray.CharSeqByteArray(value, DEFAULT_ENCODING);
    }

    /**
     * Convert a char[] to a {@link ClearableSourceCharSeqByteArray} using the default encoding.
     *
     * <p>
     *     Calling {@link ClearableSourceCharSeqByteArray#clearSource()} on the returned value will mark this instance
     *     to wipe the given char[] along with any other internally maintained state (e.g. in a try-with-resources
     *     block).
     * </p>
     *
     * @param value The char[] to convert
     * @return A new {@link ClearableSourceCharSeqByteArray} instance.
     */
    public static ClearableSourceCharSeqByteArray toByteArray(char[] value) {
        return new com.kosprov.jargon2.internal.ByteArray.ClearableSourceCharSeqByteArray(value, DEFAULT_ENCODING);
    }

    /**
     * Wrap a byte[] into a {@link ClearableSourceByteArray}.
     *
     * <p>
     *     Calling {@link ClearableSourceByteArray#clearSource()} on the returned value will mark this instance
     *     to wipe the given byte[] along with any other internally maintained state (e.g. in a try-with-resources
     *     block).
     * </p>
     *
     * @param bytes The {@link InputStream} to consume.
     * @return A new {@link ClearableSourceByteArray} instance.
     */
    public static ClearableSourceByteArray toByteArray(byte[] bytes) {
        return new com.kosprov.jargon2.internal.ByteArray.ClearableSourceByteArray(bytes);
    }

    /**
     * Consume an {@link InputStream} into a {@link ByteArray} using the default buffer size.
     *
     * @param is The {@link InputStream} to consume.
     * @return A new {@link ByteArray} instance.
     */
    public static ByteArray toByteArray(InputStream is) {
        return new com.kosprov.jargon2.internal.ByteArray(is, STREAM_BUFFER_SIZE);
    }

    /**
     * Consume an {@link InputStream} into a {@link ByteArray}.
     *
     * @param is The {@link InputStream} to consume.
     * @param bufferSize The buffer size to use during read
     * @return A new {@link ByteArray} instance.
     */
    public static ByteArray toByteArray(InputStream is, int bufferSize) {
        return new com.kosprov.jargon2.internal.ByteArray(is, bufferSize);
    }

    /**
     * Consume a {@link Reader} into a {@link CharSeqByteArray} using the default buffer size.
     *
     * @param reader The {@link Reader} to consume.
     * @return A new {@link CharSeqByteArray} instance.
     */
    public static CharSeqByteArray toByteArray(Reader reader) {
        return new com.kosprov.jargon2.internal.ByteArray.CharSeqByteArray(reader, STREAM_BUFFER_SIZE, DEFAULT_ENCODING);
    }

    /**
     * Consume a {@link Reader} into a {@link CharSeqByteArray}.
     *
     * @param reader The {@link Reader} to consume.
     * @param bufferSize The buffer size to use during read
     * @return A new {@link CharSeqByteArray} instance.
     */
    public static CharSeqByteArray toByteArray(Reader reader, int bufferSize) {
        return new com.kosprov.jargon2.internal.ByteArray.CharSeqByteArray(reader, bufferSize, DEFAULT_ENCODING);
    }

    /**
     * {@link AutoCloseable} converter of a value to a byte array.
     *
     * <p>
     *     During construction, the value is copied and converted to a byte array maintained internally.
     * </p>
     *
     * <p>
     *     The byte array is cleared in a try-with-resources block or by calling {@link #close()} or {@link #clear()}.
     *     In addition, calling {@link #finalizable()} will attach a finalization trigger to the object which will
     *     clear the byte array before garbage collection.
     * </p>
     */
    public interface ByteArray extends AutoCloseable {

        /**
         * Get a reference to the internally copied bytes
         *
         * @return A reference to the internally copied bytes
         */
        byte[] getBytes();

        /**
         * Clears any internally maintained state.
         *
         * <p>
         *     Has the same effect as a call to {@link #clear()}.
         * </p>
         *
         * @throws Exception N/A
         */
        @Override
        void close() throws Exception;

        /**
         * Clears any internally maintained state.
         */
        void clear();

        /**
         * Attach a finalization trigger to the {@link ByteArray} which will call {@link #clear()} before garbage
         * collection.
         *
         * @return The same {@link ByteArray} instance
         */
        ByteArray finalizable();
    }

    /**
     * {@link ByteArray} that originates from character data (strings, char arrays and readers).
     */
    public interface CharSeqByteArray extends ByteArray {

        @Override
        CharSeqByteArray finalizable();

        /**
         * Change the encoding to be used when conversion from characters to bytes is performed.
         *
         * @param encoding The encoding to be used
         * @return The same {@link CharSeqByteArray} instance
         */
        CharSeqByteArray encoding(String encoding);

        /**
         * Change the encoding to be used when conversion from characters to bytes is performed.
         *
         * @param encoding The encoding to be used
         * @return The same {@link CharSeqByteArray} instance
         */
        CharSeqByteArray encoding(Charset encoding);

        /**
         * Normalize the char sequence to the default normalized form before converting it to byte array.
         *
         * <p>
         *     <b>CAUTION</b>: This operation leaves a copy of the data source into memory. For example,
         *     if the given value was a password char[], a copy of it will be allocated, garbage collected
         *     BUT NOT WIPED after use. These memory locations will be overridden only when the JVM decides to
         *     lay out a new object on top of them.
         * </p>
         *
         * @return The same {@link CharSeqByteArray} instance
         */
        CharSeqByteArray normalize();

        /**
         * Normalize the char sequence to the given normalized form before converting it to byte array.
         *
         * <p>
         *     <b>CAUTION</b>: This operation leaves a copy of the data source into memory. For example,
         *     if the given value was a password char[], a copy of it will be allocated, garbage collected
         *     BUT NOT WIPED after use. These memory locations will be overridden only when the JVM decides to
         *     lay out a new object on top of them.
         * </p>
         *
         * @param normalization The normalized form to convert to
         * @return The same {@link CharSeqByteArray} instance
         */
        CharSeqByteArray normalize(Normalization normalization);
    }

    interface ClearableSource {
        /**
         * Mark whether the {@link ByteArray} must clear the source data of the {@link ByteArray}, along with any
         * internally maintained state.
         *
         * <p>Equivalent to a call {@link #clearSource(boolean)} with <tt>true</tt>.</p>
         *
         * @return The same {@link ClearableSource} instance
         */
        ClearableSource clearSource();

        /**
         * Set whether the {@link ByteArray} must clear the source data of the {@link ByteArray}, along with any
         * internally maintained state.
         *
         * @param clear If true, the source byte array will be cleared
         * @return The same {@link ClearableSource} instance
         */
        ClearableSource clearSource(boolean clear);
    }

    /**
     * {@link ByteArray} that can also clear the source of the data captured in it.
     */
    public interface ClearableSourceByteArray extends ByteArray, ClearableSource {

        @Override
        ClearableSourceByteArray finalizable();

        @Override
        ClearableSourceByteArray clearSource();

        @Override
        ClearableSourceByteArray clearSource(boolean clear);
    }

    /**
     * {@link ByteArray} that can also clear the source of the data captured in it.
     */
    public interface ClearableSourceCharSeqByteArray extends CharSeqByteArray, ClearableSource {

        @Override
        ClearableSourceCharSeqByteArray finalizable();

        @Override
        ClearableSourceCharSeqByteArray encoding(String encoding);

        @Override
        ClearableSourceCharSeqByteArray encoding(Charset encoding);

        @Override
        ClearableSourceCharSeqByteArray normalize();

        @Override
        ClearableSourceCharSeqByteArray normalize(Normalization normalization);

        @Override
        ClearableSourceCharSeqByteArray clearSource();

        @Override
        ClearableSourceCharSeqByteArray clearSource(boolean clear);
    }

    /**
     * Supported normalization forms by {@link CharSeqByteArray#normalize(Normalization)}
     */
    public enum Normalization {
        /**
         * Canonical decomposition.
         */
        NFD,

        /**
         * Canonical decomposition, followed by canonical composition.
         */
        NFC,

        /**
         * Compatibility decomposition.
         */
        NFKD,

        /**
         * Compatibility decomposition, followed by canonical composition.
         */
        NFKC
    }

    /**
     * Immutable builder (copy-on-write) to configure and use the Argon2 hashing backend.
     */
    public interface Hasher {
        /**
         * Configure the Argon2 {@link Jargon2Backend}
         *
         * If left unspecified, the backend implementation with be discovered automatically
         *
         * @param backend The backend to use
         * @return A copy of this builder
         */
        Hasher backend(Jargon2Backend backend);

        /**
         * Configure the Argon2 {@link Jargon2Backend} by class name
         *
         * If left unspecified, the backend implementation with be discovered automatically
         *
         * @param backendClass The backend class name to instantiate
         * @return A copy of this builder
         */
        Hasher backend(String backendClass);

        /**
         * Configure the Argon2 {@link Jargon2Backend} by class
         *
         * If left unspecified, the backend implementation with be discovered automatically
         *
         * @param backendClass The backend class to instantiate
         * @return A copy of this builder
         */
        Hasher backend(Class<? extends Jargon2Backend> backendClass);

        /**
         * Configure any set of options which will be passed to the backend
         *
         * @param options A map of options
         * @return A copy of this builder
         */
        Hasher options(Map<String, Object> options);

        /**
         * Configure the Argon2 {@link Type}
         *
         * @param type The type to use
         * @return A copy of this builder
         */
        Hasher type(Type type);

        /**
         * Configure the Argon2 {@link Version}
         *
         * @param version The version to use
         * @return A copy of this builder
         */
        Hasher version(Version version);

        /**
         * Configure the time cost (number of passed through memory)
         *
         * @param timeCost The time cost value
         * @return A copy of this builder
         */
        Hasher timeCost(int timeCost);

        /**
         * Configure the memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         *
         * @param memoryCost The memory cost value
         * @return A copy of this builder
         */
        Hasher memoryCost(int memoryCost);

        /**
         * Configure the parallelism of the hash calculation. Sets lanes and threads to the given value.
         *
         * @param parallelism The parallelism value
         * @return A copy of this builder
         */
        Hasher parallelism(int parallelism);

        /**
         * Configure the number of memory lanes and the maximum number of threads independently
         *
         * @param lanes The memory lanes value
         * @param threads The maximum threads value
         * @return A copy of this builder
         */
        Hasher parallelism(int lanes, int threads);

        /**
         * Configure the length of the output hash
         *
         * @param hashLength The number of bytes of the raw hash value
         * @return A copy of this builder
         */
        Hasher hashLength(int hashLength);

        /**
         * Configure the length of the salt, if generated internally by the {@link Hasher}.
         *
         * <p>
         *     The default {@link SaltGenerator} uses a singleton {@link SecureRandom} instance with the preferred
         *     provider and algorithm for the runtime platform. The selection process is described on the
         *     {@link SecureRandom#SecureRandom() SecureRandom default constructor documentation}.
         * </p>
         *
         * @param saltLength The number of bytes of the salt to be generated
         * @return A copy of this builder
         */
        Hasher saltLength(int saltLength);

        /**
         * Set the salt to be used during hashing. No salt generation is performed if salt
         * value is set externally.
         *
         * @param salt The salt value
         * @return A copy of this builder
         */
        Hasher salt(byte[] salt);

        /**
         * Set the salt to be used during hashing as a {@link ByteArray}. No salt generation is performed if salt
         * value is set externally.
         *
         * @param salt The salt value
         * @return A copy of this builder
         */
        Hasher salt(ByteArray salt);

        /**
         * Override the default, singleton {@link SaltGenerator} generator described in {@link #saltLength(int)} with
         * a custom implementation.
         *
         * @param saltGenerator The salt generator instance to be used
         * @return A copy of this builder
         */
        Hasher saltGenerator(SaltGenerator saltGenerator);

        /**
         * Use a new {@link SaltGenerator} generator with a new {@link SecureRandom} instance using the given algorithm.
         *
         * @param secureRandomAlgorithm The algorithm to be used
         * @return A copy of this builder
         */
        Hasher saltGenerator(String secureRandomAlgorithm);

        /**
         * Use a new {@link SaltGenerator} generator with a new {@link SecureRandom} instance using the given algorithm
         * and provider.
         *
         * @param secureRandomAlgorithm The algorithm to be used
         * @param secureRandomProvider The provider to be used
         * @return A copy of this builder
         */
        Hasher saltGenerator(String secureRandomAlgorithm, String secureRandomProvider);

        /**
         * Use a new {@link SaltGenerator} generator with a new {@link SecureRandom} instance using the given algorithm
         * and provider.
         *
         * @param secureRandomAlgorithm The algorithm to be used
         * @param secureRandomProvider The provider to be used
         * @return A copy of this builder
         */
        Hasher saltGenerator(String secureRandomAlgorithm, Provider secureRandomProvider);

        /**
         * Set the password value to be hashed
         *
         * @param password The password value
         * @return A copy of this builder
         */
        Hasher password(byte[] password);

        /**
         * Set the password value to be hashed as a {@link ByteArray}
         *
         * @param password The password value
         * @return A copy of this builder
         */
        Hasher password(ByteArray password);

        /**
         * Configure the secret for keyed hashing (can be left unspecified)
         *
         * @param secret The secret value
         * @return A copy of this builder
         */
        Hasher secret(byte[] secret);

        /**
         * Configure the secret for keyed hashing (can be left unspecified) as a {@link ByteArray}
         *
         * @param secret The secret value
         * @return A copy of this builder
         */
        Hasher secret(ByteArray secret);

        /**
         * Set additional authentication data to be included in the hash (can be left unspecified)
         *
         * @param ad The ad value
         * @return A copy of this builder
         */
        Hasher ad(byte[] ad);

        /**
         * Set additional authentication data to be included in the hash (can be left unspecified) as a {@link ByteArray}
         *
         * @param ad The ad value
         * @return A copy of this builder
         */
        Hasher ad(ByteArray ad);

        /**
         * Calculate the raw hash (hashLength bytes)
         *
         * @return The raw hash
         * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
         */
        byte[] rawHash() throws Jargon2Exception;

        /**
         * Calculate the encoded hash.
         *
         * Output string has the following format:
         *
         * <pre>
         * $argon2&lt;type&gt;[$v=&lt;version&gt;]$m=&lt;memoryCost&gt;,t=&lt;timeCost&gt;,p=&lt;lanes&gt;$&lt;salt&gt;$&lt;hash&gt;
         * </pre>
         *
         * The version param ($v=num) is not present for V10.
         *
         * Salt and hash are Base64 encoded without any padding, new lines or spaces.
         *
         * @return The encoded hash
         * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
         */
        String encodedHash() throws Jargon2Exception;
    }

    /**
     * Immutable builder (copy-on-write) to configure and use the Argon2 verification backend.
     */
    public interface Verifier {
        /**
         * Configure the Argon2 {@link Jargon2Backend}.
         *
         * If left unspecified, the backend implementation with be discovered automatically
         *
         * @param backend The backend to use
         * @return A copy of this builder
         */
        Verifier backend(Jargon2Backend backend);

        /**
         * Configure the Argon2 {@link Jargon2Backend} by class name
         *
         * If left unspecified, the backend implementation with be discovered automatically
         *
         * @param backendClass The backend class to instantiate
         * @return A copy of this builder
         */
        Verifier backend(String backendClass);

        /**
         * Configure the Argon2 {@link Jargon2Backend} by class name
         *
         * If left unspecified, the backend implementation with be discovered automatically
         *
         * @param backendClass The backend class to instantiate
         * @return A copy of this builder
         */
        Verifier backend(Class<? extends Jargon2Backend> backendClass);

        /**
         * Configure any set of options which will be passed to the backend
         *
         * @param options A map of options
         * @return A copy of this builder
         */
        Verifier options(Map<String, Object> options);

        /**
         * Configure the Argon2 {@link Type}.
         *
         * In case of encoded hashing, it can be left unspecified.
         *
         * @param type The type to use
         * @return A copy of this builder
         */
        Verifier type(Type type);

        /**
         * Configure the Argon2 {@link Version}
         *
         * In case of encoded hashing, it can be left unspecified.
         *
         * @param version The version to use
         * @return A copy of this builder
         */
        Verifier version(Version version);

        /**
         * Configure the time cost (number of passed through memory)
         *
         * In case of encoded hashing, it can be left unspecified.
         *
         * @param timeCost The time cost value
         * @return A copy of this builder
         */
        Verifier timeCost(int timeCost);

        /**
         * Configure the memory cost in kibi bytes (e.g. 65536 -&gt; 64MB)
         *
         * In case of encoded hashing, it can be left unspecified.
         *
         * @param memoryCost The memory cost value
         * @return A copy of this builder
         */
        Verifier memoryCost(int memoryCost);

        /**
         * Configure the parallelism of the hash calculation. Sets lanes and threads to the given value.
         *
         * <p>
         *     In case of encoded hashing, it can be left unspecified. Lanes and threads will be derived from the
         *     encoded value.
         * </p>
         *
         * @param parallelism The parallelism value
         * @return A copy of this builder
         */
        Verifier parallelism(int parallelism);

        /**
         * Configure the number of memory lanes and the maximum number of threads independently
         *
         * <p>
         *     In case of encoded hashing, it can be left unspecified. Lanes and threads will be derived from the
         *     encoded value.
         * </p>
         *
         * @param lanes The memory lanes value
         * @param threads The maximum threads value
         * @return A copy of this builder
         */
        Verifier parallelism(int lanes, int threads);

        /**
         * Configure the maximum number of threads to be used during hash recalculation regardless of the the value of
         * lanes set or derived from the encoded hash.
         *
         * <p>
         *     Lanes and threads can be specified independently. For example:
         * </p>
         *
         * <p>
         *     If lanes=4 (or p=4 in encoded hash string) and threads=1, a single thread will be used to process all 4 lanes.
         *     If lanes=4 (or p=4 in encoded hash string) and threads=8, 4 threads will be used (threads are capped to
         *     lanes).
         * </p>
         *
         * @param threads The threads value
         * @return A copy of this builder
         */
        Verifier threads(int threads);

        /**
         * Set the salt used during hashing.
         *
         * In case of encoded hashing, it can be left unspecified.
         *
         * @param salt The salt value
         * @return A copy of this builder
         */
        Verifier salt(byte[] salt);

        /**
         * Set the salt used during hashing as a {@link ByteArray}.
         *
         * In case of encoded hashing, it can be left unspecified.
         *
         * @param salt The salt value
         * @return A copy of this builder
         */
        Verifier salt(ByteArray salt);

        /**
         * Set the password value to be verified
         *
         * @param password The password value
         * @return A copy of this builder
         */
        Verifier password(byte[] password);

        /**
         * Set the password value to be verified as a {@link ByteArray}
         *
         * @param password The password value
         * @return A copy of this builder
         */
        Verifier password(ByteArray password);

        /**
         * Configure the secret for keyed hashing (can be left unspecified)
         *
         * @param secret The secret value
         * @return A copy of this builder
         */
        Verifier secret(byte[] secret);

        /**
         * Configure the secret for keyed hashing (can be left unspecified) as a {@link ByteArray}
         *
         * @param secret The secret value
         * @return A copy of this builder
         */
        Verifier secret(ByteArray secret);

        /**
         * Set the additional authentication data included in the hash (can be left unspecified)
         *
         * @param ad The ad value
         * @return A copy of this builder
         */
        Verifier ad(byte[] ad);

        /**
         * Set the additional authentication data included in the hash (can be left unspecified) as a {@link ByteArray}
         *
         * @param ad The ad value
         * @return A copy of this builder
         */
        Verifier ad(ByteArray ad);

        /**
         * Set the encoded hash value produced during hashing (can be left unspecified if raw hash was calculated)
         *
         * Encoded hash hash the following format:
         *
         * <pre>
         * $argon2&lt;type&gt;[$v=&lt;version&gt;]$m=&lt;memoryCost&gt;,t=&lt;timeCost&gt;,p=&lt;lanes&gt;$&lt;salt&gt;$&lt;hash&gt;
         * </pre>
         *
         * The version param ($v=num) is not present for V10.
         *
         * Salt and hash are Base64 encoded without any padding, new lines or spaces.
         *
         * @param encodedHash The encoded hash value
         * @return A copy of this builder
         */
        EncodedVerifier hash(String encodedHash);

        /**
         * Set the raw hash value produced during hashing (can be left unspecified if encoded hash was calculated)
         *
         * @param rawHash The raw hash value
         * @return A copy of this builder
         */
        RawVerifier hash(byte[] rawHash);
    }

    /**
     * Sub-interface of {@link Verifier} to verify encoded hash
     */
    public interface EncodedVerifier extends Verifier {

        @Override
        EncodedVerifier backend(Jargon2Backend backend);

        @Override
        EncodedVerifier backend(String backendClass);

        @Override
        EncodedVerifier backend(Class<? extends Jargon2Backend> backendClass);

        @Override
        EncodedVerifier options(Map<String, Object> options);

        @Override
        EncodedVerifier type(Type type);

        @Override
        EncodedVerifier version(Version version);

        @Override
        EncodedVerifier timeCost(int timeCost);

        @Override
        EncodedVerifier memoryCost(int memoryCost);

        @Override
        EncodedVerifier parallelism(int parallelism);

        @Override
        EncodedVerifier parallelism(int lanes, int threads);

        @Override
        EncodedVerifier threads(int threads);

        @Override
        EncodedVerifier salt(byte[] salt);

        @Override
        EncodedVerifier salt(ByteArray salt);

        @Override
        EncodedVerifier password(byte[] password);

        @Override
        EncodedVerifier password(ByteArray password);

        @Override
        EncodedVerifier secret(byte[] secret);

        @Override
        EncodedVerifier secret(ByteArray secret);

        @Override
        EncodedVerifier ad(byte[] ad);

        @Override
        EncodedVerifier ad(ByteArray ad);

        @Override
        EncodedVerifier hash(String encodedHash);

        @Override
        RawVerifier hash(byte[] rawHash);

        /**
         * Verify the encoded hash
         *
         * @return true if recalculating the hash matches
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyEncoded() throws Jargon2Exception;
    }

    /**
     * Sub-interface of {@link Verifier} to verify raw hash
     */
    public interface RawVerifier extends Verifier {

        @Override
        RawVerifier backend(Jargon2Backend backend);

        @Override
        RawVerifier backend(String backendClass);

        @Override
        RawVerifier backend(Class<? extends Jargon2Backend> backendClass);

        @Override
        RawVerifier options(Map<String, Object> options);

        @Override
        RawVerifier type(Type type);

        @Override
        RawVerifier version(Version version);

        @Override
        RawVerifier timeCost(int timeCost);

        @Override
        RawVerifier memoryCost(int memoryCost);

        @Override
        RawVerifier parallelism(int parallelism);

        @Override
        RawVerifier parallelism(int lanes, int threads);

        @Override
        RawVerifier threads(int threads);

        @Override
        RawVerifier salt(byte[] salt);

        @Override
        RawVerifier salt(ByteArray salt);

        @Override
        RawVerifier password(byte[] password);

        @Override
        RawVerifier password(ByteArray password);

        @Override
        RawVerifier secret(byte[] secret);

        @Override
        RawVerifier secret(ByteArray secret);

        @Override
        RawVerifier ad(byte[] ad);

        @Override
        RawVerifier ad(ByteArray ad);

        @Override
        EncodedVerifier hash(String encodedHash);

        @Override
        RawVerifier hash(byte[] rawHash);

        /**
         * Verify the raw hash
         *
         * @return true if recalculating the hash matches
         * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
         */
        boolean verifyRaw() throws Jargon2Exception;
    }
}
