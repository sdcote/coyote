@echo off
set APP_HOME=%DIRNAME%..
start /B javaw -jar %APP_HOME%\lib\CoyoteVault-0.0.1.jar %*
