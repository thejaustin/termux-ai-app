cmake_minimum_required(VERSION 3.22.1)

project("termux")

# Configure C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add source files
add_library(termux SHARED
    termux_pty.cpp
    pty_helper.cpp
)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)

# Link libraries
target_link_libraries(termux
    ${log-lib}
    ${android-lib}
)

# Include directories
target_include_directories(termux PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Compiler options
target_compile_options(termux PRIVATE
    -Wall
    -Wextra
    -Werror
    -fvisibility=hidden
)
