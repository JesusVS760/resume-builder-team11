@echo off
setlocal

echo Cleaning build...
rmdir /s /q build 2>nul
mkdir build

echo Finding sources...
dir /s /b src\*.java > sources.txt

echo Compiling all sources...
javac -encoding UTF-8 -d build -cp "lib/*" @sources.txt
if errorlevel 1 (
  echo.
  echo *** Compile failed. See errors above. ***
  exit /b 1
)

echo Running application...
java --add-opens java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -cp "build;lib/*" Main

endlocal
