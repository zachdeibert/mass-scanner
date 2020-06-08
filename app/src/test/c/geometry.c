#include <geometry.h>
#include "testing.h"

TEST("Solve Intersection", solve_intersection) {
    ray2_t pt0 = {
        .origin = {
            .x = 575,
            .y = 950
        },
        .angle = 2.67f
    };
    ray2_t pt1 = {
        .origin = {
            .x = 256,
            .y = 140
        },
        .angle = 0.118f
    };
    vec2_t intersection = solve_intersection(pt0, pt1);
    vec2_t exp = {
        .x = 1803.57f,
        .y = 323.47f
    };
    assert_that(float_equal(intersection.x, exp.x, 0.01f) && float_equal(intersection.y, exp.y, 0.01f), "Invalid result.  Expected (%.2f, %.2f), but got (%.2f, %.2f).", exp.x, exp.y, intersection.x, intersection.y);
}
