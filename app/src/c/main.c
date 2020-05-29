#include "errors.h"
#include "state.h"

error_t process_init(state_t *state) {
    return ERR_SUCCESS;
}

error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment) {
    size_t dx = image->width / 10;
    size_t dy = image->height / 10;
    augment->valid = 1;
    augment->points[0].x = dx;
    augment->points[0].y = dy;
    augment->points[1].x = image->width - dx;
    augment->points[1].y = dy;
    augment->points[2].x = image->width - dx;
    augment->points[2].y = image->height - dy;
    augment->points[3].x = dx;
    augment->points[3].y = image->height - dy;
    return ERR_SUCCESS;
}
