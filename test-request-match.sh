#!/bin/bash

set -e

source test-helpers.sh
WIREMOCK_BASE_URL=http://localhost:8080

launchWiremock

echo -e "done\n\nCreating proxy mapping"
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
                    "aud": ["foo", "bar"]
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

echo -e "done\n\nMaking request"
curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJhdWQiOlsiZm9vIiwiYmFyIl19.aqa_OxjpGtC4nHVCUlCqmiNHOAYK6VFyq2HFsOOmJIY' http://localhost:8080/some_url
