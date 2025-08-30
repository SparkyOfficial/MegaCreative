@echo off
echo Cleaning up build artifacts...

REM Delete Maven target directories
if exist "target" rmdir /s /q "target"
if exist "build" rmdir /s /q "build"

REM Clean Maven project
echo Running Maven clean...
call mvn clean

REM Delete any remaining class files
del /s /q *.class

REM Clean Maven dependencies
echo Cleaning Maven dependencies...
call mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false

REM Update Maven project
echo Updating Maven project...
call mvn clean install -U

echo Done! All build artifacts have been cleaned and the project has been rebuilt.
pause
