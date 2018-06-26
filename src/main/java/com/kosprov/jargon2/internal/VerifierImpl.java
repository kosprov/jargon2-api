package com.kosprov.jargon2.internal;

import com.kosprov.jargon2.api.Jargon2Exception;
import com.kosprov.jargon2.internal.discovery.Jargon2BackendDiscovery;
import com.kosprov.jargon2.spi.Jargon2Backend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.kosprov.jargon2.api.Jargon2.*;

public class VerifierImpl implements Verifier {
    Jargon2Backend backend = Jargon2BackendDiscovery.INSTANCE.getJargon2Backend();
    Map<String, Object> options = Collections.emptyMap();
    Type type = Type.ARGON2i;
    Version version = Version.V13;
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

    public VerifierImpl() {
    }

    private VerifierImpl(VerifierImpl copy) {
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
    public VerifierImpl backend(Jargon2Backend backend) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.backend = backend;
        return copy;
    }

    @Override
    public VerifierImpl backend(String backendClass) {
        try {
            return backend(Class.forName(backendClass).asSubclass(Jargon2Backend.class));
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public VerifierImpl backend(Class<? extends Jargon2Backend> backendClass) {
        try {
            return backend(backendClass.newInstance());
        } catch (Exception e) {
            throw new Jargon2Exception("Could not create Jargon2Backend instance from class " + backendClass);
        }
    }

    @Override
    public VerifierImpl options(Map<String, Object> options) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.options = options != null ? new HashMap<>(options) : Collections.<String, Object>emptyMap();
        return copy;
    }

    @Override
    public VerifierImpl type(Type type) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.type = type;
        return copy;
    }

