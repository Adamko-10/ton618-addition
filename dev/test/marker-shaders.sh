#!/usr/bin/env bash
# Dev-only: two harness runs of TA-02 with films off (the camera stays on the land, so the landing marker is in view),
# first with Complementary on (Iris + Sodium from libs/shaders), then without. Screenshots are copied to
# "../test builds/<out>/pack" and ".../nopack". Run from the repo root with JAVA_HOME set; Minecraft must be closed.
set -u
OUT="${1:-$(date +%F) marker and shaders}"
SHOTS="${SHOTS:-40,300,650,740,780,792,800,812,840}"
DEST="../test builds/$OUT"
CFG=run/clienttest/config
cp "$CFG/rimulib.properties" "$CFG/rimulib.properties.keep"
cp "$CFG/iris.properties" "$CFG/iris.properties.keep"
sed -i 's/^cutscenes=.*/cutscenes=false/' "$CFG/rimulib.properties"

run() {
    local name="$1" shaders="$2"
    sed -i "s/^enableShaders=.*/enableShaders=$shaders/" "$CFG/iris.properties"
    rm -rf run/clienttest/screenshots
    # EXTRA: more harness settings, e.g. EXTRA="-Dton618test.star=0 -Dton618test.distance=200"
    ./gradlew --no-daemon runClientTest -Pshaders "-Dton618test.shots=$SHOTS" ${EXTRA:-} > "run/clienttest/$name.log" 2>&1
    grep "TON618TEST\|Exception\|ERROR" "run/clienttest/$name.log" | grep -v "mixin/\]" | head -40
    mkdir -p "$DEST/$name"
    cp run/clienttest/screenshots/*.png "$DEST/$name/" 2>/dev/null
    echo "$name: $(ls "$DEST/$name" | wc -l) screenshots"
}

# ONLY=pack or ONLY=nopack runs one of the two
[ "${ONLY:-pack}" = pack ] || [ -z "${ONLY:-}" ] && run pack true
[ "${ONLY:-nopack}" = nopack ] && run nopack false

mv "$CFG/rimulib.properties.keep" "$CFG/rimulib.properties"
mv "$CFG/iris.properties.keep" "$CFG/iris.properties"
