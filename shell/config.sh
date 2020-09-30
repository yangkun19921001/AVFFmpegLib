
#NDK路径
export ANDROID_NDK_ROOT=/root/android/ndk/android-ndk-r21

export AOSP_API="21"

#cpu架构
if [ "$#" -lt 1 ]; then
	THE_ARCH=armv7
else
	THE_ARCH=$(tr [A-Z] [a-z] <<< "$1")
fi

#根据不同架构配置变量
case "$THE_ARCH" in
  armv7a|armeabi-v7a)
	TOOLNAME_BASE="arm-linux-androideabi"
	COMPILER_BASE="armv7a-linux-androideabi"
	AOSP_ABI="armeabi-v7a"
	AOSP_ARCH="armeabi-v7a"
	OPENSSL_ARCH="android-arm"
	HOST="arm-linux-androideabi"
	FF_EXTRA_CFLAGS="-DANDROID -Wall -fPIC"
	FF_CFLAGS="-DANDROID -Wall -fPIC"
	;;
  armv8|armv8a|aarch64|arm64|arm64-v8a)
	TOOLNAME_BASE="aarch64-linux-android"
	COMPILER_BASE="aarch64-linux-android"
	AOSP_ABI="arm64-v8a"
	AOSP_ARCH="arm64"
	OPENSSL_ARCH="android-arm64"
	HOST="aarch64-linux-android"

	FF_EXTRA_CFLAGS="-DANDROID -Wall -fPIC"
	FF_CFLAGS="-DANDROID -Wall -fPIC"
	;;
  x86)
	TOOLNAME_BASE="i686-linux-android"
	COMPILER_BASE="i686-linux-android"
	AOSP_ABI="x86"
	AOSP_ARCH="x86"
	OPENSSL_ARCH="android-x86"
	HOST="i686-linux-android"
	FF_EXTRA_CFLAGS="-DANDROID -Wall -fPIC"
	FF_CFLAGS="-DANDROID -Wall -fPIC"
	;;
  x86_64|x64)
	TOOLNAME_BASE="x86_64-linux-android"
	COMPILER_BASE="x86_64-linux-android"
	AOSP_ABI="x86_64"
	AOSP_ARCH="x86_64"
	OPENSSL_ARCH="android-x86_64"
	HOST="x86_64-linux-android"
	FF_EXTRA_CFLAGS="-DANDROID -Wall -fPIC"
	FF_CFLAGS="-DANDROID -Wall -fPIC"
	;;
  *)
	echo "ERROR: Unknown architecture $1"
	[ "$0" = "$BASH_SOURCE" ] && exit 1 || return 1
	;;
esac
# 工具链
TOOLCHAIN=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64
SYS_ROOT=$TOOLCHAIN/sysroot
# 交叉编译路径
CROSS_PREFIX=$TOOLCHAIN/bin/$TOOLNAME_BASE-
# 编译器
CC=$TOOLCHAIN/bin/$COMPILER_BASE$AOSP_API-clang
CXX=$TOOLCHAIN/bin/$COMPILER_BASE$AOSP_API-clang++

echo "TOOLNAME_BASE="$TOOLNAME_BASE
echo "COMPILER_BASE="$COMPILER_BASE
echo "AOSP_ABI="$AOSP_ABI
echo "AOSP_ARCH="$AOSP_ARCH
echo "HOST="$HOST




