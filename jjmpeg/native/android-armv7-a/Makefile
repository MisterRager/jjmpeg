SRC=..
TARGET=android-arm

include $(SRC)/config.make

FFMPEG_HOME=/Users/rager/Work/FFmpeg/jjmpeg-android/jni/build/armeabi-v7-a/include

#NDK=/usr/local/Cellar/android-ndk/r8e
NDK=/usr/local/Cellar/android-ndk/9
CROSS=$(NDK)/toolchains/arm-linux-androideabi-4.8/prebuilt/darwin-x86/bin/arm-linux-androideabi-
#CROSS=$(NDK)/toolchains/arm-linux-androideabi-4.4.3/prebuilt/darwin-x86/bin/arm-linux-androideabi-
TARGET_JNI_CFLAGS = -I$(SRC)/jni/linux
TARGET_CFLAGS = -fPIC --sysroot=${NDK}/platforms/android-8/arch-arm/ \
	-mfloat-abi=softfp -mfpu=vfpv3-d16 -marm -march=armv7-a \
	-DANDROID=1
TARGET_JJMPEG_CFLAGS= -I$(FFMPEG_HOME)
TARGET_JJMPEG_LIBS=
TARGET_JJMPEG_LDFLAGS = -Wl,-soname,libjjmpeg.so -llog --sysroot=${NDK}/platforms/android-8/arch-arm/

include ../Makefile.libs

all: $(jjmpeg_lib) #$(jjdvb_lib)
