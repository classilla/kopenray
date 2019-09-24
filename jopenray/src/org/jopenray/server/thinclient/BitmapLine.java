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

package org.jopenray.server.thinclient;

public class BitmapLine {
	private static final int NO_COLOR = 888;
	private final int[] pixels;
	private final int width;
	private final int line;

	private int col0, col1 = NO_COLOR;
	private int type = TYPE_MONOCOLOR;
	private long h;
	public static int TYPE_MONOCOLOR = 0;
	public static int TYPE_BICOLOR = 1;
	public static int TYPE_FULLCOLOR = 2;

	public BitmapLine(final int[] pixels, final int width, final int line) {
		this.pixels = pixels;
		this.width = width;
		this.line = line;

		int start = width * line;
		int stop = start + width;
		col0 = pixels[start];
		if (width == 1) {
			h = col0;
			return;
		}
		for (int i = start; i < stop; i++) {
			int p = pixels[i];

			h += p;
			if (p != col0) {
				if (col1 == NO_COLOR) {
					col1 = p;
					type = TYPE_BICOLOR;
				}

				if (p != col1) {
					type = TYPE_FULLCOLOR;
					break;
				}
			}
		}

	}

	public final int getType() {
		return type;
	}

	public final int getColor0() {
		return col0;
	}

	public final int getColor1() {
		return col1;
	}

	public final boolean canBeMergedWith(final BitmapLine line) {
		if (this.getType() != line.getType()) {
			return false;
		}
		if (type == TYPE_MONOCOLOR) {
			return (col0 == line.col0);
		}
		if (type == TYPE_BICOLOR) {
			return (col0 == line.col0) && (col1 == line.col1);
		}

		return true;
	}

	public final boolean isIdentical(BitmapLine obj) {
		// Fast check
		if (h != obj.h) {
			return false;
		}
		// Byte comparison
		final int start1 = width * line;
		final int start2 = width * obj.line;
		for (int i = 0; i < width; i++) {
			if (pixels[i + start1] != pixels[i + start2]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public final String toString() {
		String s = "BitmapLine " + line + "[" + h + "]";
		if (type == TYPE_MONOCOLOR) {
			s += " MONOCOLOR: " + col0;
		} else if (type == TYPE_BICOLOR) {
			s += " BICOLOR: " + col0 + " / " + col1;
		} else {
			s += " FULLCOLOR";
		}
		return s;
	}

	public int getWidth() {
		return this.width;
	}
}
