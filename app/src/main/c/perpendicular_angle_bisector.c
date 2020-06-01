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

point_t solve_intersection(float x0, float y0, float angle0, float x1, float y1, float angle1) {
    float c0 = cosf(angle0);
    float s0 = sinf(angle0);
    float t1 = tanf(angle1);
    float s = (x1 - x0 + ((y0 - y1) / t1)) / (c0 - s0 / t1);
    float x = x0 + s * c0;
    float y = y0 + s * s0;
    if (x < 0) {
        x = 0;
    }
    if (y < 0) {
        y = 0;
    }
    point_t p = {
        .x = (unsigned) x,
        .y = (unsigned) y
    };
    return p;
}

void perpendicular_angle_bisector_box(point_t *centers, point_t *corners) {
    float a = perpendicular_angle_bisector(centers[0], centers[3], centers[1], centers[2]);
    float b = perpendicular_angle_bisector(centers[1], centers[0], centers[2], centers[3]);
    float c = perpendicular_angle_bisector(centers[2], centers[1], centers[3], centers[0]);
    float d = perpendicular_angle_bisector(centers[3], centers[2], centers[0], centers[1]);
    corners[0] = solve_intersection((float) centers[0].x, (float) centers[0].y, a, (float) centers[1].x, (float) centers[1].y, b);
    corners[1] = solve_intersection((float) centers[1].x, (float) centers[1].y, b, (float) centers[2].x, (float) centers[2].y, c);
    corners[2] = solve_intersection((float) centers[2].x, (float) centers[2].y, c, (float) centers[3].x, (float) centers[3].y, d);
    corners[3] = solve_intersection((float) centers[3].x, (float) centers[3].y, d, (float) centers[0].x, (float) centers[0].y, a);
}
