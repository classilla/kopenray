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

package org.jopenray.server.thinclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jopenray.operation.Operation;
import org.jopenray.operation.SleepOperation;
import org.jopenray.operation.FlushOperation;
import org.jopenray.util.ByteArrayList;

public class DisplayMessage {

	private ByteArrayList operationBuffer;

	private DisplayWriterThread displayWriterThread;

	// Resends can cause potentially huge new allocations, so don't be
	// stingy here.
	private List<Operation> operations = new ArrayList<Operation>(256);
	private int l = -1;
	private static final boolean PROFILING = false;
	private boolean resending;

	public DisplayMessage(DisplayWriterThread displayWriterThread) {
		this.displayWriterThread = displayWriterThread;
		resending = false;
	}

	public DisplayMessage(DisplayWriterThread displayWriterThread,
			boolean resending) {
		this.displayWriterThread = displayWriterThread;
		this.resending = resending;
		// operationBuffer = new ByteArrayList(1448, HEADER_SIZE);

	}

	public boolean isEmpty() {
		return operations.isEmpty();
	}

	public void addOperation(Operation o) {
		this.operations.add(o);
		if (!PROFILING) return;
		l += o.getLength();
	}

	public void addOperations(ArrayList<Operation> oo) {
		this.operations.addAll(oo);
		if (!PROFILING) return;
		throw new IllegalArgumentException("needs profiling code for addOperations");
	}

