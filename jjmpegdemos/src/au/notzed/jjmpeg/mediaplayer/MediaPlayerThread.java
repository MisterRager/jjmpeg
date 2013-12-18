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

import au.notzed.jjmpeg.AVRational;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * A media sink which takes audio + video and renders them using separate
 * threads.
 * 
 * Video is synchronised to the audio, if there is no audio
 * it uses simple sleep timing.
 * @author notzed
 */
public class MediaPlayerThread implements MediaSink {

	/**
	// vq is used to order by pts
	// invq is used to throttle so we don't decode too many frames in advance
	 */
	PriorityBlockingQueue<VideoFrame> vq = new PriorityBlockingQueue<VideoFrame>(10);
	LinkedBlockingQueue<Integer> invq = new LinkedBlockingQueue<Integer>(10);
	PriorityBlockingQueue<AudioFrame> aq = new PriorityBlockingQueue<AudioFrame>(30);
	LinkedBlockingQueue<Integer> inaq = new LinkedBlockingQueue<Integer>(30);
	final MediaReader src;
	//
	AVRational videoTB;
	long videoStart;
	// debug stuff
	JLabel label;
	BufferedImage image;

	public MediaPlayerThread(MediaReader src) {
		this.src = src;
	}

	/**
	 * Render otuput to a label
	 * @param label Needed so it knows when to repaint
	 * @param image The image in the label
	 */
	public void initRenderers(JLabel label, BufferedImage image) {
		this.label = label;
		this.image = image;

		for (MediaDecoder md : src.streamMap.values()) {
			if (md instanceof VideoDecoder) {
				VideoDecoder vd = (VideoDecoder) md;

				vd.setOutputSize(image.getWidth(), image.getHeight());

				videoTB = vd.stream.getTimeBase();
				videoStart = vd.stream.getStartTime();
			} else if (md instanceof AudioDecoder) {
				AudioDecoder ad = (AudioDecoder) md;

				audioSampleRate = ad.cc.getSampleRate();
				int count = ad.cc.getChannels();
				System.out.printf("audio channels=%d\n sample rate=%d\n", count, audioSampleRate);
				if (count == 1) {
					alFormat = AL.AL_FORMAT_MONO16;
				} else {
					alFormat = AL.AL_FORMAT_STEREO16;
				}
			}
		}
	}

