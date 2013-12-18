#!/usr/bin/perl

$conf = $ARGV[0];
$abstract = $ARGV[1];
$jni = $ARGV[2];

print "Building $jni and $abstract from $conf\n";

# generate dvb bindings

open IN,"<$conf";

# c to jni type
%cntype = (
    "int64_t" => "jlong",
# check this
    "long" => "jint",
    "int32_t" => "jint",
    "uint32_t" => "jint",
    "int" => "jint",
    "int16_t" => "jshort",
    "uint16_t" => "jshort",
    "int8_t" => "jbyte",
    "char" => "jbyte",
    "void" => "void",
    "double" => "double",
    "float" => "float",

    "const char *" => "jstring"
    );

%cjtype = (
    "int64_t" => "long",
# check this
    "long" => "int",
    "int32_t" => "int",
    "uint32_t" => "int",
    "int" => "int",
    "int16_t" => "short",
    "uint16_t" => "short",
    "int8_t" => "byte",
    "char" => "byte",
    "void" => "void",
    "double" => "double",
    "float" => "float",

    "int *" => "IntBuffer",
    "int16_t *" => "ShortBuffer",
    "uint8_t *" => "ByteBuffer",
    "const char *" => "String",
    "const int16_t *" => "ShortBuffer",
    "const double *" => "DoubleBuffer"
    );

sub trim($) {
    my $val = shift;
    $val =~ s/^\s+//;
    $val =~ s/\s+$//; 
    return $val;
}

sub doat($$) {
    my $ind = shift;
    my $what = shift;
    if ($ind ne "") {
	print "(".$what.")";
    } else {
	print "()";
    }
}

sub gen_jname($) {
    my $name = shift;
    $name =~ s/^(.)/uc($1)/e;
    $name =~ s/_(.)/uc($1)/ge;
    return $name;
}

sub gen_nname($) {
    my $name = shift;
    $name =~ s/_(.)/_1$1/g;
    return $name;
}

$nativeprefix = "Java_au_notzed_jjdvb";

# scan in descriptor file
%classes = ();

