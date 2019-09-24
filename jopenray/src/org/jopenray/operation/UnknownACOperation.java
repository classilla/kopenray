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

public class UnknownACOperation extends Operation {

	public UnknownACOperation(int a, int b, int c, int d) {
		allocate(20);
		setHeader(0xAC, 0, 0, 0, 0);
		buffer.addInt16(a);
		buffer.addInt16(b);
		buffer.addInt16(c);
		buffer.addInt16(d);
	}

	public UnknownACOperation() {
		this(0, 1, 0, 4);
	}

	@Override
	public void dump() {
		System.out.println("UnknownACOperation");
	}
}
