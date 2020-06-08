#ifndef MASS_SCANNER_MULTI_PROBE_LEAST_SQUARES_H
#define MASS_SCANNER_MULTI_PROBE_LEAST_SQUARES_H

#include "geometry.h"
#include "image.h"

typedef vec2_t (*ls_probe_func_t)(image_t *image, unsigned i, vec2_t *centers, unsigned count);

ray2_t least_squares_edge(image_t *image, ls_probe_func_t probe, vec2_t *centers, unsigned samples);
void least_squares_box(vec2_t *centers, vec2_t *corners, image_t *image, unsigned samples);

vec2_t least_squares_probe_top(image_t *image, unsigned i, vec2_t *centers, unsigned count);
vec2_t least_squares_probe_left(image_t *image, unsigned i, vec2_t *centers, unsigned count);
vec2_t least_squares_probe_bottom(image_t *image, unsigned i, vec2_t *centers, unsigned count);
vec2_t least_squares_probe_right(image_t *image, unsigned i, vec2_t *centers, unsigned count);
ray2_t least_squares_top(image_t *image, vec2_t *centers, unsigned samples);
ray2_t least_squares_left(image_t *image, vec2_t *centers, unsigned samples);
ray2_t least_squares_bottom(image_t *image, vec2_t *centers, unsigned samples);
ray2_t least_squares_right(image_t *image, vec2_t *centers, unsigned samples);

#endif
