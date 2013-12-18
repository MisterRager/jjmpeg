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

import java.util.Queue;

/**
 * Base class for decoded frames.
 * 
 * Frames are re-cycled using a return queue.
 * @author notzed
 */
public abstract class MediaFrame implements Comparable<MediaFrame> {

	private Queue returnQ;

	public MediaFrame(Queue returnQ) {
		this.returnQ = returnQ;
	}

	abstract long getPTS();
	abstract void dispose();
	
	void recycle() {
		returnQ.add(this);
	}
	public int compareTo(MediaFrame o) {
		return (int)(getPTS() - o.getPTS());
	}
}
