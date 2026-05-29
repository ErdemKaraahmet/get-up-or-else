#ifndef LANDMARK_SMOOTHER_H
#define LANDMARK_SMOOTHER_H

namespace nativeengine {

// 33 landmarks × 5 params each (x, y, z, presence, visibility)
static constexpr int kLandmarksCount     = 33;
static constexpr int kParamsPerLandmark  = 5;
static constexpr int kBufferSize         = kLandmarksCount * kParamsPerLandmark;

/**
 * Exponential Moving Average (EMA) smoother for pose landmarks.
 * Smooths x, y, z coordinates; passes through presence and visibility unchanged.
 * Zero heap allocations — all state lives on the stack / in the object.
 */
class LandmarkSmoother {
public:
    explicit LandmarkSmoother(float alpha = 0.4f);

    /**
     * Applies EMA smoothing to raw landmark data.
     * @param raw   Input array of landmarkCount * kParamsPerLandmark floats.
     * @param out   Output array (same size). May alias raw.
     * @param landmarkCount  Number of landmarks (typically 33).
     */
    void smoothAll(const float* raw, float* out, int landmarkCount);

    /** Resets the smoother state so the next frame is treated as the first. */
    void reset();

private:
    float buffer_[kBufferSize];
    bool  initialized_;
    float alpha_;
};

} // namespace nativeengine

#endif // LANDMARK_SMOOTHER_H
