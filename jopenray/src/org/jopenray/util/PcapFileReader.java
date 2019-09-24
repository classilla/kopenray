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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jopenray.util.Hex;

public class PcapFileReader {

	private BufferedInputStream in;

	public PcapFileReader(File f) throws IOException {
		in = new BufferedInputStream(new FileInputStream(f));
		byte[] h = new byte[24];
		boolean isPcap = false;
		int r = in.read(h);
		if (r == h.length) {

			if (h[0] == (byte) 0xD4 && h[1] == (byte) 0xC3
					&& h[2] == (byte) 0xB2 && h[3] == (byte) 0xA1) {
				isPcap = true;
			}
		}

		if (!isPcap) {
			throw new IllegalArgumentException("Not a PCAP file");
		}

	}

	public boolean hasNext() {
		try {
			return in.available() > 0;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public PcapPacket next() {
		byte[] h = new byte[8];

		try {
			int r = in.read(h);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		byte[] n = new byte[4];
		// n on wire
		try {
			int r = in.read(n);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		// n captured
		int nbBytes = 0;
		try {
			int r = in.read(n);

			nbBytes = unsignedByteToInt(n[0]) + unsignedByteToInt(n[1]) * 256;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		if (nbBytes == 0) {
			throw new IllegalStateException("0 bytes");
		}

		byte[] data = new byte[nbBytes];
		try {

			int r = in.read(data);
			if (r != nbBytes) {
				throw new IllegalStateException("bad packet size:" + r + "/"
						+ nbBytes);
			}
			PcapPacket packet = new PcapPacket(data);
			return packet;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
	}

	public static void main(String[] args) throws IOException {
		PcapFileReader r = new PcapFileReader(new File(
				"test.pcap"));
		int c = 1;
		while (r.hasNext()) {
			System.out.println("Packet:" + c);
			PcapPacket p = r.next();
			System.out.println("isIp:" + p.isIP());
			if (p.isIP()) {
				System.out.println(p.getSourceIp() + ":" + p.getUDPSrcPort()
						+ " -> " + p.getDestIp() + ":" + p.getUDPDstPort());
				System.out.println("isUDP:" + p.isUDP());
				System.out.println("isTCP:" + p.isTCP());
				Hex d = new Hex();
				System.out.println(d.encode(p.getData()));

			}
			c++;
			if (c > 10)
				System.exit(0);
		}

	}

}
