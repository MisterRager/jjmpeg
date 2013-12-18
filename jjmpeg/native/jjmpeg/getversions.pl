#!/usr/bin/perl

print "This version of the native library was compiled against the following\n";
print "versions of FFmpeg libraries.\n\n";

foreach $file (@ARGV) {
    open IN, "<$file";
    while (<IN>) {
	if (m/\#define (LIB\w*_VERSION_MAJOR) (\d+)/) {
	    print " $1 $2\n";
	    last;
	}
    }
    close IN;

}
