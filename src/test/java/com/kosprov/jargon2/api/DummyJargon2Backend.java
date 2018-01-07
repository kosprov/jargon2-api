package com.kosprov.jargon2.api;

import com.kosprov.jargon2.spi.Jargon2Backend;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.Map;

public class DummyJargon2Backend implements Jargon2Backend {

    @Override
    public byte[] rawHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        return doDummyHash(hashLength, password, salt, secret, ad);
    }

    @Override
    public String encodedHash(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, int hashLength, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        StringBuilder sb = new StringBuilder();
        sb.append('$').append(type.getValue());
        if (version.getValue() > Jargon2.Version.V10.getValue()) {
            sb.append('$').append("v=").append(version.getValue());
        }
        sb.append('$').append("m=").append(memoryCost).append(",t=").append(timeCost).append(",p=").append(lanes);
        sb.append('$').append(encode(salt));
        sb.append('$').append(encode(rawHash(type, version, memoryCost, timeCost, lanes, threads, hashLength, secret, ad, salt, password, options)));
        return sb.toString();
    }

    @Override
    public boolean verifyRaw(Jargon2.Type type, Jargon2.Version version, int memoryCost, int timeCost, int lanes, int threads, byte[] rawHash, byte[] secret, byte[] ad, byte[] salt, byte[] password, Map<String, Object> options) {
        byte[] calcHash = doDummyHash(rawHash.length, password, salt, secret, ad);
        return Arrays.equals(rawHash, calcHash);
    }

    @Override
    public boolean verifyEncoded(String encodedHash, int threads, byte[] secret, byte[] ad, byte[] password, Map<String, Object> options) {
        int lastDollar = encodedHash.lastIndexOf('$');
        byte[] hash = decode(encodedHash.substring(lastDollar + 1));
        int lastLastDollar = encodedHash.lastIndexOf('$', lastDollar - 1);
        byte[] salt = decode(encodedHash.substring(lastLastDollar + 1, lastDollar));
        byte[] calcHash = doDummyHash(hash.length, password, salt, secret, ad);
        return Arrays.equals(hash, calcHash);
    }

    private String encode(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    private byte[] decode(String encoded) {
        return Base64.decodeBase64(encoded);
    }

    private byte[] doDummyHash(int length, byte[]... data) {
        if (data == null || data.length == 0 || length == 0) return new byte[0];

        byte[] hash = new byte[length];

        int i = 0;
        for (byte[] d : data) {
            if (d != null) {
                for (byte b : d) {
                    hash[i] = (byte) (hash[i] + 7 * b);
                    i = (i + 1) % length;
                }
            }
        }

        return hash;
    }
}