while (<IN>) {
    if (m/class (.*)/) {
	$class = $1;

	my %classinfo = ();

	$classinfo{name} = $class;

	my @fields = ();

	# read fields
	while (<IN>) {
	    last if (m/^$/) || (m/^methods$/);

	    my %fieldinfo = ();

	    ($type, $name, $opt, $jname, $offset) = split(/,/);
	    if ($jname eq "") {
		$jname = gen_jname($name);
	    } else {
		chomp $jname;
	    }
	    chomp $offset;
	    $ntype = $type;
	    $prefix = "";
	    $scope = "";
	    $nscope = "private ";
	    if ($opt =~ m/p/) {
		$scope = "public ";
	    }
	    if ($opt =~ m/o/) {
		$jtype = "ByteBuffer";
		$prefix = "_";
		$ntype = "jobject";
	    } elsif ($opt =~ m/e/) {
		$jtype = "int";
		$prefix = "_";
		$ntype = "jint";
	    } else {
		$nscope = $scope;
		$ntype = $cntype{$type};
		$jtype = $cjtype{$type};
	    }
	    if ($opt =~ m/i/) {
		$suffix = "At";
	    } else {
		$suffix = "";
	    }

	    $fieldinfo{name} = $name;
	    $fieldinfo{jname} = $jname;
	    $fieldinfo{prefix} = $prefix;
	    $fieldinfo{opt} = $opt;
	    $fieldinfo{type} = $type;
	    $fieldinfo{jtype} = $jtype;
	    $fieldinfo{ntype} = $ntype;
	    $fieldinfo{scope} = $scope;
	    $fieldinfo{nscope} = $nscope;
	    $fieldinfo{suffix} = $suffix;
	    $fieldinfo{offset} = $offset;

	    push @fields, \%fieldinfo;
	}
	$classinfo{fields} = \@fields;

	# read methods, if any
	if (m/^methods$/) {
	    my @methods = ();
	    $funcprefix = "";
	    $library = "avformat";
	    while (<IN>) {
		last if (m/^$/);

		if (m/^prefix (\w*) (\w*)/) {
		    $funcprefix = $1;
		    $library = $2;
		    next;
		}

		($type, $name, $args) = m/^(.*[ \*])(\w*)\((.*)\)/;
		$pname = $name;
		$pname =~ s/$funcprefix//;
		$jname = gen_jname($pname);
		$nname = gen_nname($pname);

		my %methodinfo = ();
		my $static = 0;
		my $wraptype = 0;

		$type = trim($type);

		if ($type =~ m/static (.*)/) {
		    $static = 1;
		    $type = $1;
		}

		if ($type =~ m/(.*) \*$/) {
		    $ctype = $1;
		    $ntype = "jobject";
		    if ($1 eq "void") {
			$jtype = "ByteBuffer";
		    } else {
			$jtype = $1;
			$wraptype = 1;
		    }
		} else {
		    $ctype = $type;
		    $ntype = $cntype{$type};
		    $jtype = $cjtype{$type};
		}

		$methodinfo{wraptype} = $wraptype;
		$methodinfo{static} = $static;
		$methodinfo{rawargs} = $args;
		$methodinfo{type} = $type;
		$methodinfo{ntype} = $ntype;
		$methodinfo{name} = $name;
		$methodinfo{jtype} = $jtype;
		$methodinfo{ctype} = $ctype;
		$methodinfo{jname} = $jname;
		$methodinfo{nname} = $nname;
		$methodinfo{pname} = $pname;
		$methodinfo{library} = $library;

		my @arginfo = ();

		my $simple = 1;
		my $dofunc = 1;

		@args = split(/,/,$args);
		# first arg is alwauys object pointer
		if (!$static) {
		    shift @args;
		}
		foreach $a (@args) {
		    ($type, $name) = $a =~ m/^(.*[ \*])(\w+)$/;

		    $type = trim($type);
		    $name = trim($name);

		    my %argdata = {};
		    my $deref = 0;
		    my $deenum = 0;

		    $argdata{type} = $type;
		    $argdata{name} = $name;

		    if ($type =~ m/(.*) \*$/) {
			$ntype = $cntype{$type};
			if ($ntype eq "") {
			    $ntype = "jobject";
			}
			$argdata{ntype} = $ntype;
			$jtype = $cjtype{$type};
			if ($jtype eq "") {
			    $jtype = $1;
			    $argdata{jntype} = "ByteBuffer";
			    $deref = 1;
			} else {
			    $argdata{jntype} = $jtype;
			}
			$argdata{jtype} = $jtype;
			$argdata{nname} = "j$name";
			$simple = 0;
			if ($cjtype{$1} ne "") {
			    $dofunc = 0;
			}
		    } elsif ($type =~ m/^enum (.*)$/) {
			$argdata{ntype} = "jint";
			$argdata{jtype} = $1;
			$argdata{jntype} = "int";
			$argdata{nname} = "$name";
			$deenum = 1;
		    } else {
			$argdata{ntype} = $cntype{$type};
			$argdata{jtype} = $cjtype{$type};
			$argdata{jntype} = $cjtype{$type};
			$argdata{nname} = "$name";
		    }

		    $argdata{deref} = $deref;
		    $argdata{deenum} = $deenum;

		    push @arginfo, \%argdata;
		}

		$methodinfo{args} = \@arginfo;
		$methodinfo{simple} = $simple;
		$methodinfo{dofunc} = $dofunc;

		push @methods, \%methodinfo;
	    }
	    $classinfo{methods} = \@methods;
	}

	push @classes, \%classinfo;
    }
}


# create jni code
open STDOUT, ">$jni";

$dlsymprefix = "d";
$dodl = 1;

print "// Auto-generated from native.conf\n";
print "#include \"jjdvb-jni.h\"\n\n";

