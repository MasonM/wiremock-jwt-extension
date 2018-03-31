#!/bin/bash

echo "Launching Wiremock and setting up proxying"
java -jar build/libs/wiremock*standalone*.jar 1>/dev/null 2>/dev/null &
WIREMOCK_PID=$!
trap "kill $WIREMOCK_PID" exit

echo -n "Waiting for Wiremock to start up."
until $(curl --output /dev/null --silent --head http://localhost:8080); do
        echo -n '.'
        sleep 1
done


echo -e "done\nCreating proxy mapping"
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

echo -e "done\nMaking request"
curl -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.XbPfbIHMI6arZ3Y922BhjWgQzWXcXNrz0ogtVhfEd2o' http://localhost:8080/some_url
