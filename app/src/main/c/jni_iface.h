#ifndef MASS_SCANNER_JNI_IFACE_H
#define MASS_SCANNER_JNI_IFACE_H
#ifdef __ANDROID__
#include <jni.h>
#endif

#define JNI_LOG_LEVEL_COUNT 6

typedef struct {
#ifdef __ANDROID__
    jclass log;
    jmethodID log_methods[JNI_LOG_LEVEL_COUNT];
#endif
} jni_state_t;

void jni_log_handler(int level, const char *tag, const char *message);

#endif
