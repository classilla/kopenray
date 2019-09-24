/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2019 Cameron Kaiser
 *  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jopenray.operation;

import java.awt.Color;
import org.jopenray.util.Hex;

public class SetMouseCursorOperation extends Operation {
	public static int INVISIBLE_CURSOR = 0;
	public static int UNIX_CURSOR = 1;

	static final byte[] unixCursorBitmap = new byte[] { 0x00, 0x00, 0x70, 0x0E,
			0x78, 0x1E, 0x7C, 0x3E, 0x3E, 0x7C, 0x1F, (byte) 0xF8, 0x0F,
			(byte) 0xF0, 0x07, (byte) 0xE0, 0x07, (byte) 0xE0, 0x0F,
			(byte) 0xF0, 0x1F, (byte) 0xF8, 0x3E, 0x7C, 0x7C, 0x3E, 0x78, 0x1E,
			0x70, 0x0E, 0x00, 0x00 };
	static final byte[] unixCursorBitmapMask = new byte[] { (byte) 0xF0, 0x0F,
			(byte) 0xF8, 0x1F, (byte) 0xFC, 0x3F, (byte) 0xFE, 0x7F, 0x7F,
			(byte) 0xFE, 0x3F, (byte) 0xFC, (byte) 0x1F, (byte) 0xF8, 0x0F,
			(byte) 0xF0, 0x0F, (byte) 0xF0, 0x1F, (byte) 0xF8, 0x3F,
			(byte) 0xFC, 0x7F, (byte) 0xFE, (byte) 0xFE, 0x7F, (byte) 0xFC,
			0x3F, (byte) 0xF8, 0x1F, (byte) 0xF0, 0x0F };

	public SetMouseCursorOperation(int type) {
		if (type == UNIX_CURSOR) {
			init(7, 7, 16, 16, Color.BLACK, Color.ORANGE, unixCursorBitmap,
					unixCursorBitmapMask);
		} else {
			init(1, 1, 16, 16, Color.BLACK, Color.WHITE, new byte[32],
					new byte[32]);
		}
	}

	public SetMouseCursorOperation(int offsetX, int offsetY, int width,
			int height, Color c0, Color c1, byte[] bitmap, byte[] mask) {
		init(offsetX, offsetY, width, height, c0, c1, bitmap, mask);

	}

	private void init(int offsetX, int offsetY, int width, int height,
			Color c0, Color c1, byte[] bitmap, byte[] mask) {
		final int expectedLength = (height * width) / 8;
		if (bitmap.length != expectedLength) {

			throw new IllegalArgumentException("Bad bitmap length:"
					+ bitmap.length + " must be " + expectedLength + "("
					+ width + "x" + height + ")");
		}
		if (mask.length != expectedLength) {
			Hex encoder = new Hex();
			System.err.println(encoder.encode(bitmap));
			System.err.println(encoder.encode(mask));
			throw new IllegalArgumentException("Bad mask length:" + mask.length
					+ " must be " + expectedLength + "(" + width + "x" + height
					+ ")");
		}

		allocate(12 + 8 + bitmap.length + mask.length);
		setHeader(0xA9, offsetX, offsetY, width, height);

		buffer.addColor(c0);
		buffer.addColor(c1);
		buffer.addBytes(bitmap);
		buffer.addBytes(mask);
	}

	@Override
	public void dump() {
		System.out.println("SetMouseCursorOperation");

	}
}
