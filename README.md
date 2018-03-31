# Overview

[![Build Status](https://travis-ci.org/MasonM/wiremock-jwt-extension.svg?branch=master)](https://travis-ci.org/MasonM/wiremock-jwt-extension)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-jwt-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-jwt-extension)

wiremock-jwt-extension consists of a [request matcher extension](http://wiremock.org/docs/extending-wiremock/#custom-request-matchers) and a [stub mapping transformer extension](http://wiremock.org/docs/record-playback/#transforming-generated-stubs) for [WireMock](http://wiremock.org).

The request matcher extracts and matches against fields in the "payload" or "header" portion of JWT tokens in the  "Authorization" header of a request. The stub mapping transformer can transform recorded stub mappings to use the request matcher if there exists a JWT token in the "Authorization" header.

# Building

Run `gradle jar` to build the JAR without dependencies or `gradle fatJar` to build a standalone JAR.
These will be placed in `build/libs/`.

# Running

Standalone server:
```sh
java -jar build/libs/wiremock-jwt-extension-0.2-standalone.jar
```

With WireMock standalone JAR:
```sh
java \
        -cp wiremock-standalone.jar:build/libs/wiremock-jwt-extension-0.2.jar \
        com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
        --extensions="com.github.masonm.JwtMatcherExtension,com.github.masonm.JwtStubMappingTransformer"
```

Programmatically in Java:
```java
new WireMockServer(wireMockConfig()
    .extensions("com.github.masonm.JwtMatcherExtension", "com.github.masonm.JwtStubMappingTransformer"))
```

# Request matcher usage

When used as a "customerMatcher" via JSON, use the name "jwt-matcher". Accepted parameters:
* `header`: Key-value map of header fields to match, e.g. `{ "alg": "HS256" }`
* `payload`: Key-value map of payload fields to match, e.g. `{ "admin": true }`
* `request`: Any additional request matchers.

Here's a cURL command to create an example stub mapping:
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
                    "name" : "John Doe"
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
curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o' http://localhost:8080/some_url
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
