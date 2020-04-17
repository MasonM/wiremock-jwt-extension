# Overview

[![Build Status](https://travis-ci.org/MasonM/wiremock-jwt-extension.svg?branch=master)](https://travis-ci.org/MasonM/wiremock-jwt-extension)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-jwt-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-jwt-extension)

wiremock-jwt-extension consists of two extensions for [WireMock](http://wiremock.org): a [request matcher extension](http://wiremock.org/docs/extending-wiremock/#custom-request-matchers) and a [stub mapping transformer extension](http://wiremock.org/docs/record-playback/#transforming-generated-stubs).

The request matcher extracts JWT tokens from incoming requests and matches against the "payload" and/or "header" portions. The stub mapping transformer can transform recorded stub mappings to use the request matcher if there exists a JWT token in the "Authorization" header.

JWE (JSON Web Encryption) and signature verification are not currently supported. Patches welcome!

# Running

There are three ways of running the extension:

1. Standalone, e.g.

    ```sh
    java -jar build/libs/wiremock-jwt-extension-0.6-standalone.jar
    ```
    
2. As an extension of the WireMock standalone JAR, e.g.

    ```sh
    wget -nc http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.26.3/wiremock-standalone-2.26.3.jar
    java \
            -cp wiremock-standalone-2.26.3.jar:build/libs/wiremock-jwt-extension-0.6.jar \
            com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
            --extensions="com.github.masonm.JwtMatcherExtension,com.github.masonm.JwtStubMappingTransformer"
    ```

3. Programmatically in Java, e.g.

    ```java
    new WireMockServer(wireMockConfig()
        .extensions("com.github.masonm.JwtMatcherExtension", "com.github.masonm.JwtStubMappingTransformer"))
    ```

# Request matcher usage

The extension accepts the following parameters:
* `header`: Key-value map of header fields to match, e.g. `{ "alg": "HS256" }`
* `payload`: Key-value map of payload fields to match, e.g. `{ "admin": true }`. If the value is an array (e.g. `{ "aud": ["aud1", "aud2"] }`, it will be matched exactly.
* `request`: Any additional request matchers. This is basically a workaround for the inability to compose extensions in WireMock.

When using the API, make sure to set the `"name"` field of the customMatcher to `"jwt-matcher"`.  Here's an example cURL command that creates a stub mapping with the request matcher:
```sh
curl -d@- http://localhost:8080/__admin/mappings <<-EOD
{
    "request" : {
        "customMatcher" : {
            "name" : "jwt-matcher",
            "parameters" : {
                "header" : {
                    "alg" : "HS256",
                    "typ": "JWT"
                },
                "payload": {
                    "name" : "John Doe",
                    "aud": ["aud1", "aud2"]
                },
                "request" : {
                    "url" : "/some_url",
                    "method" : "GET"
                }
            }
        }
    },
    "response" : {
        "status" : 200,
        "body": "success"
    }
}
EOD
```

Example request that matches the above stub mapping:
```sh
curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJhdWQiOlsiYXVkMSIsImF1ZDIiXX0.h49E7AnYrJpttdEoi4GmoZUCtg6GBSHTSjUcDGnbjRI' http://localhost:8080/some_url
```

# Stub mapping transformer usage

The transformer has the name "jwt-stub-mapping-transformer" and accepts a list of payload fields to match against via the parameter "payloadFields". Example request to `POST /__admin/recordings/snapshot`:
```json
{
    "transformers" : [ "jwt-stub-mapping-transformer" ],
    "transformerParameters" : {
        "payloadFields" : [ "name", "admin" ]
    }
}
```

# Building

Run `gradle jar` to build the JAR without WireMock or `gradle standalone` to build a standalone JAR.
These will be placed in `build/libs/`.
