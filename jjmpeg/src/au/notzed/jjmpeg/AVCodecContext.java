/*
 * Copyright (c) 2011 Michael Zucchi
 *
 * This file is part of jjmpeg, a java binding to ffmpeg's libraries.
 *
 * jjmpeg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jjmpeg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jjmpeg.  If not, see <http://www.gnu.org/licenses/>.
 */
package au.notzed.jjmpeg;

import au.notzed.jjmpeg.exception.AVDecodingError;
import au.notzed.jjmpeg.exception.AVEncodingError;
import au.notzed.jjmpeg.exception.AVIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author notzed
 */
public class AVCodecContext extends AVCodecContextAbstract {

	// TODO: move these to an interface
	public static final int AVMEDIA_TYPE_UNKNOWN = -1;
	public static final int AVMEDIA_TYPE_VIDEO = 0;
	public static final int AVMEDIA_TYPE_AUDIO = 1;
	public static final int AVMEDIA_TYPE_DATA = 2;
	public static final int AVMEDIA_TYPE_SUBTITLE = 3;
	public static final int AVMEDIA_TYPE_ATTACHMENT = 4;
	public static final int AVMEDIA_TYPE_NB = 5;
	//
	public static final int FF_MB_DECISION_SIMPLE = 0;        ///< uses mb_cmp
	public static final int FF_MB_DECISION_BITS = 1;       ///< chooses the one which needs the fewest bits
	public static final int FF_MB_DECISION_RD = 2;        ///< rate distortion	public static final int
	//
	///< Use fixed qscale.
	public static final int CODEC_FLAG_QSCALE = 0x0002;
	///< 4 MV per MB allowed / advanced prediction for H.263.
	public static final int CODEC_FLAG_4MV = 0x0004;
	///< Use qpel MC.
	public static final int CODEC_FLAG_QPEL = 0x0010;
	///< Use GMC.
	public static final int CODEC_FLAG_GMC = 0x0020;
	///< Always try a MB with MV=<0,0>.
	public static final int CODEC_FLAG_MV0 = 0x0040;
	///< Use data partitioning.
	public static final int CODEC_FLAG_PART = 0x0080;
	public static final int CODEC_FLAG_INPUT_PRESERVED = 0x0100;
	///< Use internal 2pass ratecontrol in first pass mode.
	public static final int CODEC_FLAG_PASS1 = 0x0200;
	///< Use internal 2pass ratecontrol in second pass mode.
	public static final int CODEC_FLAG_PASS2 = 0x0400;
	///< Use external Huffman table (for MJPEG).
	public static final int CODEC_FLAG_EXTERN_HUFF = 0x1000;
	///< Only decode/encode grayscale.
	public static final int CODEC_FLAG_GRAY = 0x2000;
	///< Don't draw edges.
	public static final int CODEC_FLAG_EMU_EDGE = 0x4000;
	///< error[?] variables will be set during encoding.
	public static final int CODEC_FLAG_PSNR = 0x8000;
	/** Input bitstream might be truncated at a random
	 * public static final int CODEC_FLAG_TRUNCATED = 0x00010000;
	 * location instead of only at frame boundaries. */
	///< Normalize adaptive quantization.
	public static final int CODEC_FLAG_NORMALIZE_AQP = 0x00020000;
	///< Use interlaced DCT.
	public static final int CODEC_FLAG_INTERLACED_DCT = 0x00040000;
	///< Force low delay.
	public static final int CODEC_FLAG_LOW_DELAY = 0x00080000;
	///< Use alternate scan.
	public static final int CODEC_FLAG_ALT_SCAN = 0x00100000;
	///< Place global headers in extradata instead of every keyframe.
	public static final int CODEC_FLAG_GLOBAL_HEADER = 0x00400000;
	///< Use only bitexact stuff (except (I)DCT).
	public static final int CODEC_FLAG_BITEXACT = 0x00800000;
	/* Fx : Flag for h263+ extra options */
	///< H.263 advanced intra coding / MPEG-4 AC prediction
	public static final int CODEC_FLAG_AC_PRED = 0x01000000;
	///< unlimited motion vector
	public static final int CODEC_FLAG_H263P_UMV = 0x02000000;
	///< Use rate distortion optimization for cbp.
	public static final int CODEC_FLAG_CBP_RD = 0x04000000;
	///< Use rate distortion optimization for qp selectioon.
	public static final int CODEC_FLAG_QP_RD = 0x08000000;
	///< H.263 alternative inter VLC
	public static final int CODEC_FLAG_H263P_AIV = 0x00000008;
	///< OBMC
	public static final int CODEC_FLAG_OBMC = 0x00000001;
	///< loop filter
	public static final int CODEC_FLAG_LOOP_FILTER = 0x00000800;
	public static final int CODEC_FLAG_H263P_SLICE_STRUCT = 0x10000000;
	///< interlaced motion estimation
	public static final int CODEC_FLAG_INTERLACED_ME = 0x20000000;
	///< Will reserve space for SVCD scan offset user data.
	public static final int CODEC_FLAG_SVCD_SCAN_OFFSET = 0x40000000;
	public static final int CODEC_FLAG_CLOSED_GOP = 0x80000000;
	///< Allow non spec compliant speedup tricks.
	public static final int CODEC_FLAG2_FAST = 0x00000001;
	///< Strictly enforce GOP size.
	public static final int CODEC_FLAG2_STRICT_GOP = 0x00000002;
	///< Skip bitstream encoding.
	public static final int CODEC_FLAG2_NO_OUTPUT = 0x00000004;
	///< Place global headers at every keyframe instead of in extradata.
	public static final int CODEC_FLAG2_LOCAL_HEADER = 0x00000008;
	///< H.264 allow B-frames to be used as references.
	public static final int CODEC_FLAG2_BPYRAMID = 0x00000010;
	///< H.264 weighted biprediction for B-frames
	public static final int CODEC_FLAG2_WPRED = 0x00000020;
	///< H.264 one reference per partition, as opposed to one reference per macroblock
	public static final int CODEC_FLAG2_MIXED_REFS = 0x00000040;
	///< H.264 high profile 8x8 transform
	public static final int CODEC_FLAG2_8X8DCT = 0x00000080;
	///< H.264 fast pskip
	public static final int CODEC_FLAG2_FASTPSKIP = 0x00000100;
	///< H.264 access unit delimiters
	public static final int CODEC_FLAG2_AUD = 0x00000200;
	///< B-frame rate-distortion optimization
	public static final int CODEC_FLAG2_BRDO = 0x00000400;
	///< Use MPEG-2 intra VLC table.
	public static final int CODEC_FLAG2_INTRA_VLC = 0x00000800;
	///< Only do ME/MC (I frames -> ref, P frame -> ME+MC).
	public static final int CODEC_FLAG2_MEMC_ONLY = 0x00001000;
	///< timecode is in drop frame format.
	public static final int CODEC_FLAG2_DROP_FRAME_TIMECODE = 0x00002000;
	///< RD optimal MB level residual skipping
	public static final int CODEC_FLAG2_SKIP_RD = 0x00004000;
	///< Input bitstream might be truncated at a packet boundaries instead of only at frame boundaries.
	public static final int CODEC_FLAG2_CHUNKS = 0x00008000;
	///< Use MPEG-2 nonlinear quantizer.
	public static final int CODEC_FLAG2_NON_LINEAR_QUANT = 0x00010000;
	///< Use a bit reservoir when encoding if possible
	public static final int CODEC_FLAG2_BIT_RESERVOIR = 0x00020000;
	///< Use macroblock tree ratecontrol (x264 only)
	public static final int CODEC_FLAG2_MBTREE = 0x00040000;
	///< Use psycho visual optimizations.
	public static final int CODEC_FLAG2_PSY = 0x00080000;
	///< Compute SSIM during encoding, error[] values are undefined.
	public static final int CODEC_FLAG2_SSIM = 0x00100000;
	///< Use periodic insertion of intra blocks instead of keyframes.
	public static final int CODEC_FLAG2_INTRA_REFRESH = 0x00200000;
	//
	public static final int FF_LAMBDA_SHIFT = 7;
	public static final int FF_LAMBDA_SCALE = (1 << FF_LAMBDA_SHIFT);
	public static final int FF_QP2LAMBDA = 118; ///< factor to convert from H.263 QP to lambda
	public static final int FF_LAMBDA_MAX = (256 * 128 - 1);
	//
	public static final long AV_TIME_BASE = 1000000;
	public static final long AV_NOPTS_VALUE = (0x8000000000000000L);
	public static final int AVCODEC_MAX_AUDIO_FRAME_SIZE = 192000; // 1 second of 48khz 32bit audio
	public static final int FF_MIN_BUFFER_SIZE = 16384;
	//
	private IntBuffer fin = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();

