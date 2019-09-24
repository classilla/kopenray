/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2014 Matthew Martin
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

package org.jopenray.server.thinclient;

import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import org.jopenray.operation.PadOperation;
import org.jopenray.util.HID;
import org.jopenray.util.Hex;

public class DisplayReaderThread extends Thread {

	private ThinClient client;
	private List<InputListener> listeners = new ArrayList<InputListener>();
	int lastMouseState = Integer.MIN_VALUE;
	int lastMouseX = Integer.MIN_VALUE;
	int lastMouseY = Integer.MIN_VALUE;
	List<Integer> keysPressed = new ArrayList<Integer>(4);
	private static final boolean DEBUG = false;

	DisplayReaderThread(ThinClient client) {
		this.client = client;
		setDaemon(true);
		setName("DisplayClient connection (reader)"
				+ client.getServer().getHostAddress());
	}

	@Override
	public void run() {
		byte[] buf = new byte[1500];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		try {
			while (!this.isInterrupted()) {
				client.getSocket().receive(packet);
				handlePacket(buf, packet.getLength());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void handlePacket(byte[] udpData, int l) {
		boolean dump = false;
		ByteArrayInputStream bIn = new ByteArrayInputStream(udpData, 0, l);
		int r = readInt16(bIn);
		int flag = readInt16(bIn);
		int type = readInt16(bIn);
		int dir = readInt16(bIn);
		if (DEBUG)
			System.out.print("Seq number:" + r + " Flag:" + flag
					+ " Type:" + type + " Dir:" + dir);

		// Sunray ->Server
		int a = readInt16(bIn);
		int b = readInt16(bIn);
		int c = readInt16(bIn);
		int d = readInt16(bIn);
		if (DEBUG)
			System.out.println("Sunray -> Server:" + a + "," + b + "," + c
					+ "," + d);
		if (bIn.available() == 0) {
			DisplayMessage m = new DisplayMessage(client.getWriter());
			m.addOperation(new PadOperation());
			this.client.getWriter().addMessage(m);
		} else
			while (bIn.available() > 0) {
				int opcode = bIn.read();
				int hdat = readInt16(bIn);
				int idat = bIn.read();
				String opCodeHeader = "";

				opCodeHeader += "[ Opcode: " + opcode + " , " + hdat + " ,"
					+ idat + " ]";

				switch (opcode) {
					case 0xc1:
						dump=true;
						int jdat = readInt16(bIn);
						int modifier = readInt16(bIn);
						// 6 octet
						int key1 = bIn.read();
						int key2 = bIn.read();
						int key3 = bIn.read();
						int key4 = bIn.read();
						int key5 = bIn.read();
						int key6 = bIn.read();

						//
						int mdat = readInt16(bIn);
						System.out.println("Keyboard " + opCodeHeader + " "
								+ jdat + " modifier:" + modifier + " keys:("
								+ key1 + "," + key2 + "," + key3 + "," + key4
								+ "," + key5 + "," + key6 + ") " + mdat);

						/*
						 * if(lastPressed>0){
						 * System.out.println("Send Key Released:"+lastPressed);
						 * sendKeyReleased(lastPressed); lastPressed=-1; }
						 */

						if (key1 > 0) {
							if (!keysPressed.contains(key1)) {

								int hidToKeyCode = HID.hidToKeyCode(key1);
								System.out.println(modifier + " : "
										+ (modifier & 0x2));
								sendKeyPressed(hidToKeyCode,
										(modifier & 2) != 0
										|| (modifier & 32) != 0,
										(modifier & 1) != 0,
										(modifier & 4) != 0,
										(modifier & 8) != 0,
										(modifier & 64) != 0);
								keysPressed.add(key1);
							}

						} else {
							for (int i = 0; i < this.keysPressed.size(); i++) {
								int k = keysPressed.get(i);
								int hidToKeyCode = HID.hidToKeyCode(k);
								sendKeyReleased(hidToKeyCode);

							}
							keysPressed.clear();
						}

						break;
					case 0xc2:
						int buttons = readInt16(bIn);
						int mouseX = readInt16(bIn);
						int mouseY = readInt16(bIn);
						int c2 = readInt16(bIn);
						if (DEBUG)
							System.out.println("Mouse" + opCodeHeader
									+ " buttons:" + buttons + " (" + mouseX
									+ "," + mouseY + ")" + c2);
						processMouseEvent(buttons, mouseX, mouseY);
						break;
					case 0xc4: {
							   int c41 = readInt32(bIn);
							   int c42 = readInt32(bIn);
							   int c43 = readInt32(bIn);

								if (DEBUG)
							   System.out.println("NACK  seq= " + c41 + "  type: "
									   + c42 + " , " + c43);
							   client.resend(c42, c43);
							   break;
					}
					case 0xc5:
						   int c51 = bIn.read();
						   int c52 = bIn.read();
						   int c53 = bIn.read();
						   int c54 = bIn.read();

						   System.out.println("0xC5 " + opCodeHeader + " " + c51
								   + "," + c52 + "," + c53 + "," + c54);
						   break;
					case 0xc6:
						   int dataLength = readInt16(bIn);
						   int stringLength = bIn.read();
						   byte[] string = new byte[stringLength];

						   try {
							   int rL = bIn.read(string);
							   System.out.println(dataLength + " , "
									   + stringLength + " readLength" + rL);
							   System.out.println("Firmware: "
									   + new String(string));
							   // dump = true;
						   } catch (IOException e) {
							   // TODO Auto-generated catch block
							   e.printStackTrace();
						   }

						   break;

					case 0xc7: {
							   int x1 = readInt16(bIn);
							   int y1 = readInt16(bIn);
							   int w1 = readInt16(bIn);
							   int h1 = readInt16(bIn);
							   int x2 = readInt16(bIn);
							   int y2 = readInt16(bIn);
							   int w2 = readInt16(bIn);
							   int h2 = readInt16(bIn);
							   int x3 = readInt16(bIn);
							   int y3 = readInt16(bIn);
							   int w3 = readInt16(bIn);
							   int h3 = readInt16(bIn);
							   if (DEBUG)
								   System.out.println("Rect: " + opCodeHeader + " ["
										   + x1 + "," + y1 + "," + w1 + "," + h1
										   + "][" + x2 + "," + y2 + "," + w2 + ","
										   + h2 + "][" + x3 + "," + y3 + "," + w3
										   + "," + h3 + "]");

							   break;
					}
					case 0xcb:
						   int bc1 = bIn.read();
						   int bc2 = readInt16(bIn);
						   int bc3 = readInt16(bIn);
						   int bc4 = readInt16(bIn);
						   int bc5 = readInt16(bIn);
						   int bc6 = readInt16(bIn);
						   int bc7 = readInt16(bIn);
						   int bc8 = readInt16(bIn);
						   int bc9 = readInt16(bIn);
						   int bc10 = readInt16(bIn);
						   int bc11 = readInt16(bIn);
						   if (DEBUG)
							   System.out
								   .println("0xCB " + opCodeHeader + " " + bc1
										   + "," + bc2 + "," + bc3 + "," + bc4
										   + "," + bc5 + "," + bc6 + "," + bc7
										   + "," + bc8 + "," + bc9 + ","
										   + bc10 + "," + bc11);
						   break;
					default:
						   System.out.println("Unknown opcode: " + opCodeHeader);
						   dump = true;
						   break;
				}
			}
		if (dump) {
			byte[] rBytes = new byte[l];
			System.arraycopy(udpData, 0, rBytes, 0, l);
			Hex hdump = new Hex();
			System.out.println(hdump.encode(rBytes));
		}

	}

	private void processMouseEvent(int buttons, int mouseX, int mouseY) {
		buttons = buttons - 64;
		if (this.lastMouseState == Integer.MIN_VALUE) {
			this.lastMouseState = buttons;
			this.lastMouseX = mouseX;
			this.lastMouseY = mouseY;
			return;
		}

		// Mouse move
		if (lastMouseX != mouseX || lastMouseY != mouseY) {
			sendMouseMoved(mouseX, mouseY);
		}

		// Mouse pressed/released

		int change = buttons ^ this.lastMouseState;

		if ((change & 1) > 0) {
			if ((buttons & 1) > 0) {
				System.out
					.println("DisplayReaderThread.processMouseEvent() BUTTON1 pressed");
				sendMousePressed(MouseEvent.BUTTON1, mouseX, mouseY);
			} else {
				System.out
					.println("DisplayReaderThread.processMouseEvent() BUTTON1 released");
				sendMouseReleased(MouseEvent.BUTTON1, mouseX, mouseY);
			}
		}
		if ((change & 2) > 0) {
			if ((buttons & 2) > 0) {
				System.out
					.println("DisplayReaderThread.processMouseEvent() BUTTON2 pressed");
				sendMousePressed(MouseEvent.BUTTON2, mouseX, mouseY);
			} else {
				sendMouseReleased(MouseEvent.BUTTON2, mouseX, mouseY);
			}
		}
		if ((change & 4) > 0) {
			if ((buttons & 4) > 0) {
				System.out
					.println("DisplayReaderThread.processMouseEvent() BUTTON3 pressed");
				sendMousePressed(MouseEvent.BUTTON3, mouseX, mouseY);
			} else {
				sendMouseReleased(MouseEvent.BUTTON3, mouseX, mouseY);
			}
		}
		if ((change & 8) > 0) {
			if ((buttons & 8) > 0) {
				System.out
					.println("DisplayReaderThread.processMouseEvent() mouse wheel up");
				sendMouseWheelUp(mouseX, mouseY);
			} else {
				// sendMouseReleased(MouseEvent.BUTTON3, mouseX, mouseY);
			}
		}
		if ((change & 16) > 0) {
			if ((buttons & 16) > 0) {
				System.out
					.println("DisplayReaderThread.processMouseEvent() mouse wheel down");
				sendMouseWheelDown(mouseX, mouseY);
			} else {
				// sendMouseReleased(MouseEvent.BUTTON3, mouseX, mouseY);
			}
		}

		this.lastMouseState = buttons;
		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;

	}

	private void sendMouseReleased(int button, int mouseX, int mouseY) {
		final int size = this.listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).mouseReleased(button, mouseX, mouseY);
		}
	}

	private void sendMousePressed(int button, int mouseX, int mouseY) {
		final int size = this.listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).mousePressed(button, mouseX, mouseY);
		}

	}

	private void sendMouseMoved(int mouseX, int mouseY) {
		final int size = this.listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).mouseMoved(mouseX, mouseY);
		}

	}

	private void sendMouseWheelUp(int mouseX, int mouseY) {
		final int size = this.listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).mouseWheelUp(mouseX, mouseY);
		}

	}

	private void sendMouseWheelDown(int mouseX, int mouseY) {
		final int size = this.listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).mouseWheelDown(mouseX, mouseY);
		}

	}

	private void sendKeyPressed(int key, boolean shift, boolean ctrl,
			boolean alt, boolean meta, boolean altGr) {
		System.out.println("Key pressed: code:" + key + " shift:" + shift
				+ " ctrl:" + ctrl + " alt:" + alt + " meta:" + meta);
		final int size = this.listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).keyPressed(key, shift, ctrl, alt, meta, altGr);
		}

	}

	private void sendKeyReleased(int key) {
		final int size = this.listeners.size();
		for (int i = 0; i < size; i++) {
			listeners.get(i).keyReleased(key);
		}

	}

	public void addInputListener(InputListener l) {
		this.listeners.add(l);

	}

	public void removeInputListener(InputListener l) {
		this.listeners.remove(l);

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
}
