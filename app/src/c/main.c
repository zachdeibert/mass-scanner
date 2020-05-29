#include <stdint.h>
#include "errors.h"
#include "state.h"

error_t process_init(state_t *state) {
    return ERR_SUCCESS;
}

error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment) {
    uint64_t total_luminance = 0;
    uint8_t *col = image->y.buf;
    for (int y = 0; y < image->height; ++y) {
        uint8_t *row = col;
        col += image->y.row_stride;
        for (int x = 0; x < image->width; ++x) {
            total_luminance += *row;
            row += image->y.pixel_stride;
        }
    }
    uint64_t max_luminance = 255L * ((uint64_t) image->width) * ((uint64_t) image->height);
    float luminance = ((float) total_luminance) / ((float) max_luminance);
    size_t dx = (size_t) (image->width * luminance);
    size_t dy = (size_t) (image->height * luminance);
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
