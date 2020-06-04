#ifndef HISTORY_H
#define HISTORY_H

#include "image.h"
#include "main.h"

typedef struct {
    point_t corners[4];
} history_entry_t;

typedef struct {
    unsigned entries_length;
    unsigned entries_pos;
    unsigned until_enable;
    unsigned until_disable;
    history_entry_t sum;
    history_entry_t entries[0];
} history_t;

history_t *history_alloc(unsigned max_entries);
int history_push(image_t *image, history_t *state, point_t *corners_in, point_t *corners_out);

#endif
