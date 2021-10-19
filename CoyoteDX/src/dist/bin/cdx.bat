@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Coyote DX startup script for Windows
@rem
@rem ##########################################################################

if "%HOME%"=="" goto homeDrivePathPre
if exist "%HOME%\cdx_pre.bat" call "%HOME%\cdx_pre.bat"

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:homeDrivePathPre
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
if "%HOMEDRIVE%%HOMEPATH%"=="" goto userProfilePre
if "%HOMEDRIVE%%HOMEPATH%"=="%HOME%" goto userProfilePre
if exist "%HOMEDRIVE%%HOMEPATH%\cdx_pre.bat" call "%HOMEDRIVE%%HOMEPATH%\cdx_pre.bat"

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:userProfilePre
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
if "%USERPROFILE%"=="" goto alpha
if "%USERPROFILE%"=="%HOME%" goto alpha
if "%USERPROFILE%"=="%HOMEDRIVE%%HOMEPATH%" goto alpha
if exist "%USERPROFILE%\cdx_pre.bat" call "%USERPROFILE%\cdx_pre.bat"

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:alpha

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem Add default JVM options here. You can also use JAVA_OPTS and CDX_OPTS to
@rem pass JVM options to this script.
set DEFAULT_JVM_OPTS=

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:findJavaFromJavaHome
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:init
@rem Get command-line arguments, handling Windoze variants
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -

if not "%OS%" == "Windows_NT" goto win9xME_args
if "%@eval[2+2]" == "4" goto 4NT_args

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:win9xME_args
@rem Slurp the command line arguments.
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*
goto execute

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
set CMD_LINE_ARGS=%$

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:execute
@rem Setup the command line
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
@rem create the classpath by prepending the fix and cfg directories to the list
@rem of JAR files in the lib directory 
set JARS=
set CLASSPATH=
for %%i in (%APP_HOME%\lib\*.jar) do call cpb.bat %%i
set CLASSPATH=%APP_HOME%\fix;%APP_HOME%\cfg;%JARS%

@rem Make sure app.work property is set
if "%APP_WORK%"=="" set APP_WORK=%APP_HOME%\wrk

@rem Execute Coyote DX
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %CDX_OPTS% -Dapp.home="%APP_HOME%" -Dapp.work="%APP_WORK%" -classpath "%CLASSPATH%" coyote.loader.BootStrap %CMD_LINE_ARGS%

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:end
@rem End local scope for the variables with windows NT shell
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
if "%ERRORLEVEL%"=="0" goto mainEnd

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:fail
@rem Set variable CDX_EXIT_CONSOLE if you need the _script_ return code 
@rem instead of the _cmd.exe /c_ return code!
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
if  not "" == "%CDX_EXIT_CONSOLE%" exit 1
exit /b 1

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:mainEnd
@rem   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -   -
rem If there were no errors, we run the post script.
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

if "%HOME%"=="" goto homeDrivePathPost
if exist "%HOME%\cdx_post.bat" call "%HOME%\cdx_post.bat"

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:homeDrivePathPost
if "%HOMEDRIVE%%HOMEPATH%"=="" goto userProfilePost
if "%HOMEDRIVE%%HOMEPATH%"=="%HOME%" goto userProfilePost
if exist "%HOMEDRIVE%%HOMEPATH%\cdx_post.bat" call "%HOMEDRIVE%%HOMEPATH%\cdx_post.bat"

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:userProfilePost
if "%USERPROFILE%"=="" goto omega
if "%USERPROFILE%"=="%HOME%" goto omega
if "%USERPROFILE%"=="%HOMEDRIVE%%HOMEPATH%" goto omega
if exist "%USERPROFILE%\cdx_post.bat" call "%USERPROFILE%\cdx_post.bat"

@rem - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
:omega

exit /b %ERRORLEVEL%