	protected AVCodecContext(ByteBuffer p) {
		setNative(new AVCodecContextNative(this, p));
	}

	static AVCodecContext create(ByteBuffer p) {
		return new AVCodecContext(p);
	}

    /*
	public static AVCodecContext create() {
		AVCodecContext cc = allocContext();

		if (cc != null) {
			((AVCodecContextNative) cc.n).allocated = true;
		}

		return cc;
	}
	*/
	private boolean opened = false;

	public void open(AVCodec codec) throws AVIOException {
		int res = AVCodecContextNative.open2(n.p, codec.n.p, null);

		if (res < 0) {
			throw new AVIOException(res);
		}
		opened = true;
	}

	@Override
	public int close() {
		if (opened) {
			opened = false;
			return super.close();
		} else {
			return 0;
		}
	}

	@Override
	public void dispose() {
		close();
		super.dispose();
	}

	/**
	 * Returns true if decoding frame complete.
	 *
	 * @param frame
	 * @param packet
	 * @return
	 * @throws AVDecodingError
	 */
	public boolean decodeVideo(AVFrame frame, AVPacket packet) throws AVDecodingError {
		int res;

		res = decodeVideo2(frame, fin, packet);
		if (res < 0) {
			throw new AVDecodingError(-res);
		}

		return (fin.get(0) != 0);
	}

