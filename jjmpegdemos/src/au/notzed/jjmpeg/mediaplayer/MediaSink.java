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

/**
 * Interface to a player that takes the audio/video frames and synchronises them.
 * @author notzed
 */
public interface MediaSink {

	public void addMediaSinkListener(MediaSinkListener listener);

	public void queueFrame(AudioFrame frame) throws InterruptedException;

	public void queueFrame(VideoFrame frame) throws InterruptedException;

	public void postSeek(long stampms);

	public void pause();

	public void unpause();
}
