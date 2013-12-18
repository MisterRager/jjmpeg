#
# This is a bit of a rushed mess.
#
VERSION=0.0
jar_distfiles=README COPYING COPYING.LESSER
native_distfiles=$(jar_distfiles) native/README

native_jjmpeg=$(wildcard native/*/*jjmpeg.so native/*/*jjmpeg.dll)
native_jjdvb=$(wildcard native/*/*jjdvb.so)

dist_dir=jjmpeg-$(VERSION)

src_distfiles=$(jar_distfiles) \
	--exclude .svn \
	src \
	Makefile \
	webstart \
	native/README \
	native/Makefile \
	native/Makefile.libs \
	native/config.make \
	native/jni \
	native/jjmpeg \
	native/jjdvb \
	$(wildcard native/*/Makefile) \
	build.xml \
	--exclude private nbproject


bin_distfiles=$(jar_distfiles) \
	$(native_distfiles) \
	$(native_jjmpeg) \
	$(native_jjdvb) \
	$(wildcard native/*/README*) \
	dist/jjmpeg.jar \
	dist/jjdvb.jar

.PHONY: webstart all native src

all: jar

tar: src_tar bin_tar

clean:
	-rm -rf classes
	-rm -rf dist
	(cd native ; make clean )

src_tar: jar
	-rm -rf $(dist_dir)
	mkdir $(dist_dir)
	tar cf - $(src_distfiles) | ( cd $(dist_dir) ; tar xf -)
	tar cjf $(dist_dir).tar.bz2 $(dist_dir)
	rm -rf $(dist_dir)

bin_tar: jar
	-rm -rf $(dist_dir)
	mkdir $(dist_dir)
	tar cf - $(bin_distfiles) | ( cd $(dist_dir) ; tar xf -)
	tar cjf $(dist_dir)-bin.tar.bz2 $(dist_dir)
	rm -rf $(dist_dir)

jar: dist/jjmpeg.jar dist/jjdvb.jar

dist/jjmpeg.jar: classes/classes
	-mkdir dist
	jar cf dist/jjmpeg.jar $(jar_distfiles) -C classes au/notzed/jjmpeg

dist/jjdvb.jar: classes/classes
	-mkdir dist
	jar cf dist/jjdvb.jar $(jar_distfiles) -C classes au/notzed/jjdvb

classes/classes:
	-rm -rf classes
	-mkdir classes
	javac -d classes -sourcepath src `find src -name '*.java'`
	touch classes/classes

native: dist/jjmpeg.jar dist/jjdvb.jar
	-( cd native ; make )

webstart:
	( cd webstart ; make )
	( cd native ; make webstart )
