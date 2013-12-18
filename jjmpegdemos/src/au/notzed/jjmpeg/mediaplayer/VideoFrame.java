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
package au.notzed.jjmpeg.mediaplayer;

import au.notzed.jjmpeg.AVFrame;
import au.notzed.jjmpeg.AVPlane;
import au.notzed.jjmpeg.PixelFormat;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Queue;

/**
 * Video frame that can (possibly) dispose itself
 * @author notzed
 */
public class VideoFrame extends MediaFrame {

	public final AVFrame frame;
	long pts;

	public VideoFrame(Queue<VideoFrame> rq, AVFrame frame) {
		super(rq);
		this.frame = frame;
	}

	@Override
	long getPTS() {
		return pts;
	}

	public void dispose() {
		frame.dispose();
	}

	/**
	 * Copy the AVFrame to an image.
	 *
	 * The image must be the same size as the AVFrame.
	 *
	 * The AVFrame must be in BGR24 format, and the image
	 * must be TYPE_3BYTE_BGR.
	 * @param image
	 */
	public void fillImage(BufferedImage image) {
		AVPlane splane = frame.getPlaneAt(0, PixelFormat.PIX_FMT_BGR24, image.getWidth(), image.getHeight());
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

		splane.data.get(data, 0, Math.min(data.length, splane.data.capacity()));
		splane.data.rewind();
	}
}
