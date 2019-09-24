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

package org.jopenray.client;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.jopenray.util.Hex;

public class RendererListener {

	DatagramSocket socket;
	int port = -1;
	private byte[] udpData;
	private int dataLength;

	// Stats
	private long connectionDate;
	private long byteCount;

	private long idleTime;
	private long packetLostCount;
	private long packetCount;

	RendererListener() {
		udpData = new byte[4096];
	}

	public void start() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				connectionDate = System.currentTimeMillis();
				DatagramPacket data = new DatagramPacket(udpData,
						udpData.length);
				try {
					while (true) {
						System.out.println("Listening on port:" + port);
						socket.receive(data);
						dataLength = data.getLength();
						handlePacket();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		});
		t.start();
	}

	public int getPort() throws IOException {

		if (port < 0) {
			int rcvPort = 3000;
			int maxPort = 4000;
			while (rcvPort <= maxPort) {
				try {
					socket = new DatagramSocket(rcvPort);
					port = rcvPort;
					return port;
				} catch (SocketException bind_ex) {
					rcvPort++;
				} catch (SecurityException sec_ex) {
					rcvPort++;
				}
			}

			throw new IOException(
					"Unable to find an available port for UDP communication");
		}
		return port;
	}

	private void handlePacket() throws IOException {
		byteCount += dataLength;
		packetCount++;

		PrintStream out = System.out;
		boolean dump = false;
		ByteArrayInputStream bIn = new ByteArrayInputStream(udpData, 0,
				this.dataLength);
		int r = readInt16(bIn);
		if (r == 1) {
			out
					.println("===================================================================================");
		}
		out.print("Seq number:" + r);
		int flag = readInt16(bIn);
		out.print(" Flag:" + flag);
		int type = readInt16(bIn);
		out.print(" Type:" + type);
		int dir = readInt16(bIn);
		out.println(" Dir:" + dir + " dataSize:" + udpData.length);
		if (dir != 2000) {

			// Server -> Sunray
			int a = readInt16(bIn);
			int b = readInt16(bIn);
			int c = readInt16(bIn);
			int d = readInt16(bIn);
			out.println("Server -> Sunray:" + a + "," + b + "," + c + "," + d);
			boolean endOfFrame = false;
			while (bIn.available() > 0 && !endOfFrame) {
				String opCodeHeader = "";
				int opcode = bIn.read();
				opCodeHeader += "[ Opcode: " + opcode;
				int f = bIn.read();
				opCodeHeader += " Flag" + f;
				int oseq = readInt16(bIn);
				opCodeHeader += " OpcodeSeq:" + oseq;
				int x = readInt16(bIn);
				int y = readInt16(bIn);

				int w = readInt16(bIn);
				int h = readInt16(bIn);

				opCodeHeader += " x,y: " + x + "," + y + " w:" + w + " h:" + h
						+ " ]";

				switch (opcode) {
				case 0x0: {
					endOfFrame = true;
					break;
				}
				case 0x03:
					out.println("0x03 Strange opcode " + opCodeHeader);
					break;
				case 0xA1:
					int ap1 = bIn.read();
					int ap2 = bIn.read();
					int ap3 = bIn.read();
					int ap4 = bIn.read();
					out.println("0xA1:" + ap1 + "," + ap2 + "," + ap3 + ","
							+ ap4 + opCodeHeader);
					break;
				case 0xA2:
					// out.println("FillRect");
					int a2p1 = bIn.read();
					int a2p2 = bIn.read();
					int a2p3 = bIn.read();
					int a2p4 = bIn.read();
					out.println("FillRect: Color:" + a2p1 + "," + a2p2 + ","
							+ a2p3 + "," + a2p4 + opCodeHeader);
					break;
				case 0xA3: {

					int a3p1 = bIn.read();
					int a3p2 = bIn.read();
					int a3p3 = bIn.read();
					int a3p4 = bIn.read();
					int nbBytesPerRow = round(w, 8) / 8;
					int nbBytes = round(nbBytesPerRow * h, 4);
					byte[] unkuonw = new byte[nbBytes];
					try {
						int lRead = bIn.read(unkuonw);
						Hex hdump = new Hex();
						// out.println(hdump.encode(unkuonw));
						out.println("FillRectBitmap: Color:" + a3p1 + ","
								+ a3p2 + "," + a3p3 + "," + a3p4
								+ " | bytes/row:" + nbBytesPerRow + "l:"
								+ nbBytes + " lRead:" + lRead + opCodeHeader);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				}
				case 0xA4:
					int xsrc = readInt16(bIn);
					int ysrc = readInt16(bIn);
					out.println("CopyRect from : " + xsrc + "," + ysrc
							+ opCodeHeader);

					break;
				case 0xA5: {
					// out.println("SetRectBitmap");
					// err.println("SetRectBitmap not yet implemented");
					try {
						Color c1 = readColor(bIn);
						Color c2 = readColor(bIn);
						int nbBytesPerRow = round(w, 8) / 8;
						int nbBytes = round(nbBytesPerRow * h, 4);
						byte[] unkuonw = new byte[nbBytes];

						int lRead = bIn.read(unkuonw);

						out.println("SetRectBitmap: " + w + "x" + h + " at "
								+ x + "," + y + " Color:" + c1 + " / " + c2
								+ " | bytes/row:" + nbBytesPerRow + " l:"
								+ nbBytes + " lRead:" + lRead + opCodeHeader);
						if (nbBytes > 1024) {
							out.println("! data too long:" + nbBytes);
						} else {
							Hex hdump = new Hex();
							out.println(hdump.encode(unkuonw));
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						dump = true;
					}

					break;
				}
				case 0xA6: {

					out.println("SetRect:" + opCodeHeader);
					int nbBytesPerRow = round(w * 3, 4);
					int nbBytes = round(nbBytesPerRow * h, 4);
					// int nbBytes=w*h;
					if (nbBytes > 1000000) {
						System.out.println("Bad length:" + nbBytes);
					} else {
						byte[] colors = new byte[nbBytes];

						int lRead = bIn.read(colors);
						if (lRead != nbBytes) {
							System.out.println("Bad length:" + nbBytes + " != "
									+ lRead);
						}
						// colors contains colors (r,g,b)
					}
					break;
				}
				case 0xA8: {
					int xbound = readInt16(bIn);
					int ybound = readInt16(bIn);
					int wbound = readInt16(bIn);
					int hbound = readInt16(bIn);
					out.println("SetMouseBound to: " + xbound + "," + ybound
							+ " w:" + wbound + " h:" + hbound + " "
							+ opCodeHeader + opCodeHeader);

					break;
				}
				case 0xA9: {

					Color c1 = readColor(bIn);
					Color c2 = readColor(bIn);
					out.println("SetMousePointer pos:" + x + "," + y + " size:"
							+ w + "x" + h + " Color:" + c1 + " , " + c2
							+ opCodeHeader);
					int l = (w * h) / 8;
					byte[] b1 = new byte[l];
					bIn.read(b1);
					out.println("Bitmap");
					// printBits(w, h, b1);

					byte[] b2 = new byte[l];
					bIn.read(b2);
					out.println("Mask");
					// printBits(w, h, b2);
					break;
				}
				case 0xAA:
					int aap1 = bIn.read();
					int aap2 = bIn.read();
					int aap3 = bIn.read();
					int aap4 = bIn.read();
					out.println("SetMousePosition: " + aap1 + "," + aap2 + ","
							+ aap3 + "," + aap4 + opCodeHeader);
					break;
				case 0xAB:
					int ab1 = readInt16(bIn);
					int ab2 = readInt16(bIn);
					out
							.println("SetKeyLock: " + ab1 + " " + ab2
									+ opCodeHeader);

					break;
				case 0xAC:

					int ac1 = readInt16(bIn);
					int ac2 = readInt16(bIn);
					int ac3 = readInt16(bIn);
					int ac4 = readInt16(bIn);
					out.println("0xAC : " + ac1 + " , " + ac2 + "," + ac3
							+ " , " + ac4 + opCodeHeader + opCodeHeader);
					break;
				case 0xAD:
					out.println("0xAD" + opCodeHeader);

					int l = readInt16(bIn);
					// l = (l & 0xfffc) + 2;

					out.println("l: " + l);
					out.println("(l & 0xfffc) + 2 :" + (l & 0xfffc) + 2);
					byte[] unkuonwn = new byte[l];
					dump = true;
					try {
						int lRead = bIn.read(unkuonwn);
						Hex hdump = new Hex();
						// out.println(hdump.encode(unkuonwn));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				case 0xAF: {

					int p1 = bIn.read();
					int p2 = bIn.read();
					int p3 = bIn.read();
					int p4 = bIn.read();
					for (int i = 0; i < 8; i++) {
						bIn.read();
					}
					if (p1 != 255 && p2 != 255 && p3 != 255 && p4 != 255) {
						out.println("PAD:" + p1 + "," + p2 + "," + p3 + ","
								+ p4 + opCodeHeader);
					} else {
						out.println("PAD " + opCodeHeader);
					}
					break;
				}
				case 0xB1:

					out
							.println("AUDIO:" + r + "|" + flag + "|" + type
									+ "|" + dir + " l:" + udpData.length + " "
									+ opCodeHeader);
					// outTest.print("AUDIO:"+r+"|"+flag+"|"+type+"|"+dir+" " +
					// opCodeHeader);
					/*
					 * int xbound = readInt16(bIn); int ybound = readInt16(bIn);
					 * int wbound = readInt16(bIn); int hbound = readInt16(bIn);
					 * out.println(" to: " + xbound + "," + ybound + " w:" +
					 * wbound + " h:" + hbound + " " + opCodeHeader +
					 * opCodeHeader); dump=true;
					 */

					int v1 = 0;
					int v2 = 0;
					int totalv1et2 = 0;
					int bigTotal = 0;
					while (bIn.available() >= 0) {
						int b1 = bIn.read();
						int b2 = bIn.read();
						if (b1 == -1 && b2 == -1) {
							// outTest.print(totalv1et2+" : big total: "+bigTotal);
							break;
						}
						// soundOut.write(b2);
						// soundOut.write(b1);

						if (b1 == 0x7F && b2 == 0xFF) {
							v1++;
							bigTotal++;
							totalv1et2++;
							if (v2 > 0)
								out.println("v2=" + v2);
							v2 = 0;
						} else if (b1 == 0x80 && b2 == 0x01) {
							v2++;
							totalv1et2++;
							bigTotal++;
							if (v1 > 0)
								out.println("v1=" + v1);
							v1 = 0;
						} else {
							if (v2 > 0)
								out.println("v2=" + v2);
							if (v1 > 0)
								out.println("v1=" + v1);
							out.println("Unknwon:" + b1 + " et " + b2 + "["
									+ (b1 * 256 + b2) + "] total v1+v2:"
									+ totalv1et2);
							// if(totalv1et2>0)
							// outTest.print(totalv1et2+",");
							v1 = 0;
							v2 = 0;
							totalv1et2 = 0;
						}
						/*
						 * bIn.read(); bIn.read(); for (int j = 0; j < 8; j++) {
						 * for (int i = 0; i < 12; i++) {
						 * 
						 * int aaa1 = bIn.read(); int aaa2 = bIn.read(); if (i %
						 * 2 == 0) { soundOut.write(aaa2); soundOut.write(aaa1);
						 * } } }
						 */

					}
					// outTest.println();

					break;
				case 0xD1:
					out.println("0xD1 " + opCodeHeader + opCodeHeader);
					break;
				case 0xD8:
					out.println("0xD8 " + opCodeHeader + opCodeHeader);
					break;
				case 0xB0: {
					out.println("0xB0 " + opCodeHeader + opCodeHeader);
					int p1 = readInt16(bIn);

					int p2 = readInt16(bIn);
					int p3 = readInt16(bIn);
					int nb = readInt16(bIn);
					out.println(p1 + " ; " + p2 + " ; " + p3);
					for (int i = 0; i < nb; i++) {
						int xx = readInt16(bIn);
						int yy = readInt16(bIn);
						int ww = readInt16(bIn);
						int hh = readInt16(bIn);
						out.println("[" + xx + "," + yy + " " + ww + "x" + hh
								+ "]");
					}
					break;
				}
				case 0xB4: {
					// ??
					out.println("0xB4 " + opCodeHeader + opCodeHeader);

					for (int i = 0; i < 19; i++) {
						int p1 = readInt16(bIn);
						out.print(p1 + ",");
					}
					int end = readInt16(bIn);
					out.println(end);
					break;
				}
				case 0xB9: {
					// ??
					out.println("0xB9 " + opCodeHeader + opCodeHeader);
					break;
				}
				case 0xBF: {
					int le = readInt16(bIn);
					out.println("0xBF " + le + " bytes " + opCodeHeader);

					byte[] unknown = new byte[le];

					try {
						int lRead = bIn.read(unknown);
						if (lRead != le) {
							out.println("Bad length:" + lRead + " / " + le);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				}
				default:
					out.println("Unknown opcode:" + opcode + opCodeHeader);
					dump = true;
					endOfFrame = true;
					break;
				}
			}

		} else {
			out.println("Unknown packet direction:" + dir);
			if (dir != 0)
				dump = true;
		}
		if (dump) {
			Hex hdump = new Hex();
			out.println(hdump.encode(udpData));
		}
	}

	public static int readInt16(ByteArrayInputStream in) {
		int a = in.read();
		int b = in.read();
		if (a < 0 || b < 0) {
			throw new IllegalStateException("Unexpected end of stream");
		}
		return a * 256 + b;
	}

	public static int readInt32(ByteArrayInputStream in) {
		int a = in.read();
		int b = in.read();
		int c = in.read();
		int d = in.read();
		if (a < 0 || b < 0 || c < 0 || d < 0) {
			throw new IllegalStateException("Unexpected end of stream");
		}
		return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;

	}

	public static Color readColor(ByteArrayInputStream in) {
		return new Color(in.read(), in.read(), in.read(), in.read());
	}

	public static int round(int value, int multiple) {
		int d = value % multiple;
		if (d > 0) {
			return value + multiple - d;
		}
		return value;
	}

	public Stat getStats() {
		return new Stat(byteCount, connectionDate - System.currentTimeMillis(),
				idleTime, packetLostCount, packetCount);
	}
}
