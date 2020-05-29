#include <stdlib.h>
#include <jni.h>
#include "errors.h"
#include "state.h"

#define REQ_VERSION (JNI_VERSION_1_4)
#define PARAMETERS_SIZE (19 * sizeof(int32_t))
#define OUTPUT_FLAG_IMAGE_READY (0x00000001)
#define OUTPUT_FLAG_AUGMENT_READY (0x00000002)
#define OUTPUT_FLAG_SETUP_COMPLETE (0x00000004)

// Parameter IntBuffer fields:
#define PARAMETER_IMAGE_WIDTH (0)
#define PARAMETER_IMAGE_HEIGHT (1)
#define PARAMETER_Y_PIXEL_STRIDE (2)
#define PARAMETER_Y_ROW_STRIDE (3)
#define PARAMETER_U_PIXEL_STRIDE (4)
#define PARAMETER_U_ROW_STRIDE (5)
#define PARAMETER_V_PIXEL_STRIDE (6)
#define PARAMETER_V_ROW_STRIDE (7)

#define PARAMETER_OUTPUT_FLAGS (8)
#define PARAMETER_OUTPUT_IMAGE_WIDTH (9)
#define PARAMETER_OUTPUT_IMAGE_HEIGHT (10)
#define PARAMETER_AUGMENT_POINT_1X (11)
#define PARAMETER_AUGMENT_POINT_1Y (12)
#define PARAMETER_AUGMENT_POINT_2X (13)
#define PARAMETER_AUGMENT_POINT_2Y (14)
#define PARAMETER_AUGMENT_POINT_3X (15)
#define PARAMETER_AUGMENT_POINT_3Y (16)
#define PARAMETER_AUGMENT_POINT_4X (17)
#define PARAMETER_AUGMENT_POINT_4Y (18)

#define SUPPRESS_UNUSED_PARAMETER(x) do { (void) x; } while (0)

JNIEXPORT jlong JNICALL jni_setup(JNIEnv *env, jobject this, jobject parameters) {
    SUPPRESS_UNUSED_PARAMETER(env);
    SUPPRESS_UNUSED_PARAMETER(this);
    state_t *state = (state_t *) malloc(sizeof(state_t));
    if (!state) {
        return 0;
    }
    state->magic = STATE_T_MAGIC;
    if (!parameters) {
        state->setup_error = ERR_ARGUMENT_NULL;
        return (jlong) state;
    }
    if ((*env)->GetDirectBufferCapacity(env, parameters) != PARAMETERS_SIZE) {
        state->setup_error = ERR_INVALID_BUFFER;
        return (jlong) state;
    }
    state->parameters = (*env)->GetDirectBufferAddress(env, parameters);
    state->parameters[PARAMETER_OUTPUT_FLAGS] = OUTPUT_FLAG_SETUP_COMPLETE;
    state->setup_error = process_init(state);
    return (jlong) state;
}

#define METHOD_HEAD \
    state_t *state; \
    do { \
        SUPPRESS_UNUSED_PARAMETER(env); \
        SUPPRESS_UNUSED_PARAMETER(this); \
        if (!ptr) { \
            return ERR_INVALID_PTR; \
        } \
        state = (state_t *) ptr; \
        if (state->magic != STATE_T_MAGIC) { \
            return ERR_INVALID_PTR; \
        } \
    } while (0)

JNIEXPORT jint JNICALL jni_get_setup_error(JNIEnv *env, jobject this, jlong ptr) {
    METHOD_HEAD;
    return state->setup_error;
}

