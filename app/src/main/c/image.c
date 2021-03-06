#include <stdint.h>
#include "image.h"

unsigned real_image_width(image_t *image) {
    if ((image->rotation >= 45 && image->rotation < 135) || (image->rotation >= 225 && image->rotation < 315)) {
        return image->height;
    }
    return image->width;
}

unsigned real_image_height(image_t *image) {
    if ((image->rotation >= 45 && image->rotation < 135) || (image->rotation >= 225 && image->rotation < 315)) {
        return image->width;
    }
    return image->height;
}

plane_vector_t create_plane_vector(image_t *image, plane_t plane, unsigned x, unsigned y, signed dx, signed dy, unsigned count) {
    plane_vector_t vec;
    if (image->rotation < 45 || image->rotation >= 315) {
    } else if (image->rotation < 135) {
        unsigned s = x;
        x = y;
        y = image->height - 1 - s;
        signed t = dx;
        dx = dy;
        dy = -t;
    } else if (image->rotation < 225) {
        x = image->width - 1 - x;
        y = image->height - 1 - y;
        dx = -dx;
        dy = -dy;
    } else {
        unsigned s = x;
        x = image->width - 1 - y;
        y = s;
        signed t = dx;
        dx = -dy;
        dy = t;
    }
    vec.stride = dx * plane.pixel_stride + dy * plane.row_stride;
    vec.buf = vec.start = ((uint8_t *) plane.buf) + x * plane.pixel_stride + ((signed) y) * plane.row_stride - vec.stride;
    vec.end = vec.start + ((signed) count) * vec.stride;
    return vec;
}
