#ifndef MASS_SCANNER_PERPENDICULAR_ANGLE_BISECTOR_H
#define MASS_SCANNER_PERPENDICULAR_ANGLE_BISECTOR_H

#include "main.h"

float perpendicular_angle_bisector(point_t pivot, point_t a, point_t b, point_t inside);
point_t solve_intersection(point_t pt0, float angle0, point_t pt1, float angle1);
void perpendicular_angle_bisector_box(point_t *centers, point_t *corners);

#endif
