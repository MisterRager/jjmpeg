/* I am automatically generated.  Editing me would be pointless,
   but I wont stop you if you so desire. */

package au.notzed.jjmpeg;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.IntBuffer;
import java.nio.DoubleBuffer;

abstract class AVPacketListNativeAbstract extends AVNative {
	protected AVPacketListNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native ByteBuffer getPkt(ByteBuffer p);
	static native void setPkt(ByteBuffer p, ByteBuffer val);
	static native ByteBuffer getNext(ByteBuffer p);
	static native void setNext(ByteBuffer p, ByteBuffer val);
	// Native Methods
}

abstract class AVPacketListAbstract extends AVObject {
	// Fields
	public  AVPacket getPkt() {
		return AVPacket.create(AVPacketListNativeAbstract.getPkt(n.p));
	}
	public  void setPkt(AVPacket val) {
		AVPacketListNativeAbstract.setPkt(n.p, val != null ? val.n.p : null);
	}
	public  AVPacketList getNext() {
		return AVPacketList.create(AVPacketListNativeAbstract.getNext(n.p));
	}
	public  void setNext(AVPacketList val) {
		AVPacketListNativeAbstract.setNext(n.p, val != null ? val.n.p : null);
	}
	// Public Methods
}
abstract class AVPacketNativeAbstract extends AVNative {
	protected AVPacketNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native long getPTS(ByteBuffer p);
	static native void setPTS(ByteBuffer p, long val);
	static native long getDTS(ByteBuffer p);
	static native void setDTS(ByteBuffer p, long val);
	static native int getSize(ByteBuffer p);
	static native int getStreamIndex(ByteBuffer p);
	static native void setStreamIndex(ByteBuffer p, int val);
	static native long getPos(ByteBuffer p);
	static native int getFlags(ByteBuffer p);
	static native void setFlags(ByteBuffer p, int val);
	// Native Methods
	static native void free_packet(ByteBuffer p);
	static native void init_packet(ByteBuffer p);
}

abstract class AVPacketAbstract extends AVObject {
	// Fields
	public  long getPTS() {
		return AVPacketNativeAbstract.getPTS(n.p);
	}
	public  void setPTS(long val) {
		AVPacketNativeAbstract.setPTS(n.p, val);
	}
	public  long getDTS() {
		return AVPacketNativeAbstract.getDTS(n.p);
	}
	public  void setDTS(long val) {
		AVPacketNativeAbstract.setDTS(n.p, val);
	}
	public  int getSize() {
		return AVPacketNativeAbstract.getSize(n.p);
	}
	public  int getStreamIndex() {
		return AVPacketNativeAbstract.getStreamIndex(n.p);
	}
	public  void setStreamIndex(int val) {
		AVPacketNativeAbstract.setStreamIndex(n.p, val);
	}
	public  long getPos() {
		return AVPacketNativeAbstract.getPos(n.p);
	}
	public  int getFlags() {
		return AVPacketNativeAbstract.getFlags(n.p);
	}
	public  void setFlags(int val) {
		AVPacketNativeAbstract.setFlags(n.p, val);
	}
	// Public Methods
	public void freePacket() {
		AVPacketNativeAbstract.free_packet(n.p);
	}
	public void initPacket() {
		AVPacketNativeAbstract.init_packet(n.p);
	}
}
abstract class AVFormatContextNativeAbstract extends AVNative {
	protected AVFormatContextNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getNbStreams(ByteBuffer p);
	static native ByteBuffer getStreamAt(ByteBuffer p, int index);
	static native ByteBuffer getIOContext(ByteBuffer p);
	static native void setIOContext(ByteBuffer p, ByteBuffer val);
	static native String getFilename(ByteBuffer p);
	// Native Methods
	static native void register_all();
	static native int read_frame(ByteBuffer p, ByteBuffer pkt);
	static native void open_input(ByteBuffer p);
	static native void close_input(ByteBuffer p);
	static native void free_context(ByteBuffer p);
	static native ByteBuffer alloc_context();
}