	public void send(final ThinClient client) throws IOException {
		// System.out.println("\n=====Sending Message...");
		int maxSize = client.getMTU() - 10 - 42;
		byte[] buffer = new byte[maxSize];
		int type = 1;
		int bufferLength = 16;
		int nextPacketStartAt = 0;

		int size = this.operations.size();
		for (int i = 0; i < size; i++) {
			Operation o = this.operations.get(i);
			if (o instanceof SleepOperation) {
				try {
					Thread.sleep(((SleepOperation) o).getSleepTime(), 0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			if (o instanceof FlushOperation) {
				client.flushHistory();
				// Don't exit, just flush.
				continue;
			}
			try {
				if (!resending) {

					o.setSequence(displayWriterThread.getNextOpcodeSeq(o
							.getSequenceIncrement()));
					client.addToHistory(o);
				}
				int nbCopiedBytes = o.copyStart(buffer, bufferLength);
				int nbBytesMissing = o.getLength() - nbCopiedBytes;
				bufferLength += nbCopiedBytes;
				if (nbBytesMissing > 0) {
					// Envoi le packet splitté
					type = 6;

					// add header
					ByteArrayList.setInt16(buffer, 0, displayWriterThread
							.getNextSeq());
					ByteArrayList.setInt16(buffer, 2, nextPacketStartAt);
					ByteArrayList.setInt16(buffer, 4, type);
					// next 12 bytes are 0

					client.sendBytes(buffer, bufferLength);

					o.copyEnd(buffer, nbBytesMissing, 16);
					bufferLength = nbBytesMissing + 16;

					if (nbBytesMissing == o.getLength()) {
						nextPacketStartAt = 0;
					} else {
						nextPacketStartAt = nbBytesMissing;
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
				new IllegalStateException("Failed to process operation:" + o);
			}
		}
		if (bufferLength > 0) {

			// add header
			ByteArrayList.setInt16(buffer, 0, displayWriterThread.getNextSeq());
			ByteArrayList.setInt16(buffer, 2, nextPacketStartAt);
			ByteArrayList.setInt16(buffer, 4, type);
			// next 12 bytes are 0
			client.sendBytes(buffer, bufferLength);
		}
		// System.out.println("=====Sending Message done...");
	}

	/*
	 * public void readFromRawFile(String string) { File b = new File(string);
	 * if (!b.exists()) { System.err.println("Not found:" +
	 * b.getAbsolutePath()); } try { FileInputStream fIn = new
	 * FileInputStream(b); byte[] fileContent = new byte[(int) b.length()]; int
	 * r = fIn.read(fileContent); ByteBuffer newBuffer =
	 * ByteBuffer.wrap(fileContent, 0, r); newBuffer.putInt(0,
	 * buffer.getInt(0)); buffer = newBuffer;
	 * 
	 * System.out.println("Read:" + r + " bytes"); } catch (Exception e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); } }
	 */

	/*
	 * public void addFillRect(int x, int y, int width, int height, Color color)
	 * { int nextOpcodeSeq = this.displayWriterThread.getNextOpcodeSeq();
	 * System.
	 * out.println("DisplayMessage.addFillRect() at: "+x+" , "+y+"   "+width
	 * +" x "+height +" opseq:"+nextOpcodeSeq); operationBuffer.addInt8(0xA2);
	 * // fillrect // hflags operationBuffer.addInt8(0x00); // seq
	 * 
	 * operationBuffer.addInt16(nextOpcodeSeq); // x y
	 * operationBuffer.addInt16(x); operationBuffer.addInt16(y); // w h
	 * operationBuffer.addInt16(width); operationBuffer.addInt16(height); //
	 * fillrect
	 * 
	 * operationBuffer.addInt8(0xFF); operationBuffer.addInt8(color.getBlue());
	 * operationBuffer.addInt8(color.getGreen());
	 * operationBuffer.addInt8(color.getRed());
	 * 
	 * }
	 */

	/*
	 * public void addAC() { operationBuffer.addInt8(0xAC); // hflags
	 * operationBuffer.addInt8(0x00); // seq
	 * operationBuffer.addInt16(this.displayWriterThread.getNextOpcodeSeq()); //
	 * x y operationBuffer.addInt16(0); operationBuffer.addInt16(0); // w h
	 * operationBuffer.addInt16(0); operationBuffer.addInt16(0);
	 * 
	 * operationBuffer.addInt16(0); operationBuffer.addInt16(1);
	 * operationBuffer.addInt16(0); operationBuffer.addInt16(4); }
	 */

	/*
	 * public void addD1(int x, int y, int width, int height) {
	 * operationBuffer.addInt8(0xD1); // hflags operationBuffer.addInt8(0x00);
	 * // seq
	 * operationBuffer.addInt16(this.displayWriterThread.getNextOpcodeSeq()); //
	 * x y operationBuffer.addInt16(x); operationBuffer.addInt16(y); // w h
	 * operationBuffer.addInt16(width); operationBuffer.addInt16(height);
	 * 
	 * }
	 */

	/*
	 * public void addD8(int x, int y, int width, int height) {
	 * operationBuffer.addInt8(0xD8); // hflags operationBuffer.addInt8(0x00);
	 * // seq
	 * operationBuffer.addInt16(this.displayWriterThread.getNextOpcodeSeq()); //
	 * x y operationBuffer.addInt16(x); operationBuffer.addInt16(y); // w h
	 * operationBuffer.addInt16(width); operationBuffer.addInt16(height);
	 * 
	 * }
	 */

	/*
	 * public void addPad() { operationBuffer.addInt8(0xAF); // hflags
	 * operationBuffer.addInt8(0x00); // seq operationBuffer
	 * .addInt16(this.displayWriterThread.getCurrentOpcodeSeq()); // x y
	 * operationBuffer.addInt16(0); operationBuffer.addInt16(1); // w h
	 * operationBuffer.addInt16(0xFFFF); operationBuffer.addInt16(0xFFFF);
	 * 
	 * operationBuffer.addInt8(0xFF); operationBuffer.addInt8(0xFF);
	 * operationBuffer.addInt8(0xFF); operationBuffer.addInt8(0xFF); for (int i
	 * = 0; i < 8; i++) { operationBuffer.addInt8(0xFF); } }
	 */

	/*
	 * public void addA1() { operationBuffer.addInt8(0xA1); // hflags
	 * operationBuffer.addInt8(0x00); // seq
	 * operationBuffer.addInt16(this.displayWriterThread.getNextOpcodeSeq()); //
	 * x y operationBuffer.addInt16(0); operationBuffer.addInt16(0); // w h
	 * operationBuffer.addInt16(0x0001); operationBuffer.addInt16(0x0001);
	 * 
	 * operationBuffer.addInt8(0x00); operationBuffer.addInt8(0x00);
	 * operationBuffer.addInt8(0x00); operationBuffer.addInt8(0x00);
	 * 
	 * }
	 */

	/*
	 * public void addSetMouseBound(int x, int y, int width, int height) {
	 * operationBuffer.addInt8(0xA8); // hflags operationBuffer.addInt8(0x00);
	 * // seq
	 * operationBuffer.addInt16(this.displayWriterThread.getNextOpcodeSeq()); //
	 * x y operationBuffer.addInt16(x); operationBuffer.addInt16(y); // w h
	 * operationBuffer.addInt16(width); operationBuffer.addInt16(height);
	 * 
	 * // Bounds operationBuffer.addInt16(x); operationBuffer.addInt16(y);
	 * operationBuffer.addInt16(width); operationBuffer.addInt16(height);
	 * 
	 * }
	 */

	public void addSetMouseCursor(int x, int y, int width, int height) {
		operationBuffer.addInt8(0xA9);
		// hflags
		operationBuffer.addInt8(0x00);
		// seq
		operationBuffer.addInt16(this.displayWriterThread.getNextOpcodeSeq(0));
		// x y
		operationBuffer.addInt16(x);
		operationBuffer.addInt16(y);
		// w h
		operationBuffer.addInt16(width);
		operationBuffer.addInt16(height);

		// C1
		operationBuffer.addInt8(0x0);
		operationBuffer.addInt8(0x255);
		operationBuffer.addInt8(0x255);
		operationBuffer.addInt8(0x255);

		// C2
		operationBuffer.addInt8(0x0);
		operationBuffer.addInt8(0x0);
		operationBuffer.addInt8(0x0);
		operationBuffer.addInt8(0x0);

		// Bitmap
		byte[] bitmap = new byte[] { 0x00, 0x00, 0x70, 0x0E, 0x78, 0x1E, 0x7C,
				0x3E, 0x3E, 0x7C, 0x1F, (byte) 0xF8, 0x0F, (byte) 0xF0, 0x07,
				(byte) 0xE0, 0x07, (byte) 0xE0, 0x0F, (byte) 0xF0, 0x1F,
				(byte) 0xF8, 0x3E, 0x7C, 0x7C, 0x3E, 0x78, 0x1E, 0x70, 0x0E,
				0x00, 0x00 };
		operationBuffer.addBytes(bitmap);
		// Bitmap
		byte[] mask = new byte[] { (byte) 0xF0, 0x0F, (byte) 0xF8, 0x1F,
				(byte) 0xFC, 0x3F, (byte) 0xFE, 0x7F, 0x7F, (byte) 0xFE, 0x3F,
				(byte) 0xFC, (byte) 0x1F, (byte) 0xF8, 0x0F, (byte) 0xF0, 0x0F,
				(byte) 0xF0, 0x1F, (byte) 0xF8, 0x3F, (byte) 0xFC, 0x7F,
				(byte) 0xFE, (byte) 0xFE, 0x7F, (byte) 0xFC, 0x3F, (byte) 0xF8,
				0x1F, (byte) 0xF0, 0x0F };
		operationBuffer.addBytes(mask);

	}

	/*
	 * public void addSetRectBitmap(int x, int y, int width, int height, Color
	 * c0, Color c1, BitArray b) { int nextOpcodeSeq =
	 * this.displayWriterThread.getNextOpcodeSeq();
	 * System.out.println("DisplayMessage.addSetRectBitmap() at: "
	 * +x+" , "+y+"   "+width+" x "+height +" opcodeseq:"+nextOpcodeSeq);
	 * 
	 * operationBuffer.addInt8(0xA5); // hflags operationBuffer.addInt8(0x00);
	 * // seq operationBuffer.addInt16(nextOpcodeSeq); // x y
	 * operationBuffer.addInt16(x); operationBuffer.addInt16(y); // w h
	 * operationBuffer.addInt16(width); operationBuffer.addInt16(height); // C1
	 * operationBuffer.addInt8(0x00); operationBuffer.addInt8(c0.getBlue());
	 * operationBuffer.addInt8(c0.getGreen());
	 * operationBuffer.addInt8(c0.getRed()); // C2
	 * operationBuffer.addInt8(0x00); operationBuffer.addInt8(c1.getBlue());
	 * operationBuffer.addInt8(c1.getGreen());
	 * operationBuffer.addInt8(c1.getRed());
	 * 
	 * 
	 * operationBuffer.addBytes(b.toByteArray());
	 * 
	 * }
	 */

	public int getLength() {
		return l;
	}

	public void dump() {
		final int size = this.operations.size();
		for (int i = 0; i < size; i++) {
			System.out.println("Operation " + i);
			this.operations.get(i).dump();
		}

	}
}
