package com.github.masonm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.Encoding;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;

import java.io.IOException;
import java.util.List;

public class Jwt {
    private final Optional<JsonNode> header;
    private final Optional<JsonNode> payload;

    public Jwt(String token) {
        List<String> parts = Splitter.on(".").splitToList(token);
        if (parts.size() != 3) {
            this.header  = Optional.absent();
            this.payload = Optional.absent();
        } else {
            this.header = Optional.fromNullable(parsePart(parts.get(0)));
            this.payload = Optional.fromNullable(parsePart(parts.get(1)));
        }
    }

    private JsonNode parsePart(String part) {
        byte[] decodedJwtBody;

        try {
            decodedJwtBody = Encoding.decodeBase64(part);
        } catch (IllegalArgumentException ex) {
            return null;
        }

        try {
            ObjectMapper mapper = Json.getObjectMapper();
            return mapper.readValue(decodedJwtBody, JsonNode.class);
        } catch (IOException ioe) {
            return null;
        }
    }

    public Optional<JsonNode> getPayload() {
        return payload;
    }

    public Optional<JsonNode> getHeader() {
        return header;
    }
}
