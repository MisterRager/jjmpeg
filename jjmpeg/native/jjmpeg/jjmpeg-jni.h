/*
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjmpeg, a java binding to ffmpeg's libraries.
 *
 * jjmpeg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jjmpeg.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Global includes
 */

#include "au_notzed_jjmpeg_AVCodecContext.h"
#include "au_notzed_jjmpeg_AVCodecNative.h"
#include "au_notzed_jjmpeg_AVFormatContextNative.h"
#include "au_notzed_jjmpeg_AVFrameNative.h"
#include "au_notzed_jjmpeg_AVNative.h"
#include "au_notzed_jjmpeg_AVPacketNative.h"
#include "au_notzed_jjmpeg_AVStreamNative.h"
#include "au_notzed_jjmpeg_SwsContextNative.h"
#include "au_notzed_jjmpeg_AVFormatContextNativeAbstract.h"
#include "au_notzed_jjmpeg_AVDictionaryNative.h"

// This is a mess, but that's what you get.

#if defined(ANDROID)
#include <android/log.h>
#define LOG(...) __android_log_print(ANDROID_LOG_INFO, "jjmpeg", __VA_ARGS__)
#else
#define LOG(...) do { fprintf(stderr, __VA_ARGS__); fflush(stderr); } while (0)
#endif

#if defined(ANDROID)
#define LIBPREFIX  "/data/data/au.swordfish.enhance/lib/"
#else
#define LIBPREFIX ""
#endif

#if defined(WIN64) || defined(WIN32)
#  include <windows.h>

#  define _TOSTR(x) #x
#  define TOSTR(x) _TOSTR(x)

#  define DLOPEN(x, lib, ver) do { x = LoadLibrary(lib "-"  _TOSTR(ver) ".dll"); if (x == NULL) { LOG("cannot open %s\n",  lib "-" TOSTR(ver) ".dll"); return 0; } } while(0)
#  define CALLDL(x) (*d ## x)
#  define MAPDL(x, lib) do { if ((d ## x = (void *)GetProcAddress(lib, #x)) == NULL) { LOG("cannot resolve %s\n", #x); return 0; } } while(0)
#else
#  include <dlfcn.h>

#  if (defined (__APPLE__) && defined (__MACH__))
#    define LIBEXT ".dylib"
#  else
#    define LIBEXT ".so"
#  endif

#  define DLOPEN(x, lib, ver) x = dlopen(LIBPREFIX "lib" lib LIBEXT, RTLD_LAZY|RTLD_GLOBAL); do { if (x == NULL) { LOG("cannot open %s\n", LIBPREFIX lib LIBEXT); return 0; } } while (0)
#  define CALLDL(x) (*d ## x)
#  define MAPDL(x, lib) do { if ((d ## x = dlsym(lib, #x)) == NULL) { LOG("cannot resolve %s\n", #x); return 0; } } while (0)
#endif

#define ADDR(jp) (jp != NULL ? (*env)->GetDirectBufferAddress(env, jp) : NULL)
#define SIZE(jp) (jp != NULL ? (*env)->GetDirectBufferCapacity(env, jp) : 0)
#define STR(jp) (jp != NULL ? (*env)->GetStringUTFChars(env, jp, NULL) : NULL)
#define RSTR(jp, cp) ((jp != NULL) ? ((*env)->ReleaseStringUTFChars(env, jp, cp)):0 )

#define WRAP(cp, clen) ((*env)->NewDirectByteBuffer(env, cp, clen))
#define WRAPSTR(js) ((*env)->NewStringUTF(env, js))

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/dict.h>

//To make AVDictionary have a real sizeof
struct AVDictionary {
	int count;
	AVDictionaryEntry *elems;
};

static int init_local(JNIEnv *env);

// swscale leaves SwsContext opaque.  We just need a dummy type for the binding library.
#include <libswscale/swscale.h>
struct SwsContext {
	int dummy;
};

typedef struct SwsContext SwsContext;

// same for ReSampleContext
struct SwrContext {
	int dummy;
};
//typedef struct ReSampleContext ReSampleContext;


/**  Library handles */
static void *avutil_lib;
static void *avcodec_lib;
static void *avformat_lib;
static void *swscale_lib;
