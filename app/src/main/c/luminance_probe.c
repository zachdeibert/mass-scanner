#include <stdint.h>
#include "image.h"
#include "luminance_probe.h"

unsigned luminance_probe(image_t *image, plane_vector_t vec) {
    uint64_t lum_before = 0;
    uint64_t lum_after = 0;
    unsigned count;
    for (count = 0; plane_vector_next(&vec); ++count) {
        lum_after += plane_vector_pixel(vec);
    }
    unsigned res = 0;
    float max_diff = 0;
    reset_plane_vector(&vec);
    for (unsigned i = 0; plane_vector_next(&vec); ++i) {
        lum_before += plane_vector_pixel(vec);
        lum_after -= plane_vector_pixel(vec);
        float before = ((float) lum_before) / ((float) (i + 1));
        float after = ((float) lum_after) / ((float) (count - i - 1));
        float diff = before - after;
        if (diff < 0) {
            diff *= -1;
        }
        if (diff > max_diff) {
            res = i;
            max_diff = diff;
        }
    }
    return res;
}

point_t luminance_probe_top(image_t *image, unsigned x) {
    unsigned height = real_image_height(image);
    point_t pt = {
            .x = x,
            .y = luminance_probe(image, create_plane_vector(image, image->y, x, 0, 0, 1, height / 2))
    };
    return pt;
}

point_t luminance_probe_left(image_t *image, unsigned y) {
    unsigned width = real_image_width(image);
    point_t pt = {
            .x = luminance_probe(image, create_plane_vector(image, image->y, 0, y, 1, 0, width / 2)),
            .y = y
    };
    return pt;
}

point_t luminance_probe_bottom(image_t *image, unsigned x) {
    unsigned height = real_image_height(image);
    point_t pt = {
            .x = x,
            .y = height - luminance_probe(image, create_plane_vector(image, image->y, x, height - 1, 0, -1, height / 2))
    };
    return pt;
}

point_t luminance_probe_right(image_t *image, unsigned y) {
    unsigned width = real_image_width(image);
    point_t pt = {
            .x = width - luminance_probe(image, create_plane_vector(image, image->y, width - 1, y, -1, 0, width / 2)),
            .y = y
    };
    return pt;
}