JNIEXPORT jint JNICALL jni_process_frame(JNIEnv *env, jobject this, jlong ptr, jobject y, jobject u, jobject v, jobject bmp) {
    METHOD_HEAD;
    if (!y || !u || !v || !bmp) {
        return ERR_ARGUMENT_NULL;
    }
    image_t image = {
            .width = (size_t) state->parameters[PARAMETER_IMAGE_WIDTH],
            .height = (size_t) state->parameters[PARAMETER_IMAGE_HEIGHT],
            .y = {
                    .buf = (*env)->GetDirectBufferAddress(env, y),
                    .buf_len = (size_t) (*env)->GetDirectBufferCapacity(env, y),
                    .pixel_stride = (size_t) state->parameters[PARAMETER_Y_PIXEL_STRIDE],
                    .row_stride = (size_t) state->parameters[PARAMETER_Y_ROW_STRIDE],
            },
            .u = {
                    .buf = (*env)->GetDirectBufferAddress(env, u),
                    .buf_len = (size_t) (*env)->GetDirectBufferCapacity(env, u),
                    .pixel_stride = (size_t) state->parameters[PARAMETER_U_PIXEL_STRIDE],
                    .row_stride = (size_t) state->parameters[PARAMETER_U_ROW_STRIDE],
            },
            .v = {
                    .buf = (*env)->GetDirectBufferAddress(env, v),
                    .buf_len = (size_t) (*env)->GetDirectBufferCapacity(env, v),
                    .pixel_stride = (size_t) state->parameters[PARAMETER_V_PIXEL_STRIDE],
                    .row_stride = (size_t) state->parameters[PARAMETER_V_ROW_STRIDE],
            }
    };
    bitmap_t bitmap = {
            .captured = 0,
            .buf = (*env)->GetDirectBufferAddress(env, bmp),
            .buf_len = (size_t) (*env)->GetDirectBufferCapacity(env, bmp),
            .width = 0,
            .height = 0
    };
    augment_t augment = {
            .valid = 0,
            .points = {
                    { .x = 0, .y = 0 },
                    { .x = 0, .y = 0 },
                    { .x = 0, .y = 0 },
                    { .x = 0, .y = 0 }
            }
    };
    if (image.y.buf_len < 0 || image.u.buf_len < 0 || image.v.buf_len < 0 || bitmap.buf_len < 0) {
        return ERR_INVALID_BUFFER;
    }
    error_t err = process_main(state, &image, &bitmap, &augment);
    state->parameters[PARAMETER_OUTPUT_FLAGS] = 0 |
                    (bitmap.captured ? OUTPUT_FLAG_IMAGE_READY : 0) |
                    (augment.valid ? OUTPUT_FLAG_AUGMENT_READY : 0);
    state->parameters[PARAMETER_OUTPUT_IMAGE_WIDTH] = image.width;
    state->parameters[PARAMETER_OUTPUT_IMAGE_HEIGHT] = image.height;
    state->parameters[PARAMETER_AUGMENT_POINT_1X] = augment.points[0].x;
    state->parameters[PARAMETER_AUGMENT_POINT_1Y] = augment.points[0].y;
    state->parameters[PARAMETER_AUGMENT_POINT_2X] = augment.points[1].x;
    state->parameters[PARAMETER_AUGMENT_POINT_2Y] = augment.points[1].y;
    state->parameters[PARAMETER_AUGMENT_POINT_3X] = augment.points[2].x;
    state->parameters[PARAMETER_AUGMENT_POINT_3Y] = augment.points[2].y;
    state->parameters[PARAMETER_AUGMENT_POINT_4X] = augment.points[3].x;
    state->parameters[PARAMETER_AUGMENT_POINT_4Y] = augment.points[3].y;
    return err;
}

JNIEXPORT jint JNICALL jni_cleanup(JNIEnv *env, jobject this, jlong ptr) {
    METHOD_HEAD;
    state->magic = 0;
    free(state);
    return ERR_SUCCESS;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    SUPPRESS_UNUSED_PARAMETER(reserved);
    JNIEnv *env;
    int rc = (*vm)->GetEnv(vm, (void **) &env, REQ_VERSION);
    if (rc != JNI_OK) {
        return JNI_ERR;
    }
    jclass c = (*env)->FindClass(env, "com/github/zachdeibert/massscanner/ui/scan/ScanNative");
    if (!c) {
        return JNI_ERR;
    }
    static const JNINativeMethod methods[] = {
            { "setup", "(Ljava/nio/ByteBuffer;)J", jni_setup },
            { "setup_error", "(J)I", jni_get_setup_error },
            { "process_frame", "(JLjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I", jni_process_frame },
            { "cleanup", "(J)I", jni_cleanup }
    };
    rc = (*env)->RegisterNatives(env, c, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if (rc != JNI_OK) {
        return rc;
    }
    return REQ_VERSION;
}
