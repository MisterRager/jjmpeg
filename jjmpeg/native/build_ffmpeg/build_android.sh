#!/bin/bash

#NDK=$HOME/Desktop/adt/android-ndk-r9
NDK=/usr/local/Cellar/android-ndk/9
#NDK=/usr/local/Cellar/android-ndk/r8e
#SYSROOT=$NDK/platforms/android-9/arch-arm/
#SYSROOT=$NDK/platforms/android-9/arch-arm/
SYSROOT=$NDK/platforms/android-8/arch-arm/
#TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.8/prebuilt/linux-x86_64
TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.8/prebuilt/darwin-x86
#TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.4.3/prebuilt/darwin-x86
FFMPEG_PATH=/Users/rager/Work/FFmpeg
BUILD_PATH=$(pwd)/ffmpeg

echo "Building FFMpeg at $BUILD_PATH"

function build_one
{
	cd "$FFMPEG_PATH"
	./configure \
	    --prefix=$PREFIX \
	    --enable-shared \
	    --disable-static \
	    --disable-doc \
	    --disable-ffmpeg \
	    --disable-ffplay \
	    --disable-ffprobe \
	    --disable-ffserver \
	    --disable-avdevice \
	    --disable-doc \
	    --disable-symver \
	    --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
	    --target-os=linux \
	    --arch=arm \
	    --enable-cross-compile \
	    --sysroot=$SYSROOT \
	    --extra-cflags="-Os -fpic $ADDI_CFLAGS" \
	    --extra-ldflags="$ADDI_LDFLAGS" \
	    $ADDITIONAL_CONFIGURE_FLAG
	make clean
	make -l 10
	make install
}

#CPU=arm
#ADDI_CFLAGS="-marm"
for i in configs/*.config; do
	source $i
	PREFIX=$BUILD_PATH/$CPU 

	echo "Building config $i to $PREFIX"
	build_one
done
