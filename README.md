# Overview

[![Build Status](https://github.com/MasonM/wiremock-jwt-extension/actions/workflows/gradle.yml/badge.svg)](https://github.com/MasonM/wiremock-jwt-extension/actions/workflows/gradle.yml?query=branch%3Amaster)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-jwt-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-jwt-extension)

wiremock-jwt-extension consists of two extensions for [WireMock](http://wiremock.org): a [request matcher extension](http://wiremock.org/docs/extending-wiremock/#custom-request-matchers) and a [stub mapping transformer extension](http://wiremock.org/docs/record-playback/#transforming-generated-stubs).

The request matcher extracts JWT tokens from incoming requests and matches against the "payload" and/or "header" portions. The stub mapping transformer can transform recorded stub mappings to use the request matcher if there exists a JWT token in the "Authorization" header.

JWE (JSON Web Encryption) and signature verification are not currently supported. Patches welcome!

# Installation

Maven:
```xml
<dependency>
  <groupId>com.github.masonm</groupId>
  <artifactId>wiremock-jwt-extension</artifactId>
  <version>0.10</version>
</dependency>
```

Gradle:
```groovy
implementation 'com.github.masonm:wiremock-jwt-extension:0.10'
```

# Running

There are three ways of running the extension:

1. Standalone, e.g.

    ```sh
    java -jar build/libs/wiremock-jwt-extension-0.10-standalone.jar
    ```
    
2. As an extension of the WireMock standalone JAR, e.g.

    ```sh
    wget -nc https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-jre8-standalone/2.33.2/wiremock-jre8-standalone-2.33.2.jar
    java \
            -cp wiremock-jre8-standalone-2.33.2.jar:build/libs/wiremock-jwt-extension-0.10.jar \
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
* `request`: (legacy) Any additional request matchers. Only for Wiremock versions before 2.20 that lacked support for composing standard and custom matchers.

When using the API, make sure to set the `"name"` field of the customMatcher to `"jwt-matcher"`.  Here's an example cURL command that creates a stub mapping with the request matcher:
```sh
curl -d@- http://localhost:8080/__admin/mappings <<-EOD
{
    "request" : {
        "url" : "/some_url",
        "method" : "GET",
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
