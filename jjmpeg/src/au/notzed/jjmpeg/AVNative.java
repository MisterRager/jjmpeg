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

import java.io.PrintStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author notzed
 */
abstract public class AVNative extends WeakReference<AVObject> {

	ByteBuffer p;
	static private ReferenceQueue<AVObject> refqueue = new ReferenceQueue<AVObject>();
	static private LinkedList<AVNative> reflist = new LinkedList<AVNative>();

	protected AVNative(AVObject jobject, ByteBuffer p) {
		super(jobject, refqueue);
		reflist.add(this);

		this.p = p;
		p.order(ByteOrder.nativeOrder());

		//System.out.println("new " + getClass().getName());
		gc();
	}
	static final boolean is64;
	/**
	 * Index of libavformat major version number in result from getVersions()
	 */
	static final int LIBAVFORMAT_VERSION = 0;
	static final int LIBAVCODEC_VERSION = 1;
	static final int LIBAVUTIL_VERSION = 2;

	static {
		int bits;

		System.loadLibrary("jjmpeg");
		bits = initNative();

		if (bits == 0) {
			throw new UnsatisfiedLinkError("Unable to open jjmpeg");
		}
		is64 = bits == 64;

		// may as well do these here i guess?
		AVCodecContext.init();
		AVFormatContext.registerAll();
	}

	static native ByteBuffer getPointer(ByteBuffer base, int offset, int size);

	static native ByteBuffer getPointerIndex(ByteBuffer base, int offset, int size, int index);

	static native int initNative();

	static native void getVersions(ByteBuffer b);

	static native ByteBuffer _malloc(int size);

	static native void _free(ByteBuffer mem);

	/**
	 * Retrieve run-time library version info.
	 *
	 * use LIB*_VERSION indices to get actual versions.
	 */
	static public int[] getVersions() {
		ByteBuffer bvers = ByteBuffer.allocateDirect(4 * 3).order(ByteOrder.nativeOrder());

		getVersions(bvers);

		int[] vers = new int[3];
		bvers.asIntBuffer().get(vers);
		return vers;
	}

	private static void gc() {
		AVNative an;

		while (((an = (AVNative) refqueue.poll()) != null)) {
			Logger.getLogger(AVNative.class.getName()).log(Level.FINE, "Auto Disposing: {0}", an.getClass().getName());
			//System.out.printf("** Auto Disposing: {0}\n", an.getClass().getName());
			an.dispose();
		}

		//for (AVNative n : reflist) {
		//	if (n.get() == null) {
		//		System.out.println("Unreachable: " + n.getClass().getName() + " is enqueued = " + n.isEnqueued());
		//	}
		//}
	}

	/**
	 * Debug: dump live object list
	 * @param out
	 */
	public static void dumpLive(PrintStream out) {
		try {
			for (AVNative n : reflist) {
				if (n.get() != null) {
					System.out.println("Live : " + n.getClass().getName());
				}
			}
			// just ignore these
		} catch (NullPointerException x) {
		} catch (ConcurrentModificationException x) {
		}
	}

	/**
	 * Dispose of this resource. It must be safe to call this multiple times.
	 *
	 * The default dispose sets this.p = null;
	 */
	public void dispose() {
		if (p != null) {
			reflist.remove(this);
			p = null;
			Logger.getLogger(AVNative.class.getName()).log(Level.FINE, "Disposing: {0}", getClass().getName());
		}
	}
}
