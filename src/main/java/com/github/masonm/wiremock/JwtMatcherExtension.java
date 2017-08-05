package com.github.masonm.wiremock;

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
    @Override
    public String getName() {
        return "jwt-matcher";
    }

    @Override
    public MatchResult match(Request request, Parameters parameters) {
        if (!parameters.containsKey("payload") && !parameters.containsKey("header")) {
            return noMatch();
        }

        if (parameters.containsKey("request")) {
            RequestPattern requestPattern = Json.mapToObject((Map<String, Object>) parameters.get("request"), RequestPattern.class);
            if (!requestPattern.match(request).isExactMatch()) {
                return noMatch();
            }
        }

        String authString = request.getHeader("Authorization");
        if (authString == null || authString.isEmpty() || !authString.startsWith("Bearer ")) {
            return noMatch();
        }

        Jwt token = new Jwt(authString.substring("Bearer ".length()));

        if (parameters.containsKey("header")) {
            if (!matchParams(token.getHeader(), (Map<String, String>)parameters.get("header"))) {
                return noMatch();
            }
        }

        if (parameters.containsKey("payload")) {
            if (!matchParams(token.getPayload(), (Map<String, String>)parameters.get("payload"))) {
                return noMatch();
            }
        }

        return exactMatch();
    }

    private boolean matchParams(JsonNode tokenValues, Map<String, String> parameters) {
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            String tokenValue = tokenValues.path(entry.getKey()).asText();
            if (!Objects.equals(tokenValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
