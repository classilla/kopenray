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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jopenray.operation.FillOperation;
import org.jopenray.operation.SetBoundsOperation;
import org.jopenray.operation.SetMouseCursorOperation;
import org.jopenray.operation.SleepOperation;
import org.jopenray.operation.FlushOperation;

public class DisplayWriterThread extends Thread {

	private static final int WIDTH = 4;
	private static final int MAX_SIZE = 320;
	private static final boolean DEBUG = false;
	private static final int NO_COLOR = 888;
	private ThinClient client;
	List<DisplayMessage> toSend = new LinkedList<DisplayMessage>();
	public BitmapEncoder encoder;

	public DisplayWriterThread(ThinClient client) {
		this.client = client;
		setDaemon(true);

		encoder = new BitmapEncoder(this);

		setName("Display connection" + client.getServer().getHostAddress());
		this.setPriority(MIN_PRIORITY);
	}

	private int seq = 0;
	private int opSeq = 0;
	private long totalBytesToSend = 0;

	public void addMessage(DisplayMessage m) {

		synchronized (toSend) {
			totalBytesToSend += m.getLength();
			// System.out.println("Total bytes to send:" + totalBytesToSend);
			toSend.add(m);
			// Only send notifications if the message queue
			// was totally empty before or we spend a lot of time
			// waking up threads that are already awake.
			if (toSend.size() == 1) toSend.notify();
		}

	}

	public void addHighPriorityMessage(DisplayMessage m) {
		synchronized (toSend) {
			totalBytesToSend += m.getLength();
			//System.out.println("Total bytes to send:" + totalBytesToSend);
			toSend.add(0, m);
			// Ditto.
			if (toSend.size() == 1) toSend.notify();
		}

	}

	long counter = 0;

	@Override
	public void run() {

		sendInit();
		while (!this.isInterrupted()) {
			DisplayMessage m = null;
			synchronized (toSend) {
				if (!toSend.isEmpty()) {
					m = toSend.remove(0);
				} else {
					try {
						toSend.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			if (m != null) {
				try {
					m.send(client);
					totalBytesToSend -= m.getLength();
					counter += m.getLength();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

	}

	void sendInit() {
		final int screenWidth = client.getScreenWidth();
		final int screenHeight = client.getScreenHeight();
		synchronized (toSend) {
			{

				DisplayMessage mBound = new DisplayMessage(this);

				mBound.addOperation(new SetBoundsOperation(0, 0, screenWidth,
						screenHeight));
				addMessage(mBound);

			}

			{
				DisplayMessage mCursor = new DisplayMessage(this);
				mCursor.addOperation(new SetMouseCursorOperation(
						SetMouseCursorOperation.INVISIBLE_CURSOR));
				addMessage(mCursor);

			}

			final int stop = 2 * screenWidth / 3;
			for (int i = 0; i < stop; i += WIDTH) {
				DisplayMessage mClear = new DisplayMessage(this);

				mClear.addOperation(new FillOperation(screenWidth - WIDTH - i,
						0, WIDTH, screenHeight, Color.BLACK));
				mClear.addOperation(new FillOperation(i, 0, WIDTH,
						screenHeight, Color.BLACK));

				mClear.addOperation(new SleepOperation(4));
				// Don't let NACKs replay clearing the screen
				// or we end up refreshing over and over.
				mClear.addOperation(new FlushOperation());
				addMessage(mClear);
			}
		}
	}

	private void test() {

		{

			DisplayMessage mBound = new DisplayMessage(this);
			mBound.addOperation(new SetBoundsOperation(0, 0, 1280, 1024));
			addMessage(mBound);

		}

		{
			DisplayMessage mCursor = new DisplayMessage(this);
			mCursor.addOperation(new SetMouseCursorOperation(
					SetMouseCursorOperation.UNIX_CURSOR));
			addMessage(mCursor);

		}

		DisplayMessage mClear = new DisplayMessage(this);

		mClear.addOperation(new FillOperation(0, 0, 1280, 1024, Color.RED));
		addMessage(mClear);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out
				.println("=====================   init done ==========================");

		sendImage("test.png", 0, 0);
		sendImage("console.png", 212, 23);
		System.out
				.println("=====================   bitmap sent ========================");

	}

	public void sendImage(String fileName, int toX, int toY) {
		try {
			BufferedImage image = ImageIO.read(new File(fileName));
			encoder.encode(image, toX, toY, true /* flush */);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void sendImage(BufferedImage image, int toX, int toY) {
		sendImage(image, toX, toY, true /* flush */);
	}

	public void sendImage(BufferedImage image, int toX, int toY, boolean flush) {
		try {
			encoder.encode(image, toX, toY, flush);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("sendImage:" + image.getWidth() + "x"
					+ image.getHeight() + " to:" + toX + "," + toY);

			e.printStackTrace();
		}
	}

	public static Color getColorFrom(int c1) {
		if (c1 == NO_COLOR) {
			throw new IllegalArgumentException("Bad color");
		}
		int red1 = (c1 & 0x00ff0000) >> 16;
		int green1 = (c1 & 0x0000ff00) >> 8;
		int blue1 = c1 & 0x000000ff;
		return new Color(red1, green1, blue1);
	}

	public synchronized int getNextSeq() {
		seq++;
		return seq;
	}

	public synchronized int getNextOpcodeSeq(int i) {
		opSeq += i;
		return opSeq;
	}

	public synchronized int getCurrentOpcodeSeq() {
		return opSeq;
	}

	public int getMessageToSendCount() {
		return this.toSend.size();
	}

	public String getStats() {
		return encoder.getStats();
	}

	public void dumpOperationsToSend() {
		synchronized (toSend) {
			System.out.println(encoder.getStats());
			for (Iterator iterator = toSend.iterator(); iterator.hasNext();) {
				DisplayMessage type = (DisplayMessage) iterator.next();
				type.dump();
				// System.out.println(encoder.getStats());
			}
		}

	}
}
