#ifndef MASS_SCANNER_STATE_H
#define MASS_SCANNER_STATE_H

#include <stdint.h>
#include "errors.h"
#include "history.h"
#include "jni_iface.h"
#include "main.h"

#define STATE_T_MAGIC 0xCF26092C

typedef struct {
    int32_t magic;
    int32_t setup_error;
    int32_t *parameters;
    jni_state_t *jni_state;
    history_t *history;
} state_t;

error_t process_init(state_t *state);
error_t process_free(state_t *state);
error_t process_main(state_t *state, image_t *image, bitmap_t *bitmap, augment_t *augment);

#endif
