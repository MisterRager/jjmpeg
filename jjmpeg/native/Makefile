
# Master make file for native libraries
#
# make [ all | clean | "" ]
#  Will attempt to build all targets.  Failures are ignored.
#
# make TARGETS="target list" [ all | clean | "" ]
#  Will only attempt to build the specified targets.
#
# All targets supported
#TARGETS_ALL=linux-amd64 linux-i386 mswin-amd64 mswin-i386 darwin-amd64 darwin-i386 android-arm android-armv7-a-neon
TARGETS_ALL=android-armv7 android-armv5te android-armv7-a-neon

TARGETS ?= $(TARGETS_ALL)

all clean webstart:
	for n in $(TARGETS) ; do \
		( cd $$n && $(MAKE) $@ ) ; \
	done