abstract class AVFormatContextAbstract extends AVObject {
	// Fields
	public  int getNbStreams() {
		return AVFormatContextNativeAbstract.getNbStreams(n.p);
	}
	public  AVStream getStreamAt(int index) {
		return AVStream.create(AVFormatContextNativeAbstract.getStreamAt(n.p, index));
	}
	public  AVIOContext getIOContext() {
		return AVIOContext.create(AVFormatContextNativeAbstract.getIOContext(n.p));
	}
	public  void setIOContext(AVIOContext val) {
		AVFormatContextNativeAbstract.setIOContext(n.p, val != null ? val.n.p : null);
	}
	public  String getFilename() {
		return AVFormatContextNativeAbstract.getFilename(n.p);
	}
	// Public Methods
	static public void registerAll() {
		AVFormatContextNativeAbstract.register_all();
	}
	public int readFrame(AVPacket pkt) {
		return AVFormatContextNativeAbstract.read_frame(n.p, pkt != null ? pkt.n.p : null);
	}
	public void openInput() {
		AVFormatContextNativeAbstract.open_input(n.p);
	}
	public void closeInput() {
		AVFormatContextNativeAbstract.close_input(n.p);
	}
	protected void freeContext() {
		AVFormatContextNativeAbstract.free_context(n.p);
	}
	static public AVFormatContext allocContext() {
		return AVFormatContext.create(AVFormatContextNativeAbstract.alloc_context());
	}
}
abstract class AVDictionaryNativeAbstract extends AVNative {
	protected AVDictionaryNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getCount(ByteBuffer p);
	static native ByteBuffer getElemsAt(ByteBuffer p, int index);
	// Native Methods
}

abstract class AVDictionaryAbstract extends AVObject {
	// Fields
	public  int getCount() {
		return AVDictionaryNativeAbstract.getCount(n.p);
	}
	public  AVDictionaryEntry getElemsAt(int index) {
		return AVDictionaryEntry.create(AVDictionaryNativeAbstract.getElemsAt(n.p, index));
	}
	// Public Methods
}
abstract class AVDictionaryEntryNativeAbstract extends AVNative {
	protected AVDictionaryEntryNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native String getKey(ByteBuffer p);
	static native String getValue(ByteBuffer p);
	// Native Methods
}

abstract class AVDictionaryEntryAbstract extends AVObject {
	// Fields
	public  String getKey() {
		return AVDictionaryEntryNativeAbstract.getKey(n.p);
	}
	public  String getValue() {
		return AVDictionaryEntryNativeAbstract.getValue(n.p);
	}
	// Public Methods
}
abstract class AVStreamNativeAbstract extends AVNative {
	protected AVStreamNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native ByteBuffer getCodec(ByteBuffer p);
	static native ByteBuffer gettimeBase(ByteBuffer p);
	// Native Methods
}

abstract class AVStreamAbstract extends AVObject {
	// Fields
	public  AVCodecContext getCodec() {
		return AVCodecContext.create(AVStreamNativeAbstract.getCodec(n.p));
	}
	public  AVRational gettimeBase() {
		return AVRational.create(AVStreamNativeAbstract.gettimeBase(n.p));
	}
	// Public Methods
}
abstract class AVRationalNativeAbstract extends AVNative {
	protected AVRationalNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getnumerator(ByteBuffer p);
	static native int getdenominator(ByteBuffer p);
	// Native Methods
}

