# Automatisch generiert
$ErrorActionPreference = 'Stop'
$root = "C:\Users\Dan40810\Documents\NetBeansProjects\Lighthouse"
$jar  = "C:\Users\Dan40810\Documents\NetBeansProjects\Lighthouse\dist\Lighthouse.jar"
$ico  = "C:\Users\Dan40810\Documents\NetBeansProjects\Lighthouse\icon\lighthouse.ico"

if (-not (Test-Path $jar)) { throw "JAR fehlt: $jar" }
if (-not (Test-Path $ico)) { throw "ICO fehlt: $ico" }

$javaw = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javaw.exe'))) {
    $javaw = Join-Path $env:JAVA_HOME 'bin\javaw.exe'
} else {
    $cmd = Get-Command javaw.exe -ErrorAction SilentlyContinue
    if ($cmd) { $javaw = $cmd.Source }
}
if (-not $javaw) { throw "javaw.exe nicht gefunden - JAVA_HOME setzen oder Java in den PATH aufnehmen." }

$desktop = [Environment]::GetFolderPath('Desktop')
$lnk = Join-Path $desktop "Lighthouse.lnk"

$sh = New-Object -ComObject WScript.Shell
$s = $sh.CreateShortcut($lnk)
$s.TargetPath       = $javaw
$s.Arguments        = '-Xmx4g -Dsun.java2d.uiScale.enabled=true --enable-native-access=ALL-UNNAMED --add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.awt.windows=ALL-UNNAMED --add-opens java.desktop/sun.java2d=ALL-UNNAMED -jar "' + $jar + '"'
$s.WorkingDirectory = $root
$s.IconLocation     = $ico
$s.Description      = "Lighthouse - Java Anwendung"
$s.Save()

Write-Host "Verknuepfung angelegt: $lnk"
