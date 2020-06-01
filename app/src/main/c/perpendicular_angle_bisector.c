#include <math.h>
#include "main.h"
#include "perpendicular_angle_bisector.h"

float perpendicular_angle_bisector(point_t pivot, point_t a, point_t b, point_t inside) {
    float angle0 = atanf(((float) (((signed) pivot.y) - ((signed) a.y))) / ((float) (((signed) pivot.x) - ((signed) a.x))));
    float angle1 = atanf(((float) (((signed) pivot.y) - ((signed) b.y))) / ((float) (((signed) pivot.x) - ((signed) b.x))));
    float insideAngle = atanf(((float) (((signed) pivot.y) - ((signed) inside.y))) / ((float) (((signed) pivot.x) - ((signed) inside.x))));
    float pi2 = asinf(1);
    if (angle0 - insideAngle > pi2) {
        angle0 -= 2*pi2;
    } else if (insideAngle - angle0 > pi2) {
        angle0 += 2*pi2;
    }
    if (angle1 - insideAngle > pi2) {
        angle1 -= 2*pi2;
    } else if (insideAngle - angle1 > pi2) {
        angle1 += 2*pi2;
    }
    return (angle0 + angle1) / 2 + pi2;
}

point_t solve_intersection(point_t pt0, float angle0, point_t pt1, float angle1) {
    float c0 = cosf(angle0);
    float s0 = sinf(angle0);
    float t1 = tanf(angle1);
    float s = (((float) (((signed) pt1.x) - ((signed) pt0.x))) + ((float) (((signed) pt0.y) - ((signed) pt1.y))) / t1) / (c0 - s0 / t1);
    point_t p = {
        .x = (unsigned) (((float) pt0.x) + s * c0),
        .y = (unsigned) (((float) pt0.y) + s * s0)
    };
    return p;
}

void perpendicular_angle_bisector_box(point_t *centers, point_t *corners) {
    float a = perpendicular_angle_bisector(centers[0], centers[3], centers[1], centers[2]);
    float b = perpendicular_angle_bisector(centers[1], centers[0], centers[2], centers[3]);
    float c = perpendicular_angle_bisector(centers[2], centers[1], centers[3], centers[0]);
    float d = perpendicular_angle_bisector(centers[3], centers[2], centers[0], centers[1]);
    corners[0] = solve_intersection(centers[0], a, centers[1], b);
    corners[1] = solve_intersection(centers[1], b, centers[2], c);
    corners[2] = solve_intersection(centers[2], c, centers[3], d);
    corners[3] = solve_intersection(centers[3], d, centers[0], a);
}
