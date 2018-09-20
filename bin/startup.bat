@echo off
set argC=0
for %%x in (%*) do Set /A argC+=1

SETLOCAL ENABLEDELAYEDEXPANSION
@set basedir="%~dp0"
pushd %basedir%

@set mainclass=com.xjj.tools.bigdata.tunnel.ConsoleMain
@set libpath=%cd%\..\libs\
@set classpath=.;!libpath!\*

popd
java -Xms64m -Xmx512m %java9opt% -classpath "!classpath!" %mainclass% %*
if errorlevel 1 (
  if %argC% == 0 (
   PAUSE
  )
)

ENDLOCAL