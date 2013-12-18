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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents a single raw plane
 * @author notzed
 */
public class AVPlane {

	public final ByteBuffer data;
	public final int lineSize;
	public final int width;
	public final int height;

	public AVPlane(ByteBuffer data, int lineSize, int width, int height) {
		data.order(ByteOrder.nativeOrder());
		this.data = data;
		this.lineSize = lineSize;
		this.width = width;
		this.height = height;
	}
}
