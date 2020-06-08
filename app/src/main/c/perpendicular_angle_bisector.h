#ifndef MASS_SCANNER_PERPENDICULAR_ANGLE_BISECTOR_H
#define MASS_SCANNER_PERPENDICULAR_ANGLE_BISECTOR_H

#include "geometry.h"

ray2_t perpendicular_angle_bisector(vec2_t pivot, vec2_t a, vec2_t b, vec2_t inside);
void perpendicular_angle_bisector_box(vec2_t *centers, vec2_t *corners);

#endif
