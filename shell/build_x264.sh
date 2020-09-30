#!/bin/bash

ARCH=$1

# 导入配置文件
source config.sh $ARCH
# 输出路径
LIBS_DIR=$(cd `dirname $0`; pwd)/libs/libx264
echo "LIBS_DIR="$LIBS_DIR

# x264源码路径
cd /root/android/library/x264/x264-master/

export CC=$CC
export CXX=$CXX
export CXXFLAGS=$FF_EXTRA_CFLAGS
export CFLAGS=$FF_CFLAGS
export AR="${CROSS_PREFIX}ar"
export LD="${CROSS_PREFIX}ld"
export AS="${CROSS_PREFIX}as"
export NM="${CROSS_COMPILE}nm"
export STRIP="${CROSS_COMPILE}strip"
export RANLIB="${CROSS_COMPILE}ranlib"

PREFIX=$LIBS_DIR/$AOSP_ABI

./configure --prefix=$PREFIX \
--enable-static \
--enable-pic \
--disable-cli \
--disable-asm \
--host=$HOST \
--cross-prefix=$CROSS_PREFIX \
--sysroot=$SYS_ROOT \
--extra-cflags="$FF_CFLAGS" \
--extra-ldflags=""

make clean
make 
make install

cd ..




