cmake_minimum_required(VERSION 3.4.1)

add_library(test-lib SHARED src/main/cpp/ndk-test.cpp)
target_link_libraries(test-lib android log)