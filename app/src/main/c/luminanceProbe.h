#ifndef MASS_SCANNER_LUMINANCEPROBE_H
#define MASS_SCANNER_LUMINANCEPROBE_H

#include "image.h"
#include "main.h"

unsigned luminanceProbe(image_t *image, plane_vector_t vec);

point_t luminanceProbeTop(image_t *image, unsigned x);
point_t luminanceProbeLeft(image_t *image, unsigned y);
point_t luminanceProbeBottom(image_t *image, unsigned x);
point_t luminanceProbeRight(image_t *image, unsigned y);

#endif
