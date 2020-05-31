#include <stdint.h>
#include "image.h"
#include "luminanceProbe.h"

unsigned luminanceProbe(image_t *image, plane_vector_t vec) {
    uint64_t lum_before = 0;
    uint64_t lum_after = 0;
    unsigned count;
    for (count = 0; planeVectorNext(&vec); ++count) {
        lum_after += planeVectorPixel(vec);
    }
    unsigned res = 0;
    float maxDiff = 0;
    resetPlaneVector(&vec);
    for (unsigned i = 0; planeVectorNext(&vec); ++i) {
        lum_before += planeVectorPixel(vec);
        lum_after -= planeVectorPixel(vec);
        float before = ((float) lum_before) / ((float) (i + 1));
        float after = ((float) lum_after) / ((float) (count - i - 1));
        float diff = before - after;
        if (diff < 0) {
            diff *= -1;
        }
        if (diff > maxDiff) {
            res = i;
            maxDiff = diff;
        }
    }
    return res;
}

point_t luminanceProbeTop(image_t *image, unsigned x) {
    unsigned height = realImageHeight(image);
    point_t pt = {
            .x = x,
            .y = luminanceProbe(image, createPlaneVector(image, image->y, x, 0, 0, 1, height / 2))
    };
    return pt;
}

point_t luminanceProbeLeft(image_t *image, unsigned y) {
    unsigned width = realImageWidth(image);
    point_t pt = {
            .x = luminanceProbe(image, createPlaneVector(image, image->y, 0, y, 1, 0, width / 2)),
            .y = y
    };
    return pt;
}

point_t luminanceProbeBottom(image_t *image, unsigned x) {
    unsigned height = realImageHeight(image);
    point_t pt = {
            .x = x,
            .y = height - luminanceProbe(image, createPlaneVector(image, image->y, x, height - 1, 0, -1, height / 2))
    };
    return pt;
}

point_t luminanceProbeRight(image_t *image, unsigned y) {
    unsigned width = realImageWidth(image);
    point_t pt = {
            .x = width - luminanceProbe(image, createPlaneVector(image, image->y, width - 1, y, -1, 0, width / 2)),
            .y = y
    };
    return pt;
}
