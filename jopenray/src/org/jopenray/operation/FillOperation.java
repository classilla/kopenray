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

import java.awt.Color;

public class FillOperation extends Operation {
	public FillOperation(int x, int y, int width, int height, Color color) {
		// System.err.println("Fill Operation: " + x + "," + y + " " + width
		// + "x" + height);

		allocate(16);
		setHeader(0xA2, x, y, width, height);
		buffer.addInt8(0xFF);
		buffer.addInt8(color.getBlue());
		buffer.addInt8(color.getGreen());
		buffer.addInt8(color.getRed());
	}

	@Override
	public void dump() {
		System.out.println("FillOperation");

	}
}
