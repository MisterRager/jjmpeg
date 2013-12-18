#!/usr/bin/perl

# build the codecid table.

open IN,"</usr/include/ffmpeg/libavcodec/avcodec.h";

open OUT,">codecid.c";
print OUT "#include <stdio.h>\n";
print OUT "#include <libavcodec/avcodec.h>\n";
print OUT "struct foo { int id; const char *name; } table[] = {\n";

line:
while (<IN>) {
    if (m/enum CodecID/) {
	while (<IN>) {
	    if (m/(CODEC_ID_[_A-Z0-9]*)/) {
		$id = $1;
		print OUT "{ ${id}, \"${id}\" },\n";
	    } elsif (m/};/) {
		last line;
	    }
	}
    }
}
close IN;

print OUT "};\n";
print OUT <<END;

int main(int argc, char **argv) {
    int i;
    for (i=0;i<sizeof(table)/sizeof(table[0]);i++) {
	if (table[i].id <  0x10000) {
	    printf("\\tpublic final static int %s = %d;\\n", table[i].name, table[i].id);
	} else {
	    printf("\\tpublic final static int %s = 0x%x;\\n", table[i].name, table[i].id);
	}
}
    return 0;
}
END

system("gcc -o codecid -I/usr/include/ffmpeg codecid.c");
system("./codecid > codecid.txt");


