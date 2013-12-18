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
import au.notzed.jjmpeg.AVPacket;
import au.notzed.jjmpeg.AVStream;
import au.notzed.jjmpeg.PixelFormat;
import au.notzed.jjmpeg.SwsContext;
import au.notzed.jjmpeg.exception.AVDecodingError;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Takes AV packets and decodes video from them.
 * @author notzed
 */
public class VideoDecoder extends MediaDecoder {

	int width;
	int height;
	PixelFormat format;
	AVFrame frame;
	int swidth;
	int sheight;
	SwsContext scale;

	/**
	 * Create a new video decoder for a given stream.
	 * 
	 * @param src where encoded frames come from
	 * @param dest where decoded frames are sent
	 * @param stream stream information
	 * @param streamid corresponding stream id
	 * @throws IOException 
	 */
	VideoDecoder(MediaReader src, MediaSink dest, AVStream stream, int streamid) throws IOException {
		super(src, dest, stream, streamid);

		// init some local stuff like picture frames
		height = cc.getHeight();
		width = cc.getWidth();
		format = cc.getPixFmt();
		frame = AVFrame.create();

		swidth = width;
		sheight = height;

		scale = SwsContext.create(width, height, format, swidth, sheight, PixelFormat.PIX_FMT_BGR24, SwsContext.SWS_BILINEAR);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public void setOutputSize(int swidth, int sheight) {
		scale.dispose();
		this.swidth = swidth;
		this.sheight = sheight;
		scale = SwsContext.create(width, height, format, swidth, sheight, PixelFormat.PIX_FMT_BGR24, SwsContext.SWS_BILINEAR);
	}

	// for recycling frames
	ConcurrentLinkedQueue<VideoFrame> frameQueue = new ConcurrentLinkedQueue<VideoFrame>();

	VideoFrame getOutputFrame() {
		VideoFrame vf = frameQueue.poll();

		if (vf == null) {
			vf = new VideoFrame(frameQueue, AVFrame.create(PixelFormat.PIX_FMT_BGR24, swidth, sheight));
			System.out.println("allocate new video frame");
		} else {
			//System.out.println("recycle frame");
		}
		return vf;
	}

	@Override
	void init() {
	}

	void decodePacket(AVPacket packet) throws AVDecodingError, InterruptedException {
		//System.out.println("video decode packet()");
		//if (true)
		//	return;
		boolean frameFinished = cc.decodeVideo(frame, packet);

		if (frameFinished) {
			VideoFrame vf = getOutputFrame();

			vf.pts = convertPTS(packet.getDTS());

			scale.scale(frame, 0, height, vf.frame);

			dest.queueFrame(vf);
		}
	}
}
