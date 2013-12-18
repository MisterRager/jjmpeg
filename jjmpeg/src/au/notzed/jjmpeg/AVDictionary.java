package au.notzed.jjmpeg;

import java.nio.ByteBuffer;

public class AVDictionary extends AVDictionaryAbstract {
    public AVDictionary(ByteBuffer p) {
        setNative(new AVDictionaryNative(this, p));
    }

    public static AVDictionary create(ByteBuffer p) {
        return new AVDictionary(p);
    }
}

class AVDictionaryNative extends AVDictionaryNativeAbstract {

    protected AVDictionaryNative(AVObject o, ByteBuffer p) {
        super(o, p);
    }
}
