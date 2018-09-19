
cmake_minimum_required(VERSION 3.4.1)

# 设置要添加的头文件路径
include_directories(src/main/jni/ffmpeg/include)
include_directories(src/main/jni/include)

# 设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/jniLibs/${ANDROID_ABI})


add_library( # Sets the name of the library.
             lh-jni-lib-1

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/jni/queue.c
             src/main/jni/lh_player.c
             src/main/jni/lh_ffmpeg_player_use_yuv.c
             src/main/jni/lh-jni-lib-1.c)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

#添加libavcodec.so
add_library( avcodec
             SHARED
             IMPORTED)
set_target_properties( avcodec
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libavcodec.so)

#添加libavfilter.so
add_library( avfilter
             SHARED
             IMPORTED)
set_target_properties( avfilter
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libavfilter.so)

#添加libavformat.so
add_library( avformat
             SHARED
             IMPORTED)
set_target_properties( avformat
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libavformat.so)

#添加libavutil.so
add_library( avutil
             SHARED
             IMPORTED)
set_target_properties( avutil
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libavutil.so)


#添加libswresample.so
add_library( swresample
             SHARED
             IMPORTED)
set_target_properties( swresample
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libswresample.so)

#添加libswscale.so
add_library( swscale
             SHARED
             IMPORTED)
set_target_properties( swscale
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libswscale.so)

 #添加libpostproc.so
 add_library( postproc
              SHARED
              IMPORTED)
 set_target_properties( postproc
                        PROPERTIES IMPORTED_LOCATION
                        ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libpostproc.so)

   #添加libyuv.so
   add_library( yuv
                SHARED
                IMPORTED)
   set_target_properties( yuv
                          PROPERTIES IMPORTED_LOCATION
                          ${CMAKE_SOURCE_DIR}/src/main/jni/ffmpeg/armeabi-v7a/libyuv.so)

  target_link_libraries( # Specifies the target library.

                         lh-jni-lib-1
                         avcodec
                         avfilter
                         avformat
                         avutil
                         swresample
                         swscale
                         postproc
                         yuv
                         jnigraphics
                         android
                         OpenSLES

                         # Links the target library to the log library
                         # included in the NDK.
                         ${log-lib} )