	public void initRenderers() {
		for (MediaDecoder md : src.streamMap.values()) {
			if (md instanceof VideoDecoder) {
				final VideoDecoder vd = (VideoDecoder) md;
				JFrame win = new JFrame("Video Out");

				image = new BufferedImage(vd.cc.getWidth(), vd.cc.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				win.add(label = new JLabel(new ImageIcon(image)));
				win.pack();
				win.setDefaultCloseOperation(win.EXIT_ON_CLOSE);
				win.setVisible(true);

				videoTB = vd.stream.getTimeBase();
				videoStart = vd.stream.getStartTime();

				win.addKeyListener(new KeyAdapter() {

					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == 27) {
							System.exit(0);
						}
						if (e.getKeyCode() == e.VK_LEFT) {
						} else if (e.getKeyCode() == e.VK_LEFT) {
						}
					}
				});
			} else if (md instanceof AudioDecoder) {
				AudioDecoder ad = (AudioDecoder) md;

				audioSampleRate = ad.cc.getSampleRate();
				int count = ad.cc.getChannels();
				System.out.printf("audio channels=%d\n sample rate=%d\n", count, audioSampleRate);
				if (count == 1) {
					alFormat = AL.AL_FORMAT_MONO16;
				} else {
					alFormat = AL.AL_FORMAT_STEREO16;
				}
			}
		}
	}
	// Audio
	AL al;
	int[] buffers = new int[4];
	int[] sources = new int[1];
	int nextBuffer;
	int audioSampleRate;
	int alFormat = AL.AL_FORMAT_STEREO16;

	public void initAudioOutput(AL al) {
		this.al = al;

		al.alGenBuffers(buffers.length, buffers, 0);
		if (al.alGetError() != AL.AL_NO_ERROR) {
			throw new ALException("Unable to generate buffer");
		}

		al.alGenSources(1, sources, 0);
		if (al.alGetError() != AL.AL_NO_ERROR) {
			throw new ALException("Unable to generate buffer");
		}

		al.alSourcei(sources[0], AL.AL_LOOPING, 0);
		al.alSourcef(sources[0], AL.AL_PITCH, 1f);
	}

	public long getDisplayTime() {
		return Math.max(0, clock);
	}
	LinkedList<MediaSinkListener> listeners = new LinkedList<MediaSinkListener>();

	public void addMediaSinkListener(MediaSinkListener listener) {
		listeners.add(listener);
	}

	void firePositionChanged() {
		final MediaPlayerThread player = this;
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				for (MediaSinkListener l : listeners) {
					l.positionChanged(player);
				}
			}
		});
	}

	int getNextAudioBuffer() {
		int res = nextBuffer;

		nextBuffer = (nextBuffer + 1) & (buffers.length - 1);

		return res;
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
	Integer dummy = Integer.valueOf(0);

	public void queueFrame(VideoFrame frame) throws InterruptedException {
		//System.out.printf("queue frame, invq size = %d\n", invq.size());
		vq.put(frame);
		invq.put(dummy);
	}

	public void queueFrame(AudioFrame frame) throws InterruptedException {
		//System.out.printf("queue frame, invq size = %d\n", invq.size());
		aq.put(frame);
		inaq.put(dummy);
	}
	VideoThread vthread;
	AudioThread athread;

	public void start() {
		vthread = new VideoThread();
		athread = new AudioThread();

		vthread.start();
		athread.start();
	}
	long clock;
	long startms = -1;
	long seekoffset = 0;

	public void postSeek(long stamp) {
		LinkedList<MediaFrame> dropped = new LinkedList<MediaFrame>();
		// drain queues
		vq.drainTo(dropped);
		aq.drainTo(dropped);

		invq.clear();
		inaq.clear();

		for (MediaFrame f : dropped) {
			f.recycle();
		}

		// might need to reset startms or clock?
		clock = stamp;
		startms = -1;
		seekoffset = stamp;
	}

	public void pause() {
		// could do something so it pauses immediately?
	}

	public void unpause() {
		// ensure we re-sync delay
		startms = -1;
	}

	class VideoThread extends CancellableThread {

		public VideoThread() {
			super("Video Render");
		}

		@Override
		public void run() {
			startms = System.currentTimeMillis();
			while (!cancelled) {
				VideoFrame vf = null;
				try {
					vf = vq.take();
					// update throttle
					invq.take();

					long pts = vf.getPTS();
					long targetms = pts + startms;
					long now = System.currentTimeMillis();

					long delay;

					if (startms == -1) {
						startms = now - pts;
						//startms = now - seekoffset;
						delay = 0;
					} else {
						delay = targetms - now;
					}
					if (false) {
						// sync to audio?
						System.out.printf("aclock = %d vclock = %d seeked = %d\n", clock, pts, seekoffset);
						long sclock = Math.max(clock, seekoffset);
						delay = pts - sclock;

						//delay = 20;
						System.out.println("sleep " + (delay));
					}
					if (delay > 0) {
						Thread.sleep(delay);
					}

					vf.fillImage(image);
					label.repaint();

					firePositionChanged();
				} catch (InterruptedException ex) {
				} finally {
					vf.recycle();
				}
			}
		}
	}

	class AudioThread extends CancellableThread {

		public AudioThread() {
			super("Audio Render");
		}

		@Override
		public void run() {
			while (!cancelled) {
				AudioFrame aframe = null;
				try {
					aframe = aq.take();
					inaq.take();

					int bufid = getNextAudioBuffer();
					ByteBuffer bb = aframe.frame.getBuffer();

					waitForBuffer(sources[0], bufid);
					al.alBufferData(buffers[bufid], alFormat, bb, bb.limit(), audioSampleRate);
					enqueueBuffer(sources[0], bufid);

					clock = aframe.getPTS();
					firePositionChanged();
				} catch (InterruptedException ex) {
				} finally {
					aframe.recycle();
				}
			}
		}
	}
}