abstract class AVRationalAbstract extends AVObject {
	// Fields
	public  int getnumerator() {
		return AVRationalNativeAbstract.getnumerator(n.p);
	}
	public  int getdenominator() {
		return AVRationalNativeAbstract.getdenominator(n.p);
	}
	// Public Methods
}
abstract class AVCodecContextNativeAbstract extends AVNative {
	protected AVCodecContextNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getWidth(ByteBuffer p);
	static native int getHeight(ByteBuffer p);
	static native int getpixelFormat(ByteBuffer p);
	static native void setpixelFormat(ByteBuffer p, int val);
	static native int getcodecType(ByteBuffer p);
	static native void setcodecType(ByteBuffer p, int val);
	static native int getChannels(ByteBuffer p);
	static native int getsampleRate(ByteBuffer p);
	static native int getsampleFormat(ByteBuffer p);
	static native void setsampleFormat(ByteBuffer p, int val);
	static native ByteBuffer getsampleAspectRatio(ByteBuffer p);
	// Native Methods
	static native ByteBuffer alloc_context3(ByteBuffer codec);
	static native void init();
	static native int close(ByteBuffer p);
	static native int open2(ByteBuffer p, ByteBuffer codec, ByteBuffer options);
	static native int encode_audio2(ByteBuffer p, ByteBuffer avpkt, ByteBuffer frame, IntBuffer got_packet_ptr);
	static native int encode_video2(ByteBuffer p, ByteBuffer avpkt, ByteBuffer frame, IntBuffer got_packet_ptr);
	static native int decode_video2(ByteBuffer p, ByteBuffer picture, IntBuffer got_picture_ptr, ByteBuffer avpkt);
	static native int decode_audio4(ByteBuffer p, ByteBuffer frame, IntBuffer frame_frame_ptr, ByteBuffer avpkt);
}

abstract class AVCodecContextAbstract extends AVObject {
	// Fields
	public  int getWidth() {
		return AVCodecContextNativeAbstract.getWidth(n.p);
	}
	public  int getHeight() {
		return AVCodecContextNativeAbstract.getHeight(n.p);
	}
	public  PixelFormat getpixelFormat() {
		return PixelFormat.values()[AVCodecContextNativeAbstract.getpixelFormat(n.p)+1];
	}
	public  void setpixelFormat(PixelFormat val) {
		AVCodecContextNativeAbstract.setpixelFormat(n.p, val.toC());
	}
	public  AVMediaType getcodecType() {
		return AVMediaType.values()[AVCodecContextNativeAbstract.getcodecType(n.p)+1];
	}
	public  void setcodecType(AVMediaType val) {
		AVCodecContextNativeAbstract.setcodecType(n.p, val.toC());
	}
	public  int getChannels() {
		return AVCodecContextNativeAbstract.getChannels(n.p);
	}
	public  int getsampleRate() {
		return AVCodecContextNativeAbstract.getsampleRate(n.p);
	}
	public  SampleFormat getsampleFormat() {
		return SampleFormat.values()[AVCodecContextNativeAbstract.getsampleFormat(n.p)+1];
	}
	public  void setsampleFormat(SampleFormat val) {
		AVCodecContextNativeAbstract.setsampleFormat(n.p, val.toC());
	}
	public  AVRational getsampleAspectRatio() {
		return AVRational.create(AVCodecContextNativeAbstract.getsampleAspectRatio(n.p));
	}
	// Public Methods
	static protected AVCodecContext allocContext3(AVCodec codec) {
		return AVCodecContext.create(AVCodecContextNativeAbstract.alloc_context3(codec != null ? codec.n.p : null));
	}
	static public void init() {
		AVCodecContextNativeAbstract.init();
	}
	public int close() {
		return AVCodecContextNativeAbstract.close(n.p);
	}
	public int open2(AVCodec codec,AVDictionary options) {
		return AVCodecContextNativeAbstract.open2(n.p, codec != null ? codec.n.p : null, options != null ? options.n.p : null);
	}
	 int encodeAudio2(AVPacket avpkt, AVFrame frame, IntBuffer got_packet_ptr) {
		return AVCodecContextNativeAbstract.encode_audio2(n.p, avpkt != null ? avpkt.n.p : null, frame != null ? frame.n.p : null, got_packet_ptr);
	}
	 int encodeVideo2(AVPacket avpkt, AVFrame frame, IntBuffer got_packet_ptr) {
		return AVCodecContextNativeAbstract.encode_video2(n.p, avpkt != null ? avpkt.n.p : null, frame != null ? frame.n.p : null, got_packet_ptr);
	}
	 int decodeVideo2(AVFrame picture, IntBuffer got_picture_ptr, AVPacket avpkt) {
		return AVCodecContextNativeAbstract.decode_video2(n.p, picture != null ? picture.n.p : null, got_picture_ptr, avpkt != null ? avpkt.n.p : null);
	}
	 int decodeAudio4(AVFrame frame, IntBuffer frame_frame_ptr, AVPacket avpkt) {
		return AVCodecContextNativeAbstract.decode_audio4(n.p, frame != null ? frame.n.p : null, frame_frame_ptr, avpkt != null ? avpkt.n.p : null);
	}
}
abstract class AVCodecNativeAbstract extends AVNative {
	protected AVCodecNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native String getName(ByteBuffer p);
	static native int gettype(ByteBuffer p);
	static native void settype(ByteBuffer p, int val);
	static native int getPrivDataSize(ByteBuffer p);
	static native void setPrivDataSize(ByteBuffer p, int val);
	static native ByteBuffer getNext(ByteBuffer p);
	// Native Methods
	static native ByteBuffer find_encoder(int id);
	static native ByteBuffer find_decoder(int id);
	static native ByteBuffer find_encoder_by_name(String name);
}

