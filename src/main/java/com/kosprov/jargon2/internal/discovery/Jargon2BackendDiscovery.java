package com.kosprov.jargon2.internal.discovery;

import com.kosprov.jargon2.spi.Jargon2Backend;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Singleton object that searches for {@link Jargon2Backend} implementations.
 *
 * <p>
 *     It checks the value of the <tt>-Dcom.kosprov.jargon2.spi.backend</tt> system property for the
 *     class name of the Jargon2Backend implementation. This implementation must has a no-args constructor.
 * </p>
 *
 * <p>
 *     Then, it checks for service providers registered with Java's {@link ServiceLoader} mechanism.
 *     See {@link Jargon2Backend} documentation for more details.
 * </p>
 *
 * <p>
 *     The previous 2 steps combined should output only 1 backend.
 * </p>
 *
 * @see Jargon2Backend
 */
public enum Jargon2BackendDiscovery {

    INSTANCE;

    private static final String JARGON2_BACKEND_SYSTEM_PROP_NAME = "com.kosprov.jargon2.spi.backend";

    private volatile Jargon2Backend backend;

    /**
     * Get the single instance of the {@link Jargon2Backend}
     *
     * @return The discovered backend instance
     */
    public Jargon2Backend getJargon2Backend() {
        if (backend == null) {
            synchronized (this) {
                if (backend == null) {
                    Set<Jargon2Backend> backendsFound = new HashSet<>();

                    String backendClassName = System.getProperty(JARGON2_BACKEND_SYSTEM_PROP_NAME);
                    if (backendClassName != null && !"".equals(backendClassName.trim())) {
                        try {
                            backendsFound.add((Jargon2Backend) Class.forName(backendClassName).newInstance());
                        } catch (Exception e) {
                            throw new Jargon2BackendDiscoveryException("Could not create Jargon2Backend instance from class " + backendClassName, e);
                        }
                    }

                    ServiceLoader<Jargon2Backend> loader = ServiceLoader.load(Jargon2Backend.class);
                    for (Jargon2Backend backend : loader) {
                        backendsFound.add(backend);
                    }

                    if (backendsFound.size() == 1) {
                        backend = backendsFound.iterator().next(); // All good
                    } else if (backendsFound.size() > 1) {
                        StringBuilder sb = new StringBuilder();
                        sb.append('[');
                        for (Jargon2Backend backend : backendsFound) {
                            sb.append(backend.getClass().getName()).append(", ");
                        }
                        sb.setLength(sb.length() - 2);
                        sb.append(']');
                        throw new Jargon2BackendDiscoveryException("Found more than one Jargon2Backends: " + sb.toString());
                    } else {
                        throw new Jargon2BackendDiscoveryException("Could not find appropriate jargon2Backend. Use either a service provider or define its class with -D" + JARGON2_BACKEND_SYSTEM_PROP_NAME);
                    }
                }
            }
        }
        return backend;
    }
}
