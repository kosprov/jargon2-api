package com.kosprov.jargon2.api;

import java.util.Arrays;

class DummySaltGenerator implements Jargon2.SaltGenerator {

    static byte DUMMY_BYTE = (byte) 0b00000011;

    private static final DummySaltGenerator INSTANCE = new DummySaltGenerator();

    static DummySaltGenerator getInstance() {
        return INSTANCE;
    }

    @Override
    public void generate(byte[] salt) {
        Arrays.fill(salt, DUMMY_BYTE    );
    }
}
