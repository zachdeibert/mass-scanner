#include <math.h>
#include "geometry.h"
#include "perpendicular_angle_bisector.h"

ray2_t perpendicular_angle_bisector(vec2_t pivot, vec2_t a, vec2_t b, vec2_t inside) {
    ray2_t r = angle_bisector(pivot, a, b, inside);
    r.angle += asinf(1);
    return r;
}

void perpendicular_angle_bisector_box(vec2_t *centers, vec2_t *corners) {
    ray2_t a = perpendicular_angle_bisector(centers[0], centers[3], centers[1], centers[2]);
    ray2_t b = perpendicular_angle_bisector(centers[1], centers[0], centers[2], centers[3]);
    ray2_t c = perpendicular_angle_bisector(centers[2], centers[1], centers[3], centers[0]);
    ray2_t d = perpendicular_angle_bisector(centers[3], centers[2], centers[0], centers[1]);
    corners[0] = solve_intersection(a, b);
    corners[1] = solve_intersection(b, c);
    corners[2] = solve_intersection(c, d);
    corners[3] = solve_intersection(d, a);
}
