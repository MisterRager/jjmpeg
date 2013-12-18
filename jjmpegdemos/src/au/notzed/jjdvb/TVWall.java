/*
 * Copyright (c) 2012 Michael Zucchi
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
package au.notzed.jjdvb;

import au.notzed.jjdvb.util.DVBChannel;
import au.notzed.jjdvb.util.DVBChannels;
import au.notzed.jjmpeg.AVCodecContext;
import au.notzed.jjmpeg.PixelFormat;
import au.notzed.jjmpeg.exception.AVException;
import au.notzed.jjmpeg.exception.AVIOException;
import au.notzed.jjmpeg.io.JJMediaReader;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Uses jjdvb to tune and jjmpeg to display all the video streams
 * present in a given bouquet (or whatever it's called).
 * 
 * It takes a long time to start the video output as libavformat
 * is scanning a lot of signal for stream information.
 * 
 * If the kernel buffer overflows, read() returns EOVERFLOW
 * and libavformat just gives up.  I can't seem to set
 * the buffer size on the dvr, or maybe it doesn't matter
 * if the decoding is too slow.  Need to have a separate
 * frame-only thread, as in au.notzed.jjmpeg.mediaplayer.
 * 
 * The timing isn't very good, but it works ok for a demo.
 * 
 * See DVBChannel for the channels.list format: it's just
 * from tzap.
 * @author notzed
 */
public class TVWall {

	DVBChannels channels;
	ReaderThread reader;
	ViewerThread viewer;
	JJMediaReader mr;
	//
	JFrame frame;
	LinkedList<ViewerThread> viewers = new LinkedList<ViewerThread>();
	HashMap<Integer, ViewerThread> viewersID = new HashMap<Integer, ViewerThread>();

