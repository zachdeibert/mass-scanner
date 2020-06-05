#include <stdint.h>
#include <stdlib.h>
#include "errors.h"
#include "history.h"
#include "image.h"
#include "luminance_probe.h"
#include "main.h"
#include "multi_probe_least_squares.h"
#include "perpendicular_angle_bisector.h"
#include "state.h"

error_t process_init(state_t *state) {
    state->history = history_alloc(10);
    return ERR_SUCCESS;
}

error_t process_free(state_t *state) {
    free(state->history);
    return ERR_SUCCESS;
}

error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment) {
    unsigned width = real_image_width(image);
    unsigned height = real_image_height(image);
    point_t centers[4] = {
        luminance_probe_top(image, width / 2),
        luminance_probe_left(image, height / 2),
        luminance_probe_bottom(image, width / 2),
        luminance_probe_right(image, height / 2)
    };
    point_t corners[4];
    least_squares_box(centers, corners, image, 5);
    int act = history_push(image, state->history, corners, augment->points);
    if (act & HISTORY_PUSH_WRITE_AUGMENT) {
        augment->valid = 1;
    }
    if (act & HISTORY_PUSH_WRITE_IMAGE) {
        bitmap->captured = 1;
    }
    return ERR_SUCCESS;
}
