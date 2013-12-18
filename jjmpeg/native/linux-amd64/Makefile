SRC=..
TARGET=linux-amd64

include $(SRC)/config.make

FFMPEG_HOME=/usr/include/ffmpeg

CROSS=
TARGET_JNI_CFLAGS = -I$(SRC)/jni/linux
TARGET_CFLAGS = -fPIC -m64
TARGET_JJMPEG_CFLAGS= -I$(FFMPEG_HOME)
TARGET_JJMPEG_LIBS= -lpthread
TARGET_JJMPEG_LDFLAGS = -Wl,-soname,libjjmpeg.so

TARGET_JJDVB_CFLAGS=
TARGET_JJDVB_LIBS=
TARGET_JJDVB_LDFLAGS = -Wl,-soname,libjjdvb.so

include ../Makefile.libs

all: $(jjmpeg_lib) $(jjdvb_lib)
