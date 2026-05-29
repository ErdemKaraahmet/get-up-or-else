#ifndef EXERCISE_ENGINE_H
#define EXERCISE_ENGINE_H

#include "LandmarkSmoother.h"

namespace nativeengine {

// ── Landmark indices (MediaPipe Pose) ───────────────────────────────
static constexpr int kLeftShoulder  = 11;
static constexpr int kRightShoulder = 12;
static constexpr int kLeftElbow     = 13;
static constexpr int kRightElbow    = 14;
static constexpr int kLeftWrist     = 15;
static constexpr int kRightWrist    = 16;

// ── Detection thresholds (must match Kotlin constants exactly) ──────
static constexpr float  kMinLandmarkVisibility          = 0.5f;
static constexpr double kElbowExtensionThresholdDegrees = 160.0;
static constexpr double kElbowFlexionThresholdDegrees   = 100.0;
static constexpr double kMinNormalizedYDisplacement     = 0.3;
static constexpr float  kEmaAlpha                       = 0.4f;
static constexpr double kMinAngleDeltaDegrees           = 15.0;

// ── State machine phases ────────────────────────────────────────────
enum Phase {
    WAITING_FOR_TOP = 0,
    TOP             = 1,
    BOTTOM          = 2
};

/**
 * Native push-up exercise engine.
 * Processes per-frame pose landmarks and counts reps via a state machine.
 * Packed result format: lower 16 bits = repCount, bits 16-17 = phase.
 */
class ExerciseEngine {
public:
    ExerciseEngine();

    /**
     * Processes one frame of raw landmark data.
     * @param rawLandmarks  Flat array of 33×5 floats.
     * @return Packed int: (phase << 16) | (repCount & 0xFFFF)
     */
    int processFrame(const float* rawLandmarks);

    int  getRepCount() const;
    void reset();

private:
    int               repCount_;
    Phase             phase_;
    float             lastAverageAngle_;
    LandmarkSmoother  smoother_;
};

} // namespace nativeengine

#endif // EXERCISE_ENGINE_H
