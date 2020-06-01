#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <tests.h>

static void usage(const char *err, const char **argv);
static void do_chdir(const char *path);

int main(int argc, const char **argv) {
    int idx = -1;
    if (argc == 1) {
        FILE *file = fopen(".vscode/debug_profile.txt", "r");
        if (file) {
            fscanf(file, "%d", &idx);
            fclose(file);
        }
    }
    if (idx < 0) {
        if (argc != 2) {
            usage("Invalid number of arguments", argv);
        }
        if (sscanf(argv[1], "%d", &idx) != 1) {
            usage("Test number must be an int", argv);
        }
    }
    if (idx < 0 || idx >= tests_count) {
        usage("Test number out of range", argv);
    }
    char *slash = strrchr(*argv, '/');
    if (slash) {
        *slash = 0;
        do_chdir(*argv);
    }
#ifdef WIN32
    else {
        slash = strrchr(*argv, '\\');
        if (slash) {
            *slash = 0;
            do_chdir(*argv);
        }
    }
#endif
    tests[idx]();
    return EXIT_SUCCESS;
}

void usage(const char *err, const char **argv) {
    fprintf(stderr, "%s\nUsage: %s <test number>\n", err, *argv);
    exit(EXIT_FAILURE);
}

#ifdef WIN32
#include <Windows.h>

static void do_chdir(const char *path) {
    if (!SetCurrentDirectory(path)) {
        fflush(stdout);
        fprintf(stderr, "chdir: Error %d\n", GetLastError());
        exit(EXIT_FAILURE);
    }
}
#else
#include <unistd.h>

static void do_chdir(const char *path) {
    if (chdir(path) < 0) {
        perror("chdir");
        exit(EXIT_FAILURE);
    }
}
#endif
