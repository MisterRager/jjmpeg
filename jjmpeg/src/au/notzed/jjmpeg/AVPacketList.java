package au.notzed.jjmpeg;

import java.nio.ByteBuffer;

/**
 *
 * @author notzed
 */
public class AVPacketList extends AVPacketListAbstract {
	AVPacketList(ByteBuffer p) {
		setNative(new AVPacketListNative(this, p));
	}

	public static AVPacketList create(ByteBuffer p) {
		return new AVPacketList(p);
	}
}

class AVPacketListNative extends AVPacketListNativeAbstract {
	public AVPacketListNative(AVObject o, ByteBuffer p) {
		super(o, p);
	}
}
