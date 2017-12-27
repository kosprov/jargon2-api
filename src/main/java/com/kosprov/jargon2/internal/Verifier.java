package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2Exception;
import com.kosprov.jargon2.internal.discovery.Jargon2BackendDiscovery;
import com.kosprov.jargon2.spi.Jargon2Backend;

import java.util.Map;

public class Verifier implements Jargon2.Verifier {
    Jargon2Backend backend = Jargon2BackendDiscovery.INSTANCE.getJargon2Backend();
    Map<String, Object> options;
    Jargon2.Type type = Jargon2.Type.ARGON2i;
    Jargon2.Version version = Jargon2.Version.V13;
    int timeCost = 3;
    int memoryCost = 4096;
    int lanes = 1;
    int threads = 1;
    boolean autoThreads = true;
    byte[] salt;
    byte[] password;
    byte[] secret;
    byte[] ad;
    String encodedHash;
    byte[] rawHash;

    public Verifier() {
    }

    private Verifier(Verifier copy) {
        this.backend = copy.backend;
        this.options = copy.options;
        this.type = copy.type;
        this.version = copy.version;
        this.timeCost = copy.timeCost;
        this.memoryCost = copy.memoryCost;
        this.lanes = copy.lanes;
        this.threads = copy.threads;
        this.autoThreads = copy.autoThreads;
        this.salt = copy.salt;
        this.password = copy.password;
        this.secret = copy.secret;
        this.ad = copy.ad;

        this.encodedHash = copy.encodedHash;
        this.rawHash = copy.rawHash;
    }

    @Override
    public Verifier backend(Jargon2Backend backend) {
        Verifier copy = new Verifier(this);
        copy.backend = backend;
        return copy;
    }

