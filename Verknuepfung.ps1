# Lighthouse · Verknüpfung auf dem Desktop anlegen (Phase 16).
# Voraussetzung: einmal „Clean and Build“ in NetBeans, damit dist\Lighthouse.jar und dist\lib da sind.
# Aufruf: Doppelklick auf Verknuepfung.bat, oder in PowerShell:  .\Verknuepfung.ps1  [-Name "Lighthouse"] [-Ordner <Zielordner>]
param(
    [string] $Name = "Lighthouse",
    [string] $Ordner = [Environment]::GetFolderPath("Desktop")
)
$ErrorActionPreference = "Stop"
$projekt = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $projekt "dist\Lighthouse.jar"
$ico = Join-Path $projekt "icon\lighthouse.ico"
if (-not (Test-Path $jar)) { Write-Host "dist\Lighthouse.jar fehlt. Bitte in NetBeans einmal Clean and Build ausfuehren."; exit 1 }
if (-not (Test-Path $ico)) { Write-Host "icon\lighthouse.ico fehlt (tools.Symbol erzeugt es)."; exit 1 }

# javaw suchen: JAVA_HOME, dann der PATH, dann die JDKs unter Program Files (JDK 21 oder neuer)
$javaw = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\javaw.exe"))) { $javaw = Join-Path $env:JAVA_HOME "bin\javaw.exe" }
if (-not $javaw) { $c = Get-Command javaw.exe -ErrorAction SilentlyContinue; if ($c) { $javaw = $c.Source } }
if (-not $javaw) {
    $kandidaten = Get-ChildItem "C:\Program Files\Java", "C:\Program Files\Eclipse Adoptium", "C:\Program Files\Microsoft" -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "bin\javaw.exe") } | Sort-Object Name -Descending
    if ($kandidaten) { $javaw = Join-Path $kandidaten[0].FullName "bin\javaw.exe" }
}
if (-not $javaw) { Write-Host "Kein javaw.exe gefunden. Bitte JAVA_HOME auf ein JDK 21 setzen."; exit 1 }

$lnk = Join-Path $Ordner ($Name + ".lnk")
$shell = New-Object -ComObject WScript.Shell
$s = $shell.CreateShortcut($lnk)
$s.TargetPath = $javaw
$s.Arguments = "-Xmx4g -Dsun.java2d.uiScale.enabled=true -jar `"$jar`""
$s.WorkingDirectory = $projekt
$s.IconLocation = "$ico,0"
$s.Description = "Lighthouse - Roter Sand bei jedem Wetter"
$s.Save()
Write-Host "Verknuepfung angelegt: $lnk"
Write-Host "  Programm:      $javaw"
Write-Host "  Arbeitsordner: $projekt  (dort liegen db\db.properties und daten\)"
