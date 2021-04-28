#!/bin/bash

VERSION=0.7

launchWiremock() {
	echo "Launching Wiremock and setting up proxying"
	#java -cp wiremock-standalone-2.26.3.jar:build/libs/wiremock-jwt-extension-${VERSION}.jar com.github.tomakehurst.wiremock.standalone.WireMockServerRunner --extensions="com.github.masonm.JwtMatcherExtension,com.github.masonm.JwtStubMappingTransformer" &
	java -jar build/libs/wiremock-jwt-extension-${VERSION}-standalone.jar &
	WIREMOCK_PID=$!
	trap "kill $WIREMOCK_PID" exit

	echo -n "Waiting for Wiremock to start up."
	until $(curl --output /dev/null --silent --head ${WIREMOCK_BASE_URL}); do
		echo -n '.'
		sleep 1
	done
}
