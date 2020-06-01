#include <stdio.h>
#include <image.h>
#include <luminance_probe.h>
#include <main.h>
#include "bitmap.h"
#include "testing.h"

TEST("Bipartite Luminance Probe", bipartite_luminance_probe) {
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
    unsigned width = real_image_width(&image);
    unsigned height = real_image_height(&image);
    assert_that(width == 48 && height == 32, "Image size incorrect.  Expected 48x32, but got %dx%d", width, height);
    for (unsigned i = 0; i < 32; ++i) {
        if (left[i] > 0) {
            point_t pt = luminance_probe_left(&image, i);
            assert_that(pt.x == left[i] && pt.y == i, "Probe left (y = %d) expected (%d, %d), but got (%d, %d)", i, left[i], i, pt.x, pt.y);
        }
        if (right[i] > 0) {
            point_t pt = luminance_probe_right(&image, i);
            assert_that(pt.x == right[i] && pt.y == i, "Probe right (y = %d) expected (%d, %d), but got (%d, %d)", i, right[i], i, pt.x, pt.y);
        }
    }
    for (unsigned i = 0; i < 48; ++i) {
        if (top[i] > 0) {
            point_t pt = luminance_probe_top(&image, i);
            assert_that(pt.x == i && pt.y == top[i], "Probe top (x = %d) expected (%d, %d), but got (%d, %d)", i, i, top[i], pt.x, pt.y);
        }
        if (bottom[i] > 0) {
            point_t pt = luminance_probe_bottom(&image, i);
            assert_that(pt.x == i && pt.y == bottom[i], "Probe bottom (x = %d) expected (%d, %d), but got (%d, %d)", i, i, bottom[i], pt.x, pt.y);
        }
    }
    free_bitmap(&image);
    pass();
}

