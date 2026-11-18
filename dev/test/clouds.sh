#!/usr/bin/env bash
# Dev-only: the named stars under the clouds. Runs dev/test/marker-shaders.sh with the game's own clouds switched on in
# the harness's options (Complementary draws its own either way), then puts the option back.
set -u
OPT=run/clienttest/options.txt
cp "$OPT" "$OPT.keep"
sed -i 's/^renderClouds:.*/renderClouds:"true"/' "$OPT"
SHOTS="${SHOTS:-40,800}" bash dev/test/marker-shaders.sh "${1:-$(date +%F) stars under clouds}"
mv "$OPT.keep" "$OPT"
