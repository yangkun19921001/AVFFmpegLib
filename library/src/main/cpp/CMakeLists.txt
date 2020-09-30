cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

#设置静态库和动态库的输出路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../../../libs/${ANDROID_ABI})
#JNI 路径
set(JNI_PATH ${CMAKE_SOURCE_DIR})
#FFMPEG 文件路径
set(FFMPEG_PATH ${CMAKE_SOURCE_DIR}/../../../../thirdparties/ffmpeg)
#openssl 文件路径
set(OPENSSL_PATH ${CMAKE_SOURCE_DIR}/../../../../thirdparties/openssl)
#libfdk-aac 文件路径
set(LIBFDKAAC_PATH ${CMAKE_SOURCE_DIR}/../../../../thirdparties/fdk-aac)
#libx264 文件路径
set(LIBX264_PATH ${CMAKE_SOURCE_DIR}/../../../../thirdparties/x264)


message(JNI_PATH--->${JNI_PATH})
message(FFMPEG_PATH--->${FFMPEG_PATH})
message(OPENSSL_PATH--->${OPENSSL_PATH})
message(LIBFDKAAC_PATH--->${LIBFDKAAC_PATH})
message(LIBX264_PATH--->${LIBX264_PATH})


#编解码库
add_library(avcodec STATIC IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavcodec.a)

#文件格式和封装协议
add_library(avformat STATIC IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavformat.a)

#重采样
add_library(avdevice STATIC IMPORTED)
set_target_properties(avdevice PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavdevice.a)

#重采样
add_library(swresample STATIC IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libswresample.a)

#后期处理
add_library(postproc STATIC IMPORTED)
set_target_properties(postproc PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libpostproc.a)

#工具包
add_library(avutil STATIC IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavutil.a)

#过滤
add_library(avfilter STATIC IMPORTED)
set_target_properties(avfilter PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavfilter.a)

#视频转码
add_library(swscale STATIC IMPORTED)
set_target_properties(swscale PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libswscale.a)
#
##fdk-aac
add_library(fdkaac STATIC IMPORTED)
set_target_properties(fdkaac PROPERTIES IMPORTED_LOCATION ${LIBFDKAAC_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libfdk-aac.a)

##libx264
add_library(x264 STATIC IMPORTED)
set_target_properties(x264 PROPERTIES IMPORTED_LOCATION ${LIBX264_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libx264.a)


##OpenSSL
add_library(openssl_crypto STATIC IMPORTED)
set_target_properties(openssl_crypto PROPERTIES IMPORTED_LOCATION ${OPENSSL_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libcrypto.a)

add_library(openssl_ssl STATIC IMPORTED)
set_target_properties(openssl_ssl PROPERTIES IMPORTED_LOCATION ${OPENSSL_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libssl.a)


include_directories(${CMAKE_SOURCE_DIR}/)
include_directories(${FFMPEG_PATH}/include/)
include_directories(${OPENSSL_PATH}/include/)
include_directories(${LIBFDKAAC_PATH}/include/)
include_directories(${LIBX264_PATH}/include/)


find_library(
        log-lib
        log)


FILE(GLOB JNI_ALL_C ${CMAKE_SOURCE_DIR}/*.c)
add_library(
        ffmpeg-tools
        SHARED
        ${JNI_ALL_C}
)

target_link_libraries(
        ffmpeg-tools
        android z ${log-lib} atomic m
        -Wl,--start-group
        avcodec avfilter avformat avutil swresample postproc swscale avdevice
        -Wl,--end-group
        openssl_ssl
        openssl_crypto
        fdkaac
        x264

)