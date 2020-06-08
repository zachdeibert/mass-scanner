#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "geometry.h"
#include "history.h"
#include "image.h"
#include "log.h"

#define TAG "history.c"
#define LOG_PERIOD 100
#define SCAN_DEVIATION 0.001
#define PREVIEW_DEVIATION_FACTOR 10
#define ENABLE_FRACTION 3 / 4
#define DISABLE_FRACTION 1 / 4

history_t *history_alloc(unsigned max_entries) {
    unsigned size = sizeof(history_t) + sizeof(history_entry_t) * max_entries;
    history_t *state = (history_t *) malloc(size);
    memset(state, 0, size);
    state->entries_length = max_entries;
    state->until_enable = max_entries * ENABLE_FRACTION;
    return state;
}

int history_push(image_t *image, history_t *state, vec2_t *corners_in, point_t *corners_out) {
    static unsigned counter = 0;
    int ret = 0;
    float mean[4][2];
    for (unsigned i = 0; i < 4; ++i) {
        state->sum.corners[i].x -= state->entries[state->entries_pos].corners[i].x;
        state->sum.corners[i].y -= state->entries[state->entries_pos].corners[i].y;
        state->entries[state->entries_pos].corners[i].x = corners_in[i].x;
        state->entries[state->entries_pos].corners[i].y = corners_in[i].y;
        state->sum.corners[i].x += corners_in[i].x;
        state->sum.corners[i].y += corners_in[i].y;
        mean[i][0] = state->sum.corners[i].x / state->entries_length;
        mean[i][1] = state->sum.corners[i].y / state->entries_length;
        corners_out[i].x = (unsigned) mean[i][0];
        corners_out[i].y = (unsigned) mean[i][1];
    }
    float deviation[4][2] = { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } };
    for (unsigned i = 0; i < state->entries_length; ++i) {
        for (unsigned j = 0; j < 4; ++j) {
            float diff = state->entries[i].corners[j].x - mean[j][0];
            deviation[j][0] += diff * diff;
            diff = state->entries[i].corners[j].y - mean[j][1];
            deviation[j][1] += diff * diff;
        }
    }
    float stddev = 0;
    for (unsigned i = 0; i < 4; ++i) {
        stddev += sqrtf(deviation[i][0] / (float) state->entries_length);
        stddev += sqrtf(deviation[i][1] / (float) state->entries_length);
    }
    stddev /= 8 * (image->width + image->height) / 2;
    if (state->until_enable == 0) {
        ret |= HISTORY_PUSH_WRITE_AUGMENT;
        if (stddev > SCAN_DEVIATION * PREVIEW_DEVIATION_FACTOR) {
            if (--state->until_disable == 0) {
                state->until_enable = state->entries_length * ENABLE_FRACTION;
                state->state &= ~HISTORY_STATE_SCANNED;
                ret ^= HISTORY_PUSH_WRITE_AUGMENT;
            }
        } else if (state->until_disable < state->entries_length * DISABLE_FRACTION) {
            ++state->until_disable;
        } else if (stddev < SCAN_DEVIATION && !(state->state & HISTORY_STATE_SCANNED)) {
            state->state |= HISTORY_STATE_SCANNED;
            ret |= HISTORY_PUSH_WRITE_IMAGE;
        }
    } else if (stddev < SCAN_DEVIATION * PREVIEW_DEVIATION_FACTOR) {
        if (--state->until_enable == 0) {
            state->until_disable = state->entries_length * DISABLE_FRACTION;
        }
    } else if (state->until_enable < state->entries_length * ENABLE_FRACTION) {
        ++state->until_enable;
    }
    if (++state->entries_pos == state->entries_length) {
        state->entries_pos = 0;
    }
    if (++counter > LOG_PERIOD) {
        counter = 0;
        log_d(TAG, "stddev = %f", stddev);
    }
    return ret;
}
