cmake_minimum_required(VERSION 3.22.1)

project(kungandroid)

add_library(native SHARED
    native-stub.cpp
)

find_library(log-lib log)

target_link_libraries(native
    ${log-lib}
)

# 16 KB page size alignment (Android 15+ requirement)
target_link_options(native PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384"
)
