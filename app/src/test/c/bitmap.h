#ifndef MASS_SCANNER_BITMAP_H
#define MASS_SCANNER_BITMAP_H

#include <image.h>

int read_bitmap(const char *filename, image_t *image);
void free_bitmap(image_t *image);

#endif
