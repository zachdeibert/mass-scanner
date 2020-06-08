#include <stdio.h>
#include <geometry.h>
#include <luminance_probe.h>
#include <multi_probe_least_squares.h>
#include "bitmap.h"
#include "testing.h"

TEST("Mutli-Probe Least Squares Box", multi_probe_least_squares_box) {
    image_t image;
    if (read_bitmap("test_image.bmp", &image) < 0) {
        perror("read_bitmap");
        fail();
    }
    unsigned width = real_image_width(&image);
    unsigned height = real_image_height(&image);
    vec2_t centers[4] = {
        luminance_probe_top(&image, width / 2),
        luminance_probe_left(&image, height / 2),
        luminance_probe_bottom(&image, width / 2),
        luminance_probe_right(&image, height / 2)
    };
    vec2_t corners[4];
    least_squares_box(centers, corners, &image, 5);
    free_bitmap(&image);
}
