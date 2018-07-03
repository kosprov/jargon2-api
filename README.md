# Jargon2: Fluent Java API for Argon2 password hashing

[![Build Status](https://travis-ci.org/kosprov/jargon2-api.svg?branch=master)](https://travis-ci.org/kosprov/jargon2-api)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/14707/badge.svg)](https://scan.coverity.com/projects/kosprov-jargon2-api)
[![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/kosprov/jargon2/jargon2-api/maven-metadata.xml.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.kosprov.jargon2%22%20AND%20a%3A%22jargon2-api%22)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.kosprov.jargon2%3Ajargon2-api&metric=alert_status)](https://sonarcloud.io/dashboard/index/com.kosprov.jargon2:jargon2-api)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.kosprov.jargon2%3Ajargon2-api&metric=security_rating)](https://sonarcloud.io/dashboard/index/com.kosprov.jargon2:jargon2-api)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](/LICENSE)

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
    <version>1.1.1</version>
</dependency>
<dependency>
    <groupId>com.kosprov.jargon2</groupId>
    <artifactId>jargon2-native-ri-backend</artifactId>
    <version>1.1.1</version>
    <scope>runtime</scope>
</dependency>
```

The second dependency is the [default Jargon2 backend implementation](https://github.com/kosprov/jargon2-backends "Jargon2 Backends repository") that wraps the [Argon2 reference implementation](https://github.com/P-H-C/phc-winner-argon2 "Argon2 reference implementation repository") written in C. It includes x86 binaries compiled for Windows, Linux and macOS, so it should work on most systems. Backend implementations can automatically be discovered using a `java.util.ServiceLoader` so there is no build-time dependency to the backend classes. More on this on the [backends](#backends) section.

> **Note**: You may need to change the version numbers to the most recent release. Keep in mind that `jargon2-api` and `jargon2-native-ri-backend` follow different release cycles and their version number would not necessarily be the same.

### Simple example

The simplest possible example would be the following:

```java
import static com.kosprov.jargon2.api.Jargon2.*;

public class Jargon2EncodedHashExample {
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

> **Tip**:  To enjoy the Jargon2 API fluency, always start with this static import:
```java
import static com.kosprov.jargon2.api.Jargon2.*;
``` 

`Hasher` and `Verifier` are immutable (copy-on-write), thread-safe objects. Each method call returns a new copy. You usually cascade method calls to build an instance with the static configuration (type, memory cost, time cost etc) and use that prototype instance to pass all values (password, salt, ad etc) needed to calculate a specific hash. The prototype object does not change and can be reused to calculate more hashes. Since it is immutable, it can be safely accessed by multiple threads.

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

Lets see a simple raw hashing example.

```java
import static com.kosprov.jargon2.api.Jargon2.*;

public class Jargon2RawHashExample {
    public static void main(String[] args) {
        byte[] salt = "this is a salt".getBytes();
        byte[] password = "this is a password".getBytes();

        Type type = Type.ARGON2d;
        int memoryCost = 65536;
        int timeCost = 3;
        int parallelism = 4;
        int hashLength = 16;

        // Configure the hasher
        Hasher hasher = jargon2Hasher()
                .type(type)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(parallelism)
                .hashLength(hashLength);

        // Configure the verifier with the same settings as the hasher
        Verifier verifier = jargon2Verifier()
                .type(type)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(parallelism);

        // Set the salt and password to calculate the raw hash
        byte[] rawHash = hasher.salt(salt).password(password).rawHash();

        System.out.printf("Hash: %s%n", Arrays.toString(rawHash));

        // Set the raw hash, salt and password and verify
        boolean matches = verifier.hash(rawHash).salt(salt).password(password).verifyRaw();

        System.out.printf("Matches: %s%n", matches);
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

To showcase how Jargon2 can be used effectively, we will assume a JavaEE environment (a CDI container) and we will build an application-scoped (singleton) component which will expose a hash/verify API. It will internally manage and use an HMAC key, support different numbers of lanes and threads and expose an API to test whether a hash needs to be upgraded.

```java
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static com.kosprov.jargon2.api.Jargon2.*;

/**
 * CDI bean that exposes encoded and raw password hashing and verification
 */
@ApplicationScoped  // The component is thread-safe, so we can have a single object
public class PasswordHasher {

    private Hasher hasher;     // The hasher instance
    private Verifier verifier; // The verifier instance

    @Inject
    private Configuration config; // Assume another CDI bean that can load configuration parameters

    @Inject
    private KeyStore keyStore;    // Assume another CDI bean that can load the HMAC key

    private ByteArray secret;     // A ByteArray that will wrap the HMAC key loaded from keyStore

    @PostConstruct
    private void init() {

        // Load the HMAC key and wrap it to a ByteArray.
        // Also, mark to clear the source byte[] when we clear this instance (see destroy() below)
        secret = toByteArray(keyStore.loadKey()).clearSource();

        // Load configuration parameters
        Type type = Type.valueOf(config.getString("password.hasher.argon2.type"));
        int memoryCost = config.getInteger("password.hasher.argon2.memoryCost");
        int timeCost = config.getInteger("password.hasher.argon2.timeCost");
        int lanes = config.getInteger("password.hasher.argon2.parallelism");
        int saltLength = config.getInteger("password.hasher.saltLength");
        int hashLength = config.getInteger("password.hasher.hashLength");

        // Use N-1 cores, regardless of the number of lanes
        int threads = Runtime.getRuntime().availableProcessors() - 1;

        hasher = jargon2Hasher()
                // Set the HMAC key
                .secret(secret)
                // Configure hasher properties
                .type(type)
                .memoryCost(memoryCost)
                .timeCost(timeCost)
                .parallelism(lanes, threads) // set lanes and threads independently
                .saltLength(saltLength)
                .hashLength(hashLength)
                ;

        verifier = jargon2Verifier()
                // Set the HMAC key
                .secret(secret)
                // Configure the threads used by the verifier, regardless of the 
                // p property found encoded in the hash
                .threads(threads)
                ;
    }

    @PreDestroy
    private void destroy() {
        // Zero out memory locations of HMAC key during undeployment
        secret.clear();
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
     * Tests whether properties found in the encoded hash (type, version, memory cost, time cost, parallelism,
     * salt length and hash length) are up-to-date with respect to the current configuration.
     *
     * @param encodedHash The encoded hash to test
     * @return <tt>true</tt> if properties found in encoded hash match with the current configuration
     */
    public boolean isUpdated(String encodedHash) {
        return hasher.propertiesMatch(encodedHash);
    }
}
```

The rationale in setting memory lanes and processing threads independently is when you run your application on heterogeneous hardware and the number of CPU cores available on any machine varies (e.g in a cloud environment). In such a case, you could select a sensible value for memory lanes based on your best hardware, but configure the number of threads not to exceed the number of cores of the CPU on the _current_ hardware. Depending on the configuration, you could see a small but non-negligible speedup. Do your own benchmarks to decide if it's worth it.

Method `isUpdated(String)` tests whether properties found in the encoded hash match with the current configuration of the hasher by delegating to `hasher.propertiesMatch(String)`. This API can help when you want to change Argon2 properties (e.g. increase memory cost to make hashes more secure) and automatically migrate current hashes, without require users to reset their passwords. For example, a login component could use `isUpdated(String)` like:

```java
// login started
// capture password from the user and load encodedHash from the database
boolean passwordValid = passwordHasher.verifyEncoded(encodedHash, password);
if (passwordValid && !passwordHasher.isUpdated(encodedHash)) { 
    String newHash = passwordHasher.encodedHash(password);
    // store newHash
}
// continue login
```

That way, hashes will be migrated to the new configuration gradually, as users login to the application. The cost of checking the encoded hash on every successful login is extremely low, so no performance penalty is induced.

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