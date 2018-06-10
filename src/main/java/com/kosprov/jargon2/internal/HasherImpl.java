package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2Exception;
import com.kosprov.jargon2.internal.discovery.Jargon2BackendDiscovery;
import com.kosprov.jargon2.spi.Jargon2Backend;

import java.security.Provider;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Pattern;

import static com.kosprov.jargon2.api.Jargon2.*;

public class HasherImpl implements Hasher {

    private Jargon2Backend backend = Jargon2BackendDiscovery.INSTANCE.getJargon2Backend();
    private Map<String, Object> options;
    private Type type = Type.ARGON2i;
    private Version version = Version.V13;
    private int timeCost = 3;
    private int memoryCost = 4096;
    private int lanes = 1;
    private int threads = 1;
    private int hashLength = 32;
    private int saltLength = 16;

    private byte[] salt;
    private byte[] password;
    private byte[] secret;
    private byte[] ad;

    private SaltGenerator saltGenerator = SecureRandomSaltGenerator.DEFAULT;

    public HasherImpl() {
    }

    private HasherImpl(HasherImpl copy) {
        this.backend = copy.backend;
        this.options = copy.options;
        this.type = copy.type;
        this.version = copy.version;
        this.timeCost = copy.timeCost;
        this.memoryCost = copy.memoryCost;
        this.lanes = copy.lanes;
        this.threads = copy.threads;
        this.hashLength = copy.hashLength;
        this.saltLength = copy.saltLength;
        this.salt = copy.salt;
        this.password = copy.password;
        this.secret = copy.secret;
        this.ad = copy.ad;
        this.saltGenerator = copy.saltGenerator;
    }

    @Override
    public HasherImpl backend(Jargon2Backend backend) {
        HasherImpl copy = new HasherImpl(this);
        copy.backend = backend;
        return copy;
    }

