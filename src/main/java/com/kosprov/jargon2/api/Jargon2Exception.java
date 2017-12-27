package com.kosprov.jargon2.api;

/**
 * Generic exception from the Jargon2 API
 */
public class Jargon2Exception extends RuntimeException {

    public Jargon2Exception() {
    }

    public Jargon2Exception(String message) {
        super(message);
    }

    public Jargon2Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Jargon2Exception(Throwable cause) {
        super(cause);
    }
}
