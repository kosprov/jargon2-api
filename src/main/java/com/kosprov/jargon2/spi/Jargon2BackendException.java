package com.kosprov.jargon2.spi;

import com.kosprov.jargon2.api.Jargon2Exception;

/**
 * Generic exception from a Jargon2 backend.
 */
public class Jargon2BackendException extends Jargon2Exception{

    public Jargon2BackendException() {
    }

    public Jargon2BackendException(String message) {
        super(message);
    }

    public Jargon2BackendException(String message, Throwable cause) {
        super(message, cause);
    }

    public Jargon2BackendException(Throwable cause) {
        super(cause);
    }
}
