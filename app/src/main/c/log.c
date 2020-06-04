#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include "jni_iface.h"
#include "log.h"

#define TAG "log.c"

#define LOG_METHOD(ltr, lvl) \
    void log_ ## ltr(const char *tag, const char *format, ...) { \
        va_list args; \
        va_start(args, format); \
        int r = vsnprintf(NULL, 0, format, args); \
        if (r < 0) { \
            log_wtf(TAG, "Failed to encode log message from native code"); \
            return; \
        } \
        ++r; \
        char *buf = (char *) malloc(r); \
        vsnprintf(buf, r, format, args); \
        va_end(args); \
        jni_log_handler(lvl, tag, buf); \
        free(buf); \
    }

LOG_METHOD(v, LOG_LEVEL_VERBOSE)
LOG_METHOD(d, LOG_LEVEL_DEBUG)
LOG_METHOD(i, LOG_LEVEL_INFO)
LOG_METHOD(w, LOG_LEVEL_WARNING)
LOG_METHOD(e, LOG_LEVEL_ERROR)
LOG_METHOD(wtf, LOG_LEVEL_WTF)

#undef LOG_METHOD
