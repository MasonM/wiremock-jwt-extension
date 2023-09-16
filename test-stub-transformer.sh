#!/bin/bash

set -e

source test-helpers.sh

PROXY_BASE_URL="https://wiremock.org"
WIREMOCK_BASE_URL=http://localhost:8080

launchWiremock

echo -e "done\n\nCreating proxy mapping"
curl -s -d '{
    "request": { "urlPattern": ".*" },
    "response": {
        "proxyBaseUrl": "'${PROXY_BASE_URL}'"
    }
}' http://localhost:8080/__admin/mappings > /dev/null


echo -e "done\n\nMaking request"
TEST_TOKEN='eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o'
curl -s -H "Authorization: Bearer ${TEST_TOKEN}" "${WIREMOCK_BASE_URL}/robots.txt?foo=bar" > /dev/null

REQUEST_JSON='{
    "outputFormat": "full",
    "persist": false,
    "transformers": ["jwt-stub-mapping-transformer"],
    "transformerParameters": {
        "payloadFields": ["iat", "user"]
    },
    "captureHeaders": {
        "Host": { "caseInsensitive": true },
        "Authorization": { "caseInsensitive": true }
    },
    "extractBodyCriteria": {
        "textSizeThreshold": "2000"
    }
}'
echo -e "done\n\nCalling snapshot API with '${REQUEST_JSON}'"
curl -X POST -d "${REQUEST_JSON}" "${WIREMOCK_BASE_URL}/__admin/recordings/snapshot"
