#include <geometry.h>
#include <perpendicular_angle_bisector.h>
#include "testing.h"

TEST("Perpendicular Angle Bisector", perpendicular_angle_bisector) {
    vec2_t pivot = {
        .x = 4,
        .y = 2
    };
    vec2_t a = {
        .x = 8,
        .y = 2
    };
    vec2_t b = {
        .x = 4,
        .y = 8
    };
    vec2_t inside = {
        .x = 8,
        .y = 8
    };
    ray2_t ray = perpendicular_angle_bisector(pivot, a, b, inside);
    float exp = 2.356f;
    assert_that(float_equal(ray.angle, exp, 0.01f), "Invalid result.  Expected %.3f, but got %.3f.", exp, ray.angle);
    pivot.x = 1804;
    pivot.y = 324;
    a.x = 575;
    a.y = 950;
    b.x = 256;
    b.y = 140;
    inside.x = 3000;
    inside.y = 0;
    exp = 1.394f;
    ray = perpendicular_angle_bisector(pivot, a, b, inside);
    assert_that(float_equal(ray.angle, exp, 0.01f), "Invalid result.  Expected %.3f, but got %.3f.", exp, ray.angle);
}

TEST("Perpendicular Angle Bisector Box", perpendicular_angle_bisector_box) {
    vec2_t centers[] = {
        {
            .x = 500,
            .y = 800
        },
        {
            .x = 950,
            .y = 500
        },
        {
            .x = 500,
            .y = 150
        },
        {
            .x = 100,
            .y = 500
        }
    };
    vec2_t exp[] = {
        {
            .x = 938.59f,
            .y = 812.17f
        },
        {
            .x = 963.28f,
            .y = 136.61f
        },
        {
            .x = 87.26f,
            .y = 161.93f
        },
        {
            .x = 110.9f,
            .y = 789.2f
        }
    };
    vec2_t corners[4];
    perpendicular_angle_bisector_box(centers, corners);
    for (unsigned i = 0; i < 4; ++i) {
        assert_that(float_equal(exp[i].x, corners[i].x, 0.01f) && float_equal(exp[i].y, corners[i].y, 0.01f), "Invalid result for corner %d.  Expected (%.2f, %.2f), but got (%.2f, %.2f).", i, exp[i].x, exp[i].y, corners[i].x, corners[i].y);
    }
}
