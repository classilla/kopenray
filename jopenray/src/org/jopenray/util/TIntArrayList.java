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

public final class TIntArrayList {

	private int count;
	private int[] data;

	public TIntArrayList(int size) {
		count = 0;
		data = new int[size];
	}

	public final boolean contains(int value) {
		for (int i = 0; i < count; i++) {
			if (data[i] == value) {
				return true;
			}
		}
		return false;
	}

	public final void add(int value) {
		data[count] = value;
		count++;
	}

}
