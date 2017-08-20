package com.github.masonm;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JwtStubMappingTransformerTest {
    private static final JwtStubMappingTransformer TRANSFORMER = new JwtStubMappingTransformer();
    private static final Parameters TEST_PARAMS = Parameters.one(JwtStubMappingTransformer.PAYLOAD_FIELDS, "foo");

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

    private Parameters getParams(String ...payloadFields) {
       return Parameters.one(JwtStubMappingTransformer.PAYLOAD_FIELDS, ImmutableList.of(payloadFields));
    }

    private void assertUnmodifiedOnTransform(StubMapping testMapping, Parameters parameters) {
        String json = testMapping.toString();
        TRANSFORMER.transform(testMapping, null, parameters);
        assertThat(testMapping.toString(), is(json));
    }
}
