#!/usr/bin/env bash
# Builds the three AMS reactors in dependency order, installing each to the local
# repository before the next one resolves against it.
#
#   ./build.sh              clean install, all three reactors
#   ./build.sh test         run the tests only
#   ./build.sh docker       build, then bring the whole stack up under docker compose
set -euo pipefail

export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home}"
export PATH="/opt/homebrew/bin:$JAVA_HOME/bin:$PATH"

HERE="$(cd "$(dirname "$0")" && pwd)"
GOAL="${1:-install}"
shift || true

build_reactors() {
  local goal="$1"; shift
  for reactor in ams-parent-bom ams-common ams-internal; do
    echo "==> [$reactor] mvn clean $goal"
    mvn -q -f "$HERE/$reactor/pom.xml" clean "$goal" "$@"
  done
  echo "==> all reactors built"
}

if [ "$GOAL" = "docker" ]; then
  # The image copies target/AssetManagementInternalWeb.war, so the Maven build has to run first.
  build_reactors install "$@"

  WAR="$HERE/ams-internal/AssetManagementInternalWeb/target/AssetManagementInternalWeb.war"
  [ -f "$WAR" ] || { echo "!! $WAR was not produced"; exit 1; }

  # Both are supplied per environment and deliberately not committed; the Dockerfile COPYs them
  # unconditionally, so a missing one fails the image build with a much less obvious message.
  LIB="$HERE/ams-internal/AssetManagementInternalWeb/lib"
  CRT="$HERE/ams-internal/AssetManagementInternalWeb/certs/internal-ca.crt"
  compgen -G "$LIB/ojdbc8-*.jar" >/dev/null \
    || { echo "!! no ojdbc8 jar in $LIB - see the README there"; exit 1; }
  [ -f "$CRT" ] \
    || { echo "!! $CRT is missing - see the README in that directory"; exit 1; }

  echo "==> docker compose up --build"
  exec docker compose -f "$HERE/docker-compose.yml" up --build
fi

build_reactors "$GOAL" "$@"
