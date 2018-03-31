package com.github.masonm;

import org.apache.commons.codec.binary.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import java.io.IOException;

public class Jwt {
    private final JsonNode header;
    private final JsonNode payload;

    public Jwt(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            this.header  = MissingNode.getInstance();
            this.payload = MissingNode.getInstance();
        } else {
            this.header = parsePart(parts[0]);
            this.payload = parsePart(parts[1]);
        }
    }

    private JsonNode parsePart(String part) {
        byte[] decodedJwtBody;
        try {
            decodedJwtBody = Base64.decodeBase64(part);
        } catch (IllegalArgumentException ex) {
            return MissingNode.getInstance();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(decodedJwtBody, JsonNode.class);
        } catch (IOException ioe) {
            return MissingNode.getInstance();
        }
    }

    public JsonNode getPayload() {
        return payload;
    }

    public JsonNode getHeader() {
        return header;
    }
}
