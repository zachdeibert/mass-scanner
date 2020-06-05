#ifndef HISTORY_H
#define HISTORY_H

#include "image.h"
#include "main.h"

#define HISTORY_STATE_SCANNED (1)

#define HISTORY_PUSH_WRITE_AUGMENT (1)
#define HISTORY_PUSH_WRITE_IMAGE (2)

typedef struct {
    point_t corners[4];
} history_entry_t;

typedef struct {
    unsigned entries_length;
    unsigned entries_pos;
    unsigned until_enable;
    unsigned until_disable;
    unsigned state;
    history_entry_t sum;
    history_entry_t entries[0];
} history_t;

history_t *history_alloc(unsigned max_entries);
int history_push(image_t *image, history_t *state, point_t *corners_in, point_t *corners_out);

#endif
