#!/bin/bash
set -e  # Stop on first error

echo "Cleaning previous builds..."
rm -rf build
mkdir -p build

echo "Compiling all Java files..."
# Find and compile every .java file in src, regardless of package or missing files
find src -name "*.java" > sources.txt
javac -d build -cp "lib/*" @sources.txt
rm sources.txt

echo "Running application..."
java --add-opens java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -cp "build:lib/*" Main
