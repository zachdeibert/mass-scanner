cmake_minimum_required(VERSION 3.10.2)
project(mass-scanner-natives)

set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS ON)

add_library(mass-scanner-natives SHARED
    src/c/jni.c
    src/c/image.c
    src/c/luminanceProbe.c
    src/c/main.c
)
target_include_directories(mass-scanner-natives PUBLIC src/c)
