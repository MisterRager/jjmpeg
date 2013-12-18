#include <stdio.h>
#include <libavcodec/avcodec.h>
struct foo { int id; const char *name; } table[] = {
};

int main(int argc, char **argv) {
    int i;
    for (i=0;i<sizeof(table)/sizeof(table[0]);i++) {
	if (table[i].id <  0x10000) {
	    printf("\tpublic final static int %s = %d;\n", table[i].name, table[i].id);
	} else {
	    printf("\tpublic final static int %s = 0x%x;\n", table[i].name, table[i].id);
	}
}
    return 0;
}
