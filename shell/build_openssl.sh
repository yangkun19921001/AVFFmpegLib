#!/bin/bash
ARCH=$1
source config.sh $ARCH
LIBS_DIR=$(cd `dirname $0`; pwd)/libs/openssl
PREFIX=$LIBS_DIR/$AOSP_ABI
echo "PREFIX"=$PREFIX

#cd /root/android/library/openssl/openssl-1.1.1/
cd /root/android/library/openssl/openssl-OpenSSL_1_1_1g/

export ANDROID_NDK_HOME=$ANDROID_NDK_ROOT
export PATH=$TOOLCHAIN/bin:$PATH
export CC="$CC"
export CXX="$CXX"
export AR="${CROSS_PREFIX}ar"
export LD="${CROSS_PREFIX}ld"
export AS="${CROSS_PREFIX}as"
export NM="${CROSS_COMPILE}nm"

./Configure $OPENSSL_ARCH \
-D__ANDROID_API__=$AOSP_API \
--prefix=$PREFIX \
no-shared \
no-engine \
no-dtls \
no-hw

make clean
make 
make install

cd ..




