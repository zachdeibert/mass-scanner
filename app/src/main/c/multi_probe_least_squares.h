#ifndef MASS_SCANNER_MULTI_PROBE_LEAST_SQUARES_H
#define MASS_SCANNER_MULTI_PROBE_LEAST_SQUARES_H

#include "image.h"
#include "main.h"

typedef point_t (*ls_probe_func_t)(image_t *image, unsigned i, point_t *centers, unsigned count);

void least_squares_edge(image_t *image, ls_probe_func_t probe, point_t *centers, unsigned samples, float *angle, float *intercept);
void least_squares_box(point_t *centers, point_t *corners, image_t *image, unsigned samples);

point_t least_squares_probe_top(image_t *image, unsigned i, point_t *centers, unsigned count);
point_t least_squares_probe_left(image_t *image, unsigned i, point_t *centers, unsigned count);
point_t least_squares_probe_bottom(image_t *image, unsigned i, point_t *centers, unsigned count);
point_t least_squares_probe_right(image_t *image, unsigned i, point_t *centers, unsigned count);
void least_squares_top(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept);
void least_squares_left(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept);
void least_squares_bottom(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept);
void least_squares_right(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept);

#endif
