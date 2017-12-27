package com.kosprov.jargon2.internal.discovery;

import com.kosprov.jargon2.api.Jargon2Exception;

class Jargon2BackendDiscoveryException extends Jargon2Exception {

    Jargon2BackendDiscoveryException(String message) {
        super(message);
    }

    Jargon2BackendDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
