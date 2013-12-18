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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple helper class which adds a safe 'cancel' operation for a thread.
 * 
 * Implementing classes must poll this.cancelled and exit run() when it
 * is true.
 * @author notzed
 */
public class CancellableThread extends Thread {

	protected boolean cancelled = false;

	public CancellableThread(String name) {
		super(name);
	}

	/**
	 * Cancel this thread, it will wait until
	 * the thread has exited.
	 */
	public void cancel() {
		if (isAlive() && !cancelled) {
			try {
				cancelled = true;
				interrupt();
				join();
			} catch (InterruptedException ex) {
				Logger.getLogger(CancellableThread.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
