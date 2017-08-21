package com.github.masonm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.matching.MatchResult.noMatch;
import static com.github.tomakehurst.wiremock.matching.MatchResult.exactMatch;

public class JwtMatcherExtension extends RequestMatcherExtension {
    public static final String NAME = "jwt-matcher";
    public static final String PARAM_NAME_PAYLOAD = "payload";
    public static final String PARAM_NAME_HEADER = "header";
    public static final String PARAM_NAME_REQUEST = "request";

    @Override
    public String getName() {
        return "jwt-matcher";
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
        if (!parameters.containsKey(PARAM_NAME_PAYLOAD) && !parameters.containsKey(PARAM_NAME_HEADER)) {
            return noMatch();
        }

        if (parameters.containsKey(PARAM_NAME_REQUEST)) {
            Map<String, Object> encodedRequest = Json.objectToMap(parameters.get(PARAM_NAME_REQUEST));
            RequestPattern requestPattern = Json.mapToObject(encodedRequest, RequestPattern.class);
            if (!requestPattern.match(request).isExactMatch()) {
                return noMatch();
            }
        }

        String authString = request.getHeader("Authorization");
        if (authString == null || authString.isEmpty() || !authString.startsWith("Bearer ")) {
            return noMatch();
        }

        Jwt token = new Jwt(authString);

        if (
            parameters.containsKey(PARAM_NAME_HEADER) &&
            !matchParams(token.getHeader(), parameters.get(PARAM_NAME_HEADER))
        ) {
            return noMatch();
        }

        if (
            parameters.containsKey(PARAM_NAME_PAYLOAD) &&
            !matchParams(token.getPayload(), parameters.get(PARAM_NAME_PAYLOAD))
        ) {
            return noMatch();
        }

        return exactMatch();
    }

    private boolean matchParams(JsonNode tokenValues, Object parameters) {
        Map<String, String> parameterMap = Json.getObjectMapper().convertValue(
            parameters,
            new TypeReference<Map<String, Object>>() {}
        );
        for (Map.Entry<String, String> entry: parameterMap.entrySet()) {
            String tokenValue = tokenValues.path(entry.getKey()).asText();
            if (!Objects.equals(tokenValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
