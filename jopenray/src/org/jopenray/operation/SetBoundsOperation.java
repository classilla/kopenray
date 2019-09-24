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

public class SetBoundsOperation extends Operation {
	public SetBoundsOperation(int x, int y, int width, int height) {
		allocate(20);
		setHeader(0xA8, x, y, width, height);
		buffer.addInt16(x);
		buffer.addInt16(y);
		buffer.addInt16(width);
		buffer.addInt16(height);
	}

	@Override
	public void dump() {
		System.out.println("SetBoundsOperation");
	}
}
