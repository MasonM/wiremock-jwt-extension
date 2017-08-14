package com.github.masonm;

import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JwtMatcherExtensionTest {
    private static final String TEST_ENCODED_HEADER = Encoding.encodeBase64(JwtTest.TEST_HEADER.getBytes());
    private static final String TEST_ENCODED_PAYLOAD = Encoding.encodeBase64(JwtTest.TEST_PAYLOAD.getBytes());
    private static final JwtMatcherExtension MATCHER = new JwtMatcherExtension();
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
        assertThat(MATCHER.match(mockRequest(), Parameters.empty()).isExactMatch(), is(false));

        Parameters invalidParameters = Parameters.one("foo", "bar");
        assertThat(MATCHER.match(mockRequest(), invalidParameters).isExactMatch(), is(false));
    }

    @Test
    public void withValidParametersAndMatchingRequest() {
        Request matchingRequest = mockRequest()
            .header("Authorization", "Bearer " + TEST_ENCODED_HEADER + "." + TEST_ENCODED_PAYLOAD + ".f00");
        assertThat(MATCHER.match(matchingRequest, HEADER_MATCH_PARAMETERS).isExactMatch(), is(true));
        assertThat(MATCHER.match(matchingRequest, PAYLOAD_MATCH_PARAMETERS).isExactMatch(), is(true));
        assertThat(MATCHER.match(matchingRequest, BOTH_MATCH_PARAMETERS).isExactMatch(), is(true));
    }

    @Test
    public void withValidParametersAndNonMatchingRequest() {
        Request notMatchingRequest = mockRequest()
            .header("Authorization", "Bearer " + TEST_ENCODED_PAYLOAD + "." + TEST_ENCODED_HEADER + ".f00");
        assertThat(MATCHER.match(notMatchingRequest, HEADER_MATCH_PARAMETERS).isExactMatch(), is(false));
        assertThat(MATCHER.match(notMatchingRequest, PAYLOAD_MATCH_PARAMETERS).isExactMatch(), is(false));
        assertThat(MATCHER.match(notMatchingRequest, BOTH_MATCH_PARAMETERS).isExactMatch(), is(false));
    }

    @Test
    public void withValidParametersAndOnlyHeaderMatchingRequest() {
        Request notMatchingRequest = mockRequest()
            .header("Authorization", "Bearer " + TEST_ENCODED_HEADER + "." + TEST_ENCODED_HEADER + ".f00");
        assertThat(MATCHER.match(notMatchingRequest, HEADER_MATCH_PARAMETERS).isExactMatch(), is(true));
        assertThat(MATCHER.match(notMatchingRequest, PAYLOAD_MATCH_PARAMETERS).isExactMatch(), is(false));
        assertThat(MATCHER.match(notMatchingRequest, BOTH_MATCH_PARAMETERS).isExactMatch(), is(false));
    }

    @Test
    public void withValidParametersAndInvalidRequest() {
        Request invalidHeaderRequest = mockRequest().header("Authorization", "Bearer f00");
        assertThat(MATCHER.match(invalidHeaderRequest, HEADER_MATCH_PARAMETERS).isExactMatch(), is(false));
        assertThat(MATCHER.match(invalidHeaderRequest, PAYLOAD_MATCH_PARAMETERS).isExactMatch(), is(false));
        assertThat(MATCHER.match(invalidHeaderRequest, BOTH_MATCH_PARAMETERS).isExactMatch(), is(false));
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
        assertThat(MATCHER.match(testRequest, requestAndBodyParmaters).isExactMatch(), is(false));

        testRequest.url("/test_url");
        assertThat(MATCHER.match(testRequest, requestAndBodyParmaters).isExactMatch(), is(true));
    }
}