	public void start() {
		try {
			channels = new DVBChannels("../jjmpeg/channels.list");

			// open frontend
			FE fe = FE.create("/dev/dvb/adapter0/frontend0");

			setChannel(fe, channels.getChannels().get(0));

			// Open demux
			DMX dmx = DMX.create("/dev/dvb/adapter0/demux0");

			// set filter to take whole stream
			DMXPESFilterParams filter = DMXPESFilterParams.create();
			filter.setPid((short) 0x2000);
			//filter.setPid((short) 0x90a);
			filter.setInput(DMXInput.DMX_IN_FRONTEND);
			filter.setOutput(DMXOutput.DMX_OUT_TS_TAP);
			//filter.setPesType(DMXPESType.DMX_PES_OTHER);
			filter.setPesType(DMXPESType.DMX_PES_VIDEO0);
			filter.setFlags(DMXPESFilterParams.DMX_IMMEDIATE_START);
			dmx.setPESFilter(filter);
			dmx.setBufferSize(1024 * 1024);

			mr = new JJMediaReader("/dev/dvb/adapter0/dvr0");

			reader = new ReaderThread();

			JPanel panel = new JPanel();

			// Find out how many we have.
			int count = 0;
			for (JJMediaReader.JJReaderStream rs : mr.getStreams()) {
				System.out.printf("Stream  %s\n", rs);
				if (rs.getType() == AVCodecContext.AVMEDIA_TYPE_VIDEO) {
					JJMediaReader.JJReaderVideo vs = (JJMediaReader.JJReaderVideo) rs;
					try {
						vs.open();
						count++;
					} catch (Throwable t) {
					}
				}
			}

			if (count == 0) {
				return;
			}

			// determine size limits based on
			int ncols = Math.max(1, (int) Math.sqrt(count));
			int nrows = count / ncols;

			panel.setLayout(new GridLayout(nrows, ncols));

			int maxWidth = 1920 - 100;
			int maxHeight = 1200 - 100;

			int width = maxWidth / ncols;
			int height = maxHeight / nrows;

			width = 400;
			height = 280;
			
			for (JJMediaReader.JJReaderStream rs : mr.getStreams()) {
				System.out.printf("Stream  %s\n", rs);
				if (rs.getType() == AVCodecContext.AVMEDIA_TYPE_VIDEO) {
					JJMediaReader.JJReaderVideo vs = (JJMediaReader.JJReaderVideo) rs;

					try {
						//vs.open();
						vs.setOutputFormat(PixelFormat.PIX_FMT_BGR24, width, height);

						if (buffer.size() == 0) {
							for (int i = 0; i < 100; i++) {
								buffer.add(new VideoFrame(-1, vs.createImage()));
							}
						}

						BufferedImage iconImage = vs.createImage();
						JLabel label = new JLabel(new ImageIcon(iconImage));

						ViewerThread vt = new ViewerThread(label, iconImage);
						viewers.add(vt);
						viewersID.put(vs.getStream().getIndex(), vt);

						vt.start();
						panel.add(label);
					} catch (Throwable t) {
					}
				}
			}


			frame = new JFrame("TV");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(panel, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);

			reader.start();

		} catch (AVException ex) {
			Logger.getLogger(TVWall.class.getName()).log(Level.SEVERE, null, ex);
		} catch (AVIOException ex) {
			Logger.getLogger(TVWall.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(TVWall.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(TVWall.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void setChannel(FE fe, DVBChannel chan) throws IOException, InterruptedException {
		int count = 0;

		// set channel
		fe.setFrontend(chan.params);

		// wait for channel to sync
		FEStatus status = new FEStatus();
		do {
			int st;

			fe.readStatus(status);
			st = status.getStatus();

			if ((st & status.FE_HAS_LOCK) != 0) {
				return;
			}

			System.out.printf("tuning status = %08x\n", st);

			Thread.sleep(100);

		} while (count++ < 100);

		throw new IOException("Timeout tuning channel");
	}

	class VideoFrame {

		long pts;
		BufferedImage image;

		public VideoFrame(long pts, BufferedImage image) {
			this.pts = pts;
			this.image = image;
		}
	}
	LinkedBlockingQueue<VideoFrame> buffer = new LinkedBlockingQueue<VideoFrame>();

	class ReaderThread extends Thread {

		public ReaderThread() {
			super("reader thread");
		}
		boolean cancelled = false;

		@Override
		public void run() {
			JJMediaReader.JJReaderVideo vr;
			int dropped = 0;
			try {
				while (!cancelled && (vr = (JJMediaReader.JJReaderVideo) mr.readFrame()) != null) {
					VideoFrame outFrame = null;

					try {
						outFrame = buffer.poll();

						ViewerThread vt = viewersID.get(vr.getStream().getIndex());

						if (vr == null) {
							continue;
						}

						if (outFrame != null) {
							vr.getOutputFrame(outFrame.image);
							outFrame.pts = vr.convertPTS(mr.getPTS());

							vt.frames.add(outFrame);
							outFrame = null;
						} else {
							// Now, I think because libavcodec probing so far it gets too many images
							// in the queue and it never ctaches up: this just flushes the queue and helps
							// things work a bit better.
							dropped++;
							if (dropped > 10) {
								System.out.println("dropped frame, reset buffer");
								vt.frames.drainTo(buffer);
							}
						}
					} finally {
						if (outFrame != null) {
							buffer.add(outFrame);
						}
					}

				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	class ViewerThread extends Thread {

		LinkedBlockingQueue<VideoFrame> frames = new LinkedBlockingQueue<VideoFrame>();
		JLabel label;
		BufferedImage iconImage;

		public ViewerThread(JLabel label, BufferedImage iconImage) {
			this.iconImage = iconImage;
			this.label = label;
		}
		boolean cancelled = false;

		@Override
		public void run() {
			long startPTS = -1;
			long startTime = System.currentTimeMillis();
			Graphics2D gg = iconImage.createGraphics();

			while (!cancelled) {
				VideoFrame inFrame = null;
				try {
					inFrame = frames.take();

					long pts = inFrame.pts;

					gg.drawImage(inFrame.image, null, null);

					if (startPTS == -1) {
						startPTS = pts;
						startTime = System.currentTimeMillis();
					} else {
						long elapsed = System.currentTimeMillis() - startTime;

						long diff = (pts - startPTS) - elapsed;

						if (Math.abs(diff) > 100) {
							startPTS = pts;
							startTime = System.currentTimeMillis();
						} else if (diff > 0) {
							sleep(diff);
						}
					}

					label.repaint();
				} catch (InterruptedException ex) {
					Logger.getLogger(TVWall.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					if (inFrame != null) {
						inFrame.pts = -1;
						buffer.add(inFrame);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				TVWall tv = new TVWall();

				tv.start();
			}
		});
	}
}
