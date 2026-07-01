#!/bin/bash
# FPS Log Collector for Linux/macOS
#
# log_collector.sh collects renderer FPS from an Android device via `adb logcat`.
#
# Usage:
#     ./log_collector.sh [<label>] [<seconds>]
#
# Parameters:
#     Label    - Name label for the log file (default: "baseline")
#     Seconds  - Duration in seconds to log. If 0 or omitted, logs indefinitely until Enter or Ctrl+C (default: 0)
#
# Examples:
#     ./log_collector.sh test
#     ./log_collector.sh test 15
#
# Saves to: logs/stats-<label>-fps.txt

LABEL="${1:-baseline}"
SECONDS_TO_RUN="${2:-0}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$SCRIPT_DIR/logs"

mkdir -p "$LOG_DIR"
FPS_FILE="$LOG_DIR/stats-$LABEL-fps.txt"

echo "--- FPS Log Collector ---"
echo "Label: $LABEL"
echo "Saving to: $FPS_FILE"

echo "Clearing logcat..."
adb logcat -c

echo "Logging RENDERER_FPS..."

# Start logcat in the background
adb logcat -s RENDERER_FPS -v raw > "$FPS_FILE" &
LOGCAT_PID=$!

cleanup() {
    echo -e "\nStopping..."
    if kill -0 $LOGCAT_PID 2>/dev/null; then
        kill $LOGCAT_PID
        wait $LOGCAT_PID 2>/dev/null
    fi
    echo "Done. Log saved to:"
    echo "  - $FPS_FILE"
    exit 0
}

# Trap SIGINT (Ctrl+C) and SIGTERM
trap cleanup SIGINT SIGTERM

if [ "$SECONDS_TO_RUN" -gt 0 ]; then
    echo "Logging for $SECONDS_TO_RUN seconds... Press Ctrl+C to abort."
    sleep "$SECONDS_TO_RUN"
    cleanup
else
    echo "--------------------------------------------------------"
    echo "Logging is RUNNING."
    echo "Press ENTER to STOP logging and exit."
    echo "--------------------------------------------------------"
    read -r
    cleanup
fi
