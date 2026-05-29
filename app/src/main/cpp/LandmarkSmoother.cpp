#include "LandmarkSmoother.h"
#include <cstring> // memcpy, memset

namespace nativeengine {

LandmarkSmoother::LandmarkSmoother(float alpha)
    : initialized_(false), alpha_(alpha) {
    std::memset(buffer_, 0, sizeof(buffer_));
}

void LandmarkSmoother::smoothAll(const float* raw, float* out, int landmarkCount) {
    const int totalParams = landmarkCount * kParamsPerLandmark;

    if (!initialized_) {
        // First frame: seed the buffer with raw data, copy to output.
        std::memcpy(buffer_, raw, totalParams * sizeof(float));
        std::memcpy(out, raw, totalParams * sizeof(float));
        initialized_ = true;
        return;
    }

    const float oneMinusAlpha = 1.0f - alpha_;

    for (int i = 0; i < landmarkCount; ++i) {
        const int base = i * kParamsPerLandmark;

        // Smooth x, y, z (offsets 0, 1, 2)
        for (int j = 0; j < 3; ++j) {
            buffer_[base + j] = alpha_ * raw[base + j]
                              + oneMinusAlpha * buffer_[base + j];
        }

        // Pass through presence and visibility (offsets 3, 4)
        buffer_[base + 3] = raw[base + 3];
        buffer_[base + 4] = raw[base + 4];
    }

    std::memcpy(out, buffer_, totalParams * sizeof(float));
}

void LandmarkSmoother::reset() {
    initialized_ = false;
    std::memset(buffer_, 0, sizeof(buffer_));
}

} // namespace nativeengine
