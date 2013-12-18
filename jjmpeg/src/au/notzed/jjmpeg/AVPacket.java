/*
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjmpeg, a java binding to ffmpeg's libraries.
 *
 * jjmpeg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jjmpeg.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.notzed.jjmpeg;

import java.nio.ByteBuffer;

/**
 *
 * @author notzed
 */
public class AVPacket extends AVPacketAbstract {

	public static final int AV_PKT_FLAG_KEY = 1;
	
	AVPacket(ByteBuffer p) {
		setNative(new AVPacketNative(this, p));
	}

	public static AVPacket create() {
		return new AVPacket(AVPacketNative.allocatePacket());
	}

    public static AVPacket create(ByteBuffer p) {
        return new AVPacket(p);
    }

	public void setData(ByteBuffer data, int size) {
		AVPacketNative.setData(n.p, data, size);
	}
	
	/**
	 * Consumes 'len' bytes by incrementing the data pointer and decrementing the
	 * length of the packet.
	 * @param len
	 * @return
	 */
	public int consume(int len) {
		return AVPacketNative.consume(n.p, len);
	}
}

class AVPacketNative extends AVPacketNativeAbstract {

	AVPacketNative(AVObject o, ByteBuffer p) {
		super(o, p);
	}

	@Override
	public void dispose() {
		if (p != null) {
			freePacket(p);
		}
		super.dispose();
	}

	static native int consume(ByteBuffer p, int len);

	static native ByteBuffer allocatePacket();

	static native void freePacket(ByteBuffer p);
	
	public static native void setData(ByteBuffer p, ByteBuffer b, int size);
}
