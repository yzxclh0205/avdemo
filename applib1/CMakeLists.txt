
cmake_minimum_required(VERSION 3.4.1)

# 设置要添加的头文件路径
include_directories(src/main/jni)

# 设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/jniLibs/${ANDROID_ABI})


add_library( # Sets the name of the library.
             lh-jni-lib-1

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/lh-jni-lib-1.c)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( # Specifies the target library.

                       lh-jni-lib-1
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )