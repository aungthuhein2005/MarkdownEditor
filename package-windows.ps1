# Build a Windows installer (.exe) for MarkdownEditor using jpackage + WiX.
# Prerequisites: JDK 23+, WiX Toolset v3.x
# Downloads JavaFX 23 (compatible with JDK 23) and FlatLaf automatically.

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

$JdkHome = "C:\Program Files\Java\jdk-23"
$JdkBin = Join-Path $JdkHome "bin"
$WixBin = "C:\Program Files (x86)\WiX Toolset v3.14\bin"
$AppName = "MarkdownEditor"
$AppVersion = "1.0.0"
$MainClass = "markdowneditor.MarkdownEditor"
$Vendor = "Aung Thu Hein"
# JavaFX 26 requires JDK 24+; package against JavaFX 23 to match JDK 23.
$JavaFxVersion = "23.0.2"

$env:Path = "$JdkBin;$WixBin;" + $env:Path

if (-not (Test-Path "$WixBin\light.exe")) {
    throw "WiX Toolset not found at $WixBin (required for .exe installer)"
}
if (-not (Test-Path (Join-Path $JdkBin "jpackage.exe"))) {
    throw "JDK with jpackage not found at $JdkHome"
}

$PackageDir = Join-Path $ProjectRoot "packaging"
$CacheDir = Join-Path $PackageDir "cache"
$InputDir = Join-Path $PackageDir "input"
$RuntimeDir = Join-Path $PackageDir "runtime"
$ClassesDir = Join-Path $ProjectRoot "build\classes"
$LibDir = Join-Path $ProjectRoot "src\lib"
$InstallerDir = Join-Path $ProjectRoot "dist\installer"
$IconPath = Join-Path $ProjectRoot "markdowneditor.ico"
$JavaFxHome = Join-Path $CacheDir "javafx-sdk-$JavaFxVersion"

Write-Host "==> Cleaning old packaging output"
function Remove-TreeSafe([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) { return }
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        Remove-Item -LiteralPath $Path -Force
    } else {
        cmd /c "rd /s /q \\?\$Path" | Out-Null
    }
    if (Test-Path -LiteralPath $Path) {
        throw "Failed to remove $Path"
    }
}
Remove-TreeSafe $InputDir
Remove-TreeSafe $RuntimeDir
Remove-TreeSafe (Join-Path $PackageDir "MANIFEST.MF")
Remove-TreeSafe (Join-Path $ProjectRoot "dist\MarkdownEditor")
Remove-TreeSafe $InstallerDir
New-Item -ItemType Directory -Path $InputDir -Force | Out-Null
New-Item -ItemType Directory -Path $InstallerDir -Force | Out-Null
New-Item -ItemType Directory -Path $CacheDir -Force | Out-Null
New-Item -ItemType Directory -Path $ClassesDir -Force | Out-Null

if (-not (Test-Path (Join-Path $JavaFxHome "lib\javafx.controls.jar"))) {
    Write-Host "==> Downloading OpenJFX $JavaFxVersion SDK"
    $FxZip = Join-Path $CacheDir "openjfx-$JavaFxVersion.zip"
    $FxUrl = "https://download2.gluonhq.com/openjfx/$JavaFxVersion/openjfx-${JavaFxVersion}_windows-x64_bin-sdk.zip"
    Invoke-WebRequest -Uri $FxUrl -OutFile $FxZip
    Expand-Archive -Path $FxZip -DestinationPath $CacheDir -Force
    Remove-Item $FxZip -Force
    if (-not (Test-Path (Join-Path $JavaFxHome "lib\javafx.controls.jar"))) {
        throw "JavaFX SDK download/extract failed"
    }
}

# Optional FlatLaf for the shipped look-and-feel
$FlatLafJar = Join-Path $LibDir "flatlaf-3.5.4.jar"
if (-not (Test-Path $FlatLafJar)) {
    Write-Host "==> Downloading FlatLaf"
    Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.4/flatlaf-3.5.4.jar" -OutFile $FlatLafJar
}

Write-Host "==> Compiling sources"
$cpJars = @(Get-ChildItem $LibDir -Filter "*.jar" | ForEach-Object { $_.FullName })
$classPath = ($cpJars -join ";")
$modulePath = Join-Path $JavaFxHome "lib"
$sourceFiles = Get-ChildItem (Join-Path $ProjectRoot "src") -Recurse -Filter "*.java" |
    Where-Object { $_.FullName -notmatch "\\lib\\" } |
    ForEach-Object { $_.FullName }

& javac --release 23 `
    --module-path $modulePath `
    --add-modules javafx.controls,javafx.web,javafx.swing `
    -cp $classPath `
    -d $ClassesDir `
    $sourceFiles
if ($LASTEXITCODE -ne 0) { throw "Compilation failed" }

Write-Host "==> Creating application JAR"
$JarPath = Join-Path $InputDir "$AppName.jar"
$manifest = @"
Manifest-Version: 1.0
Main-Class: $MainClass

"@
$ManifestPath = Join-Path $PackageDir "MANIFEST.MF"
Set-Content -Path $ManifestPath -Value $manifest -Encoding ASCII
& jar cfm $JarPath $ManifestPath -C $ClassesDir .
if ($LASTEXITCODE -ne 0) { throw "JAR creation failed" }

Write-Host "==> Copying dependency JARs"
Copy-Item (Join-Path $LibDir "*.jar") $InputDir

Write-Host "==> Creating custom runtime with JavaFX"
$fxLib = Join-Path $JavaFxHome "lib"
$jmods = Join-Path $JdkHome "jmods"
& jlink `
    --module-path "$fxLib;$jmods" `
    --add-modules java.base,java.desktop,java.logging,java.xml,java.prefs,java.datatransfer,java.naming,java.sql,java.management,jdk.unsupported,jdk.crypto.cryptoki,jdk.crypto.ec,javafx.base,javafx.graphics,javafx.controls,javafx.media,javafx.web,javafx.swing,jdk.jsobject `
    --output $RuntimeDir `
    --strip-debug `
    --no-header-files `
    --no-man-pages `
    --compress=2
if ($LASTEXITCODE -ne 0) { throw "jlink failed" }

Write-Host "==> Copying JavaFX native libraries"
$fxBin = Join-Path $JavaFxHome "bin"
Copy-Item (Join-Path $fxBin "*") (Join-Path $RuntimeDir "bin") -Force

Write-Host "==> Building Windows installer (.exe)"
$jpackageArgs = @(
    "--type", "exe",
    "--dest", $InstallerDir,
    "--name", $AppName,
    "--app-version", $AppVersion,
    "--vendor", $Vendor,
    "--description", "A simple desktop Markdown editor",
    "--input", $InputDir,
    "--main-jar", "$AppName.jar",
    "--main-class", $MainClass,
    "--runtime-image", $RuntimeDir,
    "--java-options", "--add-modules=javafx.controls,javafx.web,javafx.swing",
    "--win-shortcut",
    "--win-menu",
    "--win-dir-chooser",
    "--win-menu-group", $AppName
)
if (Test-Path $IconPath) {
    $jpackageArgs += @("--icon", $IconPath)
}

& jpackage @jpackageArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

Write-Host ""
Write-Host "Installer created:"
Get-ChildItem $InstallerDir | ForEach-Object { Write-Host "  $($_.FullName)" }
