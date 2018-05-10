# Jargon2: Fluent Java API for Argon2 password hashing

[![Build Status](https://travis-ci.org/kosprov/jargon2-api.svg?branch=master)](https://travis-ci.org/kosprov/jargon2-api)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/14707/badge.svg)](https://scan.coverity.com/projects/kosprov-jargon2-api)
[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/kosprov/jargon2/jargon2-api/maven-metadata.xml.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.kosprov.jargon2%22%20AND%20a%3A%22jargon2-api%22)
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=com.kosprov.jargon2:jargon2-api)](https://sonarcloud.io/dashboard/index/com.kosprov.jargon2:jargon2-api)
[![Dependency Status](https://www.versioneye.com/user/projects/5af3e33c0fb24f0e5d5149c5/badge.svg)](https://www.versioneye.com/user/projects/5af3e33c0fb24f0e5d5149c5)

Welcome to the Jargon2 repository.

This document provides all the resources to effectively use Jargon2 and utilize its capabilities.

## Introduction

Jargon2 is a builder-like API to configure and use a `Hasher` and `Verifier` API to calculate and verify password hashes.

Its main features are: 

- Support for encoded and raw hashing
- Pluggable backends to allow switching implementations
- Ability to set memory lanes and threads independently
- Dedicated API for keyed-hashing and additional authentication data
- Secure handling of sensitive data in memory
- Configurable salt generation
- Unified API for different input sources (`byte[]`, `char[]`, `String` etc)
- Normalization of Unicode values
- Fall-back low-level API for testing and cross-validation

## Security considerations

This section summarizes any security considerations that come with the use of this library. Make sure you evaluate them before choosing to use Jargon2 and visit this section regularly for any updates.

| Item |  Description |
| ---  | --- |
| Normalization | Choosing to convert passwords to Unicode normal form leaves short-lived copies of it in memory. See [section on normalization](#normalization) for more details. |
| Backend registration by system property | If backend registration is done by system property, its value should be monitored for improper use. See [section on backends](#backends) for more details. |
| Default backend native library can be bypassed | If you're using the default backend (`jargon2-native-ri-backend`), the shared library it binds to can be overridden by defining one of `-Djna.boot.library.path`, `-Djna.library.path` and `-Djna.nosys` system properties. See the [Jargon2 Backends repository](https://github.com/kosprov/jargon2-backends "Jargon2 Backends repository") for more details. |


## User's guide

Jargon2 requires Java 7 (or higher) and your application would need to have two dependencies; the Jargon2 API and a backend implementation.

If you're using Maven, add the following dependencies to your pom:

```xml
<dependency>
    <groupId>com.kosprov.jargon2</groupId>
    <artifactId>jargon2-api</artifactId>
    <version>1.0.2</version>
</dependency>
<dependency>
    <groupId>com.kosprov.jargon2</groupId>
    <artifactId>jargon2-native-ri-backend</artifactId>
    <version>1.1.1</version>
    <scope>runtime</scope>
</dependency>
```

You may need to change the version numbers to the most recent release. The second dependency is the [default Jargon2 backend implementation](https://github.com/kosprov/jargon2-backends "Jargon2 Backends repository") that wraps the [Argon2 reference implementation](https://github.com/P-H-C/phc-winner-argon2 "Argon2 reference implementation repository") written in C. It includes x86 binaries compiled for Windows, Linux and macOS, so it should work on most systems. Backend implementations can automatically be discovered using a `java.util.ServiceLoader` so there is no build-time dependency to the backend classes. More on this on the [backends](#backends) section.

> **Tip**:  To enjoy the Jargon2 API fluency, always start with this static import:
```java
import static com.kosprov.jargon2.api.Jargon2.*;
``` 

### Simple example

The simplest possible example would be the following:

```java
import static com.kosprov.jargon2.api.Jargon2.*;

public class Jargon2Example {
    public static void main(String[] args) {
        byte[] password = "this is a password".getBytes();

        // Configure the hasher
        Hasher hasher = jargon2Hasher()
                .type(Type.ARGON2d) // Data-dependent hashing
                .memoryCost(65536)  // 64MB memory cost
                .timeCost(3)        // 3 passes through memory
                .parallelism(4)     // use 4 lanes and 4 threads
                .saltLength(16)     // 16 random bytes salt
                .hashLength(16);    // 16 bytes output hash

        // Set the password and calculate the encoded hash
        String encodedHash = hasher.password(password).encodedHash();

        System.out.printf("Hash: %s%n", encodedHash);

        // Just get a hold on the verifier. No special configuration needed
        Verifier verifier = jargon2Verifier();

        // Set the encoded hash, the password and verify
        boolean matches = verifier.hash(encodedHash).password(password).verifyEncoded();

        System.out.printf("Matches: %s%n", matches);
    }
}
```
`Hasher` and `Verifier` are immutable (copy-on-write), thread-safe objects. Each method call returns a new copy. You usually cascade method calls to build an instance with the static configuration (type, memory cost, time cost etc) and use that prototype instance to pass all values (password, salt, ad etc) needed to calculate a specific hash. The prototype object does not change and can be reused to calculate more hashes. Since it is immutable, it can be safely accessed by multiple threads.

To understand this point further, lets see a simple component that exposes a `hash`/`verify` API to clients:

```java
import static com.kosprov.jargon2.api.Jargon2.*;

/**
 * Simple thread-safe POJO the encapsulates Argon2 configuration and
 * exposes an API for encoded hashing and verification of passwords.
 */
public class EncodedPasswordHasher {

    private final Hasher hasher;
    private final Verifier verifier;

    /**
     * Create a new instance
     */
    public EncodedPasswordHasher() {
        // build the prototype instances
        hasher = jargon2Hasher()
                .type(Type.ARGON2d)
                .memoryCost(65536)
                .timeCost(3)
                .parallelism(4)
                .saltLength(16)
                .hashLength(16);
        verifier = jargon2Verifier();
    }

    /**
     * Calculate the encoded hash of the given password
     *
     * @param password The password
     * @return The encoded hash
     */
    public String hash(byte[] password) {
        // password is captured in a copy of the prototype hasher
        return hasher.password(password).encodedHash();
    }

    /**
     * Verify if the given password matches with the encoded hash
     *
     * @param encodedHash The encoded hash
     * @param password The password
     * @return <tt>true</tt> if password matches with the hash
     */
    public boolean verify(String encodedHash, byte[] password) {
        // encodedHash and password are captured in copies of the prototype verifier
        return verifier.hash(encodedHash).password(password).verifyEncoded();
    }
}
```
`EncodedPasswordHasher` instantiates `Hasher` and `Verifier` prototype instances and creates copies of them to calculate or verify the hash. Therefore, `EncodedPasswordHasher` is thread-safe.  

### Configuration options

Jargon2 allows configurability on almost every piece of functionality it provides. Below, there is table with all available configuration options.

**`Hasher`**

| Method |  Description |
| ---  | --- |
| `backend` | Change the backend implementation for this hasher only. Multiple overloaded methods take a class name, a `Class` object or an actual backend instance (useful only for testing). |
| `options` | A set of key-value pairs to be passed to the backend. Useful if the backend needs special configuration and you don't want to be limited to system properties. |
| `type` | Set the Argon2 type (Argon2i, Argon2d or Argon2id). |
| `version` | Set the Argon2 version (1.0 or 1.3). |
| `memoryCost` | Set the number of KB of memory to fill during hash calculation. |
| `timeCost` | Set the number of passes through memory before getting the final hash value. |
| `parallelism` | Set the number of memory lanes and the number of threads to process lanes. Can be set independently, e.g. set lanes to 8 and threads to 4. That would calculate the hash with 8 lanes and allows to increase threads to 8 in the future. Setting more threads than lanes is allowed but are internally capped. |
| `hashLength` | Set the number of bytes of the output hash. |
| `saltLength` | Set the number of bytes of the automatically generated salt. This makes sense only on the encoded hash case where the salt generated internally is part of the output value. |
| `saltGenerator` | Set an implementation of the `com.kosprov.jargon2.api.Jargon2.SaltGenerator` to replace the default generator that uses a singleton `java.security.SecureRandom` instance. |
| `secret` | Set the key to be used for keyed hashing (HMAC). |

**`Verifier`**

| Method |  Description |
| ---  | --- |
| `backend` | Change the backend implementation for this verifier only. Multiple overloaded methods take a class name, a `Class` object or an actual backend instance (useful only for testing). |
| `options` | A set of key-value pairs to be passed to the backend. Useful if the backend needs special configuration and you don't want to be limited to system properties. |
| `type` | Set the Argon2 type (Argon2i, Argon2d or Argon2id). Used only when verifying a raw hash. Encoded hash verification reads the value from the encoded hash, itself. |
| `version` | Set the Argon2 version (1.0 or 1.3). Used only when verifying a raw hash. Encoded hash verification reads the value from the encoded hash, itself. |
| `memoryCost` | Set the number of KB of memory to fill during hash verification. Used only when verifying a raw hash. Encoded hash verification reads the value from the encoded hash, itself. |
| `timeCost` | Set the number of passes through memory before getting the final hash value. Used only when verifying a raw hash. Encoded hash verification reads the value from the encoded hash, itself. |
| `parallelism` | Set the number of memory lanes and the number of threads to process lanes. The number of lanes is used only when verifying a raw hash. Encoded hash verification reads the value from the encoded hash, itself. |
| `threads` | Set the number of threads to process lanes. This makes sense to use only for encoded hash verification. The number of lanes is read from the encoded hash itself and the number of threads by this value. If left unspecified, it uses as many threads as lanes. |
| `secret` | Set the key to be used for keyed hashing (HMAC). |


### Raw hashing

From a configuration standpoint, raw hashing differs from encoded hashing in just two points:

- The `Hasher` cannot be configured to generate the salt automatically. The salt needs to be generated (and stored) externally and passed along with the password to calculate or verify the hash.
- The `Verifier` must be configured with the same options as the `Hasher`. Since there is no encoded string to derive options from, the `Verifier` needs to know what was used during hashing.

Lets see how a simple raw hashing component would look like.

```java
import static com.kosprov.jargon2.api.Jargon2.*;

/**
 * Simple thread-safe POJO the encapsulates Argon2 configuration and
 * exposes an API for raw hashing and verification of passwords.
 */
public class RawPasswordHasher {

    private final Hasher hasher;
    private final Verifier verifier;

    /**
     * Create a new instance
     */
    public RawPasswordHasher() {
        Type type = Type.ARGON2d;
        int memoryCost = 65536;
        int timeCost = 3;
        int parallelism = 4;
        int hashLength = 16;

        // build the hasher prototype instance
        hasher = jargon2Hasher()
                .type(type)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(parallelism)
                .hashLength(hashLength);
        // build the verifier prototype instance with the same configuration
        verifier = jargon2Verifier()
                .type(type)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(parallelism);
    }

    /**
     * Calculate the raw hash of the given salt and password
     * 
     * @param salt The salt
     * @param password The password
     * @return The raw hash bytes
     */
    public byte[] hash(byte[] salt, byte[] password) {
        // password and salt are captured in copies of the prototype hasher
        return hasher.salt(salt).password(password).rawHash();
    }

    /**
     * Verify if the given salt and password matches with the raw hash
     *
     * @param rawHash The raw hash
     * @param salt The salt
     * @param password The password
     * @return <tt>true</tt> if salt and password matches with the hash
     */
    public boolean verify(byte[] rawHash, byte[] salt, byte[] password) {
        // rawHash, salt and password are captured in copies of the prototype verifier
        return verifier.hash(rawHash).salt(salt).password(password).verifyRaw();
    }
}
```
### The ByteArray API

Passwords and secrets are typically available as `char[]`. Converting them to `byte[]` (as this is what's needed by low-level libraries) creates a copy of the sensitive value. This copy must be safely zeroed-out after use. 

Jargon2 provides the `ByteArray` API to convert data to `byte[]` and wipe any copies created when not needed anymore. Currently, the data sources that can be converted to `ByteArray` are `char[]`, `String`, `java.io.InputStream` and `java.io.Reader`.

```java
import static com.kosprov.jargon2.api.Jargon2.*;

...

char[] password = somehowGetPassword();

ByteArray passwordByteArray = toByteArray(password); 
```

Simply wrapping the `char[]` to `ByteArray` will not trigger anything. You have to code any of the following idioms:

- Define it in a try-with-resources block

    `ByteArray` implements the `java.lang.AutoClosable` interface, so it can be used in a try-with-resources block. On block exit, `ByteArray.close()` method will wipe out any internally maintained state. 
    ```java
    try (ByteArray passwordByteArray = toByteArray(password)) {
        // use passwordByteArray with Hasher or Verifier
    }
    // Internal byte[] copy is zeroed-out here
    ```
- Define it in a try-finally block
	
    If you're not very happy with `AutoClosable`'s checked exception, you can use a simple try-finally block and manually call the `ByteArray.clear()` method.
    ```java
    ByteArray passwordByteArray = toByteArray(password);
    try {
    	// use passwordByteArray with Hasher or Verifier
    } finally {
    	passwordByteArray.clear(); // this will zero-out the internal byte[] copy
    }
- Make `ByteArray` instance clear memory during finalization
	
    If you're not too concerned about security, you can call `ByteArray.finalizable()` to make this instance zero out its memory during garbage collection. When (and if) the finalizer will call the `finalize()` method is beyond your control but it's better than nothing. On average, sensitive data will stay less time in memory.
    ```java
    ByteArray passwordByteArray = toByteArray(password).finalizable();
    // use passwordByteArray with Hasher or Verifier and let it be garbage collected
    ```

#### Clearing the source

Many authentication libraries like JAAS capture the user submitted password to `char[]` to allow your code to clear it when authentication is over.

In the previous section we saw how `ByteArray` can clear any internally maintained state in a semi-automatic manner. When `ByteArray` wraps mutable data sources like `char[]` or `byte[]`, it can be marked to clear the data source, as well.

```java
char[] password = somehowGetPassword();
try (ByteArray passwordByteArray = toByteArray(password).clearSource()) {
    // use passwordByteArray with Hasher or Verifier
}
// Internal byte[] copy AND the source char[] (password variable) are zeroed-out here
```

#### Normalization

If you allow non-ASCII characters for passwords, you may encounter some rare conditions where a user is not able to authenticate due to [Unicode equivalence](https://en.wikipedia.org/wiki/Unicode_equivalence) of some non-ASCII characters.

This problem is usually solved by converting the character sequence into a Unicode normal form. This needs to be done before calculating the hash and before each verification. Jargon2 `ByteArray` provides a convenience method for converting a text source into Unicode normal form before encoding it into a `byte[]`. This is simply done as:

```java
char[] password = somehowGetNonAsciiPassword();
try (ByteArray passwordByteArray = toByteArray(password).normalize()) {
    // use passwordByteArray with Hasher or Verifier
    // it will be normalized to NFC before converted to bytes.
}
```
> **Security consideration**. The current implementation of normalization depends on `java.text.Normalizer` which creates internal, short-lived copies of the data passed to it before returning the normalized version. This is not good for security because user passwords will stay in memory until the JVM decides to lay out some other object on top of it. On generational garbage collection schemes, this value will be created and stay on eden space and very quickly be overridden by other objects. 
> If you happen to know a normalizer that does not create copies (or wipes them before letting them to the garbage collector), please, open an issue.

### A more elaborate example

To showcase how Jargon2 can be used effectively, a more advanced integration scenario will be presented. We will assume a JavaEE environment where we will port the `EncodedPasswordHasher` and `RawPasswordHasher` implemented above. It will support both encoded and raw hashing and will also use a secret for keyed hashing.

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.inject.Inject;

import static com.kosprov.jargon2.api.Jargon2.*;

/**
 * EJB that exposes encoded and raw password hashing and verification
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class KeyedPasswordHasher {

    private ByteArray secret;
    private Hasher hasher;
    private Verifier verifier;

    @Inject
    private KeyStore keyStore; // Assume there is another component that loads the HMAC key

    @PostConstruct
    private void init() {
        // Load key and wrap it to a ByteArray. Also, mark to clear the key byte[] as well
        secret = toByteArray(keyStore.loadKey()).clearSource();

        Type type = Type.ARGON2d;
        Version version = Version.V13;
        int memoryCost = 16384;
        int timeCost = 3;
        int lanes = 4;   // use 4 lanes ...
        int threads = 1; // ... but only 1 thread
        int saltLength = 16;
        int hashLength = 16;

        hasher = jargon2Hasher()
                // Set the HMAC key
                .secret(secret)
                // Configure the hasher
                .type(type)
                .version(version)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(lanes, threads)
                .saltLength(saltLength)
                .hashLength(hashLength)
                ;

        // Configure the verifier
        verifier = jargon2Verifier()
                // Set the HMAC key
                .secret(secret)
                // Configure the verifier (only effective on raw hash verification.
                // On encoded hash verification, all these values except threads are
                // derived from the encoded hash string)
                .type(type)
                .version(Version.V13)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(lanes, threads) // threads applies to encoded hashing, as well
                ;
    }

    @PreDestroy
    private void destroy() {
        secret.clear(); // Zero out memory locations of HMAC key during undeployment
    }

    /**
     * Calculate an Argon2 encoded hash. The password is cleared before this method returns.
     *
     * @param password The password to be hashed
     * @return The encoded Argon2 hash
     */
    public String encodedHash(char[] password) {
        try (ByteArray passwordByteArray = toByteArray(password).clearSource()) {
            return hasher.password(passwordByteArray).encodedHash();
        } catch (Exception e) {
            throw new RuntimeException("Error during password hashing", e);
        }
    }

    /**
     * Verify that the password matches with the encoded hash. The password is cleared before
     * this method returns.
     *
     * @param encodedHash The encoded hash
     * @param password The password to be verified
     * @return <tt>true</tt> if the password matches with the encoded hash
     */
    public boolean verifyEncoded(String encodedHash, char[] password) {
        try (ByteArray passwordByteArray = toByteArray(password).clearSource()) {
            return verifier.hash(encodedHash).password(passwordByteArray).verifyEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Error during password verification", e);
        }
    }

    /**
     * Calculate an Argon2 raw hash. The password is cleared before this method returns.
     *
     * @param salt A unique random byte array
     * @param password The password to be hashed
     * @return The raw Argon2 hash
     */
    public byte[] rawHash(byte[] salt, char[] password) {
        try (ByteArray passwordByteArray = toByteArray(password).clearSource()) {
            return hasher.salt(salt).password(passwordByteArray).rawHash();
        } catch (Exception e) {
            throw new RuntimeException("Error during password hashing", e);
        }
    }

    /**
     * Verify that the password matches with the raw hash and the salt. The password is cleared
     * before this method returns.
     *
     * @param rawHash The raw hash
     * @param salt The salt used during hash calculation
     * @param password The password to be verified
     * @return <tt>true</tt> if the password matches with the raw hash and the salt
     */
    public boolean verifyRaw(byte[] rawHash, byte[] salt, char[] password) {
        try (ByteArray passwordByteArray = toByteArray(password).clearSource()) {
            return verifier.hash(rawHash).salt(salt).password(passwordByteArray).verifyRaw();
        } catch (Exception e) {
            throw new RuntimeException("Error during password verification", e);
        }
    }
}
```

## Low-level API

During development, you may not be confident you have configured `Hasher` or `Verifier` instances properly, and you need a way cross-check the calculated hashes with the use of another API.

Jargon2 provides a low-level API, very close to the backend SPI. You can use it as follows:

```java
import java.security.SecureRandom;
import static com.kosprov.jargon2.api.Jargon2.*;

public class SimpleLowLevelExample {
    public static void main(String[] args) {
        
        byte[] salt = new byte[16];
        SecureRandom r = new SecureRandom();
        r.nextBytes(salt);

        byte[] password = "this is a password".getBytes();

        String encodedHash = jargon2LowLevelApi()
                .encodedHash(
                        Type.ARGON2d,     // Data-dependent hashing
                        Version.V13,      // version 1.3
                        65536,            // 64MB memory cost
                        3,                // 3 passes through memory
                        4,                // use 4 lanes and 4 threads
                        16,               // 16 random bytes salt
                        salt,
                        password
                );

        System.out.printf("Hash: %s%n", encodedHash);

        boolean matches = jargon2LowLevelApi()
                .verifyEncoded(
                        encodedHash,
                        password
                );

        System.out.printf("Matches: %s%n", matches);
    }
}
```

## Backends

Jargon2 comes with a Service Provider Interface (SPI) for backend implementations. Currently, Jargon2 offers [a backend](https://github.com/kosprov/jargon2-backends "Jargon2 Backends repository") that wraps the [Argon2 reference implementation](https://github.com/P-H-C/phc-winner-argon2 "Argon2 reference implementation repository"). In the future, there may be other more optimized implementations or a implementation written in pure Java.

A Jargon2 backend is simply an implementation of the `com.kosprov.jargon2.spi.Jargon2Backend` interface. There are three ways to hook the backend into the high-level API:

- Programatically

    Implement the backend, have it in your classpath and call one of the `backend` builder methods of `Hasher` and `Verifier` every time you want to use it. You can set the class name, the `Class` instance or the actual backend instance. This method overrides automatic discovery.
    
- Declaratively by adding service provider metadata

    Jargon2 uses standard `java.util.ServiceLoader` discovery to find your implementation. Create this file: `META-INF/services/com.kosprov.jargon2.spi.Jargon2Backend` and add the class name of your backend implementation.
    ```
    fully.qualified.name.to.BackendClass
    ```
    It must be visible by the context classloader of the thread that initializes Jargon2. Having the backend on its own jar and packaging it alongside with `jargon2-api` will work in almost all cases.
    
    > **Caution**: For security reasons, the discovery process will fail if it finds more than one backend implementation. Make sure there's only one such file in your classpath.
    
- Declaratively by setting a system property

    Start the JVM with `-Dcom.kosprov.jargon2.spi.backend=fully.qualified.name.to.BackendClass`.
    
    > **Caution**: For security reasons, you cannot combine service provider discovery and registration by system property. If you have service provider metadata in your classpath (by the method above), adding a system property will produce an error and vice versa.
    
    > **Security consideration**: Keep your security engineers alerted and have them scan or change-detect for improper use of this property. Make sure they protect it as they would protect JAAS login module or security manager system properties and configuration files. Changing the system property to a malicius implementation will leak all your user's passwords.
    
In terms of security, the best approach would be to use service provider discovery, package the legitimate backend jar into your application (EAR, WAR, fat-JAR, etc) and sign it. Adding a malicius jar into the classpath, repackaging the application or trying to override with the system property would result in an error.

If you're uncertain on which backend has been loaded, just call `toString()` on a hasher or verifier. The return value contains the backend implementation in effect. 

## Performance and stability

Jargon2 default backend is a wrapper of the [Argon2 reference implementation](https://github.com/P-H-C/phc-winner-argon2 "Argon2 reference implementation repository") written in C. It packages binaries that have been compiled without any CPU-specific optimizations. It does all low-level operations with standard C code where some operations can be bulked in SIMD instructions. Expect a significant performance boost just by recompiling the C code for your particular CPU type. The gains are bigger if you're hashing with large memory and time costs. The [Jargon 2 backends repository](https://github.com/kosprov/jargon2-backends "Jargon2 Backends repository") has information on how to do that.

You can, also, checkout the [Jargon 2 examples respository](https://github.com/kosprov/jargon2-examples "Jargon 2 examples respository"). It contains simple stress tests and long-running stability tests and you can run the experiments on your server to find out what works best on your hardware. 