<#
# Performance & FPS Log Collector

log_collecter.ps1 automates the collection of performance benchmarks and renderer FPS from an Android device via `adb`.

## Usage

Run the script from a PowerShell terminal:

### 1. For Native C++ Engine

.\log_collector.ps1 native

*Saves to: `logs/stats-native-fps.txt` and `logs/stats-native-perf.txt`*

### 2. For Kotlin Engine

.\log_collector.ps1 kotlin

*Saves to: `logs/stats-kotlin-fps.txt` and `logs/stats-kotlin-perf.txt`*

All logs are saved in:
`app/src/main/cpp/logs/`
#>

param (
    [string]$Engine = "native"
)

# Set up paths
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDir = Join-Path $ScriptDir "logs"

# Ensure log directory exists
if (!(Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir | Out-Null
}

# Set filenames
$FpsFile = Join-Path $LogDir "stats-$Engine-fps.txt"
$PerfFile = Join-Path $LogDir "stats-$Engine-perf.txt"

Write-Host "--- Log Collector ---" -ForegroundColor Cyan
Write-Host "Engine: $Engine"
Write-Host "Saving to: $LogDir"

# 1. Clear logs
Write-Host "Clearing logcat..."
adb logcat -c

# 2. Start logging jobs
Write-Host "Starting background logging jobs..." -ForegroundColor Green

$FpsJob = Start-Job -ScriptBlock {
    param($tag, $file)
    adb logcat -s $tag -v raw | Tee-Object -FilePath $file
} -ArgumentList "RENDERER_FPS", $FpsFile

$PerfJob = Start-Job -ScriptBlock {
    param($tag, $file)
    adb logcat -s $tag -v raw | Tee-Object -FilePath $file
} -ArgumentList "PERF_BENCHMARK", $PerfFile

Write-Host "--------------------------------------------------------"
Write-Host "Logging is RUNNING."
Write-Host "Press any key to STOP logging and exit."
Write-Host "--------------------------------------------------------"

# Wait for user input
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

# Cleanup
Write-Host "Stopping jobs..." -ForegroundColor Yellow
Get-Job | Stop-Job
Get-Job | Remove-Job

Write-Host "Done. Logs saved to:"
Write-Host "  - $FpsFile"
Write-Host "  - $PerfFile"
