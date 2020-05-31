cmake_minimum_required(VERSION 3.10.2)
project(mass-scanner-natives-test)

set(CTEST_CUSTOM_TESTS_IGNORE
    # Put tests here to ignore them
)

set(sources
    bitmap.c
    main.c
    testing.c
)
add_custom_target(copyResources ALL
    COMMAND ${CMAKE_COMMAND} -E copy "$<TARGET_FILE:mass-scanner-natives>" "$<TARGET_FILE_DIR:mass-scanner-natives-test>/$<TARGET_FILE_NAME:mass-scanner-natives>"
    COMMAND ${CMAKE_COMMAND} -E copy "${CMAKE_CURRENT_SOURCE_DIR}/test_image.bmp" "$<TARGET_FILE_DIR:mass-scanner-natives-test>/test_image.bmp"
)

add_subdirectory(../../.. lib)

add_compile_definitions(_CRT_SECURE_NO_WARNINGS)

file(GLOB files **.c)
set(gen_test_proto_code)
set(gen_test_array_code)
set(n_tests 0)
set(test_regex "(^|.*[ \t\r\n])TEST[ \t\r\n]*\\([ \t\r\n]*\"([^\"]+)\"[ \t\r\n]*,[ \t\r\n]*([^ \t\r\n]+)[ \t\r\n]*\\).*")
foreach(file IN LISTS files)
    file(STRINGS ${file} matches REGEX ${test_regex})
    if (NOT "${matches}" STREQUAL "")
        foreach(match IN LISTS matches)
            string(REGEX REPLACE ${test_regex} "\\2" name ${match})
            string(REGEX REPLACE ${test_regex} "\\3" func ${match})
            set(gen_test_proto_code "${gen_test_proto_code}void test_${func}();\n")
            set(gen_test_array_code "${gen_test_array_code}    test_${func},\n")
            add_test(NAME "\"${name}\"" COMMAND mass-scanner-natives-test ${n_tests})
            math(EXPR n_tests "${n_tests} + 1")
        endforeach(match)
        list(APPEND sources ${file})
    endif()
endforeach(file)
configure_file(tests.h.in "${CMAKE_CURRENT_BINARY_DIR}/tests.h")

add_executable(mass-scanner-natives-test ${sources})
target_link_libraries(mass-scanner-natives-test mass-scanner-natives)
target_include_directories(mass-scanner-natives-test PRIVATE ${CMAKE_CURRENT_BINARY_DIR})

enable_testing()
add_custom_target(check COMMAND ${CMAKE_CTEST_COMMAND} -C Debug DEPENDS mass-scanner-natives-test)
