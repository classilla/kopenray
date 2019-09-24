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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import net.percederberg.tetris.Game;
import org.jopenray.adapter.RDPAdapter;
import org.jopenray.adapter.RFBAdapter;
import org.jopenray.adapter.SshAdapter;
import org.jopenray.authentication.AuthenticationMessage;
import org.jopenray.operation.FillOperation;
import org.jopenray.operation.Operation;
import org.jopenray.operation.SetMouseCursorOperation;
import org.jopenray.server.session.Session;
import org.jopenray.util.Hex;
import org.jopenray.util.MessageImage;

public class ThinClient {
	private final Map<String, String> properties = new HashMap<String, String>();
	private final List<String> keys = new ArrayList<String>();
	private InetAddress server;
	private DisplayWriterThread tWriter;
	private DisplayReaderThread tReader;
	private DatagramSocket socket;
	private final PropertyChangeSupport s = new PropertyChangeSupport(this);
	private int byteSent;
	OperationHistory h = new OperationHistory();
	private boolean allowed;
	private String name;
	private Thread thread;
	private int screenWidth;
	private int screenHeight;
	private int nativeWidth = 0;
	private int nativeHeight = 0;

	public ThinClient(String id) {
		this("Unknown", id, 1280, 1024);

	}

	public ThinClient(String name, String id, int screenWidth, int screenHeight) {
		this.name = name;
		this.properties.put("sn", id);
		this.keys.add("sn");
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	public void updateStateFrom(AuthenticationMessage m) {
		m.exportProperties(this);
		s.firePropertyChange("all", null, null);
	}

	public void addPropertyChangeListeneer(PropertyChangeListener l) {
		s.addPropertyChangeListener(l);
	}

	public int getServerPort() {
		String port = properties.get("pn");
		if (port != null) {
			final int udpPort = Integer.valueOf(port);
			return udpPort;
		}
		return -1;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public void connectDisplay(Session session) throws SocketException {
		initConnection();
		System.out.println("Starting Session to: " + session.getAllInfo());

		switch (session.getProtocol()) {
		case Session.RDP:
			startRDP(session);
			break;
		case Session.RFB:
			startRFB(session);
			break;
		case Session.SSH:
			startSSH(session);
			break;
		case Session.TETPNC:
			startAlexey(session);
			break;
		default:
			startImage();
			break;
		}

		// startRFB();

		// tWriter.sendImage("console.png", 0, 1);

		// DatagramPacket dataRecieved = new DatagramPacket(new
		// byte[length],length);
		// socket.receive(dataRecieved);
		// System.out.println("Data recieved : " + new
		// String(dataRecieved.getData()));
		// System.out.println("From : " + dataRecieved.getAddress() + ":" +
		// dataRecieved.getPort());

	}

	public void initConnection() throws SocketException {
		socket = new DatagramSocket();
		// h.clear();

		// realIP usually is *not* the actual IP. Use remoteIP, which
		// we provide as the actual source address, especially for
		// Gobis which actually *lie* about their source address.

		try {
			String ip = properties.get("remoteIP");
			if (ip != null) {
				server = InetAddress.getByName(ip);
			} else {
				server = InetAddress.getByAddress(Hex.decodeHex(properties.get("realIP")));
			}
			System.out.println("connectDislay :ip:" + server.getHostAddress());
			if (tReader != null) {
				tReader.interrupt();
			}
			tReader = new DisplayReaderThread(this);
			tReader.start();
			if (tWriter == null) {
				tWriter = new DisplayWriterThread(this);
				tWriter.start();
			} else {
				tWriter.sendInit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startImage() {
		if (thread != null && !thread.isInterrupted()) {
			thread.interrupt();
		}

		thread = new Thread() {
			public void run() {
				ThinClient client = ThinClient.this;
				BufferedImage image;
				try {
					image = ImageIO.read(new File("Images/happy.jpg"));
					client.getWriter().sendImage(image, 0, 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

/*
				// Idle until interrupted ???
				Object o = new Object();
				synchronized (o) {
					try {
						o.wait();
					} catch (InterruptedException e) {
						this.interrupt();
					}
				}
*/
			}

			@Override
			public void interrupt() {
				super.interrupt();
			}
		};

		thread.start();

	}

	private void startAlexey(final Session session) {
		if (thread != null && !thread.isInterrupted()) {
			thread.interrupt();
		}

		thread = new Thread() {
			final Game game = new Game(ThinClient.this);

			public void run() {
				// GORBACHEV SINGS TRACTORS: TURNIP! BUTTOCKS!
				tWriter.sendImage(MessageImage.createImage(
					"Zdravstvujte tovarich",
				getNativeWidth(), getNativeHeight(),
				Color.BLUE, Color.WHITE), 0, 0);
				tReader.addInputListener(game);
				game.handleStart();
			}

			@Override
			public void interrupt() {
				tReader.removeInputListener(game);
				game.quit();
				super.interrupt();
			}
		};

		thread.start();
	}

	private void startSSH(final Session session) {
		if (thread != null && !thread.isInterrupted()) {
			thread.interrupt();
		}

		thread = new Thread() {
			SshAdapter t = new SshAdapter();

			public void run() {
				t = new SshAdapter();
				tReader.addInputListener(t);
				t.start(ThinClient.this, session);

			}

			@Override
			public void interrupt() {
				tReader.removeInputListener(t);
				t.stop();
				super.interrupt();
			}
		};

		thread.start();
	}

	private void startRFB(final Session session) {
		if (true) {
			JOptionPane.showMessageDialog(null,
					"RFB sessions are disabled in this release.");
			return;
		}

		if (thread != null && !thread.isInterrupted()) {

			thread.interrupt();
		}

		thread = new Thread() {
			RFBAdapter t = new RFBAdapter();

			public void run() {
				t = new RFBAdapter();
				tReader.addInputListener(t);
				t.start(ThinClient.this, session);

			}

			@Override
			public void interrupt() {
				tReader.removeInputListener(t);
				t.stop();
				super.interrupt();
			}
		};

		thread.start();
	}

	public void stop() {

	}

	private void startRDP(final Session session) {
		if (thread != null && !thread.isInterrupted()) {

			thread.interrupt();
		}
		thread = new Thread() {
			RDPAdapter t = new RDPAdapter();

			@Override
			public void run() {

				tReader.addInputListener(t);

				if (!session.isHardwareCursorUsed()) {
					DisplayMessage mCursor = new DisplayMessage(tWriter);
					mCursor.addOperation(new SetMouseCursorOperation(
							SetMouseCursorOperation.INVISIBLE_CURSOR));
					tWriter.addMessage(mCursor);

				}

				t.start(ThinClient.this, session);

			}

			@Override
			public void interrupt() {
				tReader.removeInputListener(t);

				t.stop();
				super.interrupt();
			}
		};
		thread.setName("RDP connection to " + session.getServer());
		thread.start();
	}

	public InetAddress getServer() {
		return server;
	}

	public String getName() {
		if (name == null) {
			name = this.properties.get("sn");
		}
		return this.name;

	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPropertyCount() {
		return this.properties.size();
	}

	public String getPropertyName(int index) {
		return this.keys.get(index);
	}

	public Object getPropertyValue(int index) {
		return this.properties.get(getPropertyName(index));
	}

	public void put(String key, String value) {
		if (!this.keys.contains(key)) {
			this.keys.add(key);
		}
		this.properties.put(key, value);
	}

	public int getMTU() {
		// TODO Auto-generated method stub
		return 1500;
	}

	public void addByteSent(int nb) {
		byteSent += nb;
		System.out.println("Byte sent:" + byteSent);
	}

	public void sendBytes(byte[] buffer, int bufferLength) throws IOException {
		DatagramPacket dataSent = new DatagramPacket(buffer, 0, bufferLength,
				getServer(), getServerPort());
		getSocket().send(dataSent);

	}

	public void addToHistory(Operation o) {
		this.h.add(o);

	}

	public void flushHistory() {
		this.h.clear();
	}

	/**
	 * Renvoi de from a to (compris) ex: 19,19 ou 19,50
	 */
	public void resend(int from, int to) {
		h.resend(this.tWriter, from, to);

	}

	public DisplayWriterThread getWriter() {
		return this.tWriter;
	}

	public DisplayReaderThread getReader() {
		return this.tReader;
	}

	public void clearScreen() {
		FillOperation f = new FillOperation(0, 0, getScreenWidth(),
				getScreenHeight(), Color.PINK);
		DisplayMessage m = new DisplayMessage(this.tWriter);
		m.addOperation(f);
		//m.addOperation(new FlushOperation());
		tWriter.addMessage(m);

	}

	public void setAllowed(boolean b) {
		this.allowed = true;

	}

	public boolean isAllowed() {

		return this.allowed;
	}

	public String getSerialNumber() {
		return this.properties.get("sn");
	}

	public String getCardId() {
		return this.properties.get("id");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ThinClient) {
			ThinClient c = (ThinClient) obj;
			return this.getSerialNumber().equals(c.getSerialNumber());
		}
		return super.equals(obj);
	}

	public String getStats() {
		return tWriter.getStats();
	}

	public int getMessageToSendCount() {
		if (tWriter == null) {
			return -1;
		}
		final int messageToSendCount = tWriter.getMessageToSendCount();

		return messageToSendCount;
	}

	public String getPropertyByName(String name) {
		return this.properties.get(name);
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	private void setNativeScreenDimensions() {
		if (nativeWidth > 0 && nativeHeight > 0) return;

		// Attempt to parse startRes.
		// This could be simply wxh or wxh:wxh, in which case
		// we try only the first.
		String prop = properties.get("startRes");
		if (prop == null || prop.length() < 3 ||
				prop.indexOf("x") == -1) {
			System.out.println("invalid startRes property");
			return;
		}
		if (prop.indexOf(":") != -1) {
			prop = prop.substring(0, prop.indexOf(":"));
			if (prop == null || prop.length() < 3 ||
					prop.indexOf("x") == -1) {
				System.out.println("invalid startRes: property");
				return;
			}
		}
		try {
			nativeWidth = Integer.parseInt(prop.substring(0, prop.indexOf("x")));
			nativeHeight = Integer.parseInt(prop.substring(prop.indexOf("x")+1));
			System.out.println("native screen: "+nativeWidth+" x "+nativeHeight);
		} catch(NumberFormatException e) {
			System.out.println("setNativeScreenDimensions() failed "+e+" '"+prop+"'");
			nativeWidth = 0;
			nativeHeight = 0; // try again later
		}
	}

	public int getNativeWidth() {
		setNativeScreenDimensions();
		return (nativeWidth > 0) ? nativeWidth : screenWidth;
	}

	public int getNativeHeight() {
		setNativeScreenDimensions();
		return (nativeHeight > 0) ? nativeHeight : screenHeight;
	}
}
