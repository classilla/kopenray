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

public class PadOperation extends Operation {

	public PadOperation() {
		allocate(24);
		setHeader(0xAF, 0, 1, 0xFFFF, 0xFFFF);
		for (int i = 0; i < 12; i++) {
			buffer.addInt8(0xFF);
		}
	}

	final public int getSequenceIncrement() {
		return 0;
	}

	@Override
	public void dump() {
		System.out.println("PadOperation");

	}
}