    @Override
    public VerifierImpl version(Version version) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.version = version;
        return copy;
    }

    @Override
    public VerifierImpl timeCost(int timeCost) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.timeCost = timeCost;
        return copy;
    }

    @Override
    public VerifierImpl memoryCost(int memoryCost) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.memoryCost = memoryCost;
        return copy;
    }

    @Override
    public VerifierImpl parallelism(int parallelism) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.lanes = parallelism;
        copy.threads = parallelism;
        copy.autoThreads = false;
        return copy;
    }

    @Override
    public VerifierImpl parallelism(int lanes, int threads) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.lanes = lanes;
        copy.threads = threads;
        copy.autoThreads = false;
        return copy;
    }

    @Override
    public VerifierImpl threads(int threads) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.threads = threads;
        copy.autoThreads = false;
        return copy;
    }

    @Override
    public VerifierImpl salt(byte[] salt) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.salt = salt;
        return copy;
    }

    @Override
    public VerifierImpl salt(ByteArray salt) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.salt = salt.getBytes();
        return copy;
    }

    @Override
    public VerifierImpl password(byte[] password) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.password = password;
        return copy;
    }

    @Override
    public VerifierImpl password(ByteArray password) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.password = password.getBytes();
        return copy;
    }

    @Override
    public VerifierImpl secret(byte[] secret) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.secret = secret;
        return copy;
    }

    @Override
    public VerifierImpl secret(ByteArray secret) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.secret = secret.getBytes();
        return copy;
    }

    @Override
    public VerifierImpl ad(byte[] ad) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.ad = ad;
        return copy;
    }

    @Override
    public VerifierImpl ad(ByteArray ad) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.ad = ad.getBytes();
        return copy;
    }

    @Override
    public EncodedVerifierImpl hash(String encodedHash) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.encodedHash = encodedHash;
        copy.rawHash = null;
        return new EncodedVerifierImpl(copy);
    }

    @Override
    public RawVerifierImpl hash(byte[] rawHash) {
        VerifierImpl copy = new VerifierImpl(this);
        copy.rawHash = rawHash;
        copy.encodedHash = null;
        return new RawVerifierImpl(copy);
    }

    @Override
    public String toString() {
        // Careful not to leak any sensitive data
        return "Verifier{" +
                "backend=" + backend.getClass().getName() +
                ", options=" + options.size() + " item(s)" +
                ", type=" + type +
                ", version=" + version +
                ", timeCost=" + timeCost +
                ", memoryCost=" + memoryCost +
                ", lanes=" + lanes +
                ", threads=" + threads +
                '}';
    }

    private static class EncodedVerifierImpl implements EncodedVerifier {
        private final VerifierImpl delegate;

        EncodedVerifierImpl(VerifierImpl verifier) {
            this.delegate = verifier;
        }

        @Override
        public EncodedVerifierImpl backend(Jargon2Backend backend) {
            return new EncodedVerifierImpl(delegate.backend(backend));
        }

        @Override
        public EncodedVerifierImpl backend(String backendClass) {
            return new EncodedVerifierImpl(delegate.backend(backendClass));
        }

        @Override
        public EncodedVerifierImpl backend(Class<? extends Jargon2Backend> backendClass) {
            return new EncodedVerifierImpl(delegate.backend(backendClass));
        }

        @Override
        public EncodedVerifierImpl options(Map<String, Object> options) {
            return new EncodedVerifierImpl(delegate.options(options));
        }

        @Override
        public EncodedVerifierImpl type(Type type) {
            return new EncodedVerifierImpl(delegate.type(type));
        }

        @Override
        public EncodedVerifierImpl version(Version version) {
            return new EncodedVerifierImpl(delegate.version(version));
        }

        @Override
        public EncodedVerifierImpl timeCost(int timeCost) {
            return new EncodedVerifierImpl(delegate.timeCost(timeCost));
        }

        @Override
        public EncodedVerifierImpl memoryCost(int memoryCost) {
            return new EncodedVerifierImpl(delegate.memoryCost(memoryCost));
        }

        @Override
        public EncodedVerifierImpl parallelism(int parallelism) {
            return new EncodedVerifierImpl(delegate.parallelism(parallelism));
        }

        @Override
        public EncodedVerifierImpl parallelism(int lanes, int threads) {
            return new EncodedVerifierImpl(delegate.parallelism(lanes, threads));
        }

        @Override
        public EncodedVerifierImpl threads(int threads) {
            return new EncodedVerifierImpl(delegate.threads(threads));
        }

        @Override
        public EncodedVerifierImpl salt(byte[] salt) {
            return new EncodedVerifierImpl(delegate.salt(salt));
        }

        @Override
        public EncodedVerifierImpl salt(ByteArray salt) {
            return new EncodedVerifierImpl(delegate.salt(salt));
        }

        @Override
        public EncodedVerifierImpl password(byte[] password) {
            return new EncodedVerifierImpl(delegate.password(password));
        }

        @Override
        public EncodedVerifierImpl password(ByteArray password) {
            return new EncodedVerifierImpl(delegate.password(password));
        }

        @Override
        public EncodedVerifierImpl secret(byte[] secret) {
            return new EncodedVerifierImpl(delegate.secret(secret));
        }

        @Override
        public EncodedVerifierImpl secret(ByteArray secret) {
            return new EncodedVerifierImpl(delegate.secret(secret));
        }

        @Override
        public EncodedVerifierImpl ad(byte[] ad) {
            return new EncodedVerifierImpl(delegate.ad(ad));
        }

        @Override
        public EncodedVerifierImpl ad(ByteArray ad) {
            return new EncodedVerifierImpl(delegate.ad(ad));
        }

        @Override
        public EncodedVerifierImpl hash(String encodedHash) {
            return delegate.hash(encodedHash);
        }

        @Override
        public RawVerifierImpl hash(byte[] rawHash) {
            return delegate.hash(rawHash);
        }

        @Override
        public boolean verifyEncoded() {
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

    private static class RawVerifierImpl implements RawVerifier {
        private final VerifierImpl delegate;

        RawVerifierImpl(VerifierImpl verifier) {
            this.delegate = verifier;
        }

        @Override
        public RawVerifierImpl backend(Jargon2Backend backend) {
            return new RawVerifierImpl(delegate.backend(backend));
        }

        @Override
        public RawVerifierImpl backend(String backendClass) {
            return new RawVerifierImpl(delegate.backend(backendClass));
        }

        @Override
        public RawVerifierImpl backend(Class<? extends Jargon2Backend> backendClass) {
            return new RawVerifierImpl(delegate.backend(backendClass));
        }

        @Override
        public RawVerifierImpl options(Map<String, Object> options) {
            return new RawVerifierImpl(delegate.options(options));
        }

        @Override
        public RawVerifierImpl type(Type type) {
            return new RawVerifierImpl(delegate.type(type));
        }

        @Override
        public RawVerifierImpl version(Version version) {
            return new RawVerifierImpl(delegate.version(version));
        }

        @Override
        public RawVerifierImpl timeCost(int timeCost) {
            return new RawVerifierImpl(delegate.timeCost(timeCost));
        }

        @Override
        public RawVerifierImpl memoryCost(int memoryCost) {
            return new RawVerifierImpl(delegate.memoryCost(memoryCost));
        }

        @Override
        public RawVerifierImpl parallelism(int parallelism) {
            return new RawVerifierImpl(delegate.parallelism(parallelism));
        }

        @Override
        public RawVerifierImpl parallelism(int lanes, int threads) {
            return new RawVerifierImpl(delegate.parallelism(lanes, threads));
        }

        @Override
        public RawVerifierImpl threads(int threads) {
            return new RawVerifierImpl(delegate.threads(threads));
        }

        @Override
        public RawVerifierImpl salt(byte[] salt) {
            return new RawVerifierImpl(delegate.salt(salt));
        }

        @Override
        public RawVerifierImpl salt(ByteArray salt) {
            return new RawVerifierImpl(delegate.salt(salt));
        }

        @Override
        public RawVerifierImpl password(byte[] password) {
            return new RawVerifierImpl(delegate.password(password));
        }

        @Override
        public RawVerifierImpl password(ByteArray password) {
            return new RawVerifierImpl(delegate.password(password));
        }

        @Override
        public RawVerifierImpl secret(byte[] secret) {
            return new RawVerifierImpl(delegate.secret(secret));
        }

        @Override
        public RawVerifierImpl secret(ByteArray secret) {
            return new RawVerifierImpl(delegate.secret(secret));
        }

        @Override
        public RawVerifierImpl ad(byte[] ad) {
            return new RawVerifierImpl(delegate.ad(ad));
        }

        @Override
        public RawVerifierImpl ad(ByteArray ad) {
            return new RawVerifierImpl(delegate.ad(ad));
        }

        @Override
        public EncodedVerifierImpl hash(String encodedHash) {
            return delegate.hash(encodedHash);
        }

        @Override
        public RawVerifierImpl hash(byte[] rawHash) {
            return delegate.hash(rawHash);
        }

        @Override
        public boolean verifyRaw() {
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
