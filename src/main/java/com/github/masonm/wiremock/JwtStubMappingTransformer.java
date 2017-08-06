package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.CustomMatcherDefinition;
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

        Map<String, MultiValuePattern> requestHeaders = stubMapping.getRequest().getHeaders();
        if (!requestHeaders.containsKey("Authorization")) {
            return stubMapping;
        }

        String authHeader = requestHeaders.get("Authorization").getExpected();
        if (!authHeader.startsWith("Bearer ")) {
            return stubMapping;
        }

        Jwt token = new Jwt(authHeader);

        Parameters stubMappingParams = new Parameters();
        Map<String, Object> encodedRequest = Json.objectToMap(stubMapping.getRequest());
        encodedRequest.remove("headers");
        stubMappingParams.put("request", encodedRequest);

        Map<String, String> payload = new HashMap<>();
        Iterable<String> payloadFields = (Iterable<String>)parameters.get(PAYLOAD_FIELDS);
        for (String field: payloadFields) {
            payload.put(field, token.getPayload().path(field).asText());
        }
        stubMappingParams.put(JwtMatcherExtension.PARAM_NAME_PAYLOAD, payload);

        RequestPattern newRequest = new RequestPatternBuilder(JwtMatcherExtension.NAME, stubMappingParams).build();
        ResponseDefinition newResponse = ResponseDefinitionBuilder.like(stubMapping.getResponse()).build();
        return new StubMapping(newRequest, newResponse);
    }
}
