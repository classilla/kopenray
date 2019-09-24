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

public class Util {

	/*
	 * Split a range Ex: 100 split with 30 -> [30,30,30,10]
	 */
	public static int[] split(int n, int split) {
		int[] r;

		int reste = n % split;
		int nbFull = n / split;
		if (reste > 0) {
			r = new int[nbFull + 1];
			r[nbFull] = reste;

		} else {
			r = new int[nbFull];
		}
		for (int i = 0; i < nbFull; i++) {
			r[i] = split;
		}

		return r;

	}

	public static String arrayToString(int[] a) {
		String r = "[ ";
		for (int i = 0; i < a.length; i++) {
			r += a[i] + " ";
		}
		r += "]";
		return r;
	}

	public static void main(String[] args) {
		System.out.println(arrayToString(split(100, 30)));

		System.out.println(arrayToString(split(129, 30)));
		System.out.println(arrayToString(split(30, 30)));
		System.out.println(arrayToString(split(0, 30)));
		System.out.println(arrayToString(split(10, 30)));
		System.out.println(arrayToString(split(89, 30)));
		System.out.println(arrayToString(split(90, 30)));
		System.out.println(arrayToString(split(91, 30)));
	}

	public static void setBit(byte[] data, int index, boolean value) {

		final int MAX = data.length * 8;
		if (index >= MAX || index < 0) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}

		int pos = data.length - index / 8 - 1;
		int bitPos = index % 8;

		int d = data[pos] & 0xFF;
		if (value) {
			d = d | (1 << bitPos);
		} else {
			d = d & ~(1 << bitPos);
		}
		data[pos] = (byte) d;

	}

	public static boolean getBit(byte[] data, int index) {

		final int MAX = data.length * 8;
		if (index >= MAX || index < 0) {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}

		int pos = data.length - index / 8 - 1;
		int bitPos = index % 8;
		int d = data[pos] & 0xFF;
		return (d & (1 << bitPos)) != 0;
	}

	public static byte[] invert(byte[] data) {

		int d = 0;
		for (int i = 0; i < data.length; ++i) {
			d = data[i] & 0xFF;
			d = ~d;
			data[i] = (byte) d;
		}

		return data;
	}
}
