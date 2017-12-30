package com.kosprov.jargon2.spi;

import com.kosprov.jargon2.api.Jargon2Exception;

import java.util.Map;

import static com.kosprov.jargon2.api.Jargon2.*;

/**
 * Service Provider Interface (SPI) for Argon2 backend implementations.
 *
 * <p>
 *     Service registration can be done by one the two methods: i) Annotate the jar containing the implementation with a
 *     {@link java.util.ServiceLoader} <tt>META-INF/services/com.kosprov.jargon2.spi.Jargon2Backend</tt> file
 *     containing the fully qualified name of the implementation, or ii) by specifying the
 *     <tt>-Dcom.kosprov.jargon2.spi.backend</tt> system property equal to the fully qualified name of the
 *     implementation.
 * </p>
 *
 * <p>
 *     Only one implementation must be found for the whole JVM by any of the discovery methods.
 * </p>
 *
 * <p>
 *     Implementing classes must have a default constructor.
 * </p>
 *
 */
public interface Jargon2Backend {

    /**
     * <p><b>Implementor's guides</b></p>
     * <p>
     *     Implementors must validate input according to Argon2 specification. The basic validations are: minimum hashLength,
     *     minimum salt length, minimum and maximum lanes, minimum and maximum threads, minimum memory cost and minimum time cost.
     * </p>
     *
     * <p>
     *     Implementors must accept threads to be larger than lanes and use as many threads as lanes.
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
     * @param options Any options for the backend
     * @return A byte array of length hashLength with the hash value
     * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
     *
     * @see LowLevelApi#rawHash(Type, Version, int, int, int, int, int, byte[], byte[], byte[], byte[], Map)
     */
    byte[] rawHash(Type type, Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options);

    /**
     * <p><b>Implementor's guides</b></p>
     * <p>
     *     Implementors must validate input according to Argon2 specification. The basic validations are: minimum hashLength,
     *     minimum salt length, minimum and maximum lanes, minimum and maximum threads, minimum memory cost and minimum time cost.
     * </p>
     *
     * <p>
     *     Implementors must accept threads to be larger than lanes and use as many threads as lanes.
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
     * @param options Any options for the backend
     * @return A string containing the encoded hash value
     * @throws Jargon2Exception If required parameters are missing, are invalid or hash calculation fails unexpectedly
     *
     * @see LowLevelApi#encodedHash(Type, Version, int, int, int, int, int, byte[], byte[], byte[], byte[], Map)
     */
    String encodedHash(Type type, Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options);

    /**
     * <p><b>Implementor's guides</b></p>
     * <p>
     *     Implementors must validate input according to Argon2 specification. The basic validations are: minimum raw hash length,
     *     minimum salt length, minimum and maximum lanes, minimum and maximum threads, minimum memory cost and minimum time cost.
     * </p>
     *
     * <p>
     *     Implementors must accept threads to be larger than lanes and use as many threads as lanes.
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
     * @param options Any options for the backend
     * @return true if recalculating the hash matches the given value
     * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
     *
     * @see LowLevelApi#verifyRaw(Type, Version, int, int, int, int, byte[], byte[], byte[], byte[], byte[], Map)
     */
    boolean verifyRaw(Type type, Version version, int memoryCost, int timeCost, int lanes, int threads, byte[] rawHash, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options);

    /**
     * <p><b>Implementor's guides</b></p>
     *
     * <p>
     *     Implementors must parse the encoded hash value and perform the same validations as {@link Jargon2Backend#verifyRaw}.
     * </p>
     *
     * @param encodedHash The encoded hash
     * @param threads The maximum number of threads it be used during hash recalculation. -1 to derive the number of threads
     *                from the parallelism property of the encoded hash.
     * @param secret The secret (keyed hashing) used during hashing. Can be null
     * @param ad Additional authentication data to included during hashing. Can be null
     * @param password The password to verify
     * @param options Any options for the backend
     * @return true if recalculating the hash matches the given value
     * @throws Jargon2Exception If required parameters are missing, are invalid or verification fails unexpectedly
     *
     * @see LowLevelApi#verifyEncoded(String, int, byte[], byte[], byte[], Map)
     */
    boolean verifyEncoded(String encodedHash, int threads, byte[] secret, byte[] ad, byte[] password, Map<String, Object> options);
}
