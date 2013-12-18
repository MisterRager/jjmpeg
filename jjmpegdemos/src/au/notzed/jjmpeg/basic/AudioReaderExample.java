package au.notzed.jjmpeg.basic;

import au.notzed.jjmpeg.AVAudioPacket;
import au.notzed.jjmpeg.AVCodec;
import au.notzed.jjmpeg.AVCodecContext;
import au.notzed.jjmpeg.AVFormatContext;
import au.notzed.jjmpeg.AVPacket;
import au.notzed.jjmpeg.AVSamples;
import au.notzed.jjmpeg.AVStream;
import au.notzed.jjmpeg.exception.AVIOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads a supported audio format, and dumps out raw samples to a file.
 *
 * Output can be played with:
 *  mplayer -rawaudio channels=2:rate=44100:samplesize=2 -demuxer rawaudio audio.raw
 * @author notzed
 */
public class AudioReaderExample {

	String file;
	AVFormatContext format;
	AVCodecContext codecContext;
	AVCodecContext actx;
	AVCodec aCodec;
	AVStream audioStream;
	int audioID = -1;
//
	AVPacket packet;
	// baiscally a packet pointer to the real one
	AVAudioPacket apacket;
	AVSamples samples;

	public AudioReaderExample(String file) throws FileNotFoundException, AVIOException {
		this.file = file;

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

		long total = 0;

		FileOutputStream fos = new FileOutputStream("audio.raw");

		try {
			while (format.readFrame(packet) >= 0) {
				//System.out.println("read packet");
				try {
					if (packet.getStreamIndex() == audioID) {
						apacket.setSrc(packet);
						System.out.println("audio packet");
						while (apacket.getSize() > 0) {
							int len = actx.decodeAudio(samples, apacket);

							total += len / 4;

							System.out.println(" audio samples: " + samples.getBuffer().limit());

							fos.getChannel().write(samples.getBuffer());
						}
					}
				} catch (IOException ex) {
					Logger.getLogger(AudioReaderExample.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					packet.freePacket();
				}
			}
		} finally {
			try {
				fos.close();
			} catch (IOException ex) {
				Logger.getLogger(AudioReaderExample.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		System.out.printf("total decoded samples = %d   seconds = %d?\n", total, total / actx.getSampleRate());

		format.closeInputFile();
	}

	public static void main(String[] args) throws FileNotFoundException, AVIOException {
		String file = args.length > 0 ? args[0] :"/home/notzed/Videos/big-buck-bunny_trailer.webm";

		AudioReaderExample audioScanner = new AudioReaderExample(file);
	}
}
