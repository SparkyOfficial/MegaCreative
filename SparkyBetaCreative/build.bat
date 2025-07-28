echo off
cd /d %~dp0
rem Show all commands and their output.
echo =================================================================
echo  SparkyBetaCreative Build Script
echo =================================================================
echo.
echo Starting Maven build...
echo.

rem Directly call the mvn.cmd and enable verbose error reporting.
call "C:\apache-maven-3.9.10\bin\mvn.cmd" -e -X clean package

echo.
echo =================================================================
echo Build finished.
echo The final .jar file can be found in the 'target' folder.
echo =================================================================
echo.
echo Press any key to exit.
pause 