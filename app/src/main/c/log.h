#ifndef MASS_SCANNER_LOG_H
#define MASS_SCANNER_LOG_H

#define LOG_LEVEL_VERBOSE (0)
#define LOG_LEVEL_DEBUG (1)
#define LOG_LEVEL_INFO (2)
#define LOG_LEVEL_WARNING (3)
#define LOG_LEVEL_ERROR (4)
#define LOG_LEVEL_WTF (5)

void log_v(const char *tag, const char *format, ...);
void log_d(const char *tag, const char *format, ...);
void log_i(const char *tag, const char *format, ...);
void log_w(const char *tag, const char *format, ...);
void log_e(const char *tag, const char *format, ...);
void log_wtf(const char *tag, const char *format, ...);

#endif
