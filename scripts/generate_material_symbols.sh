#!/usr/bin/env bash
set -euo pipefail

# Adds one Material Symbol to the app's Icons object.
#
# This used to rewrite the whole file from a list of 48 symbols, which stopped being true of
# this repository twice over: icons are pruned when the screen that used them is deleted, and
# some are edited by hand afterwards (the skip arrows were filled in that way). Regenerating
# everything would have undone both - deleting seven icons that are in use, among them the
# volume and skip-silence ones, and restoring fifteen belonging to screens that no longer
# exist. It also still wrote to voice/ and VoiceIcons, neither of which has existed since the
# packages were renamed, so it would have produced a second icons file in a package nothing
# imports.
#
# So it appends one icon and leaves everything else alone.
#
# Usage: scripts/generate_material_symbols.sh volume_down VolumeDown
#        scripts/generate_material_symbols.sh <material symbol name> <Kotlin property name>

readonly BASE_URL="https://fonts.gstatic.com/render/v1/Material+Symbols+Outlined/24dp"
readonly VARIANT="opsz,wght,FILL,GRAD,ROND@24,400,0,0,50"
ICONS_FILE="${ICONS_FILE:-core/ui/src/main/kotlin/audiobook/core/ui/icons/Icons.kt}"
readonly ICONS_FILE

if [ "$#" -ne 2 ]; then
  echo "usage: $0 <material_symbol_name> <PropertyName>" >&2
  echo "example: $0 volume_down VolumeDown" >&2
  exit 2
fi

readonly ICON_NAME="$1"
readonly PROPERTY_NAME="$2"
readonly SOURCE_URL="${BASE_URL}/${ICON_NAME}.kt?var=${VARIANT}"

if [ ! -f "${ICONS_FILE}" ]; then
  echo "::error::${ICONS_FILE} not found - run this from the repository root." >&2
  exit 1
fi

if grep -qE "^  val ${PROPERTY_NAME}: ImageVector" "${ICONS_FILE}"; then
  echo "${PROPERTY_NAME} is already in ${ICONS_FILE}; nothing to do." >&2
  exit 1
fi

generated_at="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

# Written beside the file and moved into place at the end, so a failed download cannot leave
# the icons file half-appended.
work="$(mktemp)"
trap 'rm -f "${work}"' EXIT

{
  printf '\n'
  printf '  /*\n'
  printf '   * Source: %s\n' "${SOURCE_URL}"
  printf '   * Generated: %s\n' "${generated_at}"
  printf '   */\n'
  curl --compressed --fail --silent --show-error --location "${SOURCE_URL}" |
    perl -0pe '
      s/^package com\.example\.test\n\n(?:import [^\n]+\n)+\n//;
      s/\@Suppress\("CheckReturnValue"\)\npublic val '"${ICON_NAME}"': ImageVector\n  get\(\) \{\n    if \(_'"${ICON_NAME}"' != null\) \{\n      return _'"${ICON_NAME}"'!!\n    \}\n    _'"${ICON_NAME}"' =/internal val '"${PROPERTY_NAME}"'Icon: ImageVector =/;
      s/internal val '"${PROPERTY_NAME}"'Icon: ImageVector =/  val '"${PROPERTY_NAME}"': ImageVector =/;
      s/name = "'"${ICON_NAME}"'"/name = "'"${PROPERTY_NAME}"'"/g;
      s/PathFillType\.Companion\./PathFillType./g;
      s/\n    return _'"${ICON_NAME}"'!!\n  \}\n\nprivate var _'"${ICON_NAME}"': ImageVector\? = null\n?/\n/s;
    '
} > "${work}"

if ! grep -qE "^  val ${PROPERTY_NAME}: ImageVector" "${work}"; then
  echo "::error::the download did not contain '${ICON_NAME}' in the expected shape - nothing was written." >&2
  exit 1
fi

# The object's closing brace is the last line; the icon goes above it.
python3 - "${ICONS_FILE}" "${work}" <<'PY'
import sys

icons_path, addition_path = sys.argv[1], sys.argv[2]
lines = open(icons_path, encoding="utf-8").read().rstrip("\n").split("\n")
if lines[-1] != "}":
    raise SystemExit(f"::error::{icons_path} does not end with the object's closing brace")
addition = open(addition_path, encoding="utf-8").read().rstrip("\n").split("\n")
open(icons_path, "w", encoding="utf-8").write("\n".join(lines[:-1] + addition + ["}"]) + "\n")
PY

echo "Added ${PROPERTY_NAME} to ${ICONS_FILE}."
echo "Run ./gradlew formatKotlin lintKotlin before committing."
