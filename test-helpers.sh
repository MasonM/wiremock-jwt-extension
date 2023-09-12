#!/bin/bash

VERSION=1.0.0-SNAPSHOT

launchWiremock() {
	echo "Launching Wiremock and setting up proxying"
	#java -cp wiremock-standalone-3.0.4.jar:build/libs/wiremock-jwt-extension-${VERSION}.jar wiremock.Run --extensions="com.github.masonm.JwtMatcherExtension,com.github.masonm.JwtStubMappingTransformer" &
	java -jar build/libs/wiremock-jwt-extension-${VERSION}-standalone.jar &
	WIREMOCK_PID=$!
	trap "kill $WIREMOCK_PID" exit

	echo -n "Waiting for Wiremock to start up."
	until $(curl --output /dev/null --silent --head ${WIREMOCK_BASE_URL}); do
		echo -n '.'
		sleep 1
	done
}
