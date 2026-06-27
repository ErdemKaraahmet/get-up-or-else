<#
# FPS Log Collector

log_collector.ps1 collects renderer FPS from an Android device via `adb logcat`.

## Usage

    .\log_collector.ps1 <label>

Examples:
    .\log_collector.ps1 baseline
    .\log_collector.ps1 native-pipeline

Saves to: `logs/stats-<label>-fps.txt`
#>

param (
    [string]$Label = "baseline"
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

Write-Host "--------------------------------------------------------"
Write-Host "Logging is RUNNING."
Write-Host "Press any key to STOP logging and exit."
Write-Host "--------------------------------------------------------"

$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host "Stopping..." -ForegroundColor Yellow
Get-Job | Stop-Job
Get-Job | Remove-Job

Write-Host "Done. Log saved to:"
Write-Host "  - $FpsFile"
