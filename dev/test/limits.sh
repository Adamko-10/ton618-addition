#!/usr/bin/env bash
# Dev-only: TA-02 cast at Power 3 on a server whose max_power is 2 (the cast should be held to 2: "the landing blast
# (600 blocks across)" for Alpha Centauri A in the log), the debris thrown over a few ticks. Puts the harness's server config
# back afterwards.
set -u
CFG=run/clienttest/config/ton618_addition-common.toml
cp "$CFG" "$CFG.keep"
sed -i 's/^\[quasar_jets\]/[quasar_jets]\n\tmax_power = 2.0/' "$CFG"
ONLY=nopack SHOTS="${SHOTS:-800,840}" EXTRA="-Dton618test.power=3 -Dton618test.star=${STAR:-2} -Dton618test.distance=${DISTANCE:-110}" bash dev/test/marker-shaders.sh "${1:-$(date +%F) limits}"
grep -n "landing blast\|holds\|Can't keep up" run/clienttest/nopack.log
mv "$CFG.keep" "$CFG"
