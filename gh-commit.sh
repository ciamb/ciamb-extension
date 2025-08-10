#!/bin/bash
set -euo pipefail

usage() {
  echo "uso: $0 -m|--message \"testo del commit\""
}

MESSAGE=""

# parse args
while [[ $# -gt 0 ]]; do
  case "$1" in
    -m|--message)
      [[ $# -ge 2 ]] || { echo "manca il messaggio dopo $1"; usage; exit 1; }
      MESSAGE="${2:-}"; shift 2;;
    -h|--help)
      usage; exit 0;;
    *)
      echo "comando sconosciuto: $1"; exit 1;;
  esac
done

# controllo messaggio obbligatorio
[[ -z "${MESSAGE}" ]] && { echo "errore: --message/-m obbligatorio"; exit 1; }

git add -A

# controlla se c'è effettivamente qualcosa da committare
if git diff --cached --quiet; then
  echo "non c'è niente da committare"
  exit 0
fi

git commit -m "${MESSAGE}"
git push
echo "push completato"
