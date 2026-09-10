$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourceRoot = Join-Path $projectRoot "src"
$outputRoot = Join-Path $projectRoot "bin"
$iconSource = Join-Path $sourceRoot "megatris\images\icon.jpeg"
$iconPath = Join-Path $outputRoot "MegaTris-icon.ico"
$desktopPath = [Environment]::GetFolderPath("Desktop")
$shortcutPath = Join-Path $desktopPath "MegaTris.lnk"

$javac = Get-Command "javac.exe" -ErrorAction SilentlyContinue
$javaw = Get-Command "javaw.exe" -ErrorAction SilentlyContinue
if ($null -eq $javac -or $null -eq $javaw) {
    throw "A JDK with javac.exe and javaw.exe on PATH is required to build MegaTris."
}

New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null
$sourceFiles = Get-ChildItem -Path $sourceRoot -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
if ($sourceFiles.Count -eq 0) {
    throw "No Java source files were found under $sourceRoot."
}
if (-not (Test-Path $iconSource)) {
    throw "The icon source was not found: $iconSource"
}

& $javac.Source -cp (Join-Path $projectRoot "lib\*") -d $outputRoot $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}

Add-Type -AssemblyName System.Drawing
$sourceImage = [System.Drawing.Image]::FromFile($iconSource)
$iconBitmap = New-Object System.Drawing.Bitmap(256, 256)
$graphics = [System.Drawing.Graphics]::FromImage($iconBitmap)
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$graphics.DrawImage($sourceImage, 0, 0, 256, 256)
$pngStream = New-Object System.IO.MemoryStream
$iconBitmap.Save($pngStream, [System.Drawing.Imaging.ImageFormat]::Png)
$pngBytes = $pngStream.ToArray()
$pngStream.Dispose()
$graphics.Dispose()
$iconBitmap.Dispose()
$sourceImage.Dispose()

$iconStream = New-Object System.IO.MemoryStream
$iconWriter = New-Object System.IO.BinaryWriter($iconStream)
$iconWriter.Write([UInt16]0)
$iconWriter.Write([UInt16]1)
$iconWriter.Write([UInt16]1)
$iconWriter.Write([Byte]0)
$iconWriter.Write([Byte]0)
$iconWriter.Write([Byte]0)
$iconWriter.Write([Byte]0)
$iconWriter.Write([UInt16]1)
$iconWriter.Write([UInt16]32)
$iconWriter.Write([UInt32]$pngBytes.Length)
$iconWriter.Write([UInt32]22)
$iconWriter.Write($pngBytes)
$iconWriter.Flush()
[System.IO.File]::WriteAllBytes($iconPath, $iconStream.ToArray())
$iconWriter.Dispose()
$iconStream.Dispose()

$wshShell = New-Object -ComObject WScript.Shell
$shortcut = $wshShell.CreateShortcut($shortcutPath)
$shortcut.TargetPath = $javaw.Source
$shortcut.Arguments = "-cp `"$outputRoot;$projectRoot\lib\*`" megatris.ui.MegaTris"
$shortcut.WorkingDirectory = $projectRoot
$shortcut.Description = "Play MegaTris"
$shortcut.IconLocation = "$iconPath,0"
$shortcut.Save()

$iconCacheRefresh = Join-Path $env:windir "System32\ie4uinit.exe"
if (Test-Path $iconCacheRefresh) {
    Start-Process -FilePath $iconCacheRefresh -ArgumentList "-show" -Wait -WindowStyle Hidden
}

Write-Host "MegaTris compiled successfully."
Write-Host "Desktop shortcut created: $shortcutPath"
