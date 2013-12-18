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
public class AVFrame extends AVFrameAbstract {

	protected AVFrame(ByteBuffer p) {
		setNative(new AVFrameNative(this, p));
	}

	protected AVFrame(ByteBuffer p, boolean allocated) {
		setNative(new AVFrameNative(this, p, allocated));
	}

	static public AVFrame create(ByteBuffer p) {
		if (p == null) {
			return null;
		}
		return new AVFrame(p, false);
	}

	static public AVFrame create() {
		return frameAlloc();
	}

    /*
	static public AVFrame create(PixelFormat fmt, int width, int height) {
		AVFrame f = create();
		int res = f.alloc(fmt.toC(), width, height);

		if (res != 0) {
			throw new ExceptionInInitializerError("Unable to allocate bitplanes");
		}
		((AVFrameNative) f.n).filled = true;

		return f;
	}
	*/

	public AVPlane getPlaneAt(int index, PixelFormat fmt, int width, int height) {
		int lineSize = getlineSizeAt(index);

		return new AVPlane(AVFrameNative.getPlaneAt(n.p, index, fmt.toC(fmt), width, height), lineSize, width, height);
	}

	public boolean isKeyFrame() {
		return getisKeyFrame() != 0;
	}
}

class AVFrameNative extends AVFrameNativeAbstract {

	// Was it allocated (with allocFrame()), or just referenced
	boolean allocated = true;
	// Has it been filled using avpicture_alloc()
	boolean filled = false;

	AVFrameNative(AVObject o, ByteBuffer p) {
		super(o, p);
	}

	AVFrameNative(AVObject o, ByteBuffer p, boolean allocated) {
		super(o, p);
		this.allocated = allocated;
	}

	static native ByteBuffer getPlaneAt(ByteBuffer p, int index, int pixelFormat, int width, int height);

	@Override
	public void dispose() {
		if (p != null) {
            /* TODO:figure out if this is needed
			if (filled) {
				free(p);
			}
			*/
			if (allocated) {
				AVFormatContextNative._free(p);
			}
		}
		super.dispose();
	}
}
