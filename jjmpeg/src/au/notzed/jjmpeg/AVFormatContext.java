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

import au.notzed.jjmpeg.exception.AVIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author notzed
 */
public class AVFormatContext extends AVFormatContextAbstract {

	public static final int AVSEEK_FLAG_BACKWARD = 1; ///< seek backward
	public static final int AVSEEK_FLAG_BYTE = 2; ///< seeking based on position in bytes
	public static final int AVSEEK_FLAG_ANY = 4; ///< seek to any frame, even non-keyframes
	public static final int AVSEEK_FLAG_FRAME = 8; ///< seeking based on frame number

	protected AVFormatContext(ByteBuffer p, int type) {
		setNative(new AVFormatContextNative(this, p, type));
	}

	static AVFormatContext create(ByteBuffer p) {
		return new AVFormatContext(p, 0);
	}

	static AVFormatContext create(ByteBuffer p, int type) {
		return new AVFormatContext(p, type);
	}

	@Override
	public int readFrame(AVPacket packet) {
		int res = super.readFrame(packet);

		if (res < 0) {
			switch (res) {
				case -32: // EPIPE
				// the superclass binding makes this a pain , not sure how to fix
				//		throw new AVIOException(-res);
				case -1: // EOF
					break;
				default:
					break;
			}
		}

		return res;
	}


}

class AVFormatContextNative extends AVFormatContextNativeAbstract {

	private final int type;

	AVFormatContextNative(AVObject o, ByteBuffer p, int type) {
		super(o, p);
		this.type = type;
	}

	@Override
	public void dispose() {
		if (p != null) {
			switch (type) {
				case 0:
					free_context(p);
					break;
				case 1:
					close_input(p);
					break;
				case 2:
					close_input(p);
					break;
			}
			super.dispose();
		}
	}


    static native void open(ByteBuffer pb, String name, ByteBuffer fmt, ByteBuffer options);
}