    @Override
    public Verifier backend(String backendClass) {
        try {
            return backend(Class.forName(backendClass).asSubclass(Jargon2Backend.class));
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public Verifier backend(Class<? extends Jargon2Backend> backendClass) {
        try {
            return backend(backendClass.newInstance());
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public Verifier options(Map<String, Object> options) {
        Verifier copy = new Verifier(this);
        copy.options = options;
        return copy;
    }

    @Override
    public Verifier type(Jargon2.Type type) {
        Verifier copy = new Verifier(this);
        copy.type = type;
        return copy;
    }

    @Override
    public Verifier version(Jargon2.Version version) {
        Verifier copy = new Verifier(this);
        copy.version = version;
        return copy;
    }

    @Override
    public Verifier timeCost(int timeCost) {
        Verifier copy = new Verifier(this);
        copy.timeCost = timeCost;
        return copy;
    }

    @Override
    public Verifier memoryCost(int memoryCost) {
        Verifier copy = new Verifier(this);
        copy.memoryCost = memoryCost;
        return copy;
    }

    @Override
    public Verifier parallelism(int parallelism) {
        Verifier copy = new Verifier(this);
        copy.lanes = parallelism;
        copy.threads = parallelism;
        copy.autoThreads = false;
        return copy;
    }

    @Override
    public Verifier parallelism(int lanes, int threads) {
        Verifier copy = new Verifier(this);
        copy.lanes = lanes;
        copy.threads = threads;
        copy.autoThreads = false;
        return copy;
    }

    @Override
    public Verifier threads(int threads) {
        Verifier copy = new Verifier(this);
        copy.threads = threads;
        copy.autoThreads = false;
        return copy;
    }

    @Override
    public Verifier salt(byte[] salt) {
        Verifier copy = new Verifier(this);
        copy.salt = salt;
        return copy;
    }

    @Override
    public Verifier salt(Jargon2.ByteArray salt) {
        Verifier copy = new Verifier(this);
        copy.salt = salt.getBytes();
        return copy;
    }

    @Override
    public Verifier password(byte[] password) {
        Verifier copy = new Verifier(this);
        copy.password = password;
        return copy;
    }

    @Override
    public Verifier password(Jargon2.ByteArray password) {
        Verifier copy = new Verifier(this);
        copy.password = password.getBytes();
        return copy;
    }

    @Override
    public Verifier secret(byte[] secret) {
        Verifier copy = new Verifier(this);
        copy.secret = secret;
        return copy;
    }

    @Override
    public Verifier secret(Jargon2.ByteArray secret) {
        Verifier copy = new Verifier(this);
        copy.secret = secret.getBytes();
        return copy;
    }

    @Override
    public Verifier ad(byte[] ad) {
        Verifier copy = new Verifier(this);
        copy.ad = ad;
        return copy;
    }

    @Override
    public Verifier ad(Jargon2.ByteArray ad) {
        Verifier copy = new Verifier(this);
        copy.ad = ad.getBytes();
        return copy;
    }

    @Override
    public EncodedVerifier hash(String encodedHash) {
        Verifier copy = new Verifier(this);
        copy.encodedHash = encodedHash;
        copy.rawHash = null;
        return new EncodedVerifier(copy);
    }

    @Override
    public RawVerifier hash(byte[] rawHash) {
        Verifier copy = new Verifier(this);
        copy.rawHash = rawHash;
        copy.encodedHash = null;
        return new RawVerifier(copy);
    }

    @Override
    public String toString() {
        // Careful not to leak any sensitive data
        return "Verifier{" +
                "backend=" + backend.getClass().getName() +
                ", options=" + (options != null ? options.size() : "none") +
                ", type=" + type +
                ", version=" + version +
                ", timeCost=" + timeCost +
                ", memoryCost=" + memoryCost +
                ", lanes=" + lanes +
                ", threads=" + threads +
                '}';
    }

    private static class EncodedVerifier implements Jargon2.EncodedVerifier {
        private final Verifier delegate;

        EncodedVerifier(Verifier verifier) {
            this.delegate = verifier;
        }

        @Override
        public EncodedVerifier backend(Jargon2Backend backend) {
            return new EncodedVerifier(delegate.backend(backend));
        }

        @Override
        public EncodedVerifier backend(String backendClass) {
            return new EncodedVerifier(delegate.backend(backendClass));
        }

        @Override
        public EncodedVerifier backend(Class<? extends Jargon2Backend> backendClass) {
            return new EncodedVerifier(delegate.backend(backendClass));
        }

        @Override
        public EncodedVerifier options(Map<String, Object> options) {
            return new EncodedVerifier(delegate.options(options));
        }

        @Override
        public EncodedVerifier type(Jargon2.Type type) {
            return new EncodedVerifier(delegate.type(type));
        }

        @Override
        public EncodedVerifier version(Jargon2.Version version) {
            return new EncodedVerifier(delegate.version(version));
        }

        @Override
        public EncodedVerifier timeCost(int timeCost) {
            return new EncodedVerifier(delegate.timeCost(timeCost));
        }

        @Override
        public EncodedVerifier memoryCost(int memoryCost) {
            return new EncodedVerifier(delegate.memoryCost(memoryCost));
        }

        @Override
        public EncodedVerifier parallelism(int parallelism) {
            return new EncodedVerifier(delegate.parallelism(parallelism));
        }

        @Override
        public EncodedVerifier parallelism(int lanes, int threads) {
            return new EncodedVerifier(delegate.parallelism(lanes, threads));
        }

        @Override
        public EncodedVerifier threads(int threads) {
            return new EncodedVerifier(delegate.threads(threads));
        }

        @Override
        public EncodedVerifier salt(byte[] salt) {
            return new EncodedVerifier(delegate.salt(salt));
        }

        @Override
        public EncodedVerifier salt(Jargon2.ByteArray salt) {
            return new EncodedVerifier(delegate.salt(salt));
        }

        @Override
        public EncodedVerifier password(byte[] password) {
            return new EncodedVerifier(delegate.password(password));
        }

        @Override
        public EncodedVerifier password(Jargon2.ByteArray password) {
            return new EncodedVerifier(delegate.password(password));
        }

        @Override
        public EncodedVerifier secret(byte[] secret) {
            return new EncodedVerifier(delegate.secret(secret));
        }

        @Override
        public EncodedVerifier secret(Jargon2.ByteArray secret) {
            return new EncodedVerifier(delegate.secret(secret));
        }

        @Override
        public EncodedVerifier ad(byte[] ad) {
            return new EncodedVerifier(delegate.ad(ad));
        }

        @Override
        public EncodedVerifier ad(Jargon2.ByteArray ad) {
            return new EncodedVerifier(delegate.ad(ad));
        }

        @Override
        public EncodedVerifier hash(String encodedHash) {
            return delegate.hash(encodedHash);
        }

        @Override
        public RawVerifier hash(byte[] rawHash) {
            return delegate.hash(rawHash);
        }

        @Override
        public boolean verifyEncoded() throws Jargon2Exception {
            if (delegate.autoThreads) {
                return new Jargon2BackendAdapter(delegate.backend).verifyEncoded(
                        delegate.encodedHash,
                        delegate.secret,
                        delegate.ad,
                        delegate.password,
                        delegate.options
                );
            } else {
                return new Jargon2BackendAdapter(delegate.backend).verifyEncoded(
                        delegate.encodedHash,
                        delegate.threads,
                        delegate.secret,
                        delegate.ad,
                        delegate.password,
                        delegate.options
                );
            }
        }
    }

    private static class RawVerifier implements Jargon2.RawVerifier {
        private final Verifier delegate;

        RawVerifier(Verifier verifier) {
            this.delegate = verifier;
        }

        @Override
        public RawVerifier backend(Jargon2Backend backend) {
            return new RawVerifier(delegate.backend(backend));
        }

        @Override
        public RawVerifier backend(String backendClass) {
            return new RawVerifier(delegate.backend(backendClass));
        }

        @Override
        public RawVerifier backend(Class<? extends Jargon2Backend> backendClass) {
            return new RawVerifier(delegate.backend(backendClass));
        }

        @Override
        public RawVerifier options(Map<String, Object> options) {
            return new RawVerifier(delegate.options(options));
        }

        @Override
        public RawVerifier type(Jargon2.Type type) {
            return new RawVerifier(delegate.type(type));
        }

        @Override
        public RawVerifier version(Jargon2.Version version) {
            return new RawVerifier(delegate.version(version));
        }

        @Override
        public RawVerifier timeCost(int timeCost) {
            return new RawVerifier(delegate.timeCost(timeCost));
        }

        @Override
        public RawVerifier memoryCost(int memoryCost) {
            return new RawVerifier(delegate.memoryCost(memoryCost));
        }

        @Override
        public RawVerifier parallelism(int parallelism) {
            return new RawVerifier(delegate.parallelism(parallelism));
        }

        @Override
        public RawVerifier parallelism(int lanes, int threads) {
            return new RawVerifier(delegate.parallelism(lanes, threads));
        }

        @Override
        public RawVerifier threads(int threads) {
            return new RawVerifier(delegate.threads(threads));
        }

        @Override
        public RawVerifier salt(byte[] salt) {
            return new RawVerifier(delegate.salt(salt));
        }

        @Override
        public RawVerifier salt(Jargon2.ByteArray salt) {
            return new RawVerifier(delegate.salt(salt));
        }

        @Override
        public RawVerifier password(byte[] password) {
            return new RawVerifier(delegate.password(password));
        }

        @Override
        public RawVerifier password(Jargon2.ByteArray password) {
            return new RawVerifier(delegate.password(password));
        }

        @Override
        public RawVerifier secret(byte[] secret) {
            return new RawVerifier(delegate.secret(secret));
        }

        @Override
        public RawVerifier secret(Jargon2.ByteArray secret) {
            return new RawVerifier(delegate.secret(secret));
        }

        @Override
        public RawVerifier ad(byte[] ad) {
            return new RawVerifier(delegate.ad(ad));
        }

        @Override
        public RawVerifier ad(Jargon2.ByteArray ad) {
            return new RawVerifier(delegate.ad(ad));
        }

        @Override
        public EncodedVerifier hash(String encodedHash) {
            return delegate.hash(encodedHash);
        }

        @Override
        public RawVerifier hash(byte[] rawHash) {
            return delegate.hash(rawHash);
        }

        @Override
        public boolean verifyRaw() throws Jargon2Exception {
            int threads = delegate.autoThreads ? delegate.lanes : delegate.threads;
            return new Jargon2BackendAdapter(delegate.backend).verifyRaw(
                    delegate.type,
                    delegate.version,
                    delegate.memoryCost,
                    delegate.timeCost,
                    delegate.lanes, threads,
                    delegate.rawHash,
                    delegate.secret,
                    delegate.ad,
                    delegate.salt,
                    delegate.password,
                    delegate.options
            );
        }
    }
}
