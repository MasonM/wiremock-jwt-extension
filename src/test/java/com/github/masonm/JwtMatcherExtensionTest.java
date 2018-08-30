package com.github.masonm;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JwtMatcherExtensionTest {
    private static final TestAuthHeader TEST_AUTH_HEADER = new TestAuthHeader("test_header", "test_payload");

    @Test
    public void noMatchWithMissingRequiredParameters() {
        assertFalse(isExactMatch(mockRequest(), Parameters.empty()));

        Parameters invalidParameters = Parameters.one("test_header", "test_payload");
        assertFalse(isExactMatch(mockRequest(), invalidParameters));
    }

    @Test
    public void withValidParametersAndMatchingRequest() {
        final MockRequest request = mockRequest().header("Authorization", TEST_AUTH_HEADER.toString());

        assertTrue(isHeaderExactMatch(request, TEST_AUTH_HEADER));
        assertTrue(isPayloadExactMatch(request, TEST_AUTH_HEADER));
        assertTrue(isBothExactMatch(request, TEST_AUTH_HEADER));
    }

    @Test
    public void withValidParametersAndRequestWithoutAuthorization() {
        final MockRequest request = mockRequest();
        assertFalse(isHeaderExactMatch(request, TEST_AUTH_HEADER));
        assertFalse(isPayloadExactMatch(request, TEST_AUTH_HEADER));
        assertFalse(isBothExactMatch(request, TEST_AUTH_HEADER));
    }

    @Test
    public void withValidParametersAndRequestWithInvalidAuthorization() {
        final MockRequest request = mockRequest().header("Authorization", "Bearer f00");
        assertFalse(isHeaderExactMatch(request, TEST_AUTH_HEADER));
        assertFalse(isPayloadExactMatch(request, TEST_AUTH_HEADER));
        assertFalse(isBothExactMatch(request, TEST_AUTH_HEADER));
    }

    @Test
    public void withValidParametersAndNonMatchingRequest() {
        final MockRequest request = mockRequest().header("Authorization", TEST_AUTH_HEADER.toString());

        final TestAuthHeader differentAuthHeader = new TestAuthHeader("different_header", "different_payload");
        assertFalse(isHeaderExactMatch(request, differentAuthHeader));
        assertFalse(isPayloadExactMatch(request, differentAuthHeader));
        assertFalse(isBothExactMatch(request, differentAuthHeader));
    }

    @Test
    public void withValidParametersAndOnlyHeaderMatchingRequest() {
        final MockRequest request = mockRequest().header("Authorization", TEST_AUTH_HEADER.toString());

        final TestAuthHeader differentAuthHeader = new TestAuthHeader("test_header", "different_payload");
        assertTrue(isHeaderExactMatch(request, differentAuthHeader));
        assertFalse(isPayloadExactMatch(request, differentAuthHeader));
        assertFalse(isBothExactMatch(request, differentAuthHeader));
    }

    @Test
    public void withValidParametersAndOnlyPayloadMatchingRequest() {
        final MockRequest request = mockRequest().header("Authorization", TEST_AUTH_HEADER.toString());

        final TestAuthHeader differentAuthHeader = new TestAuthHeader("different_header", "test_payload");
        assertFalse(isHeaderExactMatch(request, differentAuthHeader));
        assertTrue(isPayloadExactMatch(request, differentAuthHeader));
        assertFalse(isBothExactMatch(request, differentAuthHeader));
    }

    @Test
    public void withRequestParameter() {
        final Parameters requestAndBodyParmaters = Parameters.from(TEST_AUTH_HEADER.getHeaderMatchParams());
        requestAndBodyParmaters.put(
            "request",
            ImmutableMap.of("url", "/test_url")
        );

        MockRequest testRequest = mockRequest()
            .url("/wrong_url")
            .header("Authorization", TEST_AUTH_HEADER.toString());
        assertFalse(isExactMatch(testRequest, requestAndBodyParmaters));

        testRequest.url("/test_url");
        assertTrue(isExactMatch(testRequest, requestAndBodyParmaters));
    }

    private boolean isExactMatch(MockRequest request, Parameters parameters) {
        return new JwtMatcherExtension().match(request.asLoggedRequest(), parameters).isExactMatch();
    }

    private boolean isHeaderExactMatch(MockRequest request, TestAuthHeader testAuthHeader) {
        return isExactMatch(request, testAuthHeader.getHeaderMatchParams());
    }

    private boolean isPayloadExactMatch(MockRequest request, TestAuthHeader testAuthHeader) {
        return isExactMatch(request, testAuthHeader.getPayloadMatchParameters());
    }

    private boolean isBothExactMatch(MockRequest request, TestAuthHeader testAuthHeader) {
        return isExactMatch(request, testAuthHeader.getBothMatchParameters());
    }
}
