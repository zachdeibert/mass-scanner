#ifndef MASS_SCANNER_IMAGE_H
#define MASS_SCANNER_IMAGE_H

#include <stdint.h>

typedef struct {
    void *buf;
    unsigned buf_len;
    signed pixel_stride;
    signed row_stride;
} plane_t;

typedef struct {
    unsigned width;
    unsigned height;
    unsigned rotation;
    plane_t y;
    plane_t u;
    plane_t v;
} image_t;

typedef struct {
    uint8_t *buf;
    uint8_t *start;
    uint8_t *end;
    signed stride;
} plane_vector_t;

unsigned realImageWidth(image_t *image);

unsigned realImageHeight(image_t *image);
plane_vector_t createPlaneVector(image_t *image, plane_t plane, unsigned x, unsigned y, signed dx, signed dy, unsigned count);

static inline void resetPlaneVector(plane_vector_t *vec) {
    vec->buf = vec->start;
}

static inline int planeVectorNext(plane_vector_t *vec) {
    vec->buf += vec->stride;
    if (vec->start < vec->end) {
        return vec->buf <= vec->end;
    } else {
        return vec->buf >= vec->end;
    }
}

static inline uint8_t planeVectorPixel(plane_vector_t vec) {
    return *vec.buf;
}

#endif
