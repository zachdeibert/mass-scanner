#include <stdint.h>
#include "image.h"

unsigned realImageWidth(image_t *image) {
    if ((image->rotation >= 45 && image->rotation < 135) || (image->rotation >= 225 && image->rotation < 315)) {
        return image->height;
    }
    return image->width;
}

unsigned realImageHeight(image_t *image) {
    if ((image->rotation >= 45 && image->rotation < 135) || (image->rotation >= 225 && image->rotation < 315)) {
        return image->width;
    }
    return image->height;
}

plane_vector_t createPlaneVector(image_t *image, plane_t plane, unsigned x, unsigned y, signed dx, signed dy, unsigned count) {
    plane_vector_t vec;
    if (image->rotation < 45 || image->rotation >= 315) {
    } else if (image->rotation < 135) {
        unsigned s = x;
        x = image->height - y;
        y = s;
        signed t = dx;
        dx = -dy;
        dy = t;
    } else if (image->rotation < 225) {
        x = image->width - x;
        y = image->height - y;
        dx = -dx;
        dy = -dy;
    } else {
        unsigned s = x;
        x = y;
        y = image->width - x;
        signed t = dx;
        dx = dy;
        dy = -t;
    }
    vec.stride = dx * plane.pixel_stride + dy * plane.row_stride;
    vec.buf = vec.start = ((uint8_t *) plane.buf) + x * plane.pixel_stride + ((signed) y) * plane.row_stride - vec.stride;
    vec.end = vec.start + ((signed) count) * vec.stride;
    return vec;
}
