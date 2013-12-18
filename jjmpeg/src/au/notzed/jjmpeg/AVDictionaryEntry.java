package au.notzed.jjmpeg;

import java.nio.ByteBuffer;

public class AVDictionaryEntry extends AVDictionaryEntryAbstract {
    public AVDictionaryEntry(ByteBuffer p) {
        setNative(new AVDictionaryEntryNative(this, p));
    }
    public static AVDictionaryEntry create(ByteBuffer p) {
        return new AVDictionaryEntry(p);
    }
}

class AVDictionaryEntryNative extends AVDictionaryEntryNativeAbstract {
    AVDictionaryEntryNative(AVObject o, ByteBuffer p) {
        super(o, p);
    }
}
