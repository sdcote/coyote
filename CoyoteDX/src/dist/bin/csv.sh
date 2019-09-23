#!/bin/sh
APP_HOME="`pwd -P`"
java -jar $APP_HOME/lib/CoyoteVault-0.0.3.jar "$@"
