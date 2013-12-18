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
package au.notzed.jjmpeg.streamwriter;

import au.notzed.jjmpeg.AVFormatContext;
import au.notzed.jjmpeg.exception.AVException;
import au.notzed.jjmpeg.exception.AVIOException;
import au.notzed.jjmpeg.io.JJMediaWriter;
import au.notzed.jjmpeg.io.JJMediaWriter.JJWriterVideo;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Demonstrates using the JJMediaWriter to write video frames to an arbitrary container.
 * @author notzed
 */
public class VideoWriter {

	static int width = 720;
	static int height = 576;
	static int fps = 25;

	public static void main(String[] args) {
		String filename = args.length > 0 ? args[0] : "moving-text.avi";

		try {
			AVFormatContext.registerAll();

			JJMediaWriter writer = new JJMediaWriter(filename);
			JJWriterVideo vstream = writer.addVideoStream(width, height, 25, 400000);

			BufferedImage image = vstream.createImage();

			writer.open();

			// write some frames
			Graphics2D gg = image.createGraphics();
			gg.setBackground(Color.black);
			gg.setColor(Color.white);
			for (int i = 0; i < fps * 5; i++) {
				gg.clearRect(0, 0, width, height);
				gg.drawString("Moving Text!", i, i);
				vstream.addFrame(image);
			}

			writer.close();
		} catch (AVIOException ex) {
			Logger.getLogger(VideoWriter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVException ex) {
			Logger.getLogger(VideoWriter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
