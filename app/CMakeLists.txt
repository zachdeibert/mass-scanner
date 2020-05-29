cmake_minimum_required(VERSION 3.10.2)

add_library(mass-scanner-natives SHARED
    src/c/jni.c
    src/c/main.c
)
