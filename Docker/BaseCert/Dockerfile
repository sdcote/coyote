FROM openjdk:8-jre-alpine

# copy self-signed root certificate to image
COPY Docker/BaseCert/SelfSignedCert.cer /usr/local/share/ca-certificates/SelfSignedCert.crt

# enable Java to make secure connections
RUN $JAVA_HOME/bin/keytool -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias selfcert -file /usr/local/share/ca-certificates/SelfSignedCert.crt

# Add the files from the build context
COPY Docker/Base/opt opt

# The core module
COPY CoyoteDX/build/install/CoyoteDX/lib /opt/cdx/lib

# Include the optional modules. Comment out those not required
COPY CoyoteDB/build/install/CoyoteDB/lib /opt/cdx/lib
COPY CoyoteFT/build/install/CoyoteFT/lib /opt/cdx/lib
COPY CoyoteMC/build/install/CoyoteMC/lib /opt/cdx/lib
COPY CoyoteMQ/build/install/CoyoteMQ/lib /opt/cdx/lib
COPY CoyoteMT/build/install/CoyoteMT/lib /opt/cdx/lib
COPY CoyoteSLF4J/build/install/CoyoteSLF4J/lib /opt/cdx/lib
COPY CoyoteWS/build/install/CoyoteWS/lib /opt/cdx/lib

# Run coyote data exchange
ENTRYPOINT ["/opt/cdx/bin/cdx"]
