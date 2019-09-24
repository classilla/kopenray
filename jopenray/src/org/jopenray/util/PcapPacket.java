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

public class PcapPacket {

	private byte[] raw; // Raw data
	private static int ETH_HEADER_SIZE = 14;
	private static int IP_HEADER_SIZE = 20;
	private static int UDP_HEADER_SIZE = 8;
	private static int TCP_HEADER_SIZE = 20;

	public PcapPacket(byte[] data) {
		this.raw = data;
	}

	public boolean isIP() {
		return raw[12] == (byte) 0x08 && raw[13] == (byte) 0x00;
	}

	public boolean isTCP() {
		return getPacketProtocol() == 6;
	}

	public byte getPacketProtocol() {
		return raw[ETH_HEADER_SIZE + 9];
	}

	public boolean isUDP() {
		return getPacketProtocol() == 17;
	}

	public byte[] getData() {
		int hLength = ETH_HEADER_SIZE;
		if (isUDP()) {
			hLength = ETH_HEADER_SIZE + IP_HEADER_SIZE + UDP_HEADER_SIZE;
		}
		if (isTCP()) {
			hLength = ETH_HEADER_SIZE + IP_HEADER_SIZE + TCP_HEADER_SIZE;
		}

		byte[] d = new byte[raw.length - hLength];
		System.arraycopy(raw, hLength, d, 0, d.length);
		return d;

	}

	public String getSourceIp() {
		return getInt(ETH_HEADER_SIZE + 12) + "."
				+ getInt(ETH_HEADER_SIZE + 13) + "."
				+ getInt(ETH_HEADER_SIZE + 14) + "."
				+ getInt(ETH_HEADER_SIZE + 15);
	}

	public String getDestIp() {
		return getInt(ETH_HEADER_SIZE + 16) + "."
				+ getInt(ETH_HEADER_SIZE + 17) + "."
				+ getInt(ETH_HEADER_SIZE + 18) + "."
				+ getInt(ETH_HEADER_SIZE + 19);
	}

	public int getInt(int index) {
		return (int) raw[index] & 0xFF;
	}

	public int getUDPSrcPort() {
		return getInt(ETH_HEADER_SIZE + IP_HEADER_SIZE) * 256
				+ getInt(ETH_HEADER_SIZE + IP_HEADER_SIZE + 1);
	}

	public int getUDPDstPort() {
		return getInt(ETH_HEADER_SIZE + IP_HEADER_SIZE + 2) * 256
				+ getInt(ETH_HEADER_SIZE + IP_HEADER_SIZE + 3);
	}

	public byte[] getRaw() {
		return raw;
	}
}
