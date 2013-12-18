/*
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjmpegdemos.
 * 
 * jjmpegdemos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpegdemos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jjmpegdemos.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.notzed.jjmpeg.scale;

import au.notzed.jjmpeg.AVCodecContext;
import au.notzed.jjmpeg.AVFrame;
import au.notzed.jjmpeg.AVPlane;
import au.notzed.jjmpeg.PixelFormat;
import au.notzed.jjmpeg.SwsContext;
import au.notzed.jjmpeg.exception.AVIOException;
import au.notzed.jjmpeg.exception.AVInvalidCodecException;
import au.notzed.jjmpeg.exception.AVInvalidStreamException;
import au.notzed.jjmpeg.io.JJMediaReader;
import au.notzed.jjmpeg.io.JJMediaReader.JJReaderStream;
import au.notzed.jjmpeg.io.JJMediaReader.JJReaderVideo;
import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tries out the new (as of today) direct array scaling interface.
 * 
 * @author notzed
 */
public class Performance {

	static abstract class Test {

		JJMediaReader msrc;
		JJReaderVideo vr;
		int width;
		int height;
		int frameCount = 2000;
		
		public Test(JJMediaReader msrc) {
			this.msrc = msrc;
			vr = (JJReaderVideo) msrc.getStreamByID(0);
			width = vr.getWidth();
			height = vr.getHeight();
		}

		abstract String getName();

		abstract void process(JJReaderVideo vr);

		void test() {
			JJReaderStream sr;
			int frames = 0;
			
			//while (frames < frameCount && (sr = msrc.readFrame()) != null) {
			while ((sr = msrc.readFrame()) != null) {
				long pts = msrc.getPTS();
				switch (sr.getType()) {
					case AVCodecContext.AVMEDIA_TYPE_VIDEO:
						//AVFrame frame = vr.getFrame();

						process(vr);
						//frames++;
						break;
				}
			}
		}
	}

	static class ArrayTest extends Test {

		SwsContext scale;
		int[] dataInt;

		public ArrayTest(JJMediaReader msrc) {
			super(msrc);

			scale = SwsContext.create(width, height, vr.getPixelFormat(), width, height, PixelFormat.PIX_FMT_ABGR, SwsContext.SWS_BILINEAR);
			dataInt = new int[width * height];
		}

		@Override
		String getName() {
			return "int array";
		}

		@Override
		void process(JJReaderVideo vr) {
			AVFrame frame = vr.getFrame();

			scale.scale(frame, 0, height, dataInt);

		}
	}

	static class BufferTest extends Test {

		public BufferTest(JJMediaReader msrc) {
			super(msrc);

			vr.setOutputFormat(PixelFormat.PIX_FMT_ABGR, width, height);
		}

		@Override
		String getName() {
			return "intbuffer abgr";
		}

		@Override
		void process(JJReaderVideo vr) {
			vr.getOutputFrame();
		}
	}

	static class BufferByteRGBTest extends Test {

		byte[] data;

		public BufferByteRGBTest(JJMediaReader msrc) {
			super(msrc);

			vr.setOutputFormat(PixelFormat.PIX_FMT_BGR24, width, height);

			data = new byte[width * height * 3];
		}

		@Override
		String getName() {
			return "bytebuffer to array bgr";
		}

		@Override
		void process(JJReaderVideo vr) {

			AVPlane splane = vr.getOutputFrame().getPlaneAt(0, PixelFormat.PIX_FMT_BGR24, width, height);

			splane.data.get(data, 0, Math.min(data.length, splane.data.capacity()));
			splane.data.rewind();
		}
	}

	static class BufferIntRGBTest extends Test {

		int[] data;

		public BufferIntRGBTest(JJMediaReader msrc) {
			super(msrc);

			vr.setOutputFormat(PixelFormat.PIX_FMT_ABGR, width, height);

			data = new int[width * height];
		}

		@Override
		String getName() {
			return "intbuffer to array abgr";
		}

		@Override
		void process(JJReaderVideo vr) {

			AVPlane splane = vr.getOutputFrame().getPlaneAt(0, PixelFormat.PIX_FMT_ABGR, width, height);
			
			IntBuffer ib = splane.data.asIntBuffer();

			ib.get(data, 0, Math.min(data.length, splane.data.capacity()));
			ib.rewind();
		}
	}

	static void testArray(JJMediaReader msrc) {
	}

	public static void main(String[] args) {
		try {
			//String src = "/home/notzed/Videos/big-buck-bunny_trailer.webm";
			//String src = "/home/notzed/Videos/test/test-1440x1080.mpg";
			String src = "/home/notzed/Videos/test/test-pal.avi";
			
			Class tests[] = {ArrayTest.class, BufferTest.class, BufferByteRGBTest.class, BufferIntRGBTest.class};
			//Class tests[] = { BufferTest.class };

			// input
			for (int i = 0; i < tests.length; i++) {
				for (int j = 0; j < 1; j++) {
					JJMediaReader msrc = new JJMediaReader(src);
					JJReaderVideo vr = (JJReaderVideo) msrc.getStreamByID(0);
					JJReaderStream sr;

					vr.open();

					Test t = (Test) tests[i].getConstructor(JJMediaReader.class).newInstance(msrc);
					
					long now = System.currentTimeMillis();
					t.test();
					now = System.currentTimeMillis() - now;

					System.out.printf("test %s run %d took %d.%03ds\n", t.getName(), j, now / 1000, now % 1000);
					msrc.dispose();
				}
			}
		} catch (InstantiationException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvocationTargetException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVInvalidStreamException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVIOException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVInvalidCodecException ex) {
			Logger.getLogger(Performance.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
