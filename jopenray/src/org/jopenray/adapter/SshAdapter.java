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

package org.jopenray.adapter;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

import org.jopenray.operation.CopyOperation;
import org.jopenray.server.session.Session;
import org.jopenray.server.thinclient.DisplayMessage;
import org.jopenray.server.thinclient.InputListener;
import org.jopenray.server.thinclient.ThinClient;
import org.jopenray.util.VeraFont;

import com.jcraft.jcterm.Connection;
import com.jcraft.jcterm.Emulator;
import com.jcraft.jcterm.EmulatorVT100;
import com.jcraft.jcterm.JSchSession;
import com.jcraft.jcterm.Term;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.UserInfo;

public class SshAdapter implements InputListener, Term {
	private OutputStream out;
	private InputStream in;
	private Emulator emulator;
	private Connection connection;
	private BufferedImage img;
	private BufferedImage background;
	private Graphics2D cursor_graphics;
	private Graphics2D graphics;
	private Color defaultbground = Color.black;
	private Color defaultfground = Color.white;
	private Color bground = Color.black;
	private Color fground = Color.white;
	private Font font;
	private boolean bold = false;
	private boolean underline = false;
	private boolean reverse = false;
	private int term_width = 80;
	private int term_height = 24;
	private int descent = 0;
	private int x = 0;
	private int y = 0;
	private int char_width;
	private int char_height;
	private int line_space = -2;
	private boolean antialiasing = true;
	private Thread cleanup;
	private boolean skip_cleanup = false;

	private final Object[] colors = { Color.black, new Color(189, 105, 130),
			new Color(61, 241, 121), Color.yellow, new Color(105, 130, 189),
			Color.magenta, Color.cyan, new Color(240, 240, 240) };
	private ThinClient client;

	public SshAdapter() {

		configure();

		background = new BufferedImage(char_width, char_height,
				BufferedImage.TYPE_INT_RGB);
		{
			Graphics2D foog = (Graphics2D) (background.getGraphics());
			foog.setColor(getBackGround());
			foog.fillRect(0, 0, char_width, char_height);
			foog.dispose();
		}

	}

	private void configure() {

		font = VeraFont.getMonoFont();
		if (font == null) {
			font = new Font("serif", Font.PLAIN, 14);
		}
		font = font.deriveFont(14f);

		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = (Graphics2D) (img.getGraphics());
		graphics.setFont(font);
		{
			FontMetrics fo = graphics.getFontMetrics();
			descent = fo.getDescent();

			char_width = (int) (fo.charWidth((char) '@'));
			char_height = (int) (fo.getHeight()) + (line_space * 2) + 1;
			descent += line_space;
		}
		img.flush();
		graphics.dispose();
	}

	public void setSize(int w, int h) {

		BufferedImage imgOrg = img;
		if (graphics != null)
			graphics.dispose();

		int column = w / getCharWidth();
		int row = h / getCharHeight();
		term_width = column;
		term_height = row;

		if (emulator != null)
			emulator.reset();

		img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		// Init cleanup thread. For whatever reason we don't currently
		// manage the screen well incrementally, so every couple of
		// seconds force a full repaint if there was any interval
		// partial update. This also serves to flush the Operations
		// history and prevent a lot of backtracking.
		//
		if (cleanup != null && !cleanup.isInterrupted())
			cleanup.interrupt();
		cleanup = new Thread() {
			public void run() {
				SshAdapter adapter = SshAdapter.this;
				for(;;) {
					try {
						// Not a magic number; just
						// seems to be a good
						// compromise.
						Thread.sleep(2000);
					} catch(InterruptedException e) {
						//
					}
					if (img == null) {
						this.interrupt();
					} else if (!skip_cleanup) {
						System.out.println("Ssh_cleanup");
						adapter.repaintall();
					}
				}
			}

			@Override
			public void interrupt() {
				super.interrupt();
			}
		};

		graphics = (Graphics2D) (img.getGraphics());
		graphics.setFont(font);

		clearArea(0, 0, w, h);

		if (imgOrg != null) {
			Shape clip = graphics.getClip();
			graphics.setClip(0, 0, getTermWidth(), getTermHeight());
			graphics.drawImage(imgOrg, 0, 0, null);
			graphics.setClip(clip);
		}

		if (cursor_graphics != null)
			cursor_graphics.dispose();

		cursor_graphics = (Graphics2D) (img.getGraphics());
		cursor_graphics.setColor(getForeGround());
		cursor_graphics.setXORMode(getBackGround());

		setAntiAliasing(antialiasing);

		if (connection != null) {
			connection.requestResize(this);
		}

		if (imgOrg != null) {
			imgOrg.flush();
			imgOrg = null;
		}

		skip_cleanup = false;
		cleanup.start();
	}

