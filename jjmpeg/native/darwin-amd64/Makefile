SRC=..
TARGET=darwin-amd64

include $(SRC)/config.make

FFMPEG_HOME=/usr/include/ffmpeg
jjmpeg_lib=libjjmpeg.dylib

CROSS=x86_64-apple-darwin9-
TARGET_JNI_CFLAGS = -I$(SRC)/jni/darwin
TARGET_CFLAGS = -fPIC -m64
TARGET_JJMPEG_CFLAGS= -I$(FFMPEG_HOME)
TARGET_JJMPEG_LIBS=
TARGET_JJMPEG_LDFLAGS = 

include ../Makefile.libs

all: $(jjmpeg_lib)
