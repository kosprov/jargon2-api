package com.kosprov.jargon2.internal.discovery;

import com.kosprov.jargon2.spi.Jargon2Backend;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class Jargon2BackendDiscoveryTest {

    @Test
    public void defaultTest() {
        Jargon2Backend backend = Jargon2BackendDiscovery.INSTANCE.getJargon2Backend();
        assertNotNull(backend);
    }

}