	private void repaintall() {
		this.client.getWriter().sendImage(img, 0, 0, true /* flush */);
		skip_cleanup = true;
	}

	private void repaint(int x, int y, int width, int height) {
		if (x < 0 || y < 0)
			return;

		if (img != null) {
			skip_cleanup = false;
			if (y + height > img.getHeight()) {
				height = img.getHeight() - y;
			}
			if (x + width > img.getWidth()) {
				width = img.getWidth() - x;
			}
			System.err.println("SshAdapter.repaint() " + x + "," + y + " "
					+ width + "x" + height);

			// Thread.dumpStack();
			try {
				BufferedImage image = img.getSubimage(x, y, width, height);

				this.client.getWriter().sendImage(image, x, y, false);
			} catch (Exception e) {
				System.err.println("SshAdapter.repaint() " + x + "," + y + " "
						+ width + "x" + height);
				// TODO: handle exception
				e.printStackTrace();
			}
		} else {
			System.out.println("SshAdapter.repaint() null img");
		}
	}

	byte[] obuffer = new byte[3];

	public void keyTyped(KeyEvent e) {

	}

	public int getTermWidth() {
		return char_width * term_width;
	}

	public int getTermHeight() {
		return char_height * term_height;
	}

	public int getCharWidth() {
		return char_width;
	}

	public int getCharHeight() {
		return char_height;
	}

	public int getColumnCount() {
		return term_width;
	}

	public int getRowCount() {
		return term_height;
	}

	public void clear() {
		graphics.setColor(getBackGround());
		graphics.fillRect(0, 0, char_width * term_width, char_height
				* term_height);
		graphics.setColor(getForeGround());
	}

	public void setCursor(int x, int y) {
		// System.out.println("setCursor: "+x+","+y);
		this.x = x;
		this.y = y;
	}

	public void drawCursor() {
		cursor_graphics.fillRect(x, y - char_height, char_width, char_height);
		repaint(x, y - char_height, char_width, char_height);
	}

	public void redraw(int x, int y, int width, int height) {
		repaint(x, y, width, height);
	}

	public void clearArea(int x1, int y1, int x2, int y2) {
		// System.out.println("clear_area: "+x1+" "+y1+" "+x2+" "+y2);
		graphics.setColor(getBackGround());
		graphics.fillRect(x1, y1, x2 - x1, y2 - y1);
		graphics.setColor(getForeGround());
	}

	public void scrollArea(int x, int y, int w, int h, int dx, int dy) {
		System.err.println("scroll_area: " + x + " " + y + " " + w + " " + h
				+ " " + dx + " " + dy);
		graphics.copyArea(x, y, w, h, dx, dy);
		// repaint(x + dx, y + dy, w, h);
		DisplayMessage m = new DisplayMessage(this.client.getWriter());
		m.addOperation(new CopyOperation(x + dx, y + dy, w, h, x, y));
		this.client.getWriter().addMessage(m);
		if (dy > 0) {
			repaint(x, y, w, dy);
			repaint(x, y + dy, dx, h - dy);
		} else {
			repaint(x, y, -dy, h + dy);
			repaint(x, h + dy, w, -dy);
		}
		System.err.println("scroll_area: " + x + " " + y + " " + w + " " + h
				+ " " + dx + " " + dy + " ok");

	}

