package com.github.masonm;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Arrays;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JwtStubMappingTransformerTest {
    private static final JwtStubMappingTransformer TRANSFORMER = new JwtStubMappingTransformer();

    @Test
    public void returnsMappingUnmodifiedWithMissingParameters() {
        StubMapping testMapping = WireMock.get("/").build();
        assertUnmodifiedOnTransform(testMapping, Parameters.empty());
    }

    @Test
    public void returnsMappingUnmodifiedWhenRequestHasCustomMatcher() {
        StubMapping testMapping = WireMock.get("/").build();
        testMapping.setRequest(WireMock.requestMadeFor(RequestPattern.ANYTHING).build());
        assertUnmodifiedOnTransform(testMapping, getParams("foo"));
    }

    @Test
    public void returnsMappingUnmodifiedWhenNoAuthorizationHeader() {
        StubMapping testMapping = WireMock.get("/").build();
        assertUnmodifiedOnTransform(testMapping, getParams("foo"));
    }

    @Test
    public void returnsMappingUnmodifiedWhenAuthorizationHeaderTypeIsNotBearer() {
        StubMapping testMapping = WireMock.get("/").withBasicAuth("foo", "bar").build();
        assertUnmodifiedOnTransform(testMapping, getParams("foo"));
    }

    @Test
    public void returnsMappingUnmodifiedWhenAuthorizationHeaderIsInvalid() {
        StubMapping testMapping = WireMock
            .get("/")
            .withHeader("Authorization", WireMock.equalTo("Bearer f00"))
            .build();
        assertUnmodifiedOnTransform(testMapping, getParams("foo"));
    }

    @Test
    public void returnsModifiedMappingWhenMatchingValidPayloadField() {
        final TestAuthHeader testAuthHeader = new TestAuthHeader(
            "doesnt_matter",
            "{ \"matched_key\": \"matched_value\" }"
        );
        StubMapping testMapping = WireMock
            .post("/")
            .withHeader("Authorization", WireMock.equalTo(testAuthHeader.toString()))
            .build();
        final Parameters payloadMatchParams = Parameters.one(
            JwtStubMappingTransformer.PAYLOAD_FIELDS,
            Arrays.asList("matched_key")
        );

        StubMapping transformedMapping = TRANSFORMER.transform(testMapping, null, payloadMatchParams);
        RequestPattern actualRequestPattern = transformedMapping.getRequest();

        final Parameters expectedParameters = Parameters.one(
                JwtMatcherExtension.PARAM_NAME_PAYLOAD,
                ImmutableMap.of("matched_key", "matched_value")
        );
        assertThat(actualRequestPattern.getCustomMatcher().getName(), is(JwtMatcherExtension.NAME));
        assertThat(actualRequestPattern.getCustomMatcher().getParameters(), is(expectedParameters));

        final RequestPattern expectedRequestPattern = new RequestPattern(
                null,
                null,
                null,
                WireMock.urlEqualTo("/"),
                RequestMethod.POST,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                actualRequestPattern.getCustomMatcher(),
                null,
                null
        );
        assertThat(actualRequestPattern, is(expectedRequestPattern));
    }

    @Test
    public void acceptanceTestReturnsModifiedMappingWhenMatchingValidPayloadField() {
        final TestAuthHeader testAuthHeader = new TestAuthHeader(
            "doesnt_matter",
            "{ \"matched_key\": \"matched_value\" }"
        );
        StubMapping testMapping = WireMock
            .get("/")
            .withHeader("Host", WireMock.equalTo("www.example.com"))
            .withHeader("Authorization", WireMock.equalTo(testAuthHeader.toString()))
            .willReturn(ResponseDefinitionBuilder.okForJson("foo"))
            .build();
        final Parameters payloadMatchParams = Parameters.one(
            JwtStubMappingTransformer.PAYLOAD_FIELDS,
            Arrays.asList("matched_key")
        );

        StubMapping transformedMapping = TRANSFORMER.transform(testMapping, null, payloadMatchParams);
        final String stubMappingJson = Json.write(transformedMapping);
        final String EXPECTED_STUB_MAPPING_JSON =
            "{\n" +
                "\"request\": {\n" +
                    "\"url\": \"/\",\n" +
                    "\"method\": \"GET\",\n" +
                    "\"headers\": {\n" +
                        "\"Host\": { \"equalTo\": \"www.example.com\" }\n" +
                    "},\n" +
                    "\"customMatcher\": {\n" +
                        "\"name\": \"" + JwtMatcherExtension.NAME + "\",\n" +
                        "\"parameters\": {\n" +
                            "\"payload\": {\n" +
                                "\"matched_key\": \"matched_value\"\n" +
                            "}\n" +
                        "}\n" +
                    "}\n" +
                "},\n" +
                "\"response\": {\n" +
                    "\"status\": 200,\n" +
                    "\"body\": \"\\\"foo\\\"\",\n" +
                    "\"headers\": {\n" +
                        "\"Content-Type\": \"application/json\"\n" +
                    "}\n" +
                "}\n" +
            "}";
        assertThat(stubMappingJson, equalToJson(EXPECTED_STUB_MAPPING_JSON, JSONCompareMode.STRICT_ORDER));
    }

    private Parameters getParams(String ...payloadFields) {
       return Parameters.one(JwtStubMappingTransformer.PAYLOAD_FIELDS, Arrays.asList(payloadFields));
    }

    private void assertUnmodifiedOnTransform(StubMapping testMapping, Parameters parameters) {
        String json = testMapping.toString();
        TRANSFORMER.transform(testMapping, null, parameters);
        assertThat(testMapping.toString(), is(json));
    }
}