abstract class AVCodecAbstract extends AVObject {
	// Fields
	public  String getName() {
		return AVCodecNativeAbstract.getName(n.p);
	}
	public  AVMediaType gettype() {
		return AVMediaType.values()[AVCodecNativeAbstract.gettype(n.p)+1];
	}
	public  void settype(AVMediaType val) {
		AVCodecNativeAbstract.settype(n.p, val.toC());
	}
	public  int getPrivDataSize() {
		return AVCodecNativeAbstract.getPrivDataSize(n.p);
	}
	public  void setPrivDataSize(int val) {
		AVCodecNativeAbstract.setPrivDataSize(n.p, val);
	}
	public  AVCodec getNext() {
		return AVCodec.create(AVCodecNativeAbstract.getNext(n.p));
	}
	// Public Methods
	static public AVCodec findEncoder(int id) {
		return AVCodec.create(AVCodecNativeAbstract.find_encoder(id));
	}
	static public AVCodec findDecoder(int id) {
		return AVCodec.create(AVCodecNativeAbstract.find_decoder(id));
	}
	static public AVCodec findEncoderByName(String name) {
		return AVCodec.create(AVCodecNativeAbstract.find_encoder_by_name(name));
	}
}
abstract class AVFrameNativeAbstract extends AVNative {
	protected AVFrameNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getlineSizeAt(ByteBuffer p, int index);
	static native int getisKeyFrame(ByteBuffer p);
	static native int getnumSamples(ByteBuffer p);
	static native ByteBuffer getMetadata(ByteBuffer p);
	static native int getRepeatPict(ByteBuffer p);
	// Native Methods
	static native ByteBuffer frame_alloc();
}

abstract class AVFrameAbstract extends AVObject {
	// Fields
	public  int getlineSizeAt(int index) {
		return AVFrameNativeAbstract.getlineSizeAt(n.p, index);
	}
	public  int getisKeyFrame() {
		return AVFrameNativeAbstract.getisKeyFrame(n.p);
	}
	public  int getnumSamples() {
		return AVFrameNativeAbstract.getnumSamples(n.p);
	}
	public  AVDictionary getMetadata() {
		return AVDictionary.create(AVFrameNativeAbstract.getMetadata(n.p));
	}
	public  int getRepeatPict() {
		return AVFrameNativeAbstract.getRepeatPict(n.p);
	}
	// Public Methods
	static public AVFrame frameAlloc() {
		return AVFrame.create(AVFrameNativeAbstract.frame_alloc());
	}
}
abstract class AVIOContextNativeAbstract extends AVNative {
	protected AVIOContextNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	// Native Methods
}

