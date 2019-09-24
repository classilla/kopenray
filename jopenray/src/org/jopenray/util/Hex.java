/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2019 Cameron Kaiser
 *  All rights reserved.
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

public class Hex {
	private final static byte[] hexArray = "0123456789ABCDEF".getBytes();

	public static byte[] decodeHex(String s) {
		char[] data = s.toCharArray();
		int len = data.length;
		byte[] out = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			int f = Character.digit(data[i], 16) << 4;
			f = f | Character.digit(data[i + 1], 16);
			out[i / 2] = (byte) (f & 0xFF);
		}
		return out;
	}

	// Replaces HexDumpEncoder
	public static byte[] encode(byte[] bytes) {
		byte[] hex = new byte[bytes.length + bytes.length];
		int i, j;

		for(i=0; i<bytes.length; i++) {
			j = (bytes[i] & 0xFF);
			hex[i+i] = hexArray[j >>> 4];
			hex[i+i+1] = hexArray[j & 0x0F];
		}
		return hex;
	}
}
