cmake_minimum_required(VERSION 3.22.1)

project(kungandroid)

add_library(native SHARED
    native-stub.cpp
)

find_library(log-lib log)

target_link_libraries(native
    ${log-lib}
)
