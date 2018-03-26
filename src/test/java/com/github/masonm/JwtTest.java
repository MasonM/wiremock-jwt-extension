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
    public void withValidToken() {
        final TestAuthHeader testAuthHeader = new TestAuthHeader("foo", "bar");

        final Jwt token = new Jwt(testAuthHeader.toString());
        assertThat(token.getHeader().isMissingNode(), is(false));
        assertThat(token.getPayload().isMissingNode(), is(false));

        final ObjectNode headerRoot = JsonNodeFactory.instance.objectNode();
        headerRoot.put("foo_key", "foo_value");
        assertThat(token.getHeader(), is((JsonNode) headerRoot));

        final ObjectNode payloadRoot = JsonNodeFactory.instance.objectNode();
        payloadRoot.put("bar_key", "bar_value");
        assertThat(token.getPayload(), is((JsonNode) payloadRoot));
    }
}
