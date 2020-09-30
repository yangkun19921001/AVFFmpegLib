#!/bin/bash
# 编译ffmpeg，链接x264和fdkaac
ARCH=$1

source config.sh $ARCH
NOW_DIR=$(cd `dirname $0`; pwd)
LIBS_DIR=$NOW_DIR/libs

# 源码目录，自行更改
cd /root/android/library/ffmpeg/FFmpeg-n4.2.3

# 输出路径
PREFIX=$LIBS_DIR/ffmpeg4.2.3/$AOSP_ABI

PLATFORM=$ANDROID_NDK_ROOT/platforms/$AOSP_API/$AOSP_ARCH

# 头文件目录
FDK_INCLUDE=$LIBS_DIR/libfdk-aac/$AOSP_ABI/include
# 库文件目录
FDK_LIB=$LIBS_DIR/libfdk-aac/$AOSP_ABI/lib
X264_INCLUDE=$LIBS_DIR/libx264/$AOSP_ABI/include
X264_LIB=$LIBS_DIR/libx264/$AOSP_ABI/lib
OPENSSL_INCLUDE=$LIBS_DIR/openssl/$AOSP_ABI/include
OPENSSL_LIB=$LIBS_DIR/openssl/$AOSP_ABI/lib


echo $OPENSSL_INCLUDE
echo $OPENSSL_LIB
echo $TOOLCHAIN/bin/$TOOLNAME_BASE
echo $PREFIX/lib/



$TOOLCHAIN/bin/$TOOLNAME_BASE-ld \
-rpath-link=$PLATFORM/usr/lib \
-L$PLATFORM/usr/lib \
-L$PREFIX/lib \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \ 
$PREFIX/libffmpeg.so \
$FDK_LIB/libfdk-aac.a \
$X264_LIB/libx264.a \
$OPENSSL_LIB/libcrypto.a \
$OPENSSL_LIB/libssl.a \
$PREFIX/lib/libavcodec.a \
$PREFIX/lib/libavfilter.a \
$PREFIX/lib/libswresample.a \
$PREFIX/lib/libavformat.a \
$PREFIX/lib/libavutil.a \
$PREFIX/lib/libswscale.a \
$PREFIX/lib/libpostproc.a \
$PREFIX/lib/libavdevice.a \
-lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker $TOOLCHAIN/lib/gcc/$TOOLNAME_BASE/4.9.x/libgcc_real.a

cd ..