	/**
	 * Encode video, writing result to buf.
	 *
	 * Note that it always writes to the start of the buffer, ignoring the position and limit.
	 *
	 * @param buf
	 * @param pict Picture to encode, use null to flush encoded frames.
	 * @return number of bytes written. When 0 with a null picture, encoding is complete.
	 * @throws au.notzed.jjmpeg.exception.AVEncodingError
	 */
    /*
	public int encodeVideo(ByteBuffer buf, AVFrame pict) throws AVEncodingError {
		int buf_size = buf.capacity();
		int len = encodeVideo2(buf, buf_size, pict != null ? pict : null);

		if (len >= 0) {
			buf.limit(len);
			buf.position(0);
			return len;
		} else {
			throw new AVEncodingError(-len);
		}
	}
	*/

	/**
	 * Decode an audio packet.
	 *
	 * @param samples on output the limit will be set to the number of short samples stored
	 * @param packet
	 * @return number of bytes written to samples
	 * @throws AVDecodingError
	 */
    /*
	public int decodeAudio(AVSamples samples, AVAudioPacket packet) throws AVDecodingError {
		int data = 0;
		ByteBuffer buf = samples.getBuffer();

		buf.limit(buf.capacity());

		ShortBuffer s = (ShortBuffer) samples.getSamples();

		while (data == 0 && packet.getSize() > 0) {
			int res = 0;

			fin.put(0, samples.getBuffer().capacity());
			res = decodeAudio4(s, fin, packet);
			if (res < 0) {
				throw new AVDecodingError(-res);
			}
			data = fin.get(0);
			packet.consume(res);
		}

		samples.getBuffer().position(0);
		samples.getBuffer().limit(data);
		s.position(0);
		s.limit(data / 2);

		return data;
	}
	*/

    /*
	public int encodeAudio(ByteBuffer buf, AVSamples samples) throws AVEncodingError {
		int buf_size = buf.capacity();
		int len = encodeAudio2(buf, buf_size, (ShortBuffer) samples.getSamples());

		assert (len < buf_size);

		if (len >= 0) {
			buf.limit(len);
			buf.position(0);
			return len;
		} else {
			throw new AVEncodingError(-len);
		}
	}
	*/
}

class AVCodecContextNative extends AVCodecContextNativeAbstract {

	boolean allocated = false;

	AVCodecContextNative(AVObject o, ByteBuffer p) {
		super(o, p);
	}

	@Override
	public void dispose() {
		if (p != null) {
			// close?
			if (allocated) {
				_free(p);
			}
		}
		super.dispose();
	}
}
