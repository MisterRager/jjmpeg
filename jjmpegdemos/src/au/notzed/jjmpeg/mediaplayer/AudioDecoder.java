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

import au.notzed.jjmpeg.AVAudioPacket;
import au.notzed.jjmpeg.AVPacket;
import au.notzed.jjmpeg.AVSamples;
import au.notzed.jjmpeg.AVStream;
import au.notzed.jjmpeg.exception.AVDecodingError;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Decoded AVPacket's into audio, and pass them to the MediaSink
 * @author notzed
 */
public class AudioDecoder extends MediaDecoder {

	AudioDecoder(MediaReader src, MediaSink dest, AVStream stream, int streamid) throws IOException {
		super(src, dest, stream, streamid);

		apacket = AVAudioPacket.create();
	}
	AVAudioPacket apacket;
	// for recycling frames
	ConcurrentLinkedQueue<AudioFrame> frameQueue = new ConcurrentLinkedQueue<AudioFrame>();

	AudioFrame getOutputFrame() {
		AudioFrame af = frameQueue.poll();

		if (af == null) {
			af = new AudioFrame(frameQueue, new AVSamples(this.cc.getSampleFmt()));
			System.out.println("allocate new audio frame");
		}
		return af;
	}

	@Override
	void init() {
	}

	@Override
	void decodePacket(AVPacket packet) throws AVDecodingError, InterruptedException {
		//System.out.println("audio decode packet()");
		//if (true)return;
		apacket.setSrc(packet);
		//apacket = AVAudioPacket.create(packet);

		try {
			while (apacket.getSize() > 0) {
				AudioFrame af = getOutputFrame();

				int len = cc.decodeAudio(af.frame, apacket);

				af.pts = convertPTS(packet.getDTS());

				dest.queueFrame(af);
				//af.recycle();
			}
		} catch (Exception x) {
			cc.flushBuffers();
			System.out.println("decode audio failed " + x + " packet size " + packet.getSize());
			//	af.recycle();
		}
	}
}
