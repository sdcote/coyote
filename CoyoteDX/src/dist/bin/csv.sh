#!/bin/sh
APP_HOME="`pwd -P`"
java -jar $APP_HOME/lib/CoyoteVault-0.0.6.jar "$@"
