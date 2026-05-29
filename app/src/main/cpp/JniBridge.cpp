#include <jni.h>
#include "ExerciseEngine.h"

using nativeengine::ExerciseEngine;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_getuporelse_data_ndk_NativePushUpEngine_nativeCreate(
        JNIEnv* /* env */, jobject /* thiz */) {
    auto* engine = new ExerciseEngine();
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT jint JNICALL
Java_com_getuporelse_data_ndk_NativePushUpEngine_nativeProcessFrame(
        JNIEnv* env, jobject /* thiz */, jlong ptr, jfloatArray landmarks) {
    auto* engine = reinterpret_cast<ExerciseEngine*>(ptr);
    jfloat* data = env->GetFloatArrayElements(landmarks, nullptr);
    jint result = engine->processFrame(data);
    env->ReleaseFloatArrayElements(landmarks, data, JNI_ABORT);
    return result;
}

JNIEXPORT jint JNICALL
Java_com_getuporelse_data_ndk_NativePushUpEngine_nativeGetRepCount(
        JNIEnv* /* env */, jobject /* thiz */, jlong ptr) {
    auto* engine = reinterpret_cast<ExerciseEngine*>(ptr);
    return engine->getRepCount();
}

JNIEXPORT void JNICALL
Java_com_getuporelse_data_ndk_NativePushUpEngine_nativeReset(
        JNIEnv* /* env */, jobject /* thiz */, jlong ptr) {
    auto* engine = reinterpret_cast<ExerciseEngine*>(ptr);
    engine->reset();
}

JNIEXPORT void JNICALL
Java_com_getuporelse_data_ndk_NativePushUpEngine_nativeDestroy(
        JNIEnv* /* env */, jobject /* thiz */, jlong ptr) {
    auto* engine = reinterpret_cast<ExerciseEngine*>(ptr);
    delete engine;
}

} // extern "C"
