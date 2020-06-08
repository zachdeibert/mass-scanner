#include <math.h>
#include <stdint.h>
#include <stdio.h>
#include "geometry.h"
#include "image.h"
#include "luminance_probe.h"
#include "multi_probe_least_squares.h"

typedef struct {
    vec2_iterator_t parent;
    image_t *image;
    ls_probe_func_t probe;
    vec2_t *centers;
    unsigned samples;
    unsigned i;
} least_squares_iterator_t;

static int ls_iterator_next(vec2_iterator_t *_it, vec2_t *vec) {
    least_squares_iterator_t *it = (least_squares_iterator_t *) _it;
    if (it->i < it->samples) {
        *vec = it->probe(it->image, it->i, it->centers, it->samples);
        ++it->i;
        return 1;
    }
    return 0;
}

ray2_t least_squares_edge(image_t *image, ls_probe_func_t probe, vec2_t *centers, unsigned samples) {
    least_squares_iterator_t it = {
        .parent = {
            .next = ls_iterator_next
        },
        .image = image,
        .probe = probe,
        .centers = centers,
        .samples = samples,
        .i = 0
    };
    return linear_regression(&it.parent);
}

void least_squares_box(vec2_t *centers, vec2_t *corners, image_t *image, unsigned samples) {
    ray2_t top = least_squares_top(image, centers, samples);
    ray2_t left = least_squares_left(image, centers, samples);
    ray2_t bottom = least_squares_bottom(image, centers, samples);
    ray2_t right = least_squares_right(image, centers, samples);
    corners[0] = solve_intersection(top, left);
    corners[1] = solve_intersection(left, bottom);
    corners[2] = solve_intersection(bottom, right);
    corners[3] = solve_intersection(right, top);
}

vec2_t least_squares_probe_top(image_t *image, unsigned i, vec2_t *centers, unsigned count) {
    vec2_t min = centers[1];
    vec2_t max = centers[3];
    float pos = min.x + (max.x - min.x) * (i + 1) / (count + 1);
    return luminance_probe_top(image, (unsigned) pos);
}

vec2_t least_squares_probe_left(image_t *image, unsigned i, vec2_t *centers, unsigned count) {
    vec2_t min = centers[0];
    vec2_t max = centers[2];
    float pos = min.y + (max.y - min.y) * (i + 1) / (count + 1);
    vec2_t p = luminance_probe_left(image, (unsigned) pos);
    float t = p.x;
    p.x = p.y;
    p.y = t;
    return p;
}

vec2_t least_squares_probe_bottom(image_t *image, unsigned i, vec2_t *centers, unsigned count) {
    vec2_t min = centers[1];
    vec2_t max = centers[3];
    float pos = min.x + (max.x - min.x) * (i + 1) / (count + 1);
    return luminance_probe_bottom(image, (unsigned) pos);
}

vec2_t least_squares_probe_right(image_t *image, unsigned i, vec2_t *centers, unsigned count) {
    vec2_t min = centers[0];
    vec2_t max = centers[2];
    float pos = min.y + (max.y - min.y) * (i + 1) / (count + 1);
    vec2_t p = luminance_probe_right(image, (unsigned) pos);
    float t = p.x;
    p.x = p.y;
    p.y = t;
    return p;
}

ray2_t least_squares_top(image_t *image, vec2_t *centers, unsigned samples) {
    return least_squares_edge(image, least_squares_probe_top, centers, samples);
}

ray2_t least_squares_left(image_t *image, vec2_t *centers, unsigned samples) {
    ray2_t r = least_squares_edge(image, least_squares_probe_left, centers, samples);
    r.angle = -(r.angle + asinf(1));
    return r;
}

ray2_t least_squares_bottom(image_t *image, vec2_t *centers, unsigned samples) {
    return least_squares_edge(image, least_squares_probe_bottom, centers, samples);
}

ray2_t least_squares_right(image_t *image, vec2_t *centers, unsigned samples) {
    ray2_t r = least_squares_edge(image, least_squares_probe_right, centers, samples);
    r.angle = -(r.angle + asinf(1));
    return r;
}
