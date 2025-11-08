@echo off
setlocal

set SRC=src
set OUT=build

echo === Clean ===
rmdir /s /q "%OUT%" 2>nul
mkdir "%OUT%" 2>nul

echo === Find sources ===
dir /s /b "%SRC%\*.java" > sources.txt

echo === Compile ===
javac -encoding UTF-8 -d "%OUT%" -cp "lib/*" @sources.txt
if errorlevel 1 (
  echo.
  echo *** Compile failed. See errors above. ***
  exit /b 1
)

echo === Copy resources ===
REM images for classpath lookups like "/ui/images/..."
if exist "%SRC%\ui\images" (
  xcopy /E /I /Y "%SRC%\ui\images" "%OUT%\ui\images" >nul
)

echo === Run ===
REM If Main is in a package, replace Main with your fully-qualified name.
java -Dfile.encoding=UTF-8 -cp "%OUT%;lib/*" Main

endlocal
