# Restores the JDK 8 build toolchain to C:\tools after an AWS WorkSpaces restart.
#
#   .\restore-jdk8.ps1              extract anything missing
#   .\restore-jdk8.ps1 -Force       re-extract even if already present
#   .\restore-jdk8.ps1 -Dl <path>   take the zips from somewhere else
#
# Then build from Git Bash:  ./build-jdk8.sh
#
# Only the zips live on the persistent (S3-backed) home folder; everything they expand into goes to
# C:\tools, which is fast local disk and is wiped on every restart - which is the point. A JDK is
# ~6,000 small files and the home folder is about 127x slower at creating files, so keeping each
# tool as one ~100 MB object and expanding it locally per session is the difference between minutes
# and tens of minutes.
#
# Narrower and faster than the general-purpose tools\setup-env.ps1, which this is modelled on:
#   - only jdk8, maven and the m2 snapshot, skipping the 205 MB jdk21 that a JDK 8 build never
#     touches (build-jdk8.sh pins JAVA_HOME to C:\tools\jdk8);
#   - the .NET ZipFile API rather than Expand-Archive, which is minutes faster per archive.
# Measured on 2026-10-04: jdk8 124 s, maven 27 s, m2 230 s - about 6 minutes in total.
#
# Dot-source it (. .\restore-jdk8.ps1) to leave JAVA_HOME and PATH pointing at JDK 8 in the current
# shell. Not required: build-jdk8.sh sets its own JAVA_HOME either way.

param(
    [switch]$Force,
    [string]$Dl
)

$ErrorActionPreference = 'Stop'
$ProgressPreference    = 'SilentlyContinue'

Add-Type -AssemblyName System.IO.Compression.FileSystem

$Tools = 'C:\tools'

# ---------------------------------------------------------------- locate the zips
# Derived from this script's own location rather than hardcoded, so the whole tree can be moved or
# copied without editing: the repo sits at mysoftware\1-forge\ams, so dl is two levels up.
if (-not $Dl) { $Dl = Join-Path $PSScriptRoot '..\..\tools\dl' }

$resolved = Resolve-Path -LiteralPath $Dl -ErrorAction SilentlyContinue
if ($null -eq $resolved) {
    throw "no zip folder at $Dl - pass -Dl <path> to point at the one holding jdk8.zip, maven.zip and m2.zip"
}
$Dl = $resolved.ProviderPath

New-Item -ItemType Directory -Force $Tools | Out-Null

# ---------------------------------------------------------------- extract
# jdk8.zip and maven.zip each hold a single versioned top-level directory (jdk8u504-b01,
# apache-maven-3.9.16) which is flattened to a stable name, so JAVA_HOME and M2_HOME never have to
# change when a version does. Extraction goes to a staging directory first so a failure part way
# through cannot leave a half-populated C:\tools\jdk8 that the next run would skip as "present".
function Restore-Tool {
    param([string]$Name)

    $final = Join-Path $Tools $Name
    if ((Test-Path -LiteralPath $final) -and -not $Force) {
        Write-Host ("  {0,-6} already present" -f $Name)
        return
    }

    $zip = Join-Path $Dl "$Name.zip"
    if (-not (Test-Path -LiteralPath $zip)) { throw "missing $zip - nothing to restore $Name from" }

    $staging = Join-Path $Tools "_extract_$Name"
    if (Test-Path -LiteralPath $staging) { Remove-Item -LiteralPath $staging -Recurse -Force }

    $sw = [Diagnostics.Stopwatch]::StartNew()
    [System.IO.Compression.ZipFile]::ExtractToDirectory($zip, $staging)

    $inner = Get-ChildItem -LiteralPath $staging -Directory | Select-Object -First 1
    if ($null -eq $inner) { throw "$zip did not contain a top-level directory" }
    if (Test-Path -LiteralPath $final) { Remove-Item -LiteralPath $final -Recurse -Force }
    Move-Item -LiteralPath $inner.FullName -Destination $final
    Remove-Item -LiteralPath $staging -Recurse -Force

    Write-Host ("  {0,-6} <- {1}  ({2:n0}s)" -f $Name, $inner.Name, $sw.Elapsed.TotalSeconds)
}

