#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include "image.h"
#include "luminance_probe.h"
#include "main.h"
#include "multi_probe_least_squares.h"
#include "perpendicular_angle_bisector.h"

void least_squares_edge(image_t *image, ls_probe_func_t probe, point_t *centers, unsigned samples, float *angle, float *intercept) {
    int64_t a = 0, b = 0, c = 0, d = 0, e = 0;
    for (unsigned i = 0; i < samples; ++i) {
        point_t pt = probe(image, i, centers, samples);
        printf("Sample: (%d, %d)\n", pt.x, pt.y);
        a += 2 * pt.x;
        b += 2 * pt.x * pt.x;
        c += -2 * ((int64_t) pt.x) * ((int64_t) pt.y);
        d += -2 * ((int64_t) pt.y);
        e += 2;
    }
    *intercept = (((float) c) - ((float) d) * ((float) b) / ((float) a)) / (((float) e) * ((float) b) / ((float) a) - ((float) a));
    *angle = atanf((-((float) c) - ((float) a) * *intercept) / ((float) b));
    printf("intercept = %f, angle = %f\n", *intercept, *angle);
}

void least_squares_box(point_t *centers, point_t *corners, image_t *image, unsigned samples) {
    float a_top, a_left, a_bottom, a_right, i_top, i_left, i_bottom, i_right;
    least_squares_top(image, centers, samples, &a_top, &i_top);
    least_squares_left(image, centers, samples, &a_left, &i_left);
    least_squares_bottom(image, centers, samples, &a_bottom, &i_bottom);
    least_squares_right(image, centers, samples, &a_right, &i_right);
    corners[0] = solve_intersection(0, i_top, a_top, i_left, 0, a_left);
    corners[1] = solve_intersection(i_left, 0, a_left, 0, i_bottom, a_bottom);
    corners[2] = solve_intersection(0, i_bottom, a_bottom, i_right, 0, a_right);
    corners[3] = solve_intersection(i_right, 0, a_right, 0, i_top, a_top);
}

point_t least_squares_probe_top(image_t *image, unsigned i, point_t *centers, unsigned count) {
    point_t min = centers[1];
    point_t max = centers[3];
    unsigned pos = min.x + (max.x - min.x) * (i + 1) / (count + 1);
    return luminance_probe_top(image, pos);
}

point_t least_squares_probe_left(image_t *image, unsigned i, point_t *centers, unsigned count) {
    point_t min = centers[0];
    point_t max = centers[2];
    unsigned pos = min.y + (max.y - min.y) * (i + 1) / (count + 1);
    point_t p = luminance_probe_left(image, pos);
    unsigned t = p.x;
    p.x = p.y;
    p.y = t;
    return p;
}

point_t least_squares_probe_bottom(image_t *image, unsigned i, point_t *centers, unsigned count) {
    point_t min = centers[1];
    point_t max = centers[3];
    unsigned pos = min.x + (max.x - min.x) * (i + 1) / (count + 1);
    return luminance_probe_bottom(image, pos);
}

point_t least_squares_probe_right(image_t *image, unsigned i, point_t *centers, unsigned count) {
    point_t min = centers[0];
    point_t max = centers[2];
    unsigned pos = min.y + (max.y - min.y) * (i + 1) / (count + 1);
    point_t p = luminance_probe_right(image, pos);
    unsigned t = p.x;
    p.x = p.y;
    p.y = t;
    return p;
}

void least_squares_top(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept) {
    least_squares_edge(image, least_squares_probe_top, centers, samples, angle, intercept);
}

void least_squares_left(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept) {
    float a;
    least_squares_edge(image, least_squares_probe_left, centers, samples, &a, intercept);
    *angle = -(a + asinf(1));
}

void least_squares_bottom(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept) {
    least_squares_edge(image, least_squares_probe_bottom, centers, samples, angle, intercept);
}

void least_squares_right(image_t *image, point_t *centers, unsigned samples, float *angle, float *intercept) {
    float a;
    least_squares_edge(image, least_squares_probe_right, centers, samples, &a, intercept);
    *angle = -(a + asinf(1));
}