    @Override
    public HasherImpl backend(String backendClass) {
        try {
            return backend(Class.forName(backendClass).asSubclass(Jargon2Backend.class));
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public HasherImpl backend(Class<? extends Jargon2Backend> backendClass) {
        try {
            return backend(backendClass.newInstance());
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public HasherImpl options(Map<String, Object> options) {
        HasherImpl copy = new HasherImpl(this);
        copy.options = options;
        return copy;
    }

    @Override
    public HasherImpl type(Type type) {
        HasherImpl copy = new HasherImpl(this);
        copy.type = type;
        return copy;
    }

    @Override
    public HasherImpl version(Version version) {
        HasherImpl copy = new HasherImpl(this);
        copy.version = version;
        return copy;
    }

    @Override
    public HasherImpl timeCost(int timeCost) {
        HasherImpl copy = new HasherImpl(this);
        copy.timeCost = timeCost;
        return copy;
    }

    @Override
    public HasherImpl memoryCost(int memoryCost) {
        HasherImpl copy = new HasherImpl(this);
        copy.memoryCost = memoryCost;
        return copy;
    }

    @Override
    public HasherImpl parallelism(int parallelism) {
        HasherImpl copy = new HasherImpl(this);
        copy.lanes = parallelism;
        copy.threads = parallelism;
        return copy;
    }

    @Override
    public HasherImpl parallelism(int lanes, int threads) {
        HasherImpl copy = new HasherImpl(this);
        copy.lanes = lanes;
        copy.threads = threads;
        return copy;
    }

    @Override
    public HasherImpl hashLength(int hashLength) {
        HasherImpl copy = new HasherImpl(this);
        copy.hashLength = hashLength;
        return copy;
    }

    @Override
    public HasherImpl saltLength(int saltLength) {
        HasherImpl copy = new HasherImpl(this);
        copy.saltLength = saltLength;
        return copy;
    }

    @Override
    public HasherImpl salt(byte[] salt) {
        HasherImpl copy = new HasherImpl(this);
        copy.salt = salt;
        return copy;
    }

    @Override
    public HasherImpl salt(ByteArray salt) {
        HasherImpl copy = new HasherImpl(this);
        copy.salt = salt.getBytes();
        return copy;
    }

    @Override
    public HasherImpl saltGenerator(SaltGenerator saltGenerator) {
        HasherImpl copy = new HasherImpl(this);
        copy.saltGenerator = saltGenerator;
        return copy;
    }

    @Override
    public HasherImpl saltGenerator(String secureRandomAlgorithm) {
        HasherImpl copy = new HasherImpl(this);
        copy.saltGenerator = new SecureRandomSaltGenerator(secureRandomAlgorithm);
        return copy;
    }

    @Override
    public HasherImpl saltGenerator(String secureRandomAlgorithm, String secureRandomProvider) {
        HasherImpl copy = new HasherImpl(this);
        copy.saltGenerator = new SecureRandomSaltGenerator(secureRandomAlgorithm, secureRandomProvider);
        return copy;
    }

    @Override
    public HasherImpl saltGenerator(String secureRandomAlgorithm, Provider secureRandomProvider) {
        HasherImpl copy = new HasherImpl(this);
        copy.saltGenerator = new SecureRandomSaltGenerator(secureRandomAlgorithm, secureRandomProvider);
        return copy;
    }

    @Override
    public HasherImpl password(byte[] password) {
        HasherImpl copy = new HasherImpl(this);
        copy.password = password;
        return copy;
    }

    @Override
    public HasherImpl password(ByteArray password) {
        HasherImpl copy = new HasherImpl(this);
        copy.password = password.getBytes();
        return copy;
    }

    @Override
    public HasherImpl secret(byte[] secret) {
        HasherImpl copy = new HasherImpl(this);
        copy.secret = secret;
        return copy;
    }

    @Override
    public HasherImpl secret(ByteArray secret) {
        HasherImpl copy = new HasherImpl(this);
        copy.secret = secret.getBytes();
        return copy;
    }

    @Override
    public HasherImpl ad(byte[] ad) {
        HasherImpl copy = new HasherImpl(this);
        copy.ad = ad;
        return copy;
    }

    @Override
    public HasherImpl ad(ByteArray ad) {
        HasherImpl copy = new HasherImpl(this);
        copy.ad = ad.getBytes();
        return copy;
    }

    @Override
    public byte[] rawHash() {
        if (salt == null) {
            throw new Jargon2Exception("Missing salt for raw hashing");
        }
        return new Jargon2BackendAdapter(backend).rawHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
    }

    @Override
    public String encodedHash() {
        if (salt == null) {
            salt = new byte[saltLength];
            saltGenerator.generate(salt);
        }
        return new Jargon2BackendAdapter(backend).encodedHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
    }

    @Override
    public boolean propertiesMatch(String encodedHash) {
        Pattern pattern = encodedHashPattern();
        return pattern.matcher(encodedHash).matches();
    }

    private Pattern encodedHashPattern;

    private Pattern encodedHashPattern() {
        if (encodedHashPattern == null) {
            synchronized (this) {
                if (encodedHashPattern == null) {
                    switch (version) {
                        case V10:
                            encodedHashPattern =
                                    Pattern.compile(
                                            MessageFormat.format(
                                                    "^\\${0}\\$m={1},t={2},p={3}\\$[A-Za-z0-9+/]'{'{4}'}'\\$[A-Za-z0-9+/]'{'{5}'}'$",
                                                    type.getValue(),
                                                    Integer.toString(memoryCost),
                                                    Integer.toString(timeCost),
                                                    Integer.toString(lanes),
                                                    Integer.toString(base64Length(saltLength)),
                                                    Integer.toString(base64Length(hashLength))
                                            )
                                    );
                            break;
                        case V13:
                        default:
                            encodedHashPattern =
                                    Pattern.compile(
                                            MessageFormat.format(
                                                    "^\\${0}\\$v={1}\\$m={2},t={3},p={4}\\$[A-Za-z0-9+/]'{'{5}'}'\\$[A-Za-z0-9+/]'{'{6}'}'$",
                                                    type.getValue(),
                                                    Integer.toString(version.getValue()),
                                                    Integer.toString(memoryCost),
                                                    Integer.toString(timeCost),
                                                    Integer.toString(lanes),
                                                    Integer.toString(base64Length(saltLength)),
                                                    Integer.toString(base64Length(hashLength))
                                            )
                                    );
                    }
                }
            }
        }
        return encodedHashPattern;
    }

    private static int base64Length(int bytes) {
        int base64Length = bytes / 3 * 4;
        int mod3 = bytes % 3;
        if (mod3 != 0) {
            base64Length += (mod3 + 1);
        }
        return base64Length;
    }

    @Override
    public String toString() {
        // Careful not to leak any sensitive data
        return "Hasher{" +
                "backend=" + backend.getClass().getName() +
                ", options=" + (options != null ? options.size() : "none") +
                ", type=" + type +
                ", version=" + version +
                ", timeCost=" + timeCost +
                ", memoryCost=" + memoryCost +
                ", lanes=" + lanes +
                ", threads=" + threads +
                ", hashLength=" + hashLength +
                ", saltLength=" + (salt != null ? salt.length : saltLength) +
                '}';
    }
}
