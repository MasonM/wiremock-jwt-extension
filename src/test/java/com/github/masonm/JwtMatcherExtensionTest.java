package com.github.masonm;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JwtMatcherExtensionTest {
    private static final String TEST_ENCODED_HEADER = Encoding.encodeBase64(JwtTest.TEST_HEADER.getBytes());
    private static final String TEST_ENCODED_PAYLOAD = Encoding.encodeBase64(JwtTest.TEST_PAYLOAD.getBytes());
    private static final Parameters HEADER_MATCH_PARAMETERS = Parameters.one(
        JwtMatcherExtension.PARAM_NAME_HEADER, ImmutableMap.of("foo", "bar")
    );
    private static final Parameters PAYLOAD_MATCH_PARAMETERS = Parameters.one(
        JwtMatcherExtension.PARAM_NAME_PAYLOAD, ImmutableMap.of("bar", "bam")
    );
    private static final Parameters BOTH_MATCH_PARAMETERS = Parameters.from(
        ImmutableMap.<String, Object>builder().putAll(HEADER_MATCH_PARAMETERS).putAll(PAYLOAD_MATCH_PARAMETERS).build()
    );

    @Test
    public void noMatchWithMissingRequiredParameters() {
        assertFalse(isExactMatch(mockRequest(), Parameters.empty()));

        Parameters invalidParameters = Parameters.one("foo", "bar");
        assertFalse(isExactMatch(mockRequest(), invalidParameters));
    }

    @Test
    public void withValidParametersAndMatchingRequest() {
        final MockRequest request = mockRequest()
            .header("Authorization", "Bearer " + TEST_ENCODED_HEADER + "." + TEST_ENCODED_PAYLOAD + ".f00");
        assertTrue(isExactMatch(request, HEADER_MATCH_PARAMETERS));
        assertTrue(isExactMatch(request, PAYLOAD_MATCH_PARAMETERS));
        assertTrue(isExactMatch(request, BOTH_MATCH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndRequestWithBasicAuthorizationType() {
        final MockRequest request = mockRequest()
            .header("Authorization", "Basic " + TEST_ENCODED_HEADER + "." + TEST_ENCODED_PAYLOAD + ".f00");
        assertFalse(isExactMatch(request, HEADER_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, PAYLOAD_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, BOTH_MATCH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndRequestWithoutAuthorization() {
        final MockRequest request = mockRequest();
        assertFalse(isExactMatch(request, HEADER_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, PAYLOAD_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, BOTH_MATCH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndRequestWithInvalidAuthorization() {
        final MockRequest request = mockRequest().header("Authorization", "Bearer f00");
        assertFalse(isExactMatch(request, HEADER_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, PAYLOAD_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, BOTH_MATCH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndNonMatchingRequest() {
        final MockRequest request = mockRequest()
            .header("Authorization", "Bearer " + TEST_ENCODED_PAYLOAD + "." + TEST_ENCODED_HEADER + ".f00");
        assertFalse(isExactMatch(request, HEADER_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, PAYLOAD_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, BOTH_MATCH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndOnlyHeaderMatchingRequest() {
        final MockRequest request = mockRequest()
            .header("Authorization", "Bearer " + TEST_ENCODED_HEADER + "." + TEST_ENCODED_HEADER + ".f00");
        assertTrue(isExactMatch(request, HEADER_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, PAYLOAD_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, BOTH_MATCH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndOnlyPayloadMatchingRequest() {
        final MockRequest request = mockRequest()
            .header("Authorization", "Bearer " + TEST_ENCODED_PAYLOAD + "." + TEST_ENCODED_PAYLOAD + ".f00");
        assertFalse(isExactMatch(request, HEADER_MATCH_PARAMETERS));
        assertTrue(isExactMatch(request, PAYLOAD_MATCH_PARAMETERS));
        assertFalse(isExactMatch(request, BOTH_MATCH_PARAMETERS));
    }

    @Test
    public void withRequestParameter() {
        final Parameters requestAndBodyParmaters = Parameters.from(HEADER_MATCH_PARAMETERS);
        requestAndBodyParmaters.put(
            "request",
            ImmutableMap.of("url", "/test_url")
        );

        MockRequest testRequest = mockRequest()
            .url("/wrong_url")
            .header("Authorization", "Bearer " + TEST_ENCODED_HEADER + "." + TEST_ENCODED_PAYLOAD + ".f00");
        assertFalse(isExactMatch(testRequest, requestAndBodyParmaters));

        testRequest.url("/test_url");
        assertTrue(isExactMatch(testRequest, requestAndBodyParmaters));
    }

    private boolean isExactMatch(MockRequest request, Parameters parameters) {
        return new JwtMatcherExtension().match(request.asLoggedRequest(), parameters).isExactMatch();
    }
}
