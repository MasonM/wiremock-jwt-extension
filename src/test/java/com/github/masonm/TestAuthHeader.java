package com.github.masonm;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.extension.Parameters;

import java.util.HashMap;

/**
 * Generates a Authorization header string for testing purposes, and
 * can return Parameters objects to use for request matching against the header
 */
public class TestAuthHeader {
    private String headerPrefix;
    private String payloadPrefix;

    public TestAuthHeader(String headerPrefix, String payloadPrefix) {
        this.headerPrefix = headerPrefix;
        this.payloadPrefix = payloadPrefix;
    }

    public String getEncodedHeader() {
        final String headerJson = "{ \"" + headerPrefix + "_key\": \"" + headerPrefix + "_value\" }";
        return Encoding.encodeBase64(headerJson.getBytes());
    }

    public String getEncodedPayload() {
        final String payloadJson = "{ \"" + payloadPrefix + "_key\": \"" + payloadPrefix + "_value\" }";
        return Encoding.encodeBase64(payloadJson.getBytes());
    }

    public String toString() {
        return "Bearer " + getEncodedHeader() + "." + getEncodedPayload() + ".dummy_signature";
    }

    public Parameters getHeaderMatchParams() {
        return Parameters.one(
            JwtMatcherExtension.PARAM_NAME_HEADER, new HashMap<String, String>() {{
                put(headerPrefix + "_key", headerPrefix + "_value");
            }}
        );
    }

    public Parameters getPayloadMatchParameters() {
        return Parameters.one(
            JwtMatcherExtension.PARAM_NAME_PAYLOAD, new HashMap<String, String>() {{
                put(payloadPrefix + "_key", payloadPrefix + "_value");
            }}
        );
    }

    public Parameters getBothMatchParameters() {
        return new Parameters() {{
            putAll(getHeaderMatchParams());
            putAll(getPayloadMatchParameters());
        }};
    }
}
