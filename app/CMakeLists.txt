cmake_minimum_required(VERSION 3.10.2)
project(mass-scanner-natives)

set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS ON)

add_library(mass-scanner-natives SHARED
    src/main/c/jni.c
    src/main/c/image.c
    src/main/c/luminance_probe.c
    src/main/c/main.c
    src/main/c/perpendicular_angle_bisector.c
)
target_include_directories(mass-scanner-natives PUBLIC src/main/c)
