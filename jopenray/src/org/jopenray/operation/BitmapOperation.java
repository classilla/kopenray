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

import org.jopenray.util.BitArray;
import org.jopenray.util.PacketAnalyser;

public class BitmapOperation extends Operation {
	public BitmapOperation(int x, int y, int width, int height, Color c0,
			Color c1, BitArray b) {

		// System.err.println("Bitmap Operation: " + x + "," + y + " " + width
		// + "x" + height);

		if (b.length() % 32 != 0) {
			throw new IllegalArgumentException(
					"BitArray not padded (32 bits), length: " + b.length()
							+ " bits");
		}
		allocate(20 + b.length() / 8);
		setHeader(0xA5, x, y, width, height);

		buffer.addColor(c0);
		buffer.addColor(c1);

		buffer.addBytes(b.toByteArray());
	}

	public static BitArray getBytes(int[] pixels, int bitmapWidth, int fromX,
			int fromY, int width, int height, int color1) {

		final int nbBytesPerRow = 1 + (width - 1) / 8;

		final int nbBits = PacketAnalyser.round(nbBytesPerRow * 8 * height, 32);

		final BitArray b = new BitArray(nbBits);

		int i = fromX + fromY * bitmapWidth;
		int j = 0;

		for (int y = 0; y < height; y++) {

			for (int x = 0; x < width; x++) {

				int c = pixels[i];
				i++;

				if (c == color1) {
					b.set(j);
				}
				j++;
			}
			j = (y + 1) * nbBytesPerRow * 8;
			i = fromX + (fromY + y + 1) * bitmapWidth;
		}

		return b;
	}

	@Override
	public void dump() {
		System.out.println("BitmapOperation:" + getHeader());

	}

}