Write-Host '==> toolchain'
Restore-Tool 'jdk8'
Restore-Tool 'maven'

# ---------------------------------------------------------------- maven repository
# m2.zip is unlike the tool zips: its entries start at the group level (antlr\antlr\2.7.7\...), so
# the archive IS the repository root and expands straight into C:\tools\m2 with nothing to flatten.
#
# Kept as one archive rather than ~15,000 loose files for the same reason the JDKs are, and
# restored so a fresh session does not re-resolve the whole dependency tree from Maven Central.
# build-jdk8.sh hardcodes -Dmaven.repo.local=C:\tools\m2, so this path is not free to move.
$m2 = Join-Path $Tools 'm2'

function Measure-Jars {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) { return 0 }
    return (Get-ChildItem -LiteralPath $Path -Recurse -Filter *.jar -ErrorAction SilentlyContinue | Measure-Object).Count
}

if ((Test-Path -LiteralPath $m2) -and -not $Force) {
    Write-Host ('==> maven repository already present ({0} jars)' -f (Measure-Jars $m2))
} else {
    Write-Host '==> maven repository'
    $zip = Join-Path $Dl 'm2.zip'
    if (-not (Test-Path -LiteralPath $zip)) { throw "missing $zip - nothing to restore the repository from" }
    if (Test-Path -LiteralPath $m2) { Remove-Item -LiteralPath $m2 -Recurse -Force }
    $sw = [Diagnostics.Stopwatch]::StartNew()
    [System.IO.Compression.ZipFile]::ExtractToDirectory($zip, $m2)
    Write-Host ('  m2     <- m2.zip  ({0:n0}s)' -f $sw.Elapsed.TotalSeconds)
}

# ---------------------------------------------------------------- environment
# Current shell only. The user-level JAVA_HOME is deliberately left alone: setup-env.ps1 and
# restore-toolchain.ps1 both point it at JDK 21 on purpose, because that is what forge-tool runs
# mvn with, and a JDK 8 restore has no business clobbering it.
$env:JAVA_HOME = "$Tools\jdk8"
$env:M2_HOME   = "$Tools\maven"
# -notlike, not -notmatch: this is a path prefix test, and in a regex every backslash in
# C:\tools\ would have to be escaped - a trailing one makes the pattern illegal outright.
$parts = $env:PATH.Split(';') | Where-Object { $_ -ne '' -and $_ -notlike "$Tools\*" }
$env:PATH = ((@("$Tools\jdk8\bin", "$Tools\maven\bin") + @($parts)) -join ';')

# ---------------------------------------------------------------- report
# The version comes out of the release file rather than `javac -version`. Java 8 writes its version
# banner to stderr, and in Windows PowerShell 5.1 redirecting a native executable's stderr wraps
# each line in a NativeCommandError and fails the script even though the command exited 0.
function Get-JdkVersion {
    param([string]$JdkHome)
    $release = Join-Path $JdkHome 'release'
    if (-not (Test-Path -LiteralPath $release)) { return 'unknown' }
    $hit = Select-String -LiteralPath $release -Pattern '^JAVA_VERSION="(.+)"' | Select-Object -First 1
    if ($null -eq $hit) { return 'unknown' }
    return $hit.Matches[0].Groups[1].Value
}

$jars = Measure-Jars $m2

Write-Host ''
Write-Host '==> ready'
Write-Host ("  JAVA_HOME = {0}  (Java {1})" -f $env:JAVA_HOME, (Get-JdkVersion "$Tools\jdk8"))
Write-Host ("  M2_HOME   = {0}" -f $env:M2_HOME)
Write-Host ("  repo      = {0}  ({1} jars)" -f $m2, $jars)

# A repository that restored to almost nothing is worth saying out loud: the build still succeeds,
# it just silently re-resolves everything from Maven Central, which on this machine is the
# difference between a 25 minute build and an hour of downloads.
if ($jars -lt 50) {
    Write-Warning "only $jars jars in $m2 - the snapshot looks partial; re-run with -Force to re-extract it"
}

Write-Host ''
Write-Host '  build with:  ./build-jdk8.sh          (Git Bash)'
