package com.github.masonm.wiremock;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.google.common.base.Splitter;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
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
        if (!parameters.containsKey("user_id")) {
            return noMatch();
        }

        RequestPattern requestPattern = Json.mapToObject((Map<String, Object>)parameters.get("request"), RequestPattern.class);
        if (!requestPattern.match(request).isExactMatch()) {
            return noMatch();
        }

        String authString = request.getHeader("Authorization");
        if (authString == null || authString.isEmpty() || !authString.startsWith("Bearer ")) {
            return noMatch();
        }

        List<String> parts = Splitter.on(".").splitToList(authString.substring("Bearer ".length()));
        if (parts.size() != 3) {
            return noMatch();
        }

        byte[] decodedJwtBody = Base64.getDecoder().decode(parts.get(1));
        if (decodedJwtBody == null) {
            return noMatch();
        }

        JsonNode jwtBody = Json.node(new String(decodedJwtBody, StandardCharsets.UTF_8));
        String uid = jwtBody.path("user_id").asText();
        if (!Objects.equals(uid, parameters.get("user_id"))) {
            return noMatch();
        }

        // Optionally check scope, if provided in parameters
        if (parameters.containsKey("scope")) {
            String scope = jwtBody.path("scope").asText();
            if (!Objects.equals(scope, parameters.get("scope"))) {
                return noMatch();
            }
        }

        return exactMatch();
    }
}