# generate dynamic linkage
if ($dodl) {
    # output function pointers
    foreach $classinfo (@classes) {
	%ci = %{$classinfo};

	my $class = $ci{name};
	my @methods= @{$ci{methods}};
	foreach $methodinfo (@methods) {
	    my %mi = %{$methodinfo};

	    print "static $mi{type} (*${dlsymprefix}$mi{name})($mi{rawargs});\n";
	}
    }

    # output constructor (dlopen stuff)
    print <<END;

static void *avutil_lib;
static void *avcodec_lib;
static void *avformat_lib;
static void *swscale_lib;

static jfieldID field_p;

JNIEXPORT jint JNICALL Java_au_notzed_jjdvb_DVBNative_getPointerBits
(JNIEnv *env, jclass jc) {
\tavcodec_lib = dlopen("libavcodec.so", RTLD_LAZY|RTLD_GLOBAL);
\tif(avcodec_lib == NULL) return 0;
\tavutil_lib = dlopen("libavutil.so", RTLD_LAZY|RTLD_GLOBAL);
\tif(avutil_lib == NULL) return 0;
\tavformat_lib = dlopen("libavformat.so", RTLD_LAZY|RTLD_GLOBAL);
\tif(avformat_lib == NULL) return 0;
\tswscale_lib = dlopen("libswscale.so", RTLD_LAZY|RTLD_GLOBAL);
\tif(swscale_lib == NULL) return 0;

END
    foreach $classinfo (@classes) {
	%ci = %{$classinfo};

	my $class = $ci{name};
	my @methods= @{$ci{methods}};
	foreach $methodinfo (@methods) {
	    my %mi = %{$methodinfo};

	    # man dlopen says to do this hacked up shit because of c99, but gcc whines rightly about it
	    #print "\t*(void **)(&${dlsymprefix}$mi{name}) = dlsym($mi{library}_lib, \"$mi{name}\");\n";
	    print "\t${dlsymprefix}$mi{name} = dlsym($mi{library}_lib, \"$mi{name}\");\n";
	    print "\tif (${dlsymprefix}$mi{name} == NULL) return 0;\n";
	}
    }

print "\n";
    print "\tfield_p = (*env)->GetFieldID(env, jc, \"p\", \"Ljava/nio/ByteBuffer;\");\n";
print "\tif (field_p == NULL) return 0;\n";
print "\tif (init_local(env) != 0) return 0;\n";
print "\n";
    print "\treturn sizeof(void *)*8;\n";
    print "}\n";
}

foreach $classinfo (@classes) {
    my %ci = %{$classinfo};

    my $class = $ci{name};

    print "\n\n/* Class: $class */\n\n";

    # field accessors
    my @fields = @{$ci{fields}};

    foreach $fieldinfo (@fields) {
	my %fi = %{$fieldinfo};

	# getter
	my $opt = $fi{opt};
	my $ind = $opt =~ m/i/;

	if ($opt =~ m/g/) {
	    print "JNIEXPORT $fi{ntype} JNICALL ${nativeprefix}_${class}Abstract_";
	    if ($opt =~ m/[eo]/) {
		print "_1";
	    }
	    if ($opt =~ m/i/) {
		#print "1";
		$at = "At";
	    } else {
		$at = "";
	    }
	    print "get$fi{jname}$at(";
	    print "JNIEnv *env, jobject jo";
	    if ($ind) {
		print ", jint index";
	    }
	    print ") {\n";
	    print "\tjobject jptr = (*env)->GetObjectField(env, jo, field_p);\n";
	    print "\t$class *cptr = ADDR(jptr);\n";
	    print "\treturn ";
	    if ($fi{ntype} eq "jobject") {
		print "WRAP(";
		if ($opt =~ m/r/) {
		    print "&";
		}
	    } elsif ($fi{ntype} eq "jstring") {
		print "WRAPSTR(";
	    }
	    print "cptr->$fi{name}";
	    if ($ind) {
		print "[index]";
	    }
	    if ($fi{ntype} eq "jobject") {
		print ", sizeof($fi{type}))";
	    } elsif ($fi{ntype} eq "jstring") {
		print ")";
	    }
	    print ";\n}\n\n";
	}
	if ($opt =~ m/s/) {
	    print "JNIEXPORT void JNICALL ${nativeprefix}_${class}Abstract_";
	    if ($opt =~ m/[eo]/) {
		print "_1";
	    }
	    if ($opt =~ m/i/) {
		#print "1";
		$at = "At";
	    } else {
		$at = "";
	    }
	    print "set$fi{jname}$at(";
	    print "JNIEnv *env, jobject jo";
	    if ($ind) {
		print ", jint index";
	    }
	    print ", $fi{ntype} val";
	    print ") {\n";
	    print "\tjobject jptr = (*env)->GetObjectField(env, jo, field_p);\n";
	    print "\t$class *cptr = ADDR(jptr);\n";
	    print "\tcptr->$fi{name}";
	    if ($ind) {
		print "[index]";
	    }
	    print " = val";
	    print ";\n}\n\n";
	}
    }

    # methods
    my @methods= @{$ci{methods}};
    foreach $methodinfo (@methods) {
	my %mi = %{$methodinfo};
	my @arginfo = @{$mi{args}};

	print "JNIEXPORT $mi{ntype} JNICALL ${nativeprefix}_${class}Abstract__1$mi{nname}\n";
	if ($mi{static}) {
	    print "(JNIEnv *env, jclass jc";
	    foreach $argdata (@arginfo) {
		%ai = %{$argdata};
		print ", $ai{ntype} $ai{nname}";
	    }
	    print ") {\n";
	} else {
	    print "(JNIEnv *env, jobject jo";
		
	    foreach $argdata (@arginfo) {
		%ai = %{$argdata};
		print ", $ai{ntype} $ai{nname}";
	    }
	    print ") {\n";
	    print "\tjobject jptr = (*env)->GetObjectField(env, jo, field_p);\n";
	    print "\t$class *cptr = ADDR(jptr);\n";
	}
	# wrap/converty any jni args to c args
	foreach $argdata (@arginfo) {
	    %ai = %{$argdata};
	    if ($ai{ntype} eq "jobject") {
		print "\t$ai{type} $ai{name} = ADDR($ai{nname});\n";
	    }
	    if ($ai{ntype} eq "jstring") {
		print "\t$ai{type} $ai{name} = STR($ai{nname});\n";
	    }
	}
	print "\n";
	
	# call function
	if ($mi{type} ne "void") {
	    print "\t$mi{ntype} res = ";
	} else {
	    print "\t";
	}
	if ($mi{ntype} eq "jobject") {
	    print "WRAP(";
	}
	if ($dodl) {
	    print "(*${dlsymprefix}";
	}
	print "$mi{name}";
	if ($dodl) {
	    print ")";
	}
	print "(";
	$count = 0;
	if (!$mi{static}) {
	    print "cptr";
	    $count = 1;
	}
	foreach $argdata (@arginfo) {
	    %ai = %{$argdata};
	    if ($count > 0) {
		print ", ";
	    }
	    print "$ai{name}";
	    $count++;
	}
	print ")";
	if ($mi{ntype} eq "jobject") {
	    print ", sizeof($mi{ctype}))";
	}
	print ";\n";

	# free any resources mapped (strings)
	foreach $argdata (@arginfo) {
	    %ai = %{$argdata};
	    if ($ai{ntype} eq "jstring") {
		print "\tRSTR($ai{nname}, $ai{name});\n";
	    }
	}

	if ($mi{type} ne "void") {
	    print "\treturn res;\n";
	}
	print "}\n\n";
    }
}

close STDOUT;
open STDOUT, ">$abstract";

    print "/* I am automatically generated.  Editing me would be pointless,\n   but I wont stop you if you so desire. */\n\n";

    print <<END;
package au.notzed.jjdvb;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.IntBuffer;
import java.nio.DoubleBuffer;

END
# now create java code
foreach $classinfo (@classes) {
    my %ci = %{$classinfo};

    my $class = $ci{name};

    print <<END;
abstract class ${class}Abstract extends DVBNative {
\tprotected ${class}Abstract(ByteBuffer p) {
\t\tsuper(p);
\t}
END

    print "\t// Fields\n";
    # field accessors
    my @fields = @{$ci{fields}};
    foreach $fieldinfo (@fields) {
	my %fi = %{$fieldinfo};

	# getter
	my $opt = $fi{opt};
	my $ind = $opt =~ m/i/;

	if ($opt =~ m/g/) {
	    print "\t$fi{nscope}native $fi{jtype} $fi{prefix}get$fi{jname}$fi{suffix}";
	    doat($ind, "int index");
	    print ";\n";
	    if ($opt =~ m/o/) {
		print "\t$fi{scope} $fi{type} get$fi{jname}$fi{suffix}";
		doat($ind, "int index");
		print " {\n\t\treturn $fi{type}.create($fi{prefix}get$fi{jname}$fi{suffix}";
		doat($ind, "index");
		print ");\n\t}\n";
	    } elsif ($opt =~ m/e/) {
		print "\t${scope} $fi{type} get$fi{jname}() {\n";
		print "\t\treturn $fi{type}.fromC($fi{prefix}get$fi{jname}());\n\t}\n";
	    }
	}
	if ($opt =~ m/s/) {
	    print "\t$fi{nscope}native $fi{jtype} $fi{prefix}set$fi{jname}$fi{suffix}(";
	    if ($ind) {
		print "int index, ";
	    }
	    print "$fi{jtype} val";
	    print ");\n";
	    if ($opt =~ m/o/) {
		print STDERR "ERROR: unimplemented object setting!\n";
		print "\t$fi{scope} $fi{type} set$fi{jname}$fi{suffix}";
		doat($ind, "int index");
		print " {\n\t\treturn $fi{type}.create($fi{prefix}get$fi{jname}$fi{suffix}";
		doat($ind, "index");
		print ");\n\t}\n";
	    } elsif ($opt =~ m/e/) {
		print "\t${scope} void set$fi{jname}($fi{type} val) {\n";
		print "\t\t$fi{prefix}set$fi{jname}(val.toC());\n\t}\n";
	    }
	}
    }

    # methods
    print "\t// Native Methods\n";
    my @methods= @{$ci{methods}};
    foreach $methodinfo (@methods) {
	my %mi = %{$methodinfo};
	my $name = $mi{pname};
	my $scope = "";

	if ($mi{static}) {
	    $scope = "static ".$scope;
	}

	$jtype = $mi{jtype};
	if ($mi{wraptype}) {
	    $jtype = "ByteBuffer";
	}

	print "\t${scope}native ${jtype} _${name}(";
	my @arginfo = @{$mi{args}};

	my $count = 0;
	foreach $argdata (@arginfo) {
	    %ai = %{$argdata};
	    print ", " if $count > 0;
	    print "$ai{jntype} $ai{name}";
	    $count += 1;
	}
	print ");\n";
    }
    print "\t// Public Methods\n";
    foreach $methodinfo (@methods) {
	my %mi = %{$methodinfo};
	my @arginfo = @{$mi{args}};

	# add the public wrapper - if it's simple and we can
	my $name = $mi{jname};
	my $abstract = "";

	if(!$mi{dofunc}) {
	    next;
	    $abstract = "abstract ";
	}

	if ($mi{static}) {
	    $abstract = "static ".$abstract;
	}

	$name =~ s/^(.)/lc($1)/e;
	print "\t${abstract}public $mi{jtype} ${name}(";
	$count = 0;
	foreach $argdata (@arginfo) {
	    %ai = %{$argdata};
	    print ", " if $count > 0;
	    print "$ai{jtype} $ai{name}";
	    $count += 1;
	}
	print ")";
	if ($mi{dofunc}) {
	    print " {\n\t\t";
	    if ($mi{jtype} ne "void") {
		print "return ";
	    }
	    if ($mi{wraptype}) {
		print "$mi{jtype}.create(";
	    }
	    print "_$mi{pname}(";
	    $count = 0;
	    foreach $argdata (@arginfo) {
		%ai = %{$argdata};
		print ", " if $count > 0;
		print "$ai{name}";
		if ($ai{deenum}) {
		    print ".toC()";
		}
		if ($ai{deref}) {
		    print ".p";
		}
		$count += 1;
	    }
	    print ")";
	    if ($mi{wraptype}) {
		print ")";
	    }
	    print ";\n\t}\n";	    
	} else {
	    print ";\n";
	}
    }
    print "}\n";
}
    close STDOUT;

exit
