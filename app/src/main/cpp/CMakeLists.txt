cmake_minimum_required(VERSION 3.22.1)

project("ffmpeg_test")

add_library(
        ffmpeg_test
        SHARED
        dn_ffmepg_player.c)

find_library(
        log-lib
        log)

find_library(
        log-android
        android
)

get_filename_component(third_part_jni_path ${CMAKE_CURRENT_SOURCE_DIR}/../jni ABSOLUTE)
set(third_part_lib_path ${third_part_jni_path}/${CMAKE_ANDROID_ARCH_ABI})

include_directories(${third_part_jni_path}/include)
include_directories(${third_part_jni_path}/include/ffmpeg)
include_directories(${third_part_jni_path}/include/libyuv)


# ffmpeg 部分
add_library(avcodec SHARED IMPORTED)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libavcodec-56.so)

add_library(avdevice SHARED IMPORTED)
set_target_properties(avdevice
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libavdevice-56.so)

add_library(avfilter SHARED IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libavfilter-5.so)

add_library(avformat SHARED IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libavformat-56.so)

add_library(avutil SHARED IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libavutil-54.so)

add_library(postproc SHARED IMPORTED)
set_target_properties(postproc
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libpostproc-53.so)

add_library(swresample SHARED IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libswresample-1.so)

add_library(swscale SHARED IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libswscale-3.so)

# libyuv
add_library(libyuv SHARED IMPORTED)
set_target_properties(libyuv
        PROPERTIES IMPORTED_LOCATION
        ${third_part_lib_path}/libyuv.so)

target_link_libraries(
        ffmpeg_test
        avcodec
        avdevice
        avfilter
        avformat
        avutil
        postproc
        swresample
        swscale
        libyuv
        ${log-lib}
        ${log-android})
