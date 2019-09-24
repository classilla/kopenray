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

public class CopyOperation extends Operation {
	public CopyOperation(int x, int y, int width, int height, int srcX, int srcY) {
		allocate(16);
		setHeader(0xA4, x, y, width, height);
		buffer.addInt16(srcX);
		buffer.addInt16(srcY);
	}

	@Override
	public void dump() {
		System.out.println("CopyOperation");
	}
}
