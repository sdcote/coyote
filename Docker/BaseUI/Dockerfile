# This builds on the base CDX image
FROM cdx:latest

# Add the UI JAR files
COPY CoyoteUI/build/install/CoyoteUI/lib /opt/cdx/lib

# Add the 'base' configuration file
COPY /Docker/BaseUI/base.json /opt/cdx/cfg/

# The "base" configuration file listens on part 55290, this must match the "Port" configuration option
EXPOSE 55290/tcp

# pass the 'base" configuration to the entrypoint.
CMD ["base"]

# EOF