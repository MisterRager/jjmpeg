/*
 * Taken from libavcodec/api-examples.c
 *
 * Copyright (c) 2001 Fabrice Bellard
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.notzed.jjmpeg.basic;

import au.notzed.jjmpeg.AVCodec;
import au.notzed.jjmpeg.AVCodecContext;
import au.notzed.jjmpeg.AVFormatContext;
import au.notzed.jjmpeg.AVFrame;
import au.notzed.jjmpeg.AVPlane;
import au.notzed.jjmpeg.AVRational;
import au.notzed.jjmpeg.CodecID;
import au.notzed.jjmpeg.PixelFormat;
import au.notzed.jjmpeg.SwsContext;
import au.notzed.jjmpeg.exception.AVEncodingError;
import au.notzed.jjmpeg.exception.AVIOException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * @author notzed
 */
public class VideoWriterExample {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws AVIOException {
		String filename = "/tmp/test.mpeg";
		int width = 352;
		int height = 288;

		AVCodec codec;
		AVCodecContext c;
		AVFrame picture;

		AVFormatContext.registerAll();

		codec = AVCodec.findEncoder(CodecID.CODEC_ID_MPEG2VIDEO);
		if (codec == null) {
			System.err.println("codec not found");
			System.exit(1);
		}

		try {
			c = AVCodecContext.create();
			picture = AVFrame.create(PixelFormat.PIX_FMT_YUV420P, width, height);

			c.setBitRate(400000);
			c.setWidth(width);
			c.setHeight(height);
			AVRational tb = c.getTimeBase();
			tb.setNum(1);
			tb.setDen(25);
			c.setGOPSize(10);
			c.setMaxBFrames(1);
			c.setPixFmt(PixelFormat.PIX_FMT_YUV420P);

			c.open(codec);

			FileOutputStream f = new FileOutputStream(filename);
			ByteBuffer outbuf = ByteBuffer.allocateDirect(100000);

			System.out.println("creating :" + filename);

			// encode 1 second of video
			AVPlane planeY = picture.getPlaneAt(0, PixelFormat.PIX_FMT_YUV420P, width, height);
			AVPlane planeU = picture.getPlaneAt(1, PixelFormat.PIX_FMT_YUV420P, width, height);
			AVPlane planeV = picture.getPlaneAt(2, PixelFormat.PIX_FMT_YUV420P, width, height);

			// Use Java2D to create flying title
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
			Graphics2D gg = bi.createGraphics();
			int out_size = 0;

			byte[] dataU = new byte[width / 2];
			Arrays.fill(dataU, (byte) 128);

			gg.setFont(new Font(Font.SERIF, Font.BOLD, 64));
			gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// for yuv conversion of rgb input
			AVFrame rgb = AVFrame.create(PixelFormat.PIX_FMT_BGR24, width, height);
			SwsContext sws = SwsContext.create(width, height, PixelFormat.PIX_FMT_BGR24, width, height, PixelFormat.PIX_FMT_YUV420P, SwsContext.SWS_X);
			AVPlane rgbPlane = rgb.getPlaneAt(0, PixelFormat.PIX_FMT_BGR24, width, height);

			int i;
			for (i = 0; i < 25; i++) {
				// Prepare dummy image
				gg.setColor(Color.black);
				gg.fillRect(0, 0, width, height);
				gg.setColor(Color.red);
				gg.drawString("jjmpeg!", i * 10, (height - 64) / 2);

				// Convert to YUV
				rgbPlane.data.put(data);
				rgbPlane.data.rewind();
				sws.scale(rgb, 0, height, picture);

				// encode image
				out_size = c.encodeVideo(outbuf, picture);
				System.out.printf("encoding frame %3d (size=%5d)\n", i, out_size);
				f.getChannel().write(outbuf);
			}

			// get delayed frames
			while (out_size > 0) {
				out_size = c.encodeVideo(outbuf, null);
				System.out.printf("encoding frame %3d (size=%5d)\n", i++, out_size);
				f.getChannel().write(outbuf);
			}

			// add sequence end code to have a real mpeg file
			f.write(new byte[]{0x00, 0x00, 0x01, (byte) 0xb7});
			f.close();

			sws.dispose();

			picture.dispose();
			c.close();
			c.dispose();
		} catch (AVEncodingError e) {
		} catch (IOException e) {
		}
	}
}
