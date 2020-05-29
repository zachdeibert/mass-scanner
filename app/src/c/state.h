#ifndef MASS_SCANNER_STATE_H
#define MASS_SCANNER_STATE_H

#include <stdint.h>
#include "errors.h"

#define STATE_T_MAGIC 0xCF26092C

typedef struct {
    int32_t magic;
    int32_t setup_error;
    int32_t *parameters;
} state_t;

typedef struct {
    void *buf;
    size_t buf_len;
    size_t pixel_stride;
    size_t row_stride;
} plane_t;

typedef struct {
    size_t width;
    size_t height;
    plane_t y;
    plane_t u;
    plane_t v;
} image_t;

typedef struct {
    int captured;
    void *buf;
    size_t buf_len;
    size_t width;
    size_t height;
} bitmap_t;

typedef struct {
    size_t x;
    size_t y;
} point_t;

typedef struct {
    int valid;
    point_t points[4];
} augment_t;

error_t process_init(state_t *state);
error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment);

#endif
