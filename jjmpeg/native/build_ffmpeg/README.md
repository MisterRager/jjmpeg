These are my build scripts for ffmpeg, including a patch required to configure ffmpeg to make binary files with the right names. The process, currently, is to obtain ffmpeg in your preferred manner and then set the appropriate paths in `build_android.sh`

There are configs in configs that actually describe the settings unique to each build variant. Edit these or add your own to add/remove build targets as you see fit. `build_android.sh` only reads the files `configs/*.conf`

I built ffmpeg from the git release 2.1.1 using android ndk r8e with gcc versions 4.4.3 and 4.8 to test successfully, so you probably should be able to, as well. The different parts of the system still don't fit together nicely, yet - ie, you'll have to manually copy libraries from the native side to the java side. The fact it takes so long to actually build the native libs discourages doing much beyond the eventual script that grabs the android libs and packages them up nicely in a .jar for gradle builds.
