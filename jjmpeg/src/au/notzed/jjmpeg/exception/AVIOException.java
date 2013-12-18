/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.notzed.jjmpeg.exception;

import java.io.IOException;

/**
 *
 * @author notzed
 */
public class AVIOException extends IOException {

	int errno;

	public AVIOException(int errno) {
		super("Error: " + errno);
		this.errno = errno;
	}

	public AVIOException(String what) {
		super(what);
	}

	public AVIOException(int errno, String what) {
		super(what + ", error=" + errno);
		this.errno = errno;
	}
}
