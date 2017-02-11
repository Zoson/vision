cmake_minimum_required(VERSION 3.4.1)
set(lib_src_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
set(cpp_src_DIR src/main/cpp)
include_directories(
        src/main/cpp/include
)

add_library(avcodec-57_lib SHARED IMPORTED)
set_target_properties(avcodec-57_lib PROPERTIES IMPORTED_LOCATION
                             ${lib_src_DIR}/libavcodec.so)

add_library(avformat-57_lib SHARED IMPORTED)
set_target_properties(avformat-57_lib PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libavformat.so)

add_library(avutil-55_lib SHARED IMPORTED)
set_target_properties(avutil-55_lib PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libavutil.so)

add_library(swresample-2_lib SHARED IMPORTED)
set_target_properties(swresample-2_lib PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libswresample.so)

add_library(swscale-4_lib SHARED IMPORTED)
set_target_properties(swscale-4_lib PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libswscale.so)
add_library(postproc_lib SHARED IMPORTED)
set_target_properties(postproc_lib PROPERTIES IMPORTED_LOCATION
                        ${lib_src_DIR}/libpostproc.so)

# build application's shared lib
add_library(ffmpeg-jni SHARED
            ${cpp_src_DIR}/ffmpeg-jni.cpp)

add_library(ffmpeg-rtmp SHARED
            ${cpp_src_DIR}/ffmpeg-rtmp.cpp)
add_library(ffmpeg-live SHARED
            ${cpp_src_DIR}/ffmpeg-live.cpp)

# Include libraries needed for VideoPlayer lib
target_link_libraries(
ffmpeg-jni

log
android
avcodec-57_lib
avformat-57_lib
avutil-55_lib
swresample-2_lib
postproc_lib
swscale-4_lib)

target_link_libraries(
ffmpeg-rtmp

log
android
avcodec-57_lib
avformat-57_lib
avutil-55_lib
swresample-2_lib
postproc_lib
swscale-4_lib)

target_link_libraries(
ffmpeg-live

log
android
avcodec-57_lib
avformat-57_lib
avutil-55_lib
swresample-2_lib
postproc_lib
swscale-4_lib)
