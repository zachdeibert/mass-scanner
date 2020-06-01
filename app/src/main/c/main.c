#include <stdint.h>
#include "errors.h"
#include "image.h"
#include "luminanceProbe.h"
#include "main.h"
#include "state.h"

error_t process_init(state_t *state) {
    return ERR_SUCCESS;
}

error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment) {
    augment->valid = 1;
    unsigned width = real_image_width(image);
    unsigned height = real_image_height(image);
    augment->points[0] = luminance_probe_top(image, width / 2);
    augment->points[1] = luminance_probe_left(image, height / 2);
    augment->points[2] = luminance_probe_bottom(image, width / 2);
    augment->points[3] = luminance_probe_right(image, height / 2);
    return ERR_SUCCESS;
}
