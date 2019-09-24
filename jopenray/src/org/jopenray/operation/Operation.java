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

package org.jopenray.operation;

import org.jopenray.util.ByteArrayList;

public abstract class Operation {
	protected ByteArrayList buffer;
	private int sequence;

	protected void allocate(int size) {
		buffer = new ByteArrayList(size);
	}

	/*
	 * header (12 bytes)
	 */
	protected void setHeader(int code, int x, int y, int width, int height) {
		buffer.addInt8(code);
		buffer.addInt8(0x00);

		buffer.addInt16(0); // operation seq

		buffer.addInt16(x);
		buffer.addInt16(y);

		buffer.addInt16(width);
		buffer.addInt16(height);

	}

	public String getHeader() {

		return buffer.getInt16(4) + "," + buffer.getInt16(6) + " "
				+ buffer.getInt16(8) + "x" + buffer.getInt16(10);
	}

	public int getSequenceIncrement() {
		return 1;
	}

	public void setSequence(int seq) {
		buffer.setInt16(2, seq);
		sequence = seq;
	}

	public int getSequence() {
		return sequence;
	}

	public int getLength() {
		return buffer.getLength();
	}

	/**
	 * Copy the the buffer at the destination offset of the provided array
	 * 
	 * @returns the number of copied bytes
	 * */
	public int copyStart(byte[] to, int atOffset) {
		int availableSpace = to.length - atOffset;
		int n = Math.min(this.getLength(), availableSpace);
		System.arraycopy(buffer.getInnerByteBuffer(), 0, to, atOffset, n);
		return n;
	}

	/**
	 * Copy the n last bytes of the buffer at the start of the provided array
	 * 
	 * @param atOffset
	 *            dest
	 * 
	 * */
	public void copyEnd(byte[] to, int n, int atOffset) {
		if (n > to.length) {
			System.err.println(this + " Bad length: n:" + n + " > capacity:"
					+ to.length);
		}

		System.arraycopy(buffer.getInnerByteBuffer(), this.getLength() - n, to,
				atOffset, n);
	}

	abstract public void dump();
}
