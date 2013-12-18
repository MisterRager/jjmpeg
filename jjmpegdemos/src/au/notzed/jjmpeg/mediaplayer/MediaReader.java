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

import au.notzed.jjmpeg.AVCodecContext;
import au.notzed.jjmpeg.AVFormatContext;
import au.notzed.jjmpeg.AVPacket;
import au.notzed.jjmpeg.AVStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reads AV packets and doles them out to appropriate decoding queues.
 * 
 * Also provides player functions such as seek, pause and so on.
 * @author notzed
 */
public class MediaReader extends CancellableThread {

	AVFormatContext format;
	HashMap<Integer, MediaDecoder> streamMap = new HashMap<Integer, MediaDecoder>();
	final String file;
	MediaSink dest;
	// for recycling packets
	ConcurrentLinkedQueue<AVPacket> packetQueue = new ConcurrentLinkedQueue<AVPacket>();
	// for commands to the player thread
	LinkedBlockingQueue<PlayerCMD> cmdQueue = new LinkedBlockingQueue<PlayerCMD>();
	long duration;

	public MediaReader(String fileName) throws IOException {
		super("AVReader: " + fileName);

		this.file = fileName;

		format = AVFormatContext.openInputFile(file);
		if (format.findStreamInfo() < 0) {
			throw new IOException("Unable to open AV file");
		}
		streamMap.entrySet();

	}

	public Set<Entry<Integer, MediaDecoder>> getDecoders() {
		return streamMap.entrySet();
	}

	class PlayerCMD {

		int type;
		long stamp;
		final static int QUIT = 0;
		final static int PLAY = 1;
		final static int SEEK = 2;
		final static int PAUSE = 3;
		final static int RESUME = 4;

		public PlayerCMD(int type, long stamp) {
			this.type = type;
			this.stamp = stamp;
		}
	}

	/**
	 * Returns stream duration in ms
	 * @return
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Seek to given time.
	 *
	 * This runs asynchronously and the video will have seeked at some following time.
	 * @param stamp timestamp in ms
	 * @param type, ignored
	 */
	public void seek(long stamp, int type) {
		cmdQueue.offer(new PlayerCMD(PlayerCMD.SEEK, stamp));
	}

	/**
	 * Pause at the current position.
	 */
	public void pause() {
		cmdQueue.offer(new PlayerCMD(PlayerCMD.PAUSE, 0));
	}

	/**
	 * Resume at the current position.
	 */
	public void unpause() {
		cmdQueue.offer(new PlayerCMD(PlayerCMD.RESUME, 0));
	}

	// FIXME: exceptions
	public void createDefaultDecoders(MediaSink dest) throws IOException {

		this.dest = dest;

		int nstreams = format.getNBStreams();
		AVStream vavstream = null;
		int vstream = -1;
		AVStream aavstream = null;
		int astream = -1;
		for (int i = 0; i < nstreams; i++) {
			AVStream s = format.getStreamAt(i);
			AVCodecContext cc = s.getCodec();
			int type = cc.getCodecType();
			if (vstream == -1 && type == AVCodecContext.AVMEDIA_TYPE_VIDEO) {
				vstream = i;
				vavstream = s;
			}
			if (astream == -1 && type == AVCodecContext.AVMEDIA_TYPE_AUDIO) {
				astream = i;
				aavstream = s;
			}
		}

		if (astream == -1 && vstream == -1) {
			throw new IOException("No media found");
		}

		duration = 0;
		if (vstream != -1) {
			VideoDecoder vd;
			streamMap.put(vstream, vd = new VideoDecoder(this, dest, vavstream, vstream));
			duration = vd.duration;
		}
		if (astream != -1) {
			AudioDecoder ad;
			streamMap.put(astream, ad = new AudioDecoder(this, dest, aavstream, astream));
			duration = Math.max(duration, ad.duration);
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		for (MediaDecoder dec : streamMap.values()) {
			dec.close();
		}
	}

	AVPacket getPacket() {
		//if (true) {
		//	return AVPacket.create();
		//}
		AVPacket packet = packetQueue.poll();

		if (packet == null) {
			packet = AVPacket.create();
			System.out.println("creating new avpacket");
		}
		return packet;
	}

	public void recyclePacket(AVPacket packet) {
		packet.freePacket();
		packetQueue.add(packet);
	}

	void postSeek() {
		for (MediaDecoder dec : streamMap.values()) {
			dec.postSeek();
		}
	}

	@Override
	public void run() {
		AVPacket packet = null;

		// init all decoders
		for (MediaDecoder dec : streamMap.values()) {
			dec.init();
		}

		packet = AVPacket.create();

		try {
			// ... if we get to the end of file this quits, but we don't want it to?

			out:
			while (!cancelled && format.readFrame(packet) >= 0) {
				try {
					// map to stream
					MediaDecoder md = streamMap.get(packet.getStreamIndex());
					if (md != null) {
						//System.out.println("sending packet to decoder " + packet.getStreamIndex() + " + " + packet.getSize());
						md.enqueuePacket(packet);
						packet = null;
					}

					// check for control commands
					PlayerCMD cmd;
					boolean paused = false;
					do {
						if (paused) {
							cmd = cmdQueue.take();
						} else {
							cmd = cmdQueue.poll();
						}
						if (cmd != null) {
							switch (cmd.type) {
								case PlayerCMD.QUIT:
									break out;
								case PlayerCMD.SEEK:
									format.seekFile(-1, 0, cmd.stamp * 1000, cmd.stamp * 1000, 0);
									postSeek();
									dest.postSeek(cmd.stamp);
									System.out.println("post seek: " + cmd.stamp);
									break;
								case PlayerCMD.PAUSE:
									// just wait for another command, until it is resume/play/quit
									paused = true;
									dest.pause();
									System.out.println("paused " + cmd.stamp);
									break;
								case PlayerCMD.PLAY: // TODO: seek/re-open for play?
								case PlayerCMD.RESUME:
									if (paused) {
										System.out.println("resume play");
									}
									paused = false;
									dest.unpause();
									break;
							}
						}
					} while (cmd != null || paused);
				} catch (InterruptedException x) {
				} finally {
					if (packet == null) {
						packet = getPacket();
					} else {
						packet.freePacket();
					}
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			System.out.println("File finished: " + file);
			format.closeInputFile();
		}
	}
}
