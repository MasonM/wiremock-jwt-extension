package com.github.masonm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.matching.CustomMatcherDefinition;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

public class JwtStubMappingTransformer extends StubMappingTransformer {
    public static final String PAYLOAD_FIELDS = "payloadFields";

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
        if (requestHeaders == null || !requestHeaders.containsKey(AUTHORIZATION)) {
            return stubMapping;
        }

        String authHeader = requestHeaders.get(AUTHORIZATION).getExpected();
        Parameters requestMatcherParameters = getRequestMatcherParameter(
            Jwt.fromAuthHeader(authHeader),
            parameters.get(PAYLOAD_FIELDS)
        );

        if (requestMatcherParameters == null) {
            return stubMapping;
        }

        CustomMatcherDefinition customMatcher = new CustomMatcherDefinition(JwtMatcherExtension.NAME, requestMatcherParameters);
        RequestPattern newRequest = this.getRequestPattern(stubMapping.getRequest(), customMatcher);
        stubMapping.setRequest(newRequest);
        return stubMapping;
    }

    private RequestPattern getRequestPattern(RequestPattern outer, CustomMatcherDefinition customMatcher) {
        Map<String, MultiValuePattern> newHeaders = null;
        if (outer.getHeaders() != null) {
            newHeaders = new LinkedHashMap<>(outer.getHeaders());
            newHeaders.remove(AUTHORIZATION);
            if (newHeaders.isEmpty()) {
                newHeaders = null;
            }
        }

        return new RequestPattern(
            outer.getUrlMatcher(),
            outer.getMethod(),
            newHeaders,
            outer.getQueryParameters(),
            outer.getCookies(),
            outer.getBasicAuthCredentials(),
            outer.getBodyPatterns(),
            customMatcher,
            null,
            outer.getMultipartPatterns()
        );
    }

    private Parameters getRequestMatcherParameter(Jwt token, Object payloadParamValue) {
        if (token.getPayload().isMissingNode()) {
            return null;
        }

        Iterable<String> payloadFields = new ObjectMapper().convertValue(
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
