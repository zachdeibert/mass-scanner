#include <stdio.h>
#include <image.h>
#include <luminanceProbe.h>
#include <main.h>
#include "bitmap.h"
#include "testing.h"

TEST("Black and White Bipartite Luminance Probe", blackWhiteLuminanceProbe) {
    static const signed top[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1,  9,  9,  8,
         8,  8,  7,  7,   7,  6,  6,  6,
         6,  5,  5,  5,   4,  4,  4,  3,
         3,  5,  9, 13,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed left[] = {
        -1, -1, -1, -1,  -1, -1, -1, 20,
        17, 14, 12, 12,  13, 13, 13, 13,
        14, 14, 14, 14,  15, 15, 15, 15,
        16, 16, -1, -1,  -1, -1, -1, -1
    };
    static const signed bottom[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, 21,
        25, 28, 27, 27,  26, 26, 25, 25,
        24, 24, 23, 22,  22, 21, 21, 20,
        20, 19, 19, 18,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed right[] = {
        -1, -1, -1, -1,  34, 34, 35, 35,
        35, 35, 36, 36,  36, 36, 37, 37,
        37, 36, 34, 32,  30, 28, 27, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    image_t image;
    if (read_bitmap("test_image.bmp", &image) < 0) {
        perror("read_bitmap");
        fail();
    }
    for (unsigned i = 0; i < 32; ++i) {
        if (left[i] > 0) {
            point_t pt = luminanceProbeLeft(&image, i);
            assertThat(pt.x == left[i] && pt.y == i, "Probe left (y = %d) expected (%d, %d), but got (%d, %d)", i, left[i], i, pt.x, pt.y);
        }
        if (right[i] > 0) {
            point_t pt = luminanceProbeRight(&image, i);
            assertThat(pt.x == right[i] && pt.y == i, "Probe right (y = %d) expected (%d, %d), but got (%d, %d)", i, right[i], i, pt.x, pt.y);
        }
    }
    for (unsigned i = 0; i < 48; ++i) {
        if (top[i] > 0) {
            point_t pt = luminanceProbeTop(&image, i);
            assertThat(pt.x == i && pt.y == top[i], "Probe top (x = %d) expected (%d, %d), but got (%d, %d)", i, i, top[i], pt.x, pt.y);
        }
        if (bottom[i] > 0) {
            point_t pt = luminanceProbeBottom(&image, i);
            assertThat(pt.x == i && pt.y == bottom[i], "Probe bottom (x = %d) expected (%d, %d), but got (%d, %d)", i, i, bottom[i], pt.x, pt.y);
        }
    }
    free_bitmap(&image);
    pass();
}
