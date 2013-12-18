package au.notzed.jjmpeg;

/**
 * Base class for java objects for jjmpeg.
 * @author notzed
 */
public class AVObject {
	AVNative n;

	final protected void setNative(AVNative n) {
		this.n = n;
	}

	/**
	 * Dispose of native resources for this object.
	 */
	public void dispose() {
		n.dispose();
	}
}
