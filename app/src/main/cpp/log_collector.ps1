<#
# FPS Log Collector

log_collector.ps1 collects renderer FPS from an Android device via `adb logcat`.

## Usage

    .\log_collector.ps1 [<label>] [<seconds>]

Parameters:
    Label    - Name label for the log file (default: "baseline")
    Seconds  - Duration in seconds to log. If 0 or omitted, logs indefinitely until a keypress or Ctrl+C (default: 0)

Examples:
    .\log_collector.ps1 test
    .\log_collector.ps1 test 15

Saves to: `logs/stats-<label>-fps.txt`
#>

param (
    [string]$Label = "baseline",
    [int]$Seconds = 0
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDir = Join-Path $ScriptDir "logs"

if (!(Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir | Out-Null
}

$FpsFile = Join-Path $LogDir "stats-$Label-fps.txt"

Write-Host "--- FPS Log Collector ---" -ForegroundColor Cyan
Write-Host "Label: $Label"
Write-Host "Saving to: $FpsFile"

Write-Host "Clearing logcat..."
adb logcat -c

Write-Host "Logging RENDERER_FPS..." -ForegroundColor Green

$FpsJob = Start-Job -ScriptBlock {
    param($tag, $file)
    adb logcat -s $tag -v raw | Tee-Object -FilePath $file
} -ArgumentList "RENDERER_FPS", $FpsFile

try {
    if ($Seconds -gt 0) {
        Write-Host "Logging for $Seconds seconds... Press Ctrl+C to abort." -ForegroundColor Green
        Start-Sleep -Seconds $Seconds
    } else {
        Write-Host "--------------------------------------------------------"
        Write-Host "Logging is RUNNING."
        Write-Host "Press any key to STOP logging and exit."
        Write-Host "--------------------------------------------------------"
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    }
} finally {
    Write-Host "Stopping..." -ForegroundColor Yellow
    Get-Job | Stop-Job
    Get-Job | Remove-Job

    Write-Host "Done. Log saved to:"
    Write-Host "  - $FpsFile"
}
