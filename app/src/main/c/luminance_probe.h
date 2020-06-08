#ifndef MASS_SCANNER_LUMINANCEPROBE_H
#define MASS_SCANNER_LUMINANCEPROBE_H

#include "image.h"
#include "geometry.h"

unsigned luminance_probe(image_t *image, plane_vector_t vec);

vec2_t luminance_probe_top(image_t *image, unsigned x);
vec2_t luminance_probe_left(image_t *image, unsigned y);
vec2_t luminance_probe_bottom(image_t *image, unsigned x);
vec2_t luminance_probe_right(image_t *image, unsigned y);

#endif
