#!/usr/bin/env bash
set -euo pipefail

SRC="src"
OUT="build"

echo "=== Clean ==="
rm -rf "$OUT"
mkdir -p "$OUT"

echo "=== Find sources ==="
find "$SRC" -name "*.java" > sources.txt

echo "=== Compile ==="
javac -encoding UTF-8 -d "$OUT" -cp "lib/*" @sources.txt

echo "=== Copy resources ==="
# images for classpath lookups like "/ui/images/..."
if [ -d "$SRC/ui/images" ]; then
  mkdir -p "$OUT/ui/images"
  cp -R "$SRC/ui/images/"* "$OUT/ui/images/" 2>/dev/null || true
fi

echo "=== Run ==="
# If Main is in a package, replace Main with your fully-qualified name.
exec java -Dfile.encoding=UTF-8 -cp "$OUT:lib/*" Main
