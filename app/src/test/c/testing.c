#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include "testing.h"

void pass() {
    exit(EXIT_SUCCESS);
}

void fail() {
    exit(EXIT_FAILURE);
}

void assert_that(int condition, const char *message, ...) {
    if (!condition) {
        va_list va;
        va_start(va, message);
        fflush(stdout);
        vfprintf(stderr, message, va);
        fputc('\n', stderr);
        va_end(va);
        fail();
    }
}
