#ifndef MASS_SCANNER_STATE_H
#define MASS_SCANNER_STATE_H

#include <stdint.h>
#include "errors.h"

#define STATE_T_MAGIC 0xCF26092C

typedef struct {
    int32_t magic;
    int32_t setup_error;
    int32_t *parameters;
} state_t;

error_t process_init(state_t *state);

#endif
