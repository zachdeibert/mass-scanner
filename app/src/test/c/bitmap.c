#include <errno.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <image.h>
#include "bitmap.h"

#ifdef WIN32
#pragma pack (push, 1)
#endif
typedef struct
#ifndef WIN32
__attribute__((packed))
#endif
{
    uint16_t magic;
    uint32_t file_size;
    uint16_t resv0;
    uint16_t resv1;
    uint32_t buf_addr;
    uint32_t dib_header_size;
    uint32_t width;
    uint32_t height;
    uint16_t n_color_planes;
    uint16_t bits_per_pixel;
    uint32_t compression_alg;
} bitmap_header_t;
#ifdef WIN32
#pragma pack (pop)
#endif

int read_bitmap(const char *filename, image_t *image) {
    errno = 0;
    FILE *file = fopen(filename, "rb");
    if (!file) {
        return -1;
    }
    bitmap_header_t header;
    if (fread(&header, sizeof(bitmap_header_t), 1, file) < 1) {
        int e = ferror(file);
        fclose(file);
        errno = e;
        return -1;
    }
    if (header.magic != 0x4D42) {
        fflush(stdout);
        fprintf(stderr, "Bitmap magic wrong.  Expected 0x4D42 but got 0x%04X.\n", header.magic);
        errno = EIO;
        return -1;
    }
    if (header.n_color_planes != 1) {
        fflush(stdout);
        fprintf(stderr, "Bitmap number of color planes wrong.  Expected 1 but got %d.\n", header.n_color_planes);
        errno = EIO;
        return -1;
    }
    if (header.bits_per_pixel != 24) {
        fflush(stdout);
        fprintf(stderr, "Bitmap bits per pixel wrong.  Expected 24 but got %d.\n", header.bits_per_pixel);
        errno = EIO;
        return -1;
    }
    if (header.compression_alg != 0) {
        fflush(stdout);
        fprintf(stderr, "Bitmap compression algorithm wrong.  Expected 0 (BI_RGB) but got %d.\n", header.compression_alg);
        errno = EIO;
        return -1;
    }
    fseek(file, header.buf_addr, SEEK_SET);
    int len = header.width * header.height * 3;
    void *buf = malloc(len);
    if (fread(buf, 1, len, file) < len) {
        int e = ferror(file);
        fclose(file);
        errno = e;
        return -1;
    }
    fclose(file);
    image->width = header.width;
    image->height = header.height;
    image->rotation = 0;
    image->y.buf = ((uint8_t *) buf) + 2 + header.width * (header.height - 1) * 3;
    image->y.buf_len = len - 2;
    image->y.pixel_stride = 3;
    image->y.row_stride = header.width * -3;
    image->u.buf = ((uint8_t *) buf) + 1 + header.width * (header.height - 1) * 3;
    image->u.buf_len = len - 2;
    image->u.pixel_stride = 3;
    image->u.row_stride = header.width * -3;
    image->v.buf = ((uint8_t *) buf) + 0 + header.width * (header.height - 1) * 3;
    image->v.buf_len = len - 2;
    image->v.pixel_stride = 3;
    image->v.row_stride = header.width * -3;
    return 0;
}

void free_bitmap(image_t *image) {
    free(((uint8_t *) image->y.buf) - 2 - image->width * (image->height - 1) * 3);
}
