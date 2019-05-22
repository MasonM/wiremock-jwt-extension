package com.github.masonm;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JwtMatcherExtensionTest {
    private static final TestAuthHeader TEST_AUTH_HEADER = new TestAuthHeader(
        "{ \"test_header\": \"header_value\" }",
        "{ \"test_payload\": \"payload_value\" }"
    );
    private static final Parameters PAYLOAD_PARAMETER = Parameters.one(
        JwtMatcherExtension.PARAM_NAME_PAYLOAD,
        ImmutableMap.of("test_payload", "payload_value")
    );
    private static final Parameters HEADER_PARAMETER = Parameters.one(
        JwtMatcherExtension.PARAM_NAME_HEADER,
        ImmutableMap.of("test_header", "header_value")
    );
    private static final Parameters BOTH_PARAMETERS = new Parameters() {{
        putAll(PAYLOAD_PARAMETER);
        putAll(HEADER_PARAMETER);
    }};

    @Test
    public void noMatchWithMissingRequiredParameters() {
        assertFalse(isExactMatch(mockRequest(), Parameters.empty()));

        Parameters invalidParameters = Parameters.one("test_header", "test_payload");
        assertFalse(isExactMatch(mockRequest(), invalidParameters));
    }

    @Test
    public void withValidParametersAndMatchingRequest() {
        final MockRequest request = mockRequest().header("Authorization", TEST_AUTH_HEADER.toString());

        assertTrue(isExactMatch(request, PAYLOAD_PARAMETER));
        assertTrue(isExactMatch(request, HEADER_PARAMETER));
        assertTrue(isExactMatch(request, BOTH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndRequestWithoutAuthorization() {
        final MockRequest request = mockRequest();
        assertFalse(isExactMatch(request, PAYLOAD_PARAMETER));
        assertFalse(isExactMatch(request, HEADER_PARAMETER));
        assertFalse(isExactMatch(request, BOTH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndRequestWithInvalidAuthorization() {
        final MockRequest request = mockRequest().header("Authorization", "Bearer f00");
        assertFalse(isExactMatch(request, PAYLOAD_PARAMETER));
        assertFalse(isExactMatch(request, HEADER_PARAMETER));
        assertFalse(isExactMatch(request, BOTH_PARAMETERS));
    }

    @Test
    public void withValidParametersAndNonMatchingRequest() {
        final MockRequest requestOnlyMatchingPayload = mockRequest()
            .header("Authorization", new TestAuthHeader(
                 "{}",
                 "{ \"test_payload\": \"payload_value\" }"
            ).toString());
        assertFalse(isExactMatch(requestOnlyMatchingPayload, HEADER_PARAMETER));
        assertFalse(isExactMatch(requestOnlyMatchingPayload, BOTH_PARAMETERS));

        final MockRequest requestOnlyMatchingHeader = mockRequest()
            .header("Authorization", new TestAuthHeader(
                "{ \"test_header\": \"header_value\" }",
                "{}"
            ).toString());
        assertFalse(isExactMatch(requestOnlyMatchingHeader, PAYLOAD_PARAMETER));
        assertFalse(isExactMatch(requestOnlyMatchingHeader, BOTH_PARAMETERS));
    }

    @Test
    public void withRequestParameter() {
        final Parameters requestAndBodyParameters = Parameters.from(PAYLOAD_PARAMETER);
        requestAndBodyParameters.put(
            "request",
            ImmutableMap.of("url", "/test_url")
        );

        MockRequest testRequest = mockRequest()
            .url("/wrong_url")
            .header("Authorization", TEST_AUTH_HEADER.toString());
        assertFalse(isExactMatch(testRequest, requestAndBodyParameters));

        testRequest.url("/test_url");
        assertTrue(isExactMatch(testRequest, requestAndBodyParameters));
    }

    @Test
    public void withArrayPayload() {
        final TestAuthHeader authHeaderWithAud = new TestAuthHeader(
            "{ \"test_header\": \"header_value\" }",
            "{ \"aud\": [\"foo\", \"bar\"] }"
        );
        final MockRequest request = mockRequest().header("Authorization", authHeaderWithAud.toString());

        final Parameters matchPayloadParams = Parameters.one(
            JwtMatcherExtension.PARAM_NAME_PAYLOAD,
            ImmutableMap.of("aud", new String[] { "foo", "bar" })
        );
        assertTrue(isExactMatch(request, matchPayloadParams));

        final Parameters noMatchPayloadParams = Parameters.one(
            JwtMatcherExtension.PARAM_NAME_PAYLOAD,
            ImmutableMap.of("aud", "foo")
        );
        assertFalse(isExactMatch(request, noMatchPayloadParams));
    }

    private boolean isExactMatch(MockRequest request, Parameters parameters) {
        return new JwtMatcherExtension().match(request.asLoggedRequest(), parameters).isExactMatch();
    }
}
