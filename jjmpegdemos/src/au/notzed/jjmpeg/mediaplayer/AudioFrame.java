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

import au.notzed.jjmpeg.AVSamples;
import java.util.Queue;

/**
 * Audio frame that can (possibly) dispose itself
 * @author notzed
 */
public class AudioFrame extends MediaFrame {
	long pts;
	public final AVSamples frame;

	public AudioFrame(Queue<AudioFrame> rq, AVSamples samples) {
		super(rq);
		this.frame = samples;
	}

	public void dispose() {
		frame.dispose();
	}

	@Override
	long getPTS() {
		return pts;
	}
}
