#include "MathUtils.h"
#include <cmath>

namespace nativeengine {

float calculateAngle(float ax, float ay,
                     float bx, float by,
                     float cx, float cy) {
    // Exactly mirrors Kotlin:
    //   val radians = atan2((c.y - b.y), (c.x - b.x)) - atan2((a.y - b.y), (a.x - b.x))
    //   var degrees = Math.toDegrees(radians)
    //   degrees = abs(degrees)
    //   if (degrees > 180.0) degrees = 360.0 - degrees
    double radians = std::atan2(static_cast<double>(cy - by), static_cast<double>(cx - bx))
                   - std::atan2(static_cast<double>(ay - by), static_cast<double>(ax - bx));
    double degrees = radians * (180.0 / M_PI);
    degrees = std::abs(degrees);
    if (degrees > 180.0) {
        degrees = 360.0 - degrees;
    }
    return static_cast<float>(degrees);
}

} // namespace nativeengine
