#include "ExerciseEngine.h"
#include "MathUtils.h"
#include <cmath>
#include <android/log.h>

#define LOG_TAG "NativeExerciseEngine"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

namespace nativeengine {

// ── Helpers to index into the flat landmark array ───────────────────
static inline float landmarkX(const float* data, int idx) {
    return data[idx * kParamsPerLandmark + 0];
}
static inline float landmarkY(const float* data, int idx) {
    return data[idx * kParamsPerLandmark + 1];
}
static inline float landmarkVisibility(const float* data, int idx) {
    return data[idx * kParamsPerLandmark + 4];
}

// ── Construction / Reset ────────────────────────────────────────────

ExerciseEngine::ExerciseEngine()
    : repCount_(0),
      phase_(WAITING_FOR_TOP),
      lastAverageAngle_(0.0f),
      smoother_(kEmaAlpha) {}

void ExerciseEngine::reset() {
    repCount_         = 0;
    phase_            = WAITING_FOR_TOP;
    lastAverageAngle_ = 0.0f;
    smoother_.reset();
}

int ExerciseEngine::getRepCount() const {
    return repCount_;
}

// ── Per-frame processing ────────────────────────────────────────────

int ExerciseEngine::processFrame(const float* rawLandmarks) {
    // 1. Apply EMA smoothing
    float smoothed[kBufferSize];
    smoother_.smoothAll(rawLandmarks, smoothed, kLandmarksCount);

    // 2. Visibility check for the 6 arm landmarks
    static constexpr int kArmIndices[] = {
        kLeftShoulder, kRightShoulder,
        kLeftElbow,    kRightElbow,
        kLeftWrist,    kRightWrist
    };
    for (int idx : kArmIndices) {
        if (landmarkVisibility(smoothed, idx) < kMinLandmarkVisibility) {
            // Insufficient visibility — return current state without advancing
            return (static_cast<int>(phase_) << 16) | (repCount_ & 0xFFFF);
        }
    }

    // 3. Extract coordinates
    const float lsX = landmarkX(smoothed, kLeftShoulder);
    const float lsY = landmarkY(smoothed, kLeftShoulder);
    const float rsX = landmarkX(smoothed, kRightShoulder);
    const float rsY = landmarkY(smoothed, kRightShoulder);

    const float leX = landmarkX(smoothed, kLeftElbow);
    const float leY = landmarkY(smoothed, kLeftElbow);
    const float reX = landmarkX(smoothed, kRightElbow);
    const float reY = landmarkY(smoothed, kRightElbow);

    const float lwX = landmarkX(smoothed, kLeftWrist);
    const float lwY = landmarkY(smoothed, kLeftWrist);
    const float rwX = landmarkX(smoothed, kRightWrist);
    const float rwY = landmarkY(smoothed, kRightWrist);

    // 4. Calculate elbow angles (shoulder → elbow → wrist)
    const float leftAngle  = calculateAngle(lsX, lsY, leX, leY, lwX, lwY);
    const float rightAngle = calculateAngle(rsX, rsY, reX, reY, rwX, rwY);
    const double averageAngle = (static_cast<double>(leftAngle) + static_cast<double>(rightAngle)) / 2.0;

    // 5. Compute inter-shoulder distance for normalization
    const double dx = static_cast<double>(rsX - lsX);
    const double dy = static_cast<double>(rsY - lsY);
    const double interShoulderDistance = std::sqrt(dx * dx + dy * dy);

    // 6. Compute normalized Y displacement (shoulder Y vs wrist Y — matches Kotlin)
    const double avgShoulderY = (static_cast<double>(lsY) + static_cast<double>(rsY)) / 2.0;
    const double avgWristY   = (static_cast<double>(lwY) + static_cast<double>(rwY)) / 2.0;
    const double normalizedYDisplacement =
        (interShoulderDistance > 0.0)
            ? std::abs(avgShoulderY - avgWristY) / interShoulderDistance
            : 0.0;

    // 7. Compute angle delta for noise rejection
    const double angleDelta = std::abs(averageAngle - static_cast<double>(lastAverageAngle_));
    lastAverageAngle_ = static_cast<float>(averageAngle);

    // 8. State machine — mirrors PushUpDetector.kt exactly
    switch (phase_) {
        case WAITING_FOR_TOP:
            if (averageAngle > kElbowExtensionThresholdDegrees) {
                phase_ = TOP;
                LOGD("Phase → TOP (angle=%.1f)", averageAngle);
            }
            break;

        case TOP:
            // angleDelta is only checked here (not globally), matching Kotlin
            if (averageAngle < kElbowFlexionThresholdDegrees
                && normalizedYDisplacement >= kMinNormalizedYDisplacement
                && angleDelta >= kMinAngleDeltaDegrees) {
                phase_ = BOTTOM;
                LOGD("Phase → BOTTOM (angle=%.1f, yDisp=%.3f)", averageAngle, normalizedYDisplacement);
            }
            break;

        case BOTTOM:
            if (averageAngle > kElbowExtensionThresholdDegrees) {
                repCount_++;
                phase_ = TOP;
                LOGD("Rep %d! Phase → TOP (angle=%.1f)", repCount_, averageAngle);
            }
            break;
    }

    return (static_cast<int>(phase_) << 16) | (repCount_ & 0xFFFF);
}

} // namespace nativeengine
