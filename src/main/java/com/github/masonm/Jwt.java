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
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            this.header  = MissingNode.getInstance();
            this.payload = MissingNode.getInstance();
        } else {
            this.header = parsePart(parts[0]);
            this.payload = parsePart(parts[1]);
        }
    }

    public static Jwt fromAuthHeader(String authHeader) {
        // Per RFC7235, the syntax for the credentials in the Authorization header is:
        //     credentials = auth-scheme [ 1*SP ( token68 / #auth-param ) ]
        // where auth-scheme is usually "Bearer" for JWT, but some APIs use "JWT" instead.
        int separatorIndex = authHeader.indexOf(" ");
        if (separatorIndex == -1) {
            // Missing auth-scheme. Not standard, but try parsing it anyway.
            return new Jwt(authHeader);
        } else {
            return new Jwt(authHeader.substring(separatorIndex + 1));
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
