#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if ! (echo > /dev/tcp/127.0.0.1/27017) >/dev/null 2>&1; then
  echo "MongoDB no está escuchando en localhost:27017."
  echo "Levantalo con Docker:  docker compose up -d"
  echo "O iniciá el servicio local de MongoDB y volvé a correr este script."
  exit 1
fi

./mvnw spring-boot:run
