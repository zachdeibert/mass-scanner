#ifndef MASS_SCANNER_MAIN_H
#define MASS_SCANNER_MAIN_H

#include "state.h"
#include "errors.h"
#include "image.h"

typedef struct {
    int captured;
    void *buf;
    unsigned buf_len;
    unsigned width;
    unsigned height;
} bitmap_t;

typedef struct {
    unsigned x;
    unsigned y;
} point_t;

typedef struct {
    int valid;
    point_t points[4];
} augment_t;

error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment);

#endif