abstract class AVIOContextAbstract extends AVObject {
	// Fields
	// Public Methods
}
abstract class AVPictureNativeAbstract extends AVNative {
	protected AVPictureNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getlineSizeAt(ByteBuffer p, int index);
	// Native Methods
}

abstract class AVPictureAbstract extends AVObject {
	// Fields
	public  int getlineSizeAt(int index) {
		return AVPictureNativeAbstract.getlineSizeAt(n.p, index);
	}
	// Public Methods
}
abstract class AVInputFormatNativeAbstract extends AVNative {
	protected AVInputFormatNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native String getName(ByteBuffer p);
	static native String getLongName(ByteBuffer p);
	// Native Methods
	static native ByteBuffer find_input_format(String short_name);
}

abstract class AVInputFormatAbstract extends AVObject {
	// Fields
	public  String getName() {
		return AVInputFormatNativeAbstract.getName(n.p);
	}
	public  String getLongName() {
		return AVInputFormatNativeAbstract.getLongName(n.p);
	}
	// Public Methods
	static public AVInputFormat findInputFormat(String short_name) {
		return AVInputFormat.create(AVInputFormatNativeAbstract.find_input_format(short_name));
	}
}
abstract class AVOutputFormatNativeAbstract extends AVNative {
	protected AVOutputFormatNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native String getName(ByteBuffer p);
	static native String getLongName(ByteBuffer p);
	static native String getMimeType(ByteBuffer p);
	static native String getExtensions(ByteBuffer p);
	static native int getVideoCodec(ByteBuffer p);
	static native int getAudioCodec(ByteBuffer p);
	static native int getSubtitleCodec(ByteBuffer p);
	static native int getFlags(ByteBuffer p);
	static native void setFlags(ByteBuffer p, int val);
	// Native Methods
	static native ByteBuffer guess_format(String short_name, String filename, String mime_type);
}

abstract class AVOutputFormatAbstract extends AVObject {
	// Fields
	public  String getName() {
		return AVOutputFormatNativeAbstract.getName(n.p);
	}
	public  String getLongName() {
		return AVOutputFormatNativeAbstract.getLongName(n.p);
	}
	public  String getMimeType() {
		return AVOutputFormatNativeAbstract.getMimeType(n.p);
	}
	public  String getExtensions() {
		return AVOutputFormatNativeAbstract.getExtensions(n.p);
	}
	public  int getVideoCodec() {
		return AVOutputFormatNativeAbstract.getVideoCodec(n.p);
	}
	public  int getAudioCodec() {
		return AVOutputFormatNativeAbstract.getAudioCodec(n.p);
	}
	public  int getSubtitleCodec() {
		return AVOutputFormatNativeAbstract.getSubtitleCodec(n.p);
	}
	public  int getFlags() {
		return AVOutputFormatNativeAbstract.getFlags(n.p);
	}
	public  void setFlags(int val) {
		AVOutputFormatNativeAbstract.setFlags(n.p, val);
	}
	// Public Methods
	static public AVOutputFormat guessFormat(String short_name, String filename, String mime_type) {
		return AVOutputFormat.create(AVOutputFormatNativeAbstract.guess_format(short_name, filename, mime_type));
	}
}
abstract class AVFormatParametersNativeAbstract extends AVNative {
	protected AVFormatParametersNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getChannel(ByteBuffer p);
	static native void setChannel(ByteBuffer p, int val);
	// Native Methods
}

abstract class AVFormatParametersAbstract extends AVObject {
	// Fields
	public  int getChannel() {
		return AVFormatParametersNativeAbstract.getChannel(n.p);
	}
	public  void setChannel(int val) {
		AVFormatParametersNativeAbstract.setChannel(n.p, val);
	}
	// Public Methods
}
abstract class SwsContextNativeAbstract extends AVNative {
	protected SwsContextNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	// Native Methods
	static native ByteBuffer getContext(int srcW, int srcH, int srcFormat, int dstW, int dstH, int dstFormat, int flags, ByteBuffer srcFilter, ByteBuffer dstFilter, DoubleBuffer param);
	static native void freeContext(ByteBuffer p);
}

