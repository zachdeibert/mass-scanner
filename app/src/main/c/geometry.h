#ifndef MASS_SCANNER_GEOMETRY_H
#define MASS_SCANNER_GEOMETRY_H

typedef struct {
    float x;
    float y;
} vec2_t;

typedef struct {
    vec2_t origin;
    float angle;
} ray2_t;

struct _vec2_iterator;
typedef struct _vec2_iterator vec2_iterator_t;
struct _vec2_iterator {
    int (*next)(vec2_iterator_t *, vec2_t *);
};

ray2_t angle_bisector(vec2_t pivot, vec2_t a, vec2_t b, vec2_t inside);
vec2_t solve_intersection(ray2_t a, ray2_t b);
ray2_t linear_regression(vec2_iterator_t *samples);

#endif
