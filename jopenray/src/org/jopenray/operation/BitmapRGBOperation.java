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

import org.jopenray.util.PacketAnalyser;

public class BitmapRGBOperation extends Operation {
	public BitmapRGBOperation(int x, int y, int width, int height,
			byte[] paddedBitmap) {

		int nbBytesPerRow = PacketAnalyser.round(width * 3, 4);
		int nbBytes = PacketAnalyser.round(nbBytesPerRow * height, 4);

		if (paddedBitmap.length != nbBytes) {
			throw new IllegalArgumentException(
					"Bitmap not correctly padded, current length: "
							+ paddedBitmap.length + " must be "
							+ paddedBitmap.length);
		}
		allocate(12 + paddedBitmap.length);
		setHeader(0xA6, x, y, width, height);

		buffer.addBytes(paddedBitmap);
	}

	/**
	 * Return padded bytes, max width: 480 px
	 */
	public static byte[] getBytes(int[] pixels, int bitmapWidth, int fromX,
			int fromY, int width, int height) {
		// System.out.println("BitmapRGBOperation.getBytes():" + fromX + ","
		// + fromY + " " + width + "x" + height
		// +" (bitmpaWidth:"+bitmapWidth+")");
		int nbBytesPerRow = PacketAnalyser.round(width * 3, 4);
		int nbBytes = PacketAnalyser.round(nbBytesPerRow * height, 4);
		byte[] r = new byte[nbBytes];
		int i = fromX + fromY * bitmapWidth;
		int j = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int c = pixels[i];
				i++;
				int red1 = (c & 0x00ff0000) >> 16;
				int green1 = (c & 0x0000ff00) >> 8;
				int blue1 = c & 0x000000ff;
				r[j] = (byte) blue1;
				j++;
				r[j] = (byte) green1;
				j++;
				r[j] = (byte) red1;
				j++;
			}
			j = (y + 1) * nbBytesPerRow;
			i = fromX + (fromY + y + 1) * bitmapWidth;

		}
		if (nbBytes > 1440) {
			System.err.println("BitmapRGBOperation.getBytes():" + fromX + ","
					+ fromY + " " + width + "x" + height + " out of memory:"
					+ nbBytes);
			System.exit(0);
		}
		return r;
	}

	@Override
	public void dump() {
		System.out.println("BitmapRGBOperation");

	}
}
