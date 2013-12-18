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

import au.notzed.jjmpeg.AVCodecContext;
import au.notzed.jjmpeg.AVFrame;
import au.notzed.jjmpeg.AVSamples;
import au.notzed.jjmpeg.exception.AVIOException;
import au.notzed.jjmpeg.exception.AVInvalidCodecException;
import au.notzed.jjmpeg.exception.AVInvalidFormatException;
import au.notzed.jjmpeg.exception.AVInvalidStreamException;
import au.notzed.jjmpeg.io.JJMediaReader;
import au.notzed.jjmpeg.io.JJMediaReader.JJReaderAudio;
import au.notzed.jjmpeg.io.JJMediaReader.JJReaderStream;
import au.notzed.jjmpeg.io.JJMediaReader.JJReaderVideo;
import au.notzed.jjmpeg.io.JJMediaWriter;
import au.notzed.jjmpeg.io.JJMediaWriter.JJWriterAudio;
import au.notzed.jjmpeg.io.JJMediaWriter.JJWriterVideo;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple trancoder example.
 * @author notzed
 */
public class Transcode {

	public static void main(String[] args) throws AVInvalidFormatException {
		try {
			String src = "/home/notzed/Videos/big-buck-bunny_trailer.webm";
			//String src = "/home/notzed/Videos/LIVE_ Australian Formula 1 Grand Prix Day.m2ts";
			String dst = "/tmp/buck.mp4";

			// input
			JJMediaReader msrc = new JJMediaReader(src);
			JJReaderStream sr;

			JJReaderAudio ar = null;
			JJReaderVideo vr = null;

			// open the first video and audio stream
			for (JJReaderStream m : msrc.getStreams()) {
				switch (m.getType()) {
					case AVCodecContext.AVMEDIA_TYPE_VIDEO:
						if (vr == null) {
							m.open();
							vr = (JJReaderVideo) m;
						}
						break;
					case AVCodecContext.AVMEDIA_TYPE_AUDIO:
						if (ar == null) {
							m.open();
							ar = (JJReaderAudio) m;
						}
						break;
				}
			}

			// output
			JJMediaWriter mw = new JJMediaWriter(dst);
			JJWriterVideo vw = null;
			JJWriterAudio aw = null;
			if (vr != null) {
				vw = mw.addVideoStream(mw.getFormat().getVideoCodec(), 0, vr.getWidth(), vr.getHeight(), 25, 800 * 1024);
			}

			AVSamples outsamples = null;
			if (ar != null) {
				AVCodecContext ac = ar.getContext();

				aw = mw.addAudioStream(mw.getFormat().getAudioCodec(), 1, ac.getSampleFmt(), ac.getSampleRate(), ac.getChannels(), 128 * 1000);
				//aw = mw.addAudioStream(CodecID.CODEC_ID_MP3, 1, ac.getSampleFmt(), ac.getSampleRate(), ac.getChannels(), 128 * 1000);
				//aw = mw.addAudioStream(CodecID.CODEC_ID_AAC, 1, SampleFormat.SAMPLE_FMT_S16, ac.getSampleRate(), ac.getChannels(), 128 * 1000);
			}

			mw.open();

			// post=open stuff
			if (aw != null) {
				outsamples = aw.createSamples();
			}

			while ((sr = msrc.readFrame()) != null) {
				long pts = msrc.getPTS();
				switch (sr.getType()) {
					case AVCodecContext.AVMEDIA_TYPE_VIDEO:
						if (vw != null) {
							JJReaderVideo jav = (JJReaderVideo) sr;
							AVFrame frame = jav.getFrame();

							frame.setPTS(AVCodecContext.AV_NOPTS_VALUE);
							vw.addFrame(frame);
							//System.out.printf("%04d: video frame added\n", pts);
						}
						break;
					case AVCodecContext.AVMEDIA_TYPE_AUDIO:
						if (aw != null) {
							JJReaderAudio jar = (JJMediaReader.JJReaderAudio) sr;
							AVSamples samples;
							while ((samples = jar.getSamples()) != null) {
								//System.out.printf("%10d: audio samples %d\n", sr.convertPTS(pts), samples.getBuffer().limit());
								while (outsamples.fill(samples)) {
									aw.addFrame(outsamples);
								}
							}
						}
						break;
				}
			}

			mw.close();

		} catch (AVInvalidStreamException ex) {
			Logger.getLogger(Transcode.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVIOException ex) {
			Logger.getLogger(Transcode.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVInvalidCodecException ex) {
			Logger.getLogger(Transcode.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	static double sin = 0;
}
