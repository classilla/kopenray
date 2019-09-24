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

public class BitArray {
	private byte bits[];
	private int length;

	/**
	 * Bit array
	 * 
	 * @param number
	 *            of bits
	 * */
	public BitArray(int size) {

		bits = new byte[(size - 1) / 8 + 1];
		length = size;
		// System.out.println(this);
		// clear();
	}

	public void clear() {
		for (int i = 0; i < bits.length; i++) {
			bits[i] = 0;
		}
	}

	public void set(int index) {
		bits[index / 8] |= (byte) (1 << (7 - (index % 8)));
	}

	public int length() {
		return length;
	}

	public boolean get(int index) {
		if (index >= length) {
			throw new IndexOutOfBoundsException();
		}
		byte pattern = (byte) (1 << (7 - (index % 8)));
		return (bits[index / 8] & pattern) == pattern ? true : false;
	}

	public String toString() {
		final StringBuffer str = new StringBuffer(length);
		for (int i = 0; i < length; i++) {
			str.append(get(i) ? "1" : "0");
		}
		return str.toString();
	}

	public byte[] toByteArray() {
		return bits;
	}

}
