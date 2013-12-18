SRC=..
TARGET=mswin-amd64

include $(SRC)/config.make

FFMPEG_HOME=/home/notzed/restore/home/notzed/Downloads/ffmpeg-git-9c2651a-win64-dev/include
jjmpeg_lib=jjmpeg.dll

CROSS=x86_64-w64-mingw32-

TARGET_JNI_CFLAGS = -I$(SRC)/jni/win32
TARGET_CFLAGS = -DWIN64 -m64
TARGET_JJMPEG_CFLAGS= -I$(FFMPEG_HOME)
TARGET_JJMPEG_LIBS=
TARGET_JJMPEG_LDFLAGS = -m64 -Wl,--kill-at

include ../Makefile.libs

all: $(jjmpeg_lib)
