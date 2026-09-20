# Windows equivalent of build.sh: builds the three AMS reactors in dependency order, installing
# each to the local repository before the next one resolves against it.
#
#   .\build.ps1              clean install, all three reactors
#   .\build.ps1 test         run the tests only
#   .\build.ps1 package      package without installing
#
# build.sh is kept as the macOS/Linux entry point; it hardcodes a Homebrew and
# /Library/Java/JavaVirtualMachines layout that does not exist here.
#
# Run tools\setup-env.ps1 first in a fresh session - it restores the toolchain to C:\tools and maps
# the X: drive this script expects.

param(
    [string]$Goal = 'install',
    [string]$JdkHome = 'C:\tools\jdk21',
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs
)

$ErrorActionPreference = 'Stop'

$Tools = 'C:\tools'

# JDK 21 rather than the JDK 26 on the PATH: the build's pinned JaCoCo 0.8.5 predates Java 17
# bytecode and Mockito 1.9.5's cglib defines proxy classes reflectively. TECH_STACK.md pins the
# build JDK at 17; 21 is the nearest LTS that still accepts -source/-target 8.
#
# Pinned unconditionally rather than deferring to an inherited JAVA_HOME. This machine has JDK 26
# at C:\Program Files\JDK\jdk-26 on the *system* PATH, and Windows resolves the system PATH ahead
# of the user PATH - so "whatever JAVA_HOME happens to be" is JDK 26 as often as not, and JaCoCo
# fails there. Pass -JdkHome to build against a different one deliberately.
if (-not (Test-Path "$JdkHome\bin\javac.exe")) {
    throw "no JDK at $JdkHome - run tools\setup-env.ps1 first"
}
$env:JAVA_HOME = $JdkHome
$env:PATH = "$JdkHome\bin;$Tools\maven\bin;$env:PATH"

# Everything is built through the subst drive. The repo's own path is 120 characters before the
# first source file and its longest path is 262 - already past MAX_PATH, and target\ adds ~35 more.
#
# Re-created here rather than assumed. A subst mapping belongs to the logon session that made it,
# so one established by setup-env.ps1 is not guaranteed to be visible by the time a build runs -
# and the failure mode without this is a confusing "path not found" from deep inside Maven.
$Root = 'X:'
if (-not (Test-Path "$Root\ams-parent-bom\pom.xml")) {
    $repo = Split-Path -Parent $PSCommandPath
    & subst $Root /D 2>$null | Out-Null
    & subst $Root $repo
    if (-not (Test-Path "$Root\ams-parent-bom\pom.xml")) {
        throw "could not map $Root to $repo"
    }
    Write-Host "mapped $Root -> $repo"
}

# Kept off the 5 GB D: volume and off the S3-backed home folder; setup-env.ps1 -SaveM2 snapshots it.
$RepoLocal = "$Tools\m2"

Write-Host "JAVA_HOME = $env:JAVA_HOME"
& "$Tools\maven\bin\mvn.cmd" -version | Select-Object -First 1
Write-Host ""

foreach ($reactor in @('ams-parent-bom', 'ams-common', 'ams-internal')) {
    Write-Host "==> [$reactor] mvn clean $Goal"

    $argv = @('-B', "-Dmaven.repo.local=$RepoLocal", '-f', "$Root\$reactor\pom.xml", 'clean', $Goal)
    if ($MavenArgs) { $argv += $MavenArgs }

    & "$Tools\maven\bin\mvn.cmd" @argv
    if ($LASTEXITCODE -ne 0) {
        throw "[$reactor] failed with exit code $LASTEXITCODE"
    }
}

Write-Host ""
Write-Host "==> all reactors built"

$war = "$Root\ams-internal\AssetManagementInternalWeb\target\AssetManagementInternalWeb.war"
$ear = "$Root\ams-internal\AssetManagementInternalEar\target\AssetManagementInternalEar.ear"
foreach ($a in @($war, $ear)) {
    if (Test-Path $a) {
        Write-Host ("    {0}  {1} MB" -f (Split-Path $a -Leaf), [math]::Round((Get-Item $a).Length / 1MB, 1))
    }
}
