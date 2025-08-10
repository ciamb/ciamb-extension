#!/bin/bash
set -euo pipefail

# bump-version.sh — aggiorna la <version> del progetto maven
# requisiti: mvn (o ./mvnw)
# esempi:
#   ./bump-version.sh                 # bump patch, preserva eventuale -SNAPSHOT
#   ./bump-version.sh --minor         # bump minor
#   ./bump-version.sh --major         # bump major
#   ./bump-version.sh --release       # rimuove -SNAPSHOT
#   ./bump-version.sh --snapshot      # forza -SNAPSHOT
#   ./bump-version.sh --set 1.4.0     # imposta esplicitamente la versione
#
# es: ./bump-version.sh --minor --release

BUMP="patch"          # patch | minor | major
FORCE_SUFFIX=""       # "release" | "snapshot" | ""
EXPLICIT_VERSION=""   # se passato con --set
MVN="mvn"

# Parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    --patch)   BUMP="patch"; shift;;
    --minor)   BUMP="minor"; shift;;
    --major)   BUMP="major"; shift;;
    --release) FORCE_SUFFIX="release"; shift;;
    --snapshot)FORCE_SUFFIX="snapshot"; shift;;
    --set)     EXPLICIT_VERSION="${2:-}"; shift 2;;
    --git)     DO_GIT=true; shift;;
    -h|--help)
      grep "^# " "$0" | sed 's/^# //'; exit 0;;
    *) echo "nessun comando: $1"; exit 1;;
  esac
done

# Legge la versione corrente dal POM
CURRENT_VERSION="$($MVN -q -DforceStdout -Dexpression=project.version help:evaluate)"
if [[ -z "$CURRENT_VERSION" ]]; then
  echo "impossibile leggere la versione corrente dal pom"; exit 1
fi

# Calcolo nuova versione
NEW_VERSION=""

if [[ -n "$EXPLICIT_VERSION" ]]; then
  NEW_VERSION="$EXPLICIT_VERSION"
else
  # Supporta formati tipo 1.2.3 e 1.2.3-SNAPSHOT
  if [[ "$CURRENT_VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)(-SNAPSHOT)?$ ]]; then
    MAJOR="${BASH_REMATCH[1]}"
    MINOR="${BASH_REMATCH[2]}"
    PATCH="${BASH_REMATCH[3]}"
    HAS_SNAPSHOT="${BASH_REMATCH[4]:-}"

    case "$BUMP" in
      major) MAJOR=$((MAJOR+1)); MINOR=0; PATCH=0;;
      minor) MINOR=$((MINOR+1)); PATCH=0;;
      patch) PATCH=$((PATCH+1));;
    esac

    SUFFIX="$HAS_SNAPSHOT"
    case "$FORCE_SUFFIX" in
      release) SUFFIX="";;
      snapshot) SUFFIX="-SNAPSHOT";;
      "") : ;;
    esac

    NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}${SUFFIX}"
  else
    echo "versione corrente '${CURRENT_VERSION}' non in formato semver X.Y.Z[-SNAPSHOT]"
    echo "usa --set <versione> per impostarla esplicitamente"
    exit 1
  fi
fi

if [[ "$NEW_VERSION" == "$CURRENT_VERSION" ]]; then
  echo "la nuova versione coincide con l'attuale (${CURRENT_VERSION})"
  exit 0
fi

echo "aggiorno versione: ${CURRENT_VERSION} -> ${NEW_VERSION}"

# aggiorna tutti i moduli
$MVN -q --batch-mode \
  versions:set \
  -DnewVersion="${NEW_VERSION}" \
  -DprocessAllModules=true \
  -DgenerateBackupPoms=false # evita che genera il pom di backup

echo "update a ${NEW_VERSION}"
