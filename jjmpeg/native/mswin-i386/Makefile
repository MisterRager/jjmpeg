# microsft windows makefile

SRC=..
TARGET=mswin-amd64

include $(SRC)/config.make

FFMPEG_HOME=/home/notzed/restore/home/notzed/Downloads/ffmpeg-git-9c2651a-win64-dev/include
jjmpeg_lib=jjmpeg.dll

CROSS=i686-pc-mingw32-

TARGET_JNI_CFLAGS = -I$(SRC)/jni/win32
TARGET_CFLAGS = -DWIN32 -m32
TARGET_JJMPEG_CFLAGS= -I$(FFMPEG_HOME)
TARGET_JJMPEG_LIBS=
TARGET_JJMPEG_LDFLAGS = -m32 -Wl,--kill-at

include ../Makefile.libs

all: $(jjmpeg_lib)
