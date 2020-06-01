#ifndef MASS_SCANNER_LUMINANCEPROBE_H
#define MASS_SCANNER_LUMINANCEPROBE_H

#include "image.h"
#include "main.h"

unsigned luminance_probe(image_t *image, plane_vector_t vec);

point_t luminance_probe_top(image_t *image, unsigned x);
point_t luminance_probe_left(image_t *image, unsigned y);
point_t luminance_probe_bottom(image_t *image, unsigned x);
point_t luminance_probe_right(image_t *image, unsigned y);

#endif
