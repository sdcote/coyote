FROM openjdk:8-jre-alpine

# Add the files from the build context
ADD opt opt

ENV MY_NAME="John Doe"

# The core module
ADD ../../CoyoteDX/build/install/CoyoteDX/lib /opt/coyotedx/lib

# All the optional modules, comment out those not required
ADD CoyoteDX/build/install/CoyoteDB/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteFT/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteMC/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteMQ/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteMT/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteSLF4J/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteSV/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteUI/lib /opt/coyotedx/lib
ADD CoyoteDX/build/install/CoyoteWS/lib /opt/coyotedx/lib

# Run coyote data exchange
ENTRYPOINT ["/opt/coyotedx/bin/cdx"]

# EOF