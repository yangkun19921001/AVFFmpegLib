#!/bin/bash

ARCH=$1
source config.sh $ARCH
LIBS_DIR=$(cd `dirname $0`; pwd)/libs/freetype-2.10.0
PREFIX=$LIBS_DIR/$AOSP_ABI
echo "PREFIX"=$PREFIX

cd /root/android/library/freetype-2.10.0

FLAGS="--enable-static --disable-shared --host=$HOST --target=android"

export ANDROID_NDK_HOME=$ANDROID_NDK_ROOT
export PATH=$TOOLCHAIN/bin:$PATH
export CC="$CC"
export CXX="$CXX"
export AR="${CROSS_PREFIX}ar"
export LD="${CROSS_PREFIX}ld"
export AS="${CROSS_PREFIX}as"
export NM="${CROSS_COMPILE}nm"


./configure $FLAGS \
--without-zlib \
--without-harfbuzz \
--with-png=no \
CPPFLAGS="-fPIC"


make clean
make
make install

