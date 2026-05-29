#ifndef MATH_UTILS_H
#define MATH_UTILS_H

namespace nativeengine {

/**
 * Calculates the angle at vertex B formed by points A-B-C.
 * Uses atan2 to compute the angle in degrees, clamped to [0, 180].
 * Matches Kotlin PushUpDetector.calculateAngle() exactly.
 */
float calculateAngle(float ax, float ay,
                     float bx, float by,
                     float cx, float cy);

} // namespace nativeengine

#endif // MATH_UTILS_H
