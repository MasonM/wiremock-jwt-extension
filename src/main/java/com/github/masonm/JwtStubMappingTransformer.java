package com.github.masonm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.HashMap;
import java.util.Map;

public class JwtStubMappingTransformer extends StubMappingTransformer {
    private static final String PAYLOAD_FIELDS = "payloadFields";

    @Override
    public String getName() {
        return "jwt-stub-mapping-transformer";
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    @Override
    public StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters) {
        if (!parameters.containsKey(PAYLOAD_FIELDS)) {
            return stubMapping;
        }

        if (stubMapping.getRequest().getCustomMatcher() != null) {
            // already has a custom matcher. Don't overwrite
            return stubMapping;
        }

        Map<String, MultiValuePattern> requestHeaders = stubMapping.getRequest().getHeaders();
        if (!requestHeaders.containsKey("Authorization")) {
            return stubMapping;
        }

        String authHeader = requestHeaders.get("Authorization").getExpected();
        if (!authHeader.startsWith("Bearer ")) {
            return stubMapping;
        }

        String authToken = authHeader.substring("Bearer ".length());
        Parameters requestMatcherParameters = getRequestMatcherParameter(
            new Jwt(authToken),
            parameters.get(PAYLOAD_FIELDS)
        );

        Map<String, Object> encodedRequest = Json.objectToMap(stubMapping.getRequest());
        encodedRequest.remove("headers");
        requestMatcherParameters.put("request", encodedRequest);

        RequestPattern newRequest = new RequestPatternBuilder(JwtMatcherExtension.NAME, requestMatcherParameters).build();
        stubMapping.setRequest(newRequest);
        return stubMapping;
    }

    private Parameters getRequestMatcherParameter(Jwt token, Object payloadParamValue) {
        Iterable<String> payloadFields = Json.getObjectMapper().convertValue(
            payloadParamValue,
            new TypeReference<Iterable<String>>() {}
        );
        Parameters params = new Parameters();

        Map<String, String> payload = new HashMap<>();
        for (String field: payloadFields) {
            payload.put(field, token.getPayload().path(field).asText());
        }
        params.put(JwtMatcherExtension.PARAM_NAME_PAYLOAD, payload);

        return params;
    }
}