TEST("Bipartite Luminance Probe (Rotated Clockwise)", bipartite_luminance_probe_CW) {
    static const signed right[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, 23, 23, 24,
        24, 24, 25, 25,  25, 26, 26, 26,
        26, 27, 27, 27,  28, 28, 28, 29,
        29, 27, 23, 19,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed top[] = {
        -1, -1, -1, -1,  -1, -1, 16, 16,
        15, 15, 15, 15,  14, 14, 14, 14,
        13, 13, 13, 13,  12, 12, 14, 17,
        20, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed left[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, 11,
         7,  4,  5,  5,   6,  6,  7,  7,
         8,  8,  9, 10,  10, 11, 11, 12,
        12, 13, 13, 14,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed bottom[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, 27, 28, 30,  32, 34, 36, 37,
        37, 37, 36, 36,  36, 36, 35, 35,
        35, 35, 34, 34,  -1, -1, -1, -1
    };
    image_t image;
    if (read_bitmap("test_image.bmp", &image) < 0) {
        perror("read_bitmap");
        fail();
    }
    image.rotation = 90;
    unsigned width = real_image_width(&image);
    unsigned height = real_image_height(&image);
    assert_that(width == 32 && height == 48, "Image size incorrect.  Expected 32x48, but got %dx%d", width, height);
    for (unsigned i = 0; i < 48; ++i) {
        if (left[i] > 0) {
            point_t pt = luminance_probe_left(&image, i);
            assert_that(pt.x == left[i] && pt.y == i, "Probe left (y = %d) expected (%d, %d), but got (%d, %d)", i, left[i], i, pt.x, pt.y);
        }
        if (right[i] > 0) {
            point_t pt = luminance_probe_right(&image, i);
            assert_that(pt.x == right[i] && pt.y == i, "Probe right (y = %d) expected (%d, %d), but got (%d, %d)", i, right[i], i, pt.x, pt.y);
        }
    }
    for (unsigned i = 0; i < 32; ++i) {
        if (top[i] > 0) {
            point_t pt = luminance_probe_top(&image, i);
            assert_that(pt.x == i && pt.y == top[i], "Probe top (x = %d) expected (%d, %d), but got (%d, %d)", i, i, top[i], pt.x, pt.y);
        }
        if (bottom[i] > 0) {
            point_t pt = luminance_probe_bottom(&image, i);
            assert_that(pt.x == i && pt.y == bottom[i], "Probe bottom (x = %d) expected (%d, %d), but got (%d, %d)", i, i, bottom[i], pt.x, pt.y);
        }
    }
    free_bitmap(&image);
    pass();
}

TEST("Bipartite Luminance Probe (Rotated Counter-Clockwise)", bipartite_luminance_probe_CCW) {
    static const signed left[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  13,  9,  5,  3,
         3,  4,  4,  4,   5,  5,  5,  6,
         6,  6,  6,  7,   7,  7,  8,  8,
         8,  9,  9, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed bottom[] = {
        -1, -1, -1, -1,  -1, -1, -1, 28,
        31, 34, 36, 36,  35, 35, 35, 35,
        34, 34, 34, 34,  33, 33, 33, 33,
        32, 32, -1, -1,  -1, -1, -1, -1
    };
    static const signed right[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  18, 19, 19, 20,
        20, 21, 21, 22,  22, 23, 24, 24,
        25, 25, 26, 26,  27, 27, 28, 25,
        21, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed top[] = {
        -1, -1, -1, -1,  14, 14, 13, 13,
        13, 13, 12, 12,  12, 12, 11, 11,
        11, 12, 14, 16,  18, 20, 21, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    image_t image;
    if (read_bitmap("test_image.bmp", &image) < 0) {
        perror("read_bitmap");
        fail();
    }
    image.rotation = 270;
    unsigned width = real_image_width(&image);
    unsigned height = real_image_height(&image);
    assert_that(width == 32 && height == 48, "Image size incorrect.  Expected 32x48, but got %dx%d", width, height);
    for (unsigned i = 0; i < 48; ++i) {
        if (left[i] > 0) {
            point_t pt = luminance_probe_left(&image, i);
            assert_that(pt.x == left[i] && pt.y == i, "Probe left (y = %d) expected (%d, %d), but got (%d, %d)", i, left[i], i, pt.x, pt.y);
        }
        if (right[i] > 0) {
            point_t pt = luminance_probe_right(&image, i);
            assert_that(pt.x == right[i] && pt.y == i, "Probe right (y = %d) expected (%d, %d), but got (%d, %d)", i, right[i], i, pt.x, pt.y);
        }
    }
    for (unsigned i = 0; i < 32; ++i) {
        if (top[i] > 0) {
            point_t pt = luminance_probe_top(&image, i);
            assert_that(pt.x == i && pt.y == top[i], "Probe top (x = %d) expected (%d, %d), but got (%d, %d)", i, i, top[i], pt.x, pt.y);
        }
        if (bottom[i] > 0) {
            point_t pt = luminance_probe_bottom(&image, i);
            assert_that(pt.x == i && pt.y == bottom[i], "Probe bottom (x = %d) expected (%d, %d), but got (%d, %d)", i, i, bottom[i], pt.x, pt.y);
        }
    }
    free_bitmap(&image);
    pass();
}

TEST("Bipartite Luminance Probe (Flipped)", bipartite_luminance_probe_flipped) {
    static const signed bottom[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  19, 23, 27, 29,
        29, 28, 28, 28,  27, 27, 27, 26,
        26, 26, 26, 25,  25, 25, 24, 24,
        24, 23, 23, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed right[] = {
        -1, -1, -1, -1,  -1, -1, 32, 32,
        33, 33, 33, 33,  34, 34, 34, 34,
        35, 35, 35, 35,  36, 36, 34, 31,
        28, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed top[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  14, 13, 13, 12,
        12, 11, 11, 10,  10,  9,  8,  8,
         7,  7,  6,  6,   5,  5,  4,  7,
        11, -1, -1, -1,  -1, -1, -1, -1,
        -1, -1, -1, -1,  -1, -1, -1, -1
    };
    static const signed left[] = {
        -1, -1, -1, -1,  -1, -1, -1, -1,
        -1, 21, 20, 18,  16, 14, 12, 11,
        11, 11, 12, 12,  12, 12, 13, 13,
        13, 13, 14, 14,  -1, -1, -1, -1
    };
    image_t image;
    if (read_bitmap("test_image.bmp", &image) < 0) {
        perror("read_bitmap");
        fail();
    }
    image.rotation = 180;
    unsigned width = real_image_width(&image);
    unsigned height = real_image_height(&image);
    assert_that(width == 48 && height == 32, "Image size incorrect.  Expected 48x32, but got %dx%d", width, height);
    for (unsigned i = 0; i < 32; ++i) {
        if (left[i] > 0) {
            point_t pt = luminance_probe_left(&image, i);
            assert_that(pt.x == left[i] && pt.y == i, "Probe left (y = %d) expected (%d, %d), but got (%d, %d)", i, left[i], i, pt.x, pt.y);
        }
        if (right[i] > 0) {
            point_t pt = luminance_probe_right(&image, i);
            assert_that(pt.x == right[i] && pt.y == i, "Probe right (y = %d) expected (%d, %d), but got (%d, %d)", i, right[i], i, pt.x, pt.y);
        }
    }
    for (unsigned i = 0; i < 48; ++i) {
        if (top[i] > 0) {
            point_t pt = luminance_probe_top(&image, i);
            assert_that(pt.x == i && pt.y == top[i], "Probe top (x = %d) expected (%d, %d), but got (%d, %d)", i, i, top[i], pt.x, pt.y);
        }
        if (bottom[i] > 0) {
            point_t pt = luminance_probe_bottom(&image, i);
            assert_that(pt.x == i && pt.y == bottom[i], "Probe bottom (x = %d) expected (%d, %d), but got (%d, %d)", i, i, bottom[i], pt.x, pt.y);
        }
    }
    free_bitmap(&image);
    pass();
}
