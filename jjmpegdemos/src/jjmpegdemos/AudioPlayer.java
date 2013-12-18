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
package jjmpegdemos;

import au.notzed.jjmpeg.AVAudioPacket;
import au.notzed.jjmpeg.AVCodec;
import au.notzed.jjmpeg.AVCodecContext;
import au.notzed.jjmpeg.AVFormatContext;
import au.notzed.jjmpeg.AVPacket;
import au.notzed.jjmpeg.AVSamples;
import au.notzed.jjmpeg.AVStream;
import au.notzed.jjmpeg.exception.AVDecodingError;
import au.notzed.jjmpeg.exception.AVIOException;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALException;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Plays a music file, or the first audio track of multimedia.
 *
 * TODO: check cleanup
 * @author notzed
 */
public class AudioPlayer {

	String file;
	AVFormatContext format;
	AVCodecContext codecContext;
	AVCodecContext actx;
	AVCodec aCodec;
	AVStream audioStream;
	int audioID = -1;
	//
	AVPacket packet;
	AVAudioPacket apacket;
	AVSamples samples;
	// OpenAL stuff for audio output
	AL al;
	// Make sure this is a power of 2 long
	int[] buffers = new int[8];
	int[] sources = new int[1];

	void inital() {
		ALut.alutInit();
		al = ALFactory.getAL();
		al.alGetError();

		al.alGenBuffers(buffers.length, buffers, 0);
		if (al.alGetError() != AL.AL_NO_ERROR) {
			throw new ALException("Unable to generate buffer");
		}

		al.alGenSources(1, sources, 0);
		if (al.alGetError() != AL.AL_NO_ERROR) {
			throw new ALException("Unable to generate buffer");
		}
	}

	void waitForFinish(int source) {
		int[] res = new int[1];
		int queued, done;
		al.alGetSourcei(source, AL.AL_BUFFERS_QUEUED, res, 0);
		queued = res[0];
		do {
			al.alGetSourcei(source, AL.AL_BUFFERS_PROCESSED, res, 0);
			done = res[0];
			if (done < queued) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException ex) {
					Logger.getLogger(AudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

		} while (done < queued);
	}

	void waitForBuffer(int source, int bufferid) throws InterruptedException {
		int[] res = new int[1];

		al.alGetSourcei(source, AL.AL_BUFFERS_QUEUED, res, 0);
		if (res[0] < buffers.length) {
			return;
		}

		int proc;
		do {
			al.alGetSourcei(source, AL.AL_BUFFERS_PROCESSED, res, 0);
			proc = res[0];
			if (proc == 0) {
				Thread.sleep(5);
			}
		} while (proc == 0);

		al.alSourceUnqueueBuffers(source, 1, buffers, bufferid);
	}

	void enqueueBuffer(int source, int bufferid) {
		int[] state = new int[1];

		al.alSourceQueueBuffers(source, 1, buffers, bufferid);

		al.alGetSourcei(source, AL.AL_SOURCE_STATE, state, 0);
		if (state[0] != AL.AL_PLAYING) {
			al.alSourcePlay(source);
		}
	}

	public AudioPlayer() {
	}

	public void playFile(String file) throws FileNotFoundException, AVIOException {
		this.file = file;

		inital();

		format = AVFormatContext.openInputFile(file);
		if (format.findStreamInfo() < 0) {
			return;
		}

		int nstreams = format.getNBStreams();
		for (int i = 0; i < nstreams; i++) {
			AVStream s = format.getStreamAt(i);
			AVCodecContext ctx = s.getCodec();
			int type = ctx.getCodecType();
			if (type == AVCodecContext.AVMEDIA_TYPE_AUDIO) {
				audioID = i;
				audioStream = s;
				actx = ctx;
				break;
			}
		}

		if (audioStream == null) {
			System.out.println("no audio stream");
			return;
		}

		aCodec = AVCodec.findDecoder(actx.getCodecID());
		if (aCodec == null) {
			System.out.println("no codec");
		}

		actx.open(aCodec);

		System.out.printf("Opened Audio\n channels=%d\n format=%s\n rate=%d\n", actx.getChannels(), actx.getSampleFmt(), actx.getSampleRate());

		packet = AVPacket.create();
		apacket = AVAudioPacket.create();
		samples = new AVSamples(actx.getSampleFmt());

		int alformat = actx.getChannels() == 2 ? AL.AL_FORMAT_STEREO16 : AL.AL_FORMAT_MONO16;
		int freq = actx.getSampleRate();

		int counter = 0;

		try {
			while (format.readFrame(packet) >= 0) {
				try {
					if (packet.getStreamIndex() == audioID) {
						apacket.setSrc(packet);
						while (apacket.getSize() > 0) {
							int len = actx.decodeAudio(samples, apacket);
							int inbuf = counter & (buffers.length - 1);

							waitForBuffer(sources[0], inbuf);
							al.alBufferData(buffers[inbuf], alformat, samples.getBuffer(), len, freq);
							enqueueBuffer(sources[0], inbuf);

							counter++;
						}
					}
				} catch (InterruptedException x) {
				} catch (AVDecodingError x) {
				} finally {
					packet.freePacket();
				}
			}
		} finally {
		}

		format.closeInputFile();
	}

	public static void main(String[] args) {
		final String file;

		if (args.length == 0) {
			File f = Main.chooseFile();
			if (f == null) {
				return;
			}
			file = f.getPath();
		} else {
			file = args[0];
		}

		final AudioPlayer audioScanner = new AudioPlayer();

		new Thread(new Runnable() {

			public void run() {
				System.out.println("Hit ctrl-C to quit");
				try {
					audioScanner.playFile(file);
				} catch (AVIOException ex) {
					Logger.getLogger(AudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
				} catch (FileNotFoundException ex) {
					Logger.getLogger(AudioPlayer.class.getName()).log(Level.SEVERE, null, ex);
					System.exit(0);
				}
			}
		}).start();
		JOptionPane.showMessageDialog(null, "Playing " + file, "Simple Audio Player", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
}
