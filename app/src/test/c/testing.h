#ifndef MASS_SCANNER_TESTING_H
#define MASS_SCANNER_TESTING_H

#define TEST(name, id) void test_ ## id()

void pass();
void fail();

void assert_that(int condition, const char *message, ...);

#endif