	public void drawBytes(byte[] buf, int s, int len, int x, int y) {
		// clear_area(x, y, x+len*char_width, y+char_height);
		// graphics.setColor(getForeGround());

		// System.out.println("drawString: "+x+","+y+" "+len+" "+new String(buf,
		// s, len));

		graphics.drawBytes(buf, s, len, x, y - descent);
		if (bold)
			graphics.drawBytes(buf, s, len, x + 1, y - descent);

		if (underline) {
			graphics.drawLine(x, y - 1, x + len * char_width, y - 1);
		}
	}

	public void drawString(String str, int x, int y) {
		byte[] bb = str.getBytes();
		drawBytes(bb, 0, bb.length, x, y);
	}

	public void beep() {
	}

	public void setLineSpace(int foo) {
		this.line_space = foo;
	}

	public boolean getAntiAliasing() {
		return antialiasing;
	}

	public void setAntiAliasing(boolean foo) {
		if (graphics == null)
			return;
		antialiasing = foo;
		Object mode = foo ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON
				: RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
		RenderingHints hints = new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING, mode);
		graphics.setRenderingHints(hints);
	}

	private Color toColor(Object o) {
		if (o instanceof String) {
			return java.awt.Color.getColor((String) o);
		}
		if (o instanceof java.awt.Color) {
			return (java.awt.Color) o;
		}
		return Color.white;
	}

	public void setDefaultForeGround(Object f) {
		defaultfground = toColor(f);
	}

	public void setDefaultBackGround(Object f) {
		defaultbground = toColor(f);
	}

	public void setForeGround(Object f) {
		fground = toColor(f);
		graphics.setColor(getForeGround());
	}

	public void setBackGround(Object b) {
		bground = toColor(b);
		Graphics2D foog = (Graphics2D) (background.getGraphics());
		foog.setColor(getBackGround());
		foog.fillRect(0, 0, char_width, char_height);
		foog.dispose();
	}

	private Color getForeGround() {
		if (reverse)
			return bground;
		return fground;
	}

	private Color getBackGround() {
		if (reverse)
			return fground;
		return bground;
	}

	public Object getColor(int index) {
		if (colors == null || index < 0 || colors.length <= index)
			return null;
		return colors[index];
	}

	public void setBold() {
		bold = true;
	}

	public void setUnderline() {
		underline = true;
	}

	public void setReverse() {
		reverse = true;
		if (graphics != null)
			graphics.setColor(getForeGround());
	}

	public void resetAllAttributes() {
		bold = false;
		underline = false;
		reverse = false;
		bground = defaultbground;
		fground = defaultfground;
		if (graphics != null) {
			graphics.setColor(getForeGround());
		}
	}

	@Override
	public void keyPressed(int key, boolean shift, boolean ctrl, boolean alt,
			boolean meta, boolean altGr) {

		byte[] code = null;
		switch (key) {
		case KeyEvent.VK_CONTROL:
		case KeyEvent.VK_SHIFT:
		case KeyEvent.VK_ALT:
		case KeyEvent.VK_CAPS_LOCK:
			return;
		case KeyEvent.VK_ENTER:
			code = emulator.getCodeENTER();
			break;
		case KeyEvent.VK_UP:
			code = emulator.getCodeUP();
			break;
		case KeyEvent.VK_DOWN:
			code = emulator.getCodeDOWN();
			break;
		case KeyEvent.VK_RIGHT:
			code = emulator.getCodeRIGHT();
			break;
		case KeyEvent.VK_LEFT:
			code = emulator.getCodeLEFT();
			break;
		case KeyEvent.VK_F1:
			code = emulator.getCodeF1();
			break;
		case KeyEvent.VK_F2:
			code = emulator.getCodeF2();
			break;
		case KeyEvent.VK_F3:
			code = emulator.getCodeF3();
			break;
		case KeyEvent.VK_F4:
			code = emulator.getCodeF4();
			break;
		case KeyEvent.VK_F5:
			code = emulator.getCodeF5();
			break;
		case KeyEvent.VK_F6:
			code = emulator.getCodeF6();
			break;
		case KeyEvent.VK_F7:
			code = emulator.getCodeF7();
			break;
		case KeyEvent.VK_F8:
			code = emulator.getCodeF8();
			break;
		case KeyEvent.VK_F9:
			code = emulator.getCodeF9();
			break;
		case KeyEvent.VK_F10:
			code = emulator.getCodeF10();
			break;
		case KeyEvent.VK_TAB:
			code = emulator.getCodeTAB();
			break;
		}
		if (code != null) {
			try {
				out.write(code, 0, code.length);
				out.flush();
			} catch (Exception ee) {
			}
			return;
		}

		char keychar = getCharFromKeyCode(key, shift, ctrl, alt, meta);
		System.out.println("Keypressed keychar:" + (int) keychar + " '"
				+ keychar + "' from keycode:" + key);
		if ((keychar & 0xff00) == 0) {
			System.out.println("Sending keychar:" + keychar + " from keycode:"
					+ key);
			obuffer[0] = (byte) keychar;
			try {
				out.write(obuffer, 0, 1);
				out.flush();
			} catch (Exception ee) {
			}
		}

	}

	private char getCharFromKeyCode(int key, boolean shift, boolean ctrl,
			boolean alt, boolean meta) {
		char c = (char) key;
		if (!shift) {
			if (key >= KeyEvent.VK_A && key <= KeyEvent.VK_Z) {
				// A -> a
				c += 32;
			}
		}
		return c;
	}

	@Override
	public void keyReleased(int key) {
		char keychar = getCharFromKeyCode(key, false, false, false, false);
		if ((keychar & 0xff00) != 0) {
			char[] foo = new char[1];
			foo[0] = keychar;
			try {
				byte[] goo = new String(foo).getBytes("EUC-JP");
				out.write(goo, 0, goo.length);
				out.flush();
			} catch (Exception eee) {
			}
		}

	}

	@Override
	public void mouseMoved(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(int button, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(int button, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelDown(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelUp(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	JSchSession jschsession;
	Proxy proxy = null;

	public void start(ThinClient thinClient, Session session) {
		this.client = thinClient;
		int port = 22;

		try {
			UserInfo ui = new UserInfo() {

				@Override
				public String getPassphrase() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getPassword() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean promptPassphrase(String message) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean promptPassword(String message) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean promptYesNo(String message) {

					return true;
				}

				@Override
				public void showMessage(String message) {
					// TODO Auto-generated method stub

				}
			};

			jschsession = JSchSession.getSession(session.getLogin(), session
					.getPassword(), session.getServer(), port, ui, proxy);
			java.util.Properties config = new java.util.Properties();

			config.put("compression.s2c", "none");
			config.put("compression.c2s", "none");

			jschsession.getSession().setConfig(config);
			jschsession.getSession().rekey();

			Channel channel = null;
			OutputStream out = null;
			InputStream in = null;

			channel = jschsession.getSession().openChannel("shell");

			out = channel.getOutputStream();
			in = channel.getInputStream();

			channel.connect();

			final OutputStream fout = out;
			final InputStream fin = in;
			final Channel fchannel = channel;

			connection = new Connection() {
				public InputStream getInputStream() {
					return fin;
				}

				public OutputStream getOutputStream() {
					return fout;
				}

				public void requestResize(Term term) {
					if (fchannel instanceof ChannelShell) {
						int c = term.getColumnCount();
						int r = term.getRowCount();
						((ChannelShell) fchannel)
								.setPtySize(c, r, c * term.getCharWidth(), r
										* term.getCharHeight());
					}
				}

				public void close() {
					fchannel.disconnect();
				}
			};
			setSize(thinClient.getScreenWidth(), thinClient.getScreenHeight());
			start(connection);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public void start(Connection connection) {
		this.connection = connection;
		in = connection.getInputStream();
		out = connection.getOutputStream();
		emulator = new EmulatorVT100(this, in);
		emulator.reset();
		emulator.start();

		clear();
		redraw(0, 0, getTermWidth(), getTermHeight());
	}

	public void stop() {
		this.connection.close();

	}
}
