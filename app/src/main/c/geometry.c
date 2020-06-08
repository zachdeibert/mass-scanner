#include <math.h>
#include "geometry.h"

ray2_t angle_bisector(vec2_t pivot, vec2_t a, vec2_t b, vec2_t inside) {
    float angle0 = atanf((pivot.y - a.y) / (pivot.x - a.x));
    float angle1 = atanf((pivot.y - b.y) / (pivot.x - b.x));
    float insideAngle = atanf((pivot.y - inside.y) / (pivot.x - inside.x));
    float pi = acosf(-1);
    float pi2 = asinf(1);
    if (angle0 - insideAngle > pi2) {
        angle0 -= pi;
    } else if (insideAngle - angle0 > pi2) {
        angle0 += pi;
    }
    if (angle1 - insideAngle > pi2) {
        angle1 -= pi;
    } else if (insideAngle - angle1 > pi2) {
        angle1 += pi;
    }
    ray2_t res = {
        .origin = pivot,
        .angle = (angle0 + angle1) / 2
    };
    return res;
}

vec2_t solve_intersection(ray2_t a, ray2_t b) {
    float c0 = cosf(a.angle);
    float s0 = sinf(a.angle);
    float t1 = tanf(b.angle);
    float s = (b.origin.x - a.origin.x + ((a.origin.y - b.origin.y) / t1)) / (c0 - s0 / t1);
    float x = a.origin.x + s * c0;
    float y = a.origin.y + s * s0;
    if (x < 0) {
        x = 0;
    }
    if (y < 0) {
        y = 0;
    }
    vec2_t res = {
        .x = x,
        .y = y
    };
    return res;
}

ray2_t linear_regression(vec2_iterator_t *samples) {
    float a = 0, b = 0, c = 0, d = 0, e = 0;
    vec2_t sample;
    while (samples->next(samples, &sample)) {
        a += 2 * sample.x;
        b += 2 * sample.x * sample.x;
        c += -2 * sample.x * sample.y;
        d += -2 * sample.y;
        e += 2;
    }
    float intercept = (c - d * b / a) / (e * b / a - a);
    ray2_t res = {
        .origin = {
            .x = 0,
            .y = intercept
        },
        .angle = atanf((-c - a * intercept) / b)
    };
    return res;
}
