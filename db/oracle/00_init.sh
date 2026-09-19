#!/usr/bin/env bash
#
# Builds the AMS schema inside FREEPDB1.
#
# Why this exists rather than just dropping the .sql files into /container-entrypoint-initdb.d:
# the image runs everything it finds there through SQL*Plus **as SYS, connected to the CDB root
# (FREE)** - not to the FREEPDB1 pluggable database, and not as the application user. Loose .sql
# files would therefore try to create the application tables in the root container owned by SYS,
# which is both wrong and partly disallowed.
#
# The scan also descends into subdirectories, so keeping the scripts in numbered subfolders under
# the scanned directory is not enough either - they simply get run a second time, in the wrong
# container as the wrong user. Only this file is mounted into the scanned directory; the scripts
# live outside it and AMS_SQL_DIR points here at them.
#
# So: connect to the PDB, create the AMS user, then run every object script AS that user. Objects
# then belong to AMS with no ALTER SESSION SET CURRENT_SCHEMA anywhere.

set -euo pipefail

# The object scripts are mounted somewhere other than this file's own directory, because the
# image's startup scan descends into subdirectories and would otherwise run every one of them a
# second time as SYS against the CDB root. AMS_SQL_DIR is where they actually are; the fallback
# keeps the script runnable by hand from a checkout.
HERE="${AMS_SQL_DIR:-$(cd "$(dirname "$0")" && pwd)}"
PDB="${AMS_PDB:-FREEPDB1}"
APP_USER="${APP_USER:-ams}"
APP_PASSWORD="${APP_USER_PASSWORD:?APP_USER_PASSWORD must be set}"
SYS_PASSWORD="${ORACLE_PASSWORD:?ORACLE_PASSWORD must be set}"

SYS_CONN="sys/${SYS_PASSWORD}@//localhost:1521/${PDB} AS SYSDBA"
APP_CONN="${APP_USER}/${APP_PASSWORD}@//localhost:1521/${PDB}"

log() { echo "[ams-init] $*"; }

# Common session settings, prepended to every script.
#   DEFINE OFF     - an '&' inside seed text must not become a substitution prompt that hangs
#   SQLBLANKLINES  - blank lines inside PL/SQL bodies are significant to SQL*Plus otherwise
#   EXIT SQL.SQLCODE - a failing script must fail the container, not leave a half-built schema
PRELUDE="
WHENEVER SQLERROR EXIT SQL.SQLCODE
WHENEVER OSERROR EXIT FAILURE
SET DEFINE OFF
SET SQLBLANKLINES ON
SET FEEDBACK OFF
SET HEADING OFF
SET VERIFY OFF
ALTER SESSION SET NLS_DATE_LANGUAGE = 'AMERICAN';
"

run_as_app() {
  local file="$1"
  # SHOW ERRORS after every script: a package body that fails to compile leaves an INVALID object
  # and SQL*Plus still exits 0, so without this the container would come up "healthy" and every
  # scheduling call would fail at runtime.
  printf '%s\n@%s\nSHOW ERRORS\nEXIT\n' "$PRELUDE" "$file" \
    | sqlplus -S -L "$APP_CONN" || {
        log "FAILED: $file"
        return 1
      }
}

# Runs every script in a directory, in filename order.
#
# .pks and .pkb are matched as well as .sql: package specs and bodies keep those extensions so the
# two are distinguishable at a glance, and the numeric prefixes put all specs (10_, 11_, 12_)
# before all bodies (20_, 21_, 22_).
#
# An empty directory is a hard failure, not a no-op. Globbing only *.sql here once meant the
# packages silently created nothing and the schema came up without them.
run_dir() {
  local dir="$1"
  [ -d "$HERE/$dir" ] || { log "MISSING DIRECTORY: $dir"; return 1; }

  local files=()
  while IFS= read -r f; do files+=("$f"); done \
    < <(find "$HERE/$dir" -maxdepth 1 -type f \( -name '*.sql' -o -name '*.pks' -o -name '*.pkb' \) | sort)

  if [ "${#files[@]}" -eq 0 ]; then
    log "NO SCRIPTS FOUND IN: $dir"
    return 1
  fi

  for f in "${files[@]}"; do
    log "  $(basename "$f")"
    run_as_app "$f"
  done
  log "$dir done (${#files[@]} scripts)"
  return 0
}

# ---------------------------------------------------------------------------
# 1. The application user
# ---------------------------------------------------------------------------
log "creating ${APP_USER} in ${PDB}"

# The image creates APP_USER itself when the variable is set, so this is written to be safe either
# way: create if absent, then top up the grants the application actually needs.
sqlplus -S -L "$SYS_CONN" <<SQL
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET DEFINE OFF
DECLARE
  l_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO l_count FROM DBA_USERS WHERE USERNAME = UPPER('${APP_USER}');
  IF l_count = 0 THEN
    EXECUTE IMMEDIATE 'CREATE USER ${APP_USER} IDENTIFIED BY "${APP_PASSWORD}"';
  END IF;
END;
/
GRANT CONNECT, RESOURCE TO ${APP_USER};
GRANT CREATE VIEW, CREATE SEQUENCE, CREATE PROCEDURE, CREATE TABLE TO ${APP_USER};
-- Without an explicit quota the first insert fails with ORA-01950 even though the DDL succeeded.
ALTER USER ${APP_USER} QUOTA UNLIMITED ON USERS;
EXIT
SQL

# ---------------------------------------------------------------------------
# 2. Schema objects, in dependency order
# ---------------------------------------------------------------------------
log "tables";      run_dir 01_tables
log "constraints"; run_dir 02_constraints
log "indexes";     run_dir 03_indexes
log "sequences";   run_dir 04_sequences
log "views";       run_dir 05_views

# All specs before all bodies, so a body that calls another package still compiles on a clean run.
log "packages";    run_dir 06_packages

# ---------------------------------------------------------------------------
# 3. Seed data
# ---------------------------------------------------------------------------
# Guarded so the script can be re-run by hand against an existing volume without every insert
# failing on a duplicate key. The container itself only runs this once, on an empty volume.
ALREADY=$(printf '%s\nSELECT COUNT(*) FROM AMS_CUSTOMERS;\nEXIT\n' "$PRELUDE" \
          | sqlplus -S -L "$APP_CONN" | tr -d '[:space:]')

if [ "${ALREADY:-0}" = "0" ]; then
  log "seed"
  run_dir 07_seed
else
  log "seed skipped: AMS_CUSTOMERS already has ${ALREADY} rows"
fi

# ---------------------------------------------------------------------------
# 4. Acceptance gate
# ---------------------------------------------------------------------------
# Lives in a subdirectory, not at the top level. The image's own scan of
# /container-entrypoint-initdb.d runs every top-level .sql it finds AS SYS AGAINST THE CDB ROOT,
# where the AMS packages do not exist - so a top-level validate script gets run a second time in
# the wrong place and fills the log with PLS-00201. Directories are skipped by that scan.
log "validating"
run_as_app "$HERE/09_validate/validate.sql"

log "schema ready"
