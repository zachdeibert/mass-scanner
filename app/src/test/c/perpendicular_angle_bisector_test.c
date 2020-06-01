#include <perpendicular_angle_bisector.h>
#include "testing.h"

TEST("Perpendicular Angle Bisector", perpendicular_angle_bisector) {
    point_t pivot = {
        .x = 4,
        .y = 2
    };
    point_t a = {
        .x = 8,
        .y = 2
    };
    point_t b = {
        .x = 4,
        .y = 8
    };
    point_t inside = {
        .x = 8,
        .y = 8
    };
    float angle = perpendicular_angle_bisector(pivot, a, b, inside);
    float exp = 2.356f;
    assert_that(float_equal(angle, exp, 0.01f), "Invalid result.  Expected %.3f, but got %.3f.", exp, angle);
    pivot.x = 1804;
    pivot.y = 324;
    a.x = 575;
    a.y = 950;
    b.x = 256;
    b.y = 140;
    inside.x = 3000;
    inside.y = 0;
    exp = 1.394f;
    angle = perpendicular_angle_bisector(pivot, a, b, inside);
    assert_that(float_equal(angle, exp, 0.01f), "Invalid result.  Expected %.3f, but got %.3f.", exp, angle);
}

TEST("Solve Intersection", solve_intersection) {
    point_t pt0 = {
        .x = 575,
        .y = 950
    };
    point_t pt1 = {
        .x = 256,
        .y = 140
    };
    point_t intersection = solve_intersection(pt0, 2.67f, pt1, 0.118f);
    point_t exp = {
        .x = 1803,
        .y = 323
    };
    assert_that(intersection.x == exp.x && intersection.y == exp.y, "Invalid result.  Expected (%d, %d), but got (%d, %d).", exp.x, exp.y, intersection.x, intersection.y);
}

TEST("Perpendicular Angle Bisector Box", perpendicular_angle_bisector_box) {
    point_t centers[] = {
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
    point_t exp[] = {
        {
            .x = 938,
            .y = 812
        },
        {
            .x = 963,
            .y = 136
        },
        {
            .x = 87,
            .y = 161
        },
        {
            .x = 110,
            .y = 789
        }
    };
    point_t corners[4];
    perpendicular_angle_bisector_box(centers, corners);
    for (unsigned i = 0; i < 4; ++i) {
        assert_that(exp[i].x == corners[i].x && exp[i].y == corners[i].y, "Invalid result for corner %d.  Expected (%d, %d), but got (%d, %d).", i, exp[i].x, exp[i].y, corners[i].x, corners[i].y);
    }
}
