@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  baiguotongIM startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and BAIGUOTONG_IM_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

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

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\baiguotongIM-1.0-SNAPSHOT.jar;%APP_HOME%\lib\agora_signal.jar;%APP_HOME%\lib\commons-codec-1.9.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\fastdfs-client-java-1.25.jar;%APP_HOME%\lib\grizzly-framework-2.3.25.jar;%APP_HOME%\lib\grizzly-http-2.3.25.jar;%APP_HOME%\lib\grizzly-http-server-2.3.25.jar;%APP_HOME%\lib\gson-2.8.0.jar;%APP_HOME%\lib\hamcrest-core-1.1.jar;%APP_HOME%\lib\httpclient-4.5.3.jar;%APP_HOME%\lib\httpcore-4.4.6.jar;%APP_HOME%\lib\javax.websocket-api-1.1.jar;%APP_HOME%\lib\json-simple-1.1.1.jar;%APP_HOME%\lib\slf4j-api-1.7.25.jar;%APP_HOME%\lib\slf4j-simple-1.7.25.jar;%APP_HOME%\lib\tyrus-client-1.13.1.jar;%APP_HOME%\lib\tyrus-container-grizzly-client-1.13.1.jar;%APP_HOME%\lib\tyrus-core-1.13.1.jar;%APP_HOME%\lib\tyrus-spi-1.13.1.jar;%APP_HOME%\lib\tyrus-standalone-client-jdk-1.13.1.jar;%APP_HOME%\lib\vertx-web-3.5.4.jar;%APP_HOME%\lib\vertx-redis-client-3.5.2.CR3.jar;%APP_HOME%\lib\vertx-auth-jdbc-3.5.4.jar;%APP_HOME%\lib\vertx-rabbitmq-client-3.6.0.jar;%APP_HOME%\lib\vertx-auth-common-3.5.4.jar;%APP_HOME%\lib\vertx-core-3.6.0.jar;%APP_HOME%\lib\vertx-mysql-postgresql-client-3.5.4.jar;%APP_HOME%\lib\slf4j-simple-1.7.12.jar;%APP_HOME%\lib\amqp-client-5.4.2.jar;%APP_HOME%\lib\slf4j-api-1.7.25.jar;%APP_HOME%\lib\log4j-1.2.17.jar;%APP_HOME%\lib\jackson-databind-2.9.7.jar;%APP_HOME%\lib\jackson-core-2.9.7.jar;%APP_HOME%\lib\jackson-annotations-2.9.0.jar;%APP_HOME%\lib\mysql-connector-java-5.1.33.jar;%APP_HOME%\lib\netty-codec-http2-4.1.30.Final.jar;%APP_HOME%\lib\netty-handler-4.1.30.Final.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.30.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.1.30.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.30.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.1.30.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.1.30.Final.jar;%APP_HOME%\lib\netty-codec-4.1.30.Final.jar;%APP_HOME%\lib\netty-transport-4.1.30.Final.jar;%APP_HOME%\lib\netty-buffer-4.1.30.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.30.Final.jar;%APP_HOME%\lib\netty-common-4.1.30.Final.jar;%APP_HOME%\lib\vertx-bridge-common-3.5.4.jar;%APP_HOME%\lib\vertx-jdbc-client-3.5.4.jar;%APP_HOME%\lib\vertx-sql-common-3.5.4.jar;%APP_HOME%\lib\postgresql-async_2.12-0.2.21.jar;%APP_HOME%\lib\mysql-async_2.12-0.2.21.jar;%APP_HOME%\lib\db-async-common_2.12-0.2.21.jar;%APP_HOME%\lib\scala-library-2.12.4.jar;%APP_HOME%\lib\c3p0-0.9.5.2.jar;%APP_HOME%\lib\joda-time-2.9.7.jar;%APP_HOME%\lib\joda-convert-1.8.1.jar;%APP_HOME%\lib\netty-all-4.1.6.Final.jar;%APP_HOME%\lib\javassist-3.21.0-GA.jar;%APP_HOME%\lib\mchange-commons-java-0.2.11.jar

@rem Execute baiguotongIM
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %BAIGUOTONG_IM_OPTS%  -classpath "%CLASSPATH%" io.vertx.core.Launcher %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable BAIGUOTONG_IM_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%BAIGUOTONG_IM_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
