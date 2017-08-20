package com.github.masonm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JwtTest {
    public static final String TEST_HEADER = "{ \"foo\": \"bar\" }";
    public static final String TEST_PAYLOAD = "{ \"bar\": \"bam\" }";

    @Test
    public void constructorUsesAbsentForInvalidTokens() {
        final ImmutableList<String> testValues = ImmutableList.of(
            "",
            ".",
            "too.few_segments",
            "invalid.!!!!.base64",
            "too.many.segments.here",
            Encoding.encodeBase64("invalidJson".getBytes()) + "." + Encoding.encodeBase64("invalidJson".getBytes()) + ".foobar"
        );
        for (String testValue: testValues) {
            Jwt token = new Jwt(testValue);
            String errMsg = "Failed with value '" + testValue + "'";
            assertThat(errMsg, token.getHeader().isPresent(), is(false));
            assertThat(errMsg, token.getPayload().isPresent(), is(false));
        }
    }

    @Test
    public void withValidToken() {
        final String header = Encoding.encodeBase64(TEST_HEADER.getBytes());
        final String payload = Encoding.encodeBase64(TEST_PAYLOAD.getBytes());

        final Jwt token = new Jwt(header + "." + payload + ".signature_not_verified");
        assertThat(token.getHeader().isPresent(), is(true));
        assertThat(token.getPayload().isPresent(), is(true));

        final ObjectNode headerRoot = JsonNodeFactory.instance.objectNode();
        headerRoot.put("foo", "bar");
        assertThat(token.getHeader().get(), is((JsonNode) headerRoot));

        final ObjectNode payloadRoot = JsonNodeFactory.instance.objectNode();
        payloadRoot.put("bar", "bam");
        assertThat(token.getPayload().get(), is((JsonNode) payloadRoot));
    }
}
