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
import au.notzed.jjmpeg.exception.AVIOException;
import au.notzed.jjmpeg.io.JJMediaReader;
import au.notzed.jjmpeg.mediaplayer.MediaPlayer;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Uses jjdvb to tune and jjmpeg to display DVB-T station.
 * 
 * It takes a long time to start the video output as libavformat
 * is scanning a lot of signal for stream information.
 * 
 * If the kernel buffer overflows, read() returns EOVERFLOW
 * and libavformat just gives up.  This version attempts
 * to buffer frames instead of images: it still doesn't keep up.
 * 
 * Note that this just takes the first audio and video streams
 * it finds, so they wont align!
 * 
 * The timing isn't very good, but it works ok for a demo.
 * 
 * See DVBChannel for the channels.list format: it's just
 * from tzap.
 * @author notzed
 */
public class TVWindow {

	DVBChannels channels;
	ReaderThread reader;
	ViewerThread viewer;
	JJMediaReader mr;
	JJMediaReader.JJReaderVideo vs;
	//
	JFrame frame;
	JLabel label;
	BufferedImage iconImage;
	BufferedImage image;

	public void start() {
		try {
			channels = new DVBChannels("../jjmpeg/channels.list");
			DVBChannel c = channels.getChannels().get(1);
			// open frontend
			FE fe = FE.create("/dev/dvb/adapter0/frontend0");

			setChannel(fe, c);

			// Open demux
			DMX dmx = DMX.create("/dev/dvb/adapter0/demux0");

			// set filter to take whole stream
			DMXPESFilterParams filter = DMXPESFilterParams.create();
			filter.setPid((short) c.vpid);
			//filter.setPid((short) 0x90a);
			filter.setInput(DMXInput.DMX_IN_FRONTEND);
			filter.setOutput(DMXOutput.DMX_OUT_TS_TAP);
			filter.setPesType(DMXPESType.DMX_PES_OTHER);
			//filter.setPesType(DMXPESType.DMX_PES_VIDEO0);
			filter.setFlags(DMXPESFilterParams.DMX_IMMEDIATE_START);
			dmx.setPESFilter(filter);
			dmx.addPID((short) c.apid);
			dmx.setBufferSize(1024 * 1024);

			MediaPlayer mp = new MediaPlayer("/dev/dvb/adapter0/dvr0");

			mp.start();
			/*			
			mr = new JJMediaReader("/dev/dvb/adapter0/dvr0");
			
			for (JJMediaReader.JJReaderStream rs : mr.getStreams()) {
			System.out.printf("Stream  %s\n", rs);
			}
			
			vs = mr.openFirstVideoStream();
			
			if (vs == null) {
			System.exit(1);
			}
			
			vs.setOutputFormat(PixelFormat.PIX_FMT_BGR24, vs.getWidth(), vs.getHeight());
			
			image = vs.createImage();
			iconImage = vs.createImage();
			
			for (int i = 0; i < 100; i++) {
			buffer.add(new VideoFrame(-1, vs.createImage()));
			}
			
			viewer = new ViewerThread();
			viewer.start();
			
			
			frame = new JFrame("TV");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			label = new JLabel(new ImageIcon(iconImage));
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add(label, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
			
			reader = new ReaderThread();
			reader.start();
			 */
		} catch (AVIOException ex) {
			Logger.getLogger(TVWindow.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(TVWindow.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(TVWindow.class.getName()).log(Level.SEVERE, null, ex);
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
	LinkedBlockingQueue<VideoFrame> frames = new LinkedBlockingQueue<VideoFrame>();

	class ReaderThread extends Thread {

		boolean cancelled = false;

		@Override
		public void run() {
			JJMediaReader.JJReaderVideo vr;
			int dropped = 0;
			while (!cancelled && (vr = (JJMediaReader.JJReaderVideo) mr.readFrame()) != null) {
				VideoFrame outFrame = null;
				try {
					outFrame = buffer.poll();

					if (outFrame != null) {
						vr.getOutputFrame(outFrame.image);
						outFrame.pts = vr.convertPTS(mr.getPTS());
						frames.add(outFrame);
						outFrame = null;
					} else {
						// Now, I think because libavcodec probing so far it gets too many images
						// in the queue and it never ctaches up: this just flushes the queue and helps
						// things work a bit better.
						dropped++;
						if (dropped > 10) {
							System.out.println("dropped frame, reset buffer");
							frames.drainTo(buffer);
						}
					}

				} finally {
					if (outFrame != null) {
						buffer.add(outFrame);
					}
				}
			}
		}
	}

	class ViewerThread extends Thread {

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
					Logger.getLogger(TVWindow.class.getName()).log(Level.SEVERE, null, ex);
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
				TVWindow tv = new TVWindow();

				tv.start();
			}
		});
	}
}
