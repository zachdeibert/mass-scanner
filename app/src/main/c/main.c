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
    unsigned width = realImageWidth(image);
    unsigned height = realImageHeight(image);
    augment->points[0] = luminanceProbeTop(image, width / 2);
    augment->points[1] = luminanceProbeLeft(image, height / 2);
    augment->points[2] = luminanceProbeBottom(image, width / 2);
    augment->points[3] = luminanceProbeRight(image, height / 2);
    return ERR_SUCCESS;
}
