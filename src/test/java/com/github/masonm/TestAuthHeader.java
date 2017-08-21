package com.github.masonm;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.google.common.collect.ImmutableMap;

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
            JwtMatcherExtension.PARAM_NAME_HEADER, ImmutableMap.of(headerPrefix + "_key", headerPrefix + "_value")
        );
    }

    public Parameters getPayloadMatchParameters() {
        return Parameters.one(
            JwtMatcherExtension.PARAM_NAME_PAYLOAD, ImmutableMap.of(payloadPrefix + "_key", payloadPrefix + "_value")
        );
    }

    public Parameters getBothMatchParameters() {
        return Parameters.from(
            ImmutableMap.<String, Object>builder()
                .putAll(getHeaderMatchParams())
                .putAll(getPayloadMatchParameters())
                .build()
        );
    }
}
