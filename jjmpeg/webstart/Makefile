
# must match first 2 lines in jjmpeg.txt
pass=jjmpegpass

# build key store
jjmpeg.jks: jjmpeg.txt
	-rm jjmpeg.jks
	keytool -genkey -alias jjmpeg -keystore $@ -validity 3650 < jjmpeg.txt
	echo $(pass) | keytool -selfcert -alias jjmpeg -keystore $@ -validity 3650



