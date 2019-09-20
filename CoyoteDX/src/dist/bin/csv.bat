@if "%DEBUG%" == "" @echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..
start /B javaw -jar %APP_HOME%\lib\CoyoteVault-0.0.1.jar %*
