package com.github.masonm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.common.Encoding;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JwtTest {
    @Test
    public void constructorUsesAbsentForInvalidTokens() {
        final List<String> testValues = Arrays.asList(
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
            assertThat(errMsg, token.getHeader().isMissingNode(), is(true));
            assertThat(errMsg, token.getPayload().isMissingNode(), is(true));
        }
    }

    @Test
    public void withUnsecuredValidToken() {
        final String TEST_TOKEN = "eyJhbGciOiJub25lIn0.eyJuYW1lIjoiTWFzb24gTWFsb25lIn0.";

        final Jwt token = new Jwt(TEST_TOKEN);
        assertThat(token.getHeader().isMissingNode(), is(false));
        assertThat(token.getPayload().isMissingNode(), is(false));

        final ObjectNode headerRoot = JsonNodeFactory.instance.objectNode()
                .put("alg", "none");
        assertThat(token.getHeader(), is((JsonNode) headerRoot));

        final ObjectNode payloadRoot = JsonNodeFactory.instance.objectNode()
                .put("name", "Mason Malone");
        assertThat(token.getPayload(), is((JsonNode) payloadRoot));
    }

    @Test
    public void withSecuredValidToken() {
        final String TEST_HEADER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Ik1hc29uIE1hbG9uZSIsImlhdCI6MTUxNjIzOTAyMn0.MAzWSeaKvOgZb8_uasPYK681tF7PfC0E2AmfDsLfefs";

        final Jwt token = new Jwt(TEST_HEADER);
        assertThat(token.getHeader().isMissingNode(), is(false));
        assertThat(token.getPayload().isMissingNode(), is(false));

        final ObjectNode headerRoot = JsonNodeFactory.instance.objectNode();
        headerRoot.put("alg", "HS256");
        headerRoot.put("typ", "JWT");
        assertThat(token.getHeader(), is((JsonNode) headerRoot));

        final ObjectNode payloadRoot = JsonNodeFactory.instance.objectNode();
        payloadRoot.put("sub", "1234567890");
        payloadRoot.put("name", "Mason Malone");
        payloadRoot.put("iat", 1516239022);
        assertThat(token.getPayload(), is((JsonNode) payloadRoot));
    }
}
