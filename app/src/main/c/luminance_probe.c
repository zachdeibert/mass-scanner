#include <stdint.h>
#include "geometry.h"
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

vec2_t luminance_probe_top(image_t *image, unsigned x) {
    unsigned height = real_image_height(image);
    vec2_t pt = {
            .x = (float) x,
            .y = (float) luminance_probe(image, create_plane_vector(image, image->y, x, 0, 0, 1, height / 2))
    };
    return pt;
}

vec2_t luminance_probe_left(image_t *image, unsigned y) {
    unsigned width = real_image_width(image);
    vec2_t pt = {
            .x = (float) luminance_probe(image, create_plane_vector(image, image->y, 0, y, 1, 0, width / 2)),
            .y = (float) y
    };
    return pt;
}

vec2_t luminance_probe_bottom(image_t *image, unsigned x) {
    unsigned height = real_image_height(image);
    vec2_t pt = {
            .x = (float) x,
            .y = (float) (height - luminance_probe(image, create_plane_vector(image, image->y, x, height - 1, 0, -1, height / 2)))
    };
    return pt;
}

vec2_t luminance_probe_right(image_t *image, unsigned y) {
    unsigned width = real_image_width(image);
    vec2_t pt = {
            .x = (float) (width - luminance_probe(image, create_plane_vector(image, image->y, width - 1, y, -1, 0, width / 2))),
            .y = (float) y
    };
    return pt;
}
