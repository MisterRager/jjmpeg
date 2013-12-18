/*
 * Global includes
 */

#include <dlfcn.h>
#include <jni.h>

#define ADDR(jp) (jp != NULL ? (*env)->GetDirectBufferAddress(env, jp) : NULL)
#define SIZE(jp) (jp != NULL ? (*env)->GetDirectBufferCapacity(env, jp) : 0)
#define STR(jp) ((*env)->GetStringUTFChars(env, jp, NULL))
#define RSTR(jp, cp) ((*env)->ReleaseStringUTFChars(env, jp, cp))

#define WRAP(cp, clen) ((*env)->NewDirectByteBuffer(env, cp, clen));
#define WRAPSTR(js) ((*env)->NewStringUTF(env, js));

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <inttypes.h>
#include <poll.h>

#include <linux/dvb/dmx.h>
#include <linux/dvb/frontend.h>

// for jni init
static int init_local(JNIEnv *env);

// our structures
typedef struct DMX {
	int demuxfd;
} DMX;

typedef struct FE {
	int frontendfd;
} FE;

// wrap structures in simple names
typedef struct dmx_pes_filter_params DMXPESFilterParams;
typedef struct dvb_frontend_parameters DVBFrontendParameters;
