/*
 * Taken from libavcodec/api-example.c, with title code added.
 *
 * Copyright (c) 2001 Fabrice Bellard
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjmpegdemos.
 *
 * jjmpegdemos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpegdemos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jjmpegdemos.  If not, see <http://www.gnu.org/licenses/>.
 */
package jjmpegdemos;

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
import au.notzed.jjmpeg.util.JJFileChooser;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 * Simple raw mpeg writer based on ffmpeg examples.
 *
 * This shows how the base api is used, but JJMediaWriter can be used
 * to create formats supported by libavformat in a more java-friendly way.
 * 
 * @author notzed
 */
public class TitleWriter {

	int width = 640;
	int height = 480;
	int fps = 25;
	int stepsize = 15;
	Color colour = Color.RED;

	public void writeTitles(String filename, String title) throws AVIOException {
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
			tb.setDen(fps);
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

			Font font = new Font(Font.SERIF, Font.BOLD, 64);
			gg.setFont(font);
			gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// for yuv conversion of rgb input
			AVFrame rgb = AVFrame.create(PixelFormat.PIX_FMT_BGR24, width, height);
			SwsContext sws = SwsContext.create(width, height, PixelFormat.PIX_FMT_BGR24, width, height, PixelFormat.PIX_FMT_YUV420P, SwsContext.SWS_X);
			AVPlane rgbPlane = rgb.getPlaneAt(0, PixelFormat.PIX_FMT_BGR24, width, height);

			Rectangle2D rect = font.getStringBounds(title, gg.getFontRenderContext());

			int offSize = (int) Math.round(rect.getWidth());

			int frameCount = (int) ((rect.getWidth() + width) / stepsize);

			int i= 0;
			for (i = 0; (width - i*stepsize) > (-offSize); i++) {
				// Prepare dummy image
				gg.setColor(Color.black);
				gg.fillRect(0, 0, width, height);
				gg.setColor(colour);
				gg.drawString(title, width - i * stepsize, (height - 64) / 2);

				// Convert to YUV
				rgbPlane.data.put(data);
				rgbPlane.data.rewind();
				sws.scale(rgb, 0, height, picture);

				// encode image
				out_size = c.encodeVideo(outbuf, picture);
				System.out.printf("encoding frame %3d (size=%5d)\n", i, out_size);
				f.getChannel().write(outbuf);
			}

			// get delayed fps
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

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			JTextField text;
			JSpinner speed;
			JSpinner width;
			JSpinner height;
			JSpinner fps;
			JColorChooser colour;

			@Override
			public void run() {
				Object[] msg = new Object[]{"Title", text = new JTextField(),
					"Speed", speed = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1)),
					"Width", width = new JSpinner(new SpinnerNumberModel(640, 320, 1920, 1)),
					"Height", height = new JSpinner(new SpinnerNumberModel(480, 240, 1080, 1)),
					"FrameRate", fps = new JSpinner(new SpinnerNumberModel(25, 1, 120, 1)),
					"Colour", colour = new JColorChooser(Color.red)
				};
				colour.setPreviewPanel(new JPanel());
				Object[] options = new Object[]{"Create", "Cancel"};
				if (JOptionPane.showOptionDialog(null, msg, "Title Creator", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]) == JOptionPane.YES_OPTION) {
					JJFileChooser vfc = new JJFileChooser();

					if (vfc.showSaveDialog(null) == JJFileChooser.APPROVE_OPTION) {
						File file = vfc.getSelectedFile();

						TitleWriter tw = new TitleWriter();

						tw.width = (Integer) width.getValue();
						tw.height = (Integer) height.getValue();
						tw.fps = (Integer) fps.getValue();
						tw.stepsize = (Integer) speed.getValue();
						tw.colour = colour.getColor();
						try {
							tw.writeTitles(file.getAbsolutePath(), text.getText());
						} catch (AVIOException ex) {
							Logger.getLogger(TitleWriter.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
				System.exit(0);
			}
		});
	}
}
