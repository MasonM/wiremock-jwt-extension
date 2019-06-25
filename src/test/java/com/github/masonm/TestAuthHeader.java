package com.github.masonm;

import org.apache.commons.codec.binary.Base64;

/**
 * Generates an Authorization header string for testing purposes
 */
public class TestAuthHeader {
    private final String header;
    private final String payload;

    public TestAuthHeader(String header, String payload) {
        this.header = header;
        this.payload = payload;
    }

    private String encode(String value) {
        return Base64.encodeBase64URLSafeString(value.getBytes());
    }

    public String toString() {
        return "Bearer " + encode(header) + "." + encode(payload) + ".dummy_signature";
    }
}
