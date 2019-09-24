/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
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

package org.jopenray.util;

import java.awt.Color;

public class ByteArrayList {
	private byte[] bytes;
	private int length;
	private int initialCapacity;

	public ByteArrayList(final int initialCapacity) {
		this.initialCapacity = initialCapacity;
		bytes = new byte[initialCapacity];

	}

	public final void addInt8(final int i) {
		// check(1);
		bytes[length] = (byte) i;
		length++;
	}

	public void addColor(Color c0) {
		// bytes[length] = 0 ; <= useless
		length++;
		bytes[length] = (byte) c0.getBlue();
		length++;
		bytes[length] = (byte) c0.getGreen();
		length++;
		bytes[length] = (byte) c0.getRed();
		length++;

	}

	public final void addInt16(final int i) {
		// check(2);
		bytes[length] = (byte) (i >> 8);
		length++;
		bytes[length] = (byte) i;
		length++;

	}

	public final void setInt16(int index, int value) {
		bytes[index] = (byte) (value >> 8);
		bytes[index + 1] = (byte) value;

	}

	public final void addBytes(byte[] byteArray) {
		check(byteArray.length);

		System.arraycopy(byteArray, 0, bytes, length, byteArray.length);
		length += byteArray.length;

	}

	// Check if can add n bytes
	private final void check(int n) {
		if (this.bytes.length <= length + n - 1) {
			byte[] newBytes = new byte[this.bytes.length + n + initialCapacity];
			System.arraycopy(this.bytes, 0, newBytes, 0, this.bytes.length);
			this.bytes = newBytes;
			Thread.dumpStack();
		}
	}

	public final byte[] getInnerByteBuffer() {

		return bytes;
	}

	public byte[] getBytes() {
		byte[] r = new byte[length];
		System.arraycopy(this.bytes, 0, r, 0, length);
		return r;
	}

	public final int getLength() {
		return length;
	}

	public static void setInt16(byte[] buffer, int index, int value) {
		buffer[index] = (byte) (value >> 8);
		buffer[index + 1] = (byte) value;
	}

	public String getInt16(int index) {

		return String.valueOf((bytes[index] & 0xFF) * 256
				+ (bytes[index + 1] & 0xFF));
	}

}
