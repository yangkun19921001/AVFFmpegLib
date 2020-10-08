#!/bin/bash
# 编译ffmpeg，链接x264，fdkaac，libmp3lame,openssl
ARCH=$1

source config.sh $ARCH
NOW_DIR=$(cd `dirname $0`; pwd)
LIBS_DIR=$NOW_DIR/libs

# 源码目录，自行更改
cd /root/android/library/ffmpeg/FFmpeg-n4.2.3

# 输出路径
PREFIX=$LIBS_DIR/ffmpeg4.2.3/$AOSP_ABI

# 头文件目录
FDK_INCLUDE=$LIBS_DIR/libfdk-aac/$AOSP_ABI/include
# 库文件目录
FDK_LIB=$LIBS_DIR/libfdk-aac/$AOSP_ABI/lib
X264_INCLUDE=$LIBS_DIR/libx264/$AOSP_ABI/include
X264_LIB=$LIBS_DIR/libx264/$AOSP_ABI/lib
OPENSSL_INCLUDE=$LIBS_DIR/openssl/$AOSP_ABI/include
OPENSSL_LIB=$LIBS_DIR/openssl/$AOSP_ABI/lib
LAME_INCLUDE=$LIBS_DIR/libmp3lame/$AOSP_ABI/include
LAME_LIB=$LIBS_DIR/libmp3lame/$AOSP_ABI/lib


echo $OPENSSL_INCLUDE
echo $OPENSSL_LIB


./configure \
--target-os=android \
--prefix=$PREFIX \
--enable-cross-compile \
--disable-runtime-cpudetect \
--disable-asm \
--arch=$AOSP_ARCH \
--cc=$CC \
--cxx=$CXX \
--cross-prefix=$CROSS_PREFIX \
--extra-cflags="-I$X264_INCLUDE  -I$FDK_INCLUDE -I$OPENSSL_INCLUDE -I$LAME_INCLUDE $FF_CFLAGS" \
--extra-cxxflags="$FF_EXTRA_CFLAGS" \
--extra-ldflags="-L$X264_LIB -L$FDK_LIB -L$OPENSSL_LIB -L$LAME_LIB" \
--extra-libs=-lm \
--sysroot=$SYS_ROOT \
--enable-static \
--disable-shared \
--enable-jni \
--enable-mediacodec \
--enable-pthreads \
--enable-pic \
--disable-iconv \
--enable-libx264 \
--enable-libfdk-aac \
--enable-libmp3lame  \
--enable-openssl \
--enable-gpl \
--enable-nonfree \
--disable-muxers \
--enable-muxer=mov \
--enable-muxer=mp4 \
--enable-muxer=h264 \
--enable-muxer=avi \
--enable-muxer=flv \
--enable-muxer=hls \
--enable-muxer=rtp \
--enable-muxer=rtsp \
--enable-muxer=image2 \
--enable-muxer=gif \
--disable-demuxers \
--enable-demuxer=mov \
--enable-demuxer=h264 \
--enable-demuxer=avi \
--enable-demuxer=flv \
--enable-demuxer=hls \
--enable-demuxer=rtp \
--enable-demuxer=rtsp \
--enable-demuxer=gif \
--enable-demuxer=mp3 \
--enable-demuxer=image2 \
--disable-encoders \
--enable-encoder=aac \
--enable-encoder=libfdk_aac \
--enable-encoder=libx264 \
--enable-encoder=libmp3lame  \
--enable-encoder=mpeg4 \
--enable-encoder=mjpeg \
--enable-encoder=png \
--enable-encoder=gif \
--disable-decoders \
--enable-decoder=h264_mediacodec \
--enable-decoder=mpeg4_mediacodec \
--enable-decoder=vp8_mediacodec \
--enable-decoder=vp9_mediacodec \
--enable-decoder=libfdk_aac \
--enable-decoder=aac \
--enable-decoder=aac_latm \
--enable-decoder=h264 \
--enable-decoder=mpeg4 \
--enable-decoder=mjpeg \
--enable-encoder=gif \
--enable-decoder=png \
--disable-parsers \
--enable-parser=aac \
--enable-parser=aac_latm \
--enable-parser=h264 \
--enable-parser=mjpeg \
--enable-parser=png \
--disable-protocols \
--enable-protocol=file \
--enable-protocol=crypto \
--enable-protocol=http \
--enable-protocol=https \
--enable-protocol=tls \
--enable-protocol=tcp \
--enable-protocol=udp \
--enable-protocol=rtp \
--enable-protocol=rtmp \
--enable-protocol=rtmps \
--enable-protocol=hls \
--enable-zlib \
--enable-small \
--enable-postproc \
--enable-avfilter \
--disable-outdevs \
--disable-avdevice \
--disable-outdevs \
--disable-indevs \
--disable-ffprobe \
--disable-ffplay \
--disable-ffmpeg \
--disable-debug \
--disable-symver \
--disable-stripping



make clean
make 
make install

cd ..





