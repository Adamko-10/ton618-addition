#!/usr/bin/env bash
# TA-02 · Quasar Jets' sounds from the Sonniss GDC 2026 game-audio bundle (Adam, 2026-11-16: "severe lack of sfx"; his
# pick: the Sonniss library), to Fable's cue sheet `previz/quasar-jets/SOUND-CUES.md` (names, ticks, lengths, what each
# should sound like). The engine is the SA Series' `art/sonniss_sounds.sh` (the same bundle, the same rules): each sound
# is a recipe of layers cut from the bundle's files, each brought to -20 LUFS first (so a layer's gain means the same for
# every source), then its own gain, start and filters; mixed, cut to its length, set to its loudness and written as Ogg
# Vorbis: mono for the world's sounds (Minecraft only places mono sounds), stereo for the film's. Loops are rendered a
# little longer and their tail crossfaded into their head, so they repeat without a seam.
# The layers were picked by measuring the sources (the SA project folder's `sound library\envelope.js`), not by ear:
# every result needs Adam's ears in game.
#
# Usage: bash art/sonniss_sounds.sh <out dir> [sound names...]
#   <out dir>: src/main/resources/assets/ton618_addition/sounds/quasar (or a listening folder). Names pick sounds (a
#   base name takes its variants: quasar_crystal = quasar_crystal, _2, _3, _4); none = all, which also rewrites
#   art/sounds/sources.md (which Sonniss file went into which sound, so a pick can be swapped out).
# The bundle stays outside the repo (only our mixes ship); SONNISS=<the bundle's folder> to point elsewhere.
# Needs ffmpeg (with libvorbis), node and awk.
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
BUNDLE="${SONNISS:-C:/Users/adam_/Documents/Shooting Star Addition/sound library/sonniss gdc 2026}"
LIB="$BUNDLE/files"
[ $# -ge 1 ] || { echo "usage: bash art/sonniss_sounds.sh <out dir> [sound names...]" >&2; exit 2; }
OUT=$1; shift
NAMES=("$@")
mkdir -p "$OUT"
[ -d "$LIB" ] || { echo "no Sonniss bundle at $LIB (set SONNISS)" >&2; exit 1; }
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
SR=44100
CH=1
LOOP=0
: > "$TMP/sources"

ff() { ffmpeg -hide_banner -loglevel error -y "$@"; }
calc() { awk "BEGIN{printf \"%.3f\", $1}"; }
# loudness <file>: its LUFS; a sound too short to gate (under ~0.4 s reads -70) is measured by its mean level instead
loudness() {
  local i
  i=$(ffmpeg -hide_banner -nostats -i "$1" -af ebur128 -f null - 2>&1 | awk '/^ *I:/{v=$2} END{print v}')
  if awk -v i="$i" 'BEGIN{exit !(i == "" || i + 0 <= -69)}'; then
    i=$(ffmpeg -hide_banner -nostats -i "$1" -af volumedetect -f null - 2>&1 | awk '/mean_volume:/{v=$5} END{printf "%.1f", v - 1}')
  fi
  echo "$i"
}
length() { ffprobe -v error -show_entries format=duration -of csv=p=0 "$1"; }
# rate <k>: tape-style pitch and speed by k (0.5 = an octave down and twice as long)
rate() { awk -v k="$1" -v r=$SR 'BEGIN{printf "asetrate=%d,aresample=%d", r * k + 0.5, r}'; }
want() {
  [ ${#NAMES[@]} -eq 0 ] && return 0
  local n; for n in "${NAMES[@]}"; do [ "$n" = "$1" ] && return 0; done; return 1
}

# The bundle's files by catalogue row (rows of catalogue.csv, 1 = first file): CAT[n] = its path in the files folder.
mapfile -t CAT < <(node -e '
const fs = require("fs");
const rows = fs.readFileSync(process.argv[1], "utf8").trim().split(/\r?\n/).slice(1);
console.log("");
for (const l of rows) { const o = []; let c = "", q = false;
  for (const ch of l) { if (ch === "\"") { q = !q; continue } if (ch === "," && !q) { o.push(c); c = ""; continue } c += ch }
  o.push(c); console.log(o[0].replace(/\\/g, "/")); }' "$BUNDLE/catalogue.csv")

# level <file> <start> <length>: sets LEVEL to the cut's loudness in LUFS (a cut too short to measure: its peak - 12)
declare -A LEVELS
level() {
  local key="$1|$2|$3" out i p
  if [ -z "${LEVELS[$key]:-}" ]; then
    out=$(ffmpeg -hide_banner -nostats -ss "$2" -t "$3" -i "$1" -af "aformat=channel_layouts=mono,ebur128=peak=sample" -f null - 2>&1)
    i=$(awk '/^ *I:/{v=$2} END{print v}' <<< "$out")
    p=$(awk '/^ *Peak:/{v=$2} END{print v}' <<< "$out")
    LEVELS[$key]=$(awk -v i="$i" -v p="$p" 'BEGIN{ if (i == "" || i + 0 <= -69) i = p - 12; if (i < -60) i = -60; printf "%.2f", i }')
  fi
  LEVEL=${LEVELS[$key]}
}

# L <source> <from s> <length s> <gain dB> <at s> [filters]: one layer of the next sound.
LAYERS=()
L() { LAYERS+=("$1~$2~$3~$4~$5~${6:-}"); }

# finish <wav> <name> <LUFS>: to its level, peaks held under -1 dB, Ogg Vorbis ($CH channels); up to two more passes
# make up what the limiter took.
finish() {
  local gain short
  gain=$(awk -v t="$3" -v i="$(loudness "$1")" 'BEGIN{printf "%.2f", t - i}')
  for pass in 1 2 3; do
    ff -i "$1" -af "volume=${gain}dB,alimiter=limit=0.89:attack=3:release=80:level=0" -ac $CH -ar $SR -c:a libvorbis -q:a 5 "$OUT/$2.ogg"
    short=$(awk -v t="$3" -v i="$(loudness "$OUT/$2.ogg")" 'BEGIN{d = t - i; if (d > 3) d = 3; printf "%.2f", d}')
    awk -v s="$short" 'BEGIN{exit !(s > 0.3)}' || break
    gain=$(awk -v g="$gain" -v s="$short" 'BEGIN{printf "%.2f", g + s}')
  done
  printf '%-28s %6s LUFS (aimed %s)  %6ss  %sch%s\n' "$2" "$(loudness "$OUT/$2.ogg")" "$3" "$(length "$OUT/$2.ogg" | cut -c1-5)" "$CH" "$([ "$LOOP" != 0 ] && echo ' loop')"
}

# loopify <in> <out> <length> <crossfade>: the first <length> s, with the next <crossfade> s faded into its start
loopify() {
  ff -i "$1" -filter_complex "[0:a]asplit[a][b];[a]atrim=0:$3,asetpts=N/SR/TB,afade=t=in:d=$4:curve=qsin[h];[b]atrim=$3:$(calc "$3 + $4"),asetpts=N/SR/TB,afade=t=out:d=$4:curve=qsin[t];[h][t]amix=inputs=2:normalize=0:duration=first[o]" -map "[o]" -c:a pcm_f32le "$2"
}

# make <name> <length s> <LUFS> [filters on the mix]: mixes the layers given since the last make into <name>.ogg.
# CH=2 makes it stereo (widened); LOOP=<crossfade s> makes it a seamless loop of <length>.
make() {
  local name=$1 len=$2 lufs=$3 post=${4:-} args=() graph="" layers="" labels="" n=0 m=0 i u spec file start dur gain at
  local filt norm path key splits total ending widen=""
  local -A input uses
  for spec in "${LAYERS[@]}"; do
    IFS='~' read -r file start dur gain at filt <<< "$spec"
    case $file in /*|?:*) path=$file ;; *) path="$LIB/$file" ;; esac
    [ -f "$path" ] || { echo "$name: no source $path" >&2; exit 1; }
    key="$path|$start|$dur"
    if [ -z "${input[$key]:-}" ]; then
      input[$key]=$m; uses[$m]=0; m=$((m + 1))
      args+=(-ss "$start" -t "$dur" -i "$path")
    fi
    i=${input[$key]}; u=${uses[$i]}; uses[$i]=$((u + 1))
    level "$path" "$start" "$dur"
    norm=$(awk -v l="$LEVEL" -v g="$gain" 'BEGIN{printf "%.2f", -20 - l + g}')
    layers+="[c${i}_$u]afade=t=in:d=0.004,afade=t=out:st=$(calc "$dur - 0.012"):d=0.012${filt:+,$filt},volume=${norm}dB,adelay=delays=$(awk -v a="$at" -v r=$SR 'BEGIN{printf "%d", a * r + 0.5}')S:all=1[l$n];"
    labels+="[l$n]"; n=$((n + 1))
    printf '| %s | %s | %s | %s | %s | %s | %s |\n' "$name" "${file//|/\\|}" "$start" "$dur" "$gain" "$at" "$(sed 's/|/\\|/g' <<< "${filt:-}")" >> "$TMP/sources"
  done
  for ((i = 0; i < m; i++)); do
    splits=""; for ((u = 0; u < uses[$i]; u++)); do splits+="[c${i}_$u]"; done
    graph+="[$i:a]aformat=sample_fmts=fltp:channel_layouts=mono,aresample=$SR,asetpts=N/SR/TB,asplit=${uses[$i]}$splits;"
  done
  [ "$CH" = 2 ] && widen=",pan=stereo|c0=c0|c1=c0,stereowiden=delay=14:feedback=0.2:crossfeed=0.25:drymix=0.9"
  if [ "$LOOP" != 0 ]; then
    total=$(calc "$len + $LOOP"); ending=""
  else
    total=$len; ending=",afade=t=out:st=$(calc "$len - 0.015"):d=0.015"
  fi
  graph+="$layers${labels}amix=inputs=$n:normalize=0:duration=longest,asetpts=N/SR/TB,highpass=f=22${post:+,$post}$widen,apad=whole_dur=$total,atrim=end_sample=$(awk -v l="$total" -v r=$SR 'BEGIN{printf "%d", l * r + 0.5}')$ending[out]"
  printf '%s' "$graph" > "$TMP/graph.txt"
  ff "${args[@]}" -/filter_complex "$TMP/graph.txt" -map "[out]" -c:a pcm_f32le "$TMP/$name.wav"
  if [ "$LOOP" != 0 ]; then
    loopify "$TMP/$name.wav" "$TMP/$name.loop.wav" "$len" "$LOOP"
    mv "$TMP/$name.loop.wav" "$TMP/$name.wav"
  fi
  finish "$TMP/$name.wav" "$name" "$lufs"
  LAYERS=()
}

# ---- The sources (catalogue rows) ---------------------------------------------------------------------------------
BLASTOFF=${CAT[1]};      BOOKS=${CAT[11]};        CLOCK_CROOKED=${CAT[12]}; CLOCK=${CAT[13]};   TRIMMER=${CAT[31]}
DROP_FAST16=${CAT[32]};  DROP_SLOW10=${CAT[34]};  JUMPSTART=${CAT[38]};   TONEDOWN=${CAT[40]};  PUNCH=${CAT[57]}
ROAR=${CAT[65]};         BIRDS=${CAT[68]};        COIL_HUM=${CAT[69]};    WATER_BUBBLE=${CAT[71]}; WIND_RUSH=${CAT[72]}
STORM_EYE=${CAT[74]};    SWORD=${CAT[82]};        ICE_CRUSH=${CAT[83]};   ICE_SNAP=${CAT[84]};  BIRD_FLUTE=${CAT[92]}
KALIMBA=${CAT[93]};      PULL_SWOOSH=${CAT[96]};  WOOSH_DEBRIS=${CAT[106]}; GLASSY_SNAP=${CAT[110]}; SCIFI_PING=${CAT[121]}
TING=${CAT[124]};        CHORD_REVERB=${CAT[125]}; BRAAM_CREEPY=${CAT[126]}; WOOSH_SWEEP=${CAT[127]}; TWINKLE=${CAT[128]}
METAL_RING=${CAT[132]};  WHIP=${CAT[134]};        POWERUP_SHIMMER=${CAT[144]}; FIRE_WHOOSH=${CAT[152]}; GLASS_SHARDS=${CAT[153]}
ELEC_IMPACT=${CAT[151]}
MAGIC_LIGHT=${CAT[159]}; SCIFI_BURST=${CAT[160]}; GORE_SMASH=${CAT[163]}; METAL_TAP=${CAT[167]}; LATCH=${CAT[170]}
TAPE=${CAT[171]};        SPRING=${CAT[172]};      REACTOR=${CAT[197]};    SHIMMER=${CAT[198]};  FIRE_LOOP=${CAT[201]}
WIND_WHIP=${CAT[202]};   ICE_SHATTER=${CAT[204]}; BOOM011=${CAT[210]};    BOOM075=${CAT[211]};  BOOM214=${CAT[212]}
ARC=${CAT[213]};         ARC_POWERUP=${CAT[214]}; BUZZ=${CAT[215]};       CHIMNEY=${CAT[220]};  CLOTH=${CAT[225]}
CASE_DOWN=${CAT[229]};   BEEP=${CAT[232]};        PIPE=${CAT[245]};       DEATH_DRONE=${CAT[249]}; CAMPFIRE=${CAT[258]}
FIRE_OUT=${CAT[259]};    BELLS=${CAT[261]};       HORN32=${CAT[267]};     FRIDGE=${CAT[269]};   THUNDER=${CAT[273]}
STORM_CRASH=${CAT[186]}; GRAVEL=${CAT[305]};      HURRICANE=${CAT[329]};  METAL_BOWED=${CAT[331]}; METAL_SCRAPE=${CAT[332]}
# the trailer booms' hit is 0.25 s in; the storm's thunder crash starts 3.0 s in
B=0.2
CRASH=3.0

# ======================================================================================================================
# The world's sounds (mono; the server plays them at the mark, QuasarJets.sounds)
# ======================================================================================================================
CH=1; LOOP=0

if want quasar_lock; then   # press 1, the caster only: two rising electronic blips and a soft click
  L "$KALIMBA" 0 0.3 0 0
  L "$KALIMBA" 0 0.3 0 0.13 "$(rate 1.335)"
  L "$BEEP" 0 0.12 -5 0
  L "$BEEP" 0 0.12 -5 0.13 "$(rate 1.335)"
  L "$LATCH" 0 0.2 -7 0.3
  make quasar_lock 0.5 -16
fi

if want quasar_mark; then   # tick 0: a soft sub thud with a glassy ping on top
  L "$DROP_FAST16" 0 0.8 0 0 "lowpass=f=200"
  L "$GLASSY_SNAP" 0 0.44 -2 0.02 "$(rate 0.8)"
  L "$SCIFI_PING" 0 0.6 -7 0.03
  make quasar_mark 0.8 -15 "afade=t=out:st=0.5:d=0.3"
fi

if want quasar_sun_ignite; then   # DAY, to everyone at their own place: a far, sky-wide low boom, then an airy shimmer rising
  L "$BOOM075" $B 3.0 -1 0 "lowpass=f=300"
  L "$MAGIC_LIGHT" 0 2.6 -3 0.3 "afade=t=in:d=0.8"
  L "$SHIMMER" 2 2.6 -4 0.4 "afade=t=in:d=1.2,highpass=f=1500"
  L "$WIND_RUSH" 0 1.05 -8 0.2 "$(rate 0.5)"
  make quasar_sun_ignite 3.0 -16 "afade=t=out:st=2.3:d=0.7"
fi

if want quasar_jet_incoming; then   # SWEEP, exactly 2.5 s, peaking at its end: a tearing roar racing in
  L "$BLASTOFF" 6.2 2.5 0 0 "volume='pow(t/2.5\,2.2)':eval=frame,highpass=f=60"
  L "$BOOM214" $B 2.5 -3 0 "areverse,lowpass=f=900"
  L "$FIRE_WHOOSH" 0 2.4 -5 0 "areverse,$(rate 1.3)"
  L "$WIND_RUSH" 0 1.05 -4 1.45 "volume='t/1.05':eval=frame"
  make quasar_jet_incoming 2.5 -12
fi

# LANDS (Adam, 2026-11-17: "improve sfx especially at impact"): a quasar's plasma hitting the Earth, not a bomb. Each
# player gets both, mixed by distance and pitched by the star (QuasarJets.impactSound), the far one after the speed of
# sound's delay.
if want quasar_impact_near; then   # near: an electric zap and crack, the deep electric impact, trailer booms, a sub drop,
  # arcs crackling, debris, a rolling rumble and a fizzling tail
  L "$SCIFI_BURST" 0 0.31 -2 0
  L "$ELEC_IMPACT" 0 3.69 0 0
  L "$STORM_CRASH" $CRASH 2.0 -2 0
  L "$BOOM011" $B 6.0 -1 0
  L "$BOOM214" $B 6.0 -4 0.05
  L "$DROP_SLOW10" 0 6.0 -3 0
  L "$ARC" 0 1.04 -8 0.55 "highpass=f=1500"
  L "$ARC" 0 1.04 -10 1.3 "highpass=f=1500,$(rate 0.8)"
  L "$WOOSH_DEBRIS" 0.3 2.0 -7 0.35
  L "$THUNDER" 6 6.0 -4 0.9 "afade=t=in:d=0.8,lowpass=f=700"
  L "$BUZZ" 1 3.5 -14 1.6 "highpass=f=2000,volume='exp(-0.9*t)':eval=frame"
  make quasar_impact_near 6.5 -13 "volume='exp(-0.45*t)':eval=frame,afade=t=out:st=5.3:d=1.2"
fi
if want quasar_impact_far; then   # far: only the deep part, muffled by the distance, and longer
  L "$BOOM075" $B 7.0 0 0 "lowpass=f=400"
  L "$BOOM011" $B 7.0 -2 0 "lowpass=f=250"
  L "$ELEC_IMPACT" 0 3.69 -6 0 "lowpass=f=300"
  L "$DROP_SLOW10" 0 7.0 -3 0 "lowpass=f=200"
  L "$THUNDER" 6 7.0 -1 0.3 "afade=t=in:d=0.4,lowpass=f=500"
  make quasar_impact_far 7.5 -14 "volume='exp(-0.3*t)':eval=frame,afade=t=out:st=6.0:d=1.5"
fi

if want quasar_debris; then   # LANDS + 6: rock and dirt raining back, heavy chunks first, then gravel and dust
  L "$WOOSH_DEBRIS" 0 2.38 0 0
  L "$BOOKS" 0 2.8 -2 0.3 "$(rate 0.7)"
  L "$CASE_DOWN" 0 1.4 -3 0.1 "$(rate 0.6)"
  L "$ICE_CRUSH" 0 2.8 -5 0.05 "$(rate 0.55)"
  L "$GRAVEL" 3 3.2 -4 0.4 "lowpass=f=3000"
  make quasar_debris 3.5 -14 "afade=t=out:st=2.6:d=0.9"
fi

LOOP=0.5
if want quasar_beam_loop; then   # LANDS to POP, looped: the jet standing on the ground, a deep hum with an electric fizz
  L "$FRIDGE" 30 3.5 0 0 "lowpass=f=400"
  L "$BUZZ" 2 3.5 -3 0
  L "$REACTOR" 10 3.5 -3 0
  L "$ARC_POWERUP" 2 3.5 -8 0 "highpass=f=2000"
  make quasar_beam_loop 3.0 -16
fi
if want quasar_lobe_loop; then   # LOBE to POP, looped: a crackling, bubbling plasma drone (a tesla coil under water)
  L "$DEATH_DRONE" 3 1.75 -2 0 "$(rate 0.5)"
  L "$FIRE_LOOP" 3 3.5 -4 0 "lowpass=f=1200"
  L "$WATER_BUBBLE" 0 1.21 -5 0 "$(rate 0.6)"
  L "$WATER_BUBBLE" 0 1.21 -6 1.7 "$(rate 0.5)"
  L "$ARC" 0 1.04 -6 0.3
  L "$ARC" 0 1.04 -7 1.9 "$(rate 0.8)"
  L "$COIL_HUM" 10 3.5 -10 0
  make quasar_lobe_loop 3.0 -16
fi
LOOP=0

if want quasar_lobe_rise; then   # POP - 20, exactly 1 s, peaking at its end: a fast rising pressure whine (a suck-in)
  L "$TONEDOWN" 0.05 1.0 0 0 "areverse"
  L "$WIND_RUSH" 0 1.0 -2 0 "areverse"
  L "$BOOM075" $B 1.0 -4 0 "areverse,lowpass=f=1500"
  L "$ARC_POWERUP" 4.5 1.0 -6 0 "volume='pow(t\,3)':eval=frame"
  make quasar_lobe_rise 1.0 -13
fi

if want quasar_pop; then   # POP: one sharp thunderclap, a rush of air blowing out, a sub drop; round, not an explosion
  L "$STORM_CRASH" $CRASH 1.5 0 0 "highpass=f=200"
  L "$WHIP" 0 0.6 -3 0
  L "$DROP_FAST16" 0 2.1 -1 0 "lowpass=f=180"
  L "$HURRICANE" 25 3.0 -4 0.03 "volume='min(1\,t/0.05)*exp(-0.9*t)':eval=frame"
  L "$WIND_RUSH" 0 1.05 -5 0.05
  make quasar_pop 3.2 -11.5 "volume='exp(-0.4*t)':eval=frame,afade=t=out:st=2.6:d=0.6"
fi

if want quasar_crystal; then   # each crystal growing (four takes): a glassy chime with an ice-crack attack
  L "$ICE_SNAP" 0 0.25 -2 0 "$(rate 1.4)"; L "$GLASSY_SNAP" 0 0.44 0 0.01; L "$TING" 0 0.6 -4 0.02
  make quasar_crystal 0.6 -17 "afade=t=out:st=0.4:d=0.2"
  L "$ICE_SHATTER" 0 0.3 -3 0 "$(rate 1.5)"; L "$GLASSY_SNAP" 0 0.44 0 0.01 "$(rate 1.2)"; L "$KALIMBA" 0 0.5 -5 0.02 "$(rate 2)"
  make quasar_crystal_2 0.6 -17 "afade=t=out:st=0.4:d=0.2"
  L "$ICE_CRUSH" 0 0.2 -4 0 "$(rate 2)"; L "$TWINKLE" 0 0.6 -2 0.01; L "$GLASSY_SNAP" 0 0.44 -3 0 "$(rate 1.5)"
  make quasar_crystal_3 0.6 -17 "afade=t=out:st=0.4:d=0.2"
  L "$ICE_SNAP" 0.1 0.25 -2 0 "$(rate 1.7)"; L "$SHIMMER" 5 0.6 -2 0.01; L "$TING" 0 0.6 -5 0.03 "$(rate 0.8)"
  make quasar_crystal_4 0.6 -17 "afade=t=out:st=0.4:d=0.2"
fi

if want quasar_lift; then   # each thing picked up by the jet: a soft upward fwip
  L "$WOOSH_SWEEP" 0 0.3 0 0 "$(rate 1.4)"
  L "$CLOTH" 0.2 0.3 -6 0
  make quasar_lift 0.3 -18
fi

if want quasar_night; then   # NIGHT: the second sun going out, a long soft falling sigh with a little reverb
  L "$TONEDOWN" 0 2.5 -2 0 "$(rate 1.6)"
  L "$WIND_RUSH" 0 1.05 -4 0 "$(rate 0.5),lowpass=f=1500"
  L "$MAGIC_LIGHT" 0 2.5 -9 0 "lowpass=f=3000"
  make quasar_night 2.5 -17 "aecho=0.8:0.6:120|240:0.35|0.2,afade=t=out:st=1.8:d=0.7"
fi

# ======================================================================================================================
# The film's sounds (stereo; the film plays them, not positioned: client/quasarfilm/JetCues)
# ======================================================================================================================
CH=2; LOOP=0

if want quasar_film_reticle; then   # 24: targeting ticks closing in, each a little faster and higher
  L "$BEEP" 0 0.1 0 0;    L "$BEEP" 0 0.1 0 0.17 "$(rate 1.06)"; L "$BEEP" 0 0.1 0 0.31 "$(rate 1.12)"
  L "$BEEP" 0 0.1 0 0.43 "$(rate 1.19)"; L "$BEEP" 0 0.1 0 0.53 "$(rate 1.26)"; L "$BEEP" 0 0.1 0 0.61 "$(rate 1.335)"
  make quasar_film_reticle 0.8 -16
fi

if want quasar_film_lock; then   # 40: lock confirmed, a clean techy two-note chime
  L "$KALIMBA" 0 0.55 0 0
  L "$KALIMBA" 0 0.55 -1 0.14 "$(rate 1.5)"
  L "$GLASSY_SNAP" 0 0.44 -4 0.14
  make quasar_film_lock 0.7 -15 "aecho=0.8:0.5:90:0.25"
fi

if want quasar_film_zoom; then   # 58, 1.1 s, peaking at its end: a whoosh sucking in toward the star, a dull thump
  L "$WIND_RUSH" 0 1.05 0 0 "areverse"
  L "$BOOM075" $B 1.0 -3 0 "areverse,lowpass=f=1200"
  L "$PUNCH" 0 0.3 -2 0.98 "lowpass=f=400"
  make quasar_film_zoom 1.1 -14
fi

if want quasar_film_mirror; then   # 92: the mirror unfolding, panels sliding and clicking open, a metallic ring building
  L "$TAPE" 0 2.9 0 0
  L "$SPRING" 0 2.7 -3 0.2
  for t in 0.3 0.7 1.05 1.35 1.6 1.85 2.05 2.2; do L "$LATCH" 0 0.3 -4 "$t" "$(rate "$(calc "0.8 + $t * 0.2")")"; done
  for t in 0.5 1.2 1.9; do L "$METAL_TAP" 0 0.47 -8 "$t" "$(rate 0.8)"; done
  L "$SHIMMER" 0 2.9 -6 0 "afade=t=in:d=2.5"
  L "$METAL_BOWED" 9.9 1.9 -10 0 "$(rate 1.5),afade=t=in:d=1.0"
  make quasar_film_mirror 2.9 -16 "afade=t=out:st=2.5:d=0.4"
fi

if want quasar_film_push; then   # 150: the fast-forward taking hold, a deep sub thump and a whoosh pulling away
  L "$DROP_FAST16" 0 2.0 0 0
  L "$PULL_SWOOSH" 0 2.0 -4 0.1
  L "$WIND_RUSH" 0 1.05 -3 0.05 "$(rate 0.8)"
  make quasar_film_push 2.0 -13 "afade=t=out:st=1.5:d=0.5"
fi

if want quasar_film_arrive; then   # 330: the cut to TON618, one low heavy dark hit with a long tail
  L "$HORN32" 0.1 2.5 0 0
  L "$BRAAM_CREEPY" 0 2.5 -3 0
  L "$BOOM075" $B 2.5 -2 0 "lowpass=f=500"
  L "$DROP_SLOW10" 0 2.5 -4 0
  make quasar_film_arrive 2.5 -12 "afade=t=out:st=1.8:d=0.7"
fi

if want quasar_film_mirror_break; then   # 336: the mirror torn off, metal cracking and shearing, pieces falling
  L "$METAL_RING" 0 1.0 0 0
  L "$SWORD" 2 1.0 -3 0
  L "$PIPE" 0 0.44 -3 0.05
  L "$GLASS_SHARDS" 0 0.7 -5 0.1 "$(rate 0.7)"
  L "$METAL_TAP" 0 0.47 -4 0.4 "$(rate 0.8)"
  make quasar_film_mirror_break 1.0 -14 "afade=t=out:st=0.7:d=0.3"
fi

if want quasar_film_stretch; then   # 350: the star pulled long, a huge low groan sliding down (a whale-like moan)
  L "$ROAR" 5.0 1.1 0 0 "$(rate 0.5)"
  L "$METAL_BOWED" 9.9 1.1 -3 0 "$(rate 0.5)"
  L "$TONEDOWN" 0 1.76 -4 0 "$(rate 0.8)"
  L "$METAL_SCRAPE" 0 2.2 -5 0
  make quasar_film_stretch 2.2 -14 "afade=t=out:st=1.6:d=0.6"
fi

if want quasar_film_ignite; then   # 440: one enormous whoomf and a bright rush of fire
  L "$FIRE_WHOOSH" 0 2.48 0 0 "$(rate 0.75)"
  L "$BOOM011" $B 3.3 -3 0
  L "$BLASTOFF" 3 3.3 -6 0.1 "volume='exp(-0.6*t)':eval=frame"
  L "$DROP_FAST16" 0 2.1 -3 0
  make quasar_film_ignite 3.3 -11 "volume='exp(-0.25*t)':eval=frame,afade=t=out:st=2.6:d=0.7"
fi

if want quasar_film_title; then   # 455: the title card, a deep bell over a sub, a long clean tail
  L "$BELLS" 15.15 2.1 0 0 "$(rate 0.7)"
  L "$DROP_SLOW10" 0 3.0 -4 0 "lowpass=f=150"
  L "$CHORD_REVERB" 0 2.1 -9 0 "$(rate 0.7)"
  make quasar_film_title 3.0 -14 "afade=t=out:st=2.3:d=0.7"
fi

if want quasar_film_jets; then   # 530: the jets launching, a double crack 0.15 s apart, then a tearing blast
  L "$WHIP" 0 0.6 0 0
  L "$WHIP" 0 0.6 -1 0.15 "$(rate 0.9)"
  L "$STORM_CRASH" $CRASH 1.0 -3 0 "highpass=f=300"
  L "$BLASTOFF" 6 2.3 -2 0.12 "volume='exp(-0.5*t)':eval=frame"
  L "$BOOM214" $B 2.3 -4 0.1
  make quasar_film_jets 2.3 -11 "afade=t=out:st=1.7:d=0.6"
fi

if want quasar_film_turn; then   # 620, exactly 2.25 s, peaking at its end (the flood): a riser bending up, brighter, harsher
  L "$TONEDOWN" 0.05 2.25 0 0 "areverse"
  L "$ARC_POWERUP" 1 2.25 -4 0 "volume='pow(t/2.25\,2.5)':eval=frame,highpass=f=1500"
  L "$BOOM214" $B 2.25 -4 0 "areverse,lowpass=f=1500"
  L "$WIND_RUSH" 0 1.05 -3 1.2 "areverse"
  make quasar_film_turn 2.25 -13
fi

if want quasar_film_flood; then   # 665: looking down the jet, a white crash and a high ring left hanging
  L "$STORM_CRASH" $CRASH 1.2 0 0 "highpass=f=800"
  L "$GLASS_SHARDS" 0 2.6 -3 0
  L "$SCIFI_BURST" 0 0.31 -2 0
  L "$POWERUP_SHIMMER" 0 1.52 -4 0
  L "$SHIMMER" 10 3.3 -6 0.1 "highpass=f=3000"
  L "$ARC_POWERUP" 5 2.3 -10 0.2 "highpass=f=5000"
  make quasar_film_flood 3.3 -12 "volume='exp(-0.3*t)':eval=frame,afade=t=out:st=2.6:d=0.7"
fi

if want quasar_film_birds; then   # 700: a few morning birds starting up, surprised; quiet
  L "$BIRDS" 30 2.2 0 0
  L "$BIRD_FLUTE" 0 1.03 -10 0.6 "$(rate 1.2)"
  make quasar_film_birds 2.2 -26 "afade=t=in:d=0.3,afade=t=out:st=1.6:d=0.6"
fi

if want quasar_film_impact_sub; then   # 795: under the world's impact, only the sub-bass and an air-sucked-out drop
  L "$DROP_SLOW10" 0 2.5 0 0 "lowpass=f=120"
  L "$DROP_FAST16" 0 2.1 -2 0 "lowpass=f=150"
  make quasar_film_impact_sub 2.5 -20 "afade=t=out:st=1.8:d=0.7"
fi

if want quasar_film_pop_sub; then   # 890: one round sub hit under the pop
  L "$DROP_FAST16" 0 1.5 0 0 "lowpass=f=150"
  L "$PUNCH" 0 0.4 -6 0 "lowpass=f=200"
  make quasar_film_pop_sub 1.5 -20 "afade=t=out:st=1.0:d=0.5"
fi

# the film's loops (seamless)
if want quasar_film_star_loop; then   # 80 to 334: a star up close, a low rolling fire roar; a quiet bed
  LOOP=1.0
  L "$FIRE_LOOP" 0 3.0 0 0 "$(rate 0.5)"
  L "$STORM_EYE" 30 6.0 -3 0 "lowpass=f=400"
  L "$THUNDER" 2 6.0 -8 0 "lowpass=f=300"
  make quasar_film_star_loop 5.0 -21 "lowpass=f=900"
fi
if want quasar_film_time_loop; then   # 152 to 330: time racing, ticking blurred into a whir, a thin high whine
  LOOP=0.6
  L "$CLOCK" 0 18.4 0 0 "$(rate 4)"
  L "$CLOCK_CROOKED" 0 14.7 -4 0 "$(rate 3.2)"
  L "$TRIMMER" 10 6.9 -6 0 "$(rate 1.5)"
  L "$ARC_POWERUP" 2 4.6 -12 0 "highpass=f=4000"
  make quasar_film_time_loop 4.0 -18
fi
if want quasar_film_wind_loop; then   # 200 to 330: stars streaming past, a wide rushing wind with a hollow tone
  LOOP=1.0
  L "$WIND_WHIP" 0 6.0 0 0
  L "$CHIMNEY" 20 6.0 -3 0
  L "$HURRICANE" 40 6.0 -6 0 "lowpass=f=2000"
  make quasar_film_wind_loop 5.0 -18
fi
if want quasar_film_ribbon_loop; then   # 362 to 440: the ribbon burning into the disc, a hot sizzling hiss
  LOOP=0.6
  L "$FIRE_OUT" 0 4.1 0 0 "highpass=f=1500"
  L "$CAMPFIRE" 10 4.1 -4 0 "highpass=f=2500"
  L "$FIRE_LOOP" 0 4.1 -8 0
  make quasar_film_ribbon_loop 3.5 -19
fi
if want quasar_film_quasar_loop; then   # 442 to 666: the lit quasar, a bright steady roar, hotter than the star's
  LOOP=1.0
  L "$BLASTOFF" 2.7 6.0 0 0
  L "$FIRE_LOOP" 4 4.8 -4 0 "$(rate 0.8)"
  L "$STORM_EYE" 60 6.0 -6 0 "highpass=f=400"
  make quasar_film_quasar_loop 5.0 -18
fi
if want quasar_film_jet_loop; then   # 534 to 666: the jets running, a beam roar pulsing about 3 times a second
  LOOP=0.5
  L "$REACTOR" 20 3.5 0 0
  L "$BUZZ" 3 3.5 -3 0
  L "$BLASTOFF" 3 3.5 -3 0
  make quasar_film_jet_loop 3.0 -17 "tremolo=f=3:d=0.55"
fi
LOOP=0

if [ ${#NAMES[@]} -eq 0 ]; then
  mkdir -p "$HERE/sounds"
  {
    echo "# Which Sonniss GDC 2026 file went into which sound (written by art/sonniss_sounds.sh)"
    echo
    echo "Paths are in the bundle's \`files\` folder. Gains in dB after levelling each cut to -20 LUFS; times in seconds."
    echo
    echo "| Sound | Source | From | Length | Gain | At | Filters |"
    echo "|---|---|---|---|---|---|---|"
    cat "$TMP/sources"
  } > "$HERE/sounds/sources.md"
fi
