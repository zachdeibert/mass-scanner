#include <stdint.h>
#include "errors.h"
#include "image.h"
#include "luminance_probe.h"
#include "main.h"
#include "perpendicular_angle_bisector.h"
#include "state.h"

error_t process_init(state_t *state) {
    return ERR_SUCCESS;
}

error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment) {
    augment->valid = 1;
    unsigned width = real_image_width(image);
    unsigned height = real_image_height(image);
    point_t centers[4] = {
        luminance_probe_top(image, width / 2),
        luminance_probe_left(image, height / 2),
        luminance_probe_bottom(image, width / 2),
        luminance_probe_right(image, height / 2)
    };
    perpendicular_angle_bisector_box(centers, augment->points);
    return ERR_SUCCESS;
}
