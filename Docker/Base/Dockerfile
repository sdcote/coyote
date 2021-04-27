FROM openjdk:8-jre-alpine

# Add the files from the build context
COPY Docker/Base/opt opt

ENV MY_NAME="John Doe"

#RUN mkdir -p /opt/coyotedx/lib

# The core module
COPY CoyoteDX/build/install/CoyoteDX/lib /opt/coyotedx/lib

# Include the optional modules. Comment out those not required
COPY CoyoteDB/build/install/CoyoteDB/lib /opt/coyotedx/lib
COPY CoyoteFT/build/install/CoyoteFT/lib /opt/coyotedx/lib
COPY CoyoteMC/build/install/CoyoteMC/lib /opt/coyotedx/lib
COPY CoyoteMQ/build/install/CoyoteMQ/lib /opt/coyotedx/lib
COPY CoyoteMT/build/install/CoyoteMT/lib /opt/coyotedx/lib
COPY CoyoteSLF4J/build/install/CoyoteSLF4J/lib /opt/coyotedx/lib
COPY CoyoteWS/build/install/CoyoteWS/lib /opt/coyotedx/lib

# Run coyote data exchange
ENTRYPOINT ["/opt/coyotedx/bin/cdx"]

# EOF