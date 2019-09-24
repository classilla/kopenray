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

public class ByteArrayListWithHeader {
	private byte[] bytes;
	private int length;
	private int initialCapacity;

	private int headerSize; // n bytes insérés au debut et a chaque augmentation
							// de taille

	public ByteArrayListWithHeader(int initialCapacity, int headerSize) {
		this.initialCapacity = initialCapacity;
		bytes = new byte[initialCapacity];
		this.headerSize = headerSize;
		addHeader();

	}

	private void addHeader() {
		length += headerSize;

	}

	public void addInt8(int i) {
		check(1);
		bytes[length] = (byte) i;
		length++;
	}

	public void addInt16(int i) {
		check(1);
		bytes[length] = (byte) (i >> 8);
		length++;
		check(1);
		bytes[length] = (byte) i;
		length++;

	}

	public void setInt16(int index, int value) {
		bytes[index] = (byte) (value >> 8);
		bytes[index + 1] = (byte) value;

	}

	public void addBytes(byte[] a) {
		// check(byteArray.length);
		// On ne peut pas faire de array copy..
		// System.arraycopy(byteArray, 0, bytes, length, byteArray.length);
		// length += byteArray.length;

		final int stop = a.length;
		for (int i = 0; i < stop; i++) {
			this.addInt8(a[i]);
		}
	}

	// Check if can add n bytes
	private void check(int n) {
		if (this.bytes.length <= length + n - 1) {
			byte[] newBytes = new byte[this.bytes.length + initialCapacity];
			System.arraycopy(this.bytes, 0, newBytes, 0, this.bytes.length);
			this.bytes = newBytes;

			addHeader();
		}
	}

	public byte[] getInnerByteBuffer() {

		return bytes;
	}

	public byte[] getBytes() {
		byte[] r = new byte[length];
		System.arraycopy(this.bytes, 0, r, 0, length);
		return r;
	}

	public int getLength() {

		return length;
	}

}
