cmake_minimum_required(VERSION 3.22.1)
project("native-exercise-engine")

add_library(native-exercise-engine SHARED
    MathUtils.cpp
    LandmarkSmoother.cpp
    ExerciseEngine.cpp
    JniBridge.cpp)

target_compile_features(native-exercise-engine PRIVATE cxx_std_17)
target_link_options(native-exercise-engine PRIVATE "-Wl,-z,max-page-size=16384")

find_library(log-lib log)
target_link_libraries(native-exercise-engine ${log-lib})
