package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2Exception;
import com.kosprov.jargon2.internal.discovery.Jargon2BackendDiscovery;
import com.kosprov.jargon2.spi.Jargon2Backend;

import java.security.Provider;
import java.util.Map;

public class Hasher implements Jargon2.Hasher {

    private Jargon2Backend backend = Jargon2BackendDiscovery.INSTANCE.getJargon2Backend();
    private Map<String, Object> options;
    private Jargon2.Type type = Jargon2.Type.ARGON2i;
    private Jargon2.Version version = Jargon2.Version.V13;
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

    private Jargon2.SaltGenerator saltGenerator = SecureRandomSaltGenerator.DEFAULT;

    public Hasher() {
    }

    private Hasher(Hasher copy) {
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
    public Hasher backend(Jargon2Backend backend) {
        Hasher copy = new Hasher(this);
        copy.backend = backend;
        return copy;
    }

    @Override
    public Hasher backend(String backendClass) {
        try {
            return backend(Class.forName(backendClass).asSubclass(Jargon2Backend.class));
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public Hasher backend(Class<? extends Jargon2Backend> backendClass) {
        try {
            return backend(backendClass.newInstance());
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public Jargon2.Hasher options(Map<String, Object> options) {
        Hasher copy = new Hasher(this);
        copy.options = options;
        return copy;
    }

    @Override
    public Hasher type(Jargon2.Type type) {
        Hasher copy = new Hasher(this);
        copy.type = type;
        return copy;
    }

    @Override
    public Hasher version(Jargon2.Version version) {
        Hasher copy = new Hasher(this);
        copy.version = version;
        return copy;
    }

    @Override
    public Hasher timeCost(int timeCost) {
        Hasher copy = new Hasher(this);
        copy.timeCost = timeCost;
        return copy;
    }

    @Override
    public Hasher memoryCost(int memoryCost) {
        Hasher copy = new Hasher(this);
        copy.memoryCost = memoryCost;
        return copy;
    }

    @Override
    public Jargon2.Hasher parallelism(int parallelism) {
        Hasher copy = new Hasher(this);
        copy.lanes = parallelism;
        copy.threads = parallelism;
        return copy;
    }

    @Override
    public Hasher parallelism(int lanes, int threads) {
        Hasher copy = new Hasher(this);
        copy.lanes = lanes;
        copy.threads = threads;
        return copy;
    }

    @Override
    public Hasher hashLength(int hashLength) {
        Hasher copy = new Hasher(this);
        copy.hashLength = hashLength;
        return copy;
    }

    @Override
    public Hasher saltLength(int saltLength) {
        Hasher copy = new Hasher(this);
        copy.saltLength = saltLength;
        return copy;
    }

    @Override
    public Hasher salt(byte[] salt) {
        Hasher copy = new Hasher(this);
        copy.salt = salt;
        return copy;
    }

    @Override
    public Hasher salt(Jargon2.ByteArray salt) {
        Hasher copy = new Hasher(this);
        copy.salt = salt.getBytes();
        return copy;
    }

    @Override
    public Hasher saltGenerator(Jargon2.SaltGenerator saltGenerator) {
        Hasher copy = new Hasher(this);
        copy.saltGenerator = saltGenerator;
        return copy;
    }

    @Override
    public Jargon2.Hasher saltGenerator(String secureRandomAlgorithm) {
        Hasher copy = new Hasher(this);
        copy.saltGenerator = new SecureRandomSaltGenerator(secureRandomAlgorithm);
        return copy;
    }

    @Override
    public Jargon2.Hasher saltGenerator(String secureRandomAlgorithm, String secureRandomProvider) {
        Hasher copy = new Hasher(this);
        copy.saltGenerator = new SecureRandomSaltGenerator(secureRandomAlgorithm, secureRandomProvider);
        return copy;
    }

    @Override
    public Jargon2.Hasher saltGenerator(String secureRandomAlgorithm, Provider secureRandomProvider) {
        Hasher copy = new Hasher(this);
        copy.saltGenerator = new SecureRandomSaltGenerator(secureRandomAlgorithm, secureRandomProvider);
        return copy;
    }

    @Override
    public Hasher password(byte[] password) {
        Hasher copy = new Hasher(this);
        copy.password = password;
        return copy;
    }

    @Override
    public Hasher password(Jargon2.ByteArray password) {
        Hasher copy = new Hasher(this);
        copy.password = password.getBytes();
        return copy;
    }

    @Override
    public Hasher secret(byte[] secret) {
        Hasher copy = new Hasher(this);
        copy.secret = secret;
        return copy;
    }

    @Override
    public Hasher secret(Jargon2.ByteArray secret) {
        Hasher copy = new Hasher(this);
        copy.secret = secret.getBytes();
        return copy;
    }

    @Override
    public Hasher ad(byte[] ad) {
        Hasher copy = new Hasher(this);
        copy.ad = ad;
        return copy;
    }

    @Override
    public Hasher ad(Jargon2.ByteArray ad) {
        Hasher copy = new Hasher(this);
        copy.ad = ad.getBytes();
        return copy;
    }

    @Override
    public byte[] rawHash() throws Jargon2Exception {
        if (salt == null) {
            throw new Jargon2Exception("Missing salt for raw hashing");
        }
        return new Jargon2BackendAdapter(backend).rawHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
    }

    @Override
    public String encodedHash() throws Jargon2Exception {
        if (salt == null) {
            salt = new byte[saltLength];
            saltGenerator.generate(salt);
        }
        return new Jargon2BackendAdapter(backend).encodedHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options);
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
