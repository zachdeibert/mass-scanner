#ifndef MASS_SCANNER_ERRORS_H
#define MASS_SCANNER_ERRORS_H

#include <stdint.h>

typedef int32_t error_t;

#define ERR_SUCCESS ((error_t) 0)
#define ERR_INVALID_PTR ((error_t) -1)
#define ERR_NOT_IMPLEMENTED ((error_t) -2)
#define ERR_ARGUMENT_NULL ((error_t) -3)
#define ERR_INVALID_BUFFER ((error_t) -4)

#endif