abstract class SwsContextAbstract extends AVObject {
	// Fields
	// Public Methods
	public void freeContext() {
		SwsContextNativeAbstract.freeContext(n.p);
	}
}
abstract class SwsFilterNativeAbstract extends AVNative {
	protected SwsFilterNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	// Native Methods
}

abstract class SwsFilterAbstract extends AVObject {
	// Fields
	// Public Methods
}
abstract class SwrContextNativeAbstract extends AVNative {
	protected SwrContextNativeAbstract(AVObject o, ByteBuffer p) {
		super(o, p);
	}
	// Fields
	static native int getinputSampleFormat(ByteBuffer p);
	static native void setinputSampleFormat(ByteBuffer p, int val);
	static native int getinternalSampleFormat(ByteBuffer p);
	static native void setinternalSampleFormat(ByteBuffer p, int val);
	static native int getoutputSampleFormat(ByteBuffer p);
	static native void setoutputSampleFormat(ByteBuffer p, int val);
	static native long getinputChannelLayout(ByteBuffer p);
	static native void setinputChannelLayout(ByteBuffer p, long val);
	static native long getoutputChannelLayout(ByteBuffer p);
	static native void setoutputChannelLayout(ByteBuffer p, long val);
	static native int getinputSampleRate(ByteBuffer p);
	static native void setinputSampleRate(ByteBuffer p, int val);
	static native int getoutputSampleRate(ByteBuffer p);
	static native void setoutputSampleRate(ByteBuffer p, int val);
	static native int getFlags(ByteBuffer p);
	static native void setFlags(ByteBuffer p, int val);
	// Native Methods
}

abstract class SwrContextAbstract extends AVObject {
	// Fields
	public  SampleFormat getinputSampleFormat() {
		return SampleFormat.values()[SwrContextNativeAbstract.getinputSampleFormat(n.p)+1];
	}
	public  void setinputSampleFormat(SampleFormat val) {
		SwrContextNativeAbstract.setinputSampleFormat(n.p, val.toC());
	}
	public  SampleFormat getinternalSampleFormat() {
		return SampleFormat.values()[SwrContextNativeAbstract.getinternalSampleFormat(n.p)+1];
	}
	public  void setinternalSampleFormat(SampleFormat val) {
		SwrContextNativeAbstract.setinternalSampleFormat(n.p, val.toC());
	}
	public  SampleFormat getoutputSampleFormat() {
		return SampleFormat.values()[SwrContextNativeAbstract.getoutputSampleFormat(n.p)+1];
	}
	public  void setoutputSampleFormat(SampleFormat val) {
		SwrContextNativeAbstract.setoutputSampleFormat(n.p, val.toC());
	}
	public  long getinputChannelLayout() {
		return SwrContextNativeAbstract.getinputChannelLayout(n.p);
	}
	public  void setinputChannelLayout(long val) {
		SwrContextNativeAbstract.setinputChannelLayout(n.p, val);
	}
	public  long getoutputChannelLayout() {
		return SwrContextNativeAbstract.getoutputChannelLayout(n.p);
	}
	public  void setoutputChannelLayout(long val) {
		SwrContextNativeAbstract.setoutputChannelLayout(n.p, val);
	}
	public  int getinputSampleRate() {
		return SwrContextNativeAbstract.getinputSampleRate(n.p);
	}
	public  void setinputSampleRate(int val) {
		SwrContextNativeAbstract.setinputSampleRate(n.p, val);
	}
	public  int getoutputSampleRate() {
		return SwrContextNativeAbstract.getoutputSampleRate(n.p);
	}
	public  void setoutputSampleRate(int val) {
		SwrContextNativeAbstract.setoutputSampleRate(n.p, val);
	}
	public  int getFlags() {
		return SwrContextNativeAbstract.getFlags(n.p);
	}
	public  void setFlags(int val) {
		SwrContextNativeAbstract.setFlags(n.p, val);
	}
	// Public Methods
}
