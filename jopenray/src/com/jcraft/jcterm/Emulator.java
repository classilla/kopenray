/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2007 ymnk, JCraft,Inc.
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

package com.jcraft.jcterm;

import java.io.IOException;
import java.io.InputStream;

public abstract class Emulator {
	Term term = null;
	InputStream in = null;

	public Emulator(Term term, InputStream in) {
		this.term = term;
		this.in = in;
	}

	public abstract void start();

	public abstract byte[] getCodeENTER();

	public abstract byte[] getCodeUP();

	public abstract byte[] getCodeDOWN();

	public abstract byte[] getCodeRIGHT();

	public abstract byte[] getCodeLEFT();

	public abstract byte[] getCodeF1();

	public abstract byte[] getCodeF2();

	public abstract byte[] getCodeF3();

	public abstract byte[] getCodeF4();

	public abstract byte[] getCodeF5();

	public abstract byte[] getCodeF6();

	public abstract byte[] getCodeF7();

	public abstract byte[] getCodeF8();

	public abstract byte[] getCodeF9();

	public abstract byte[] getCodeF10();

	public abstract byte[] getCodeTAB();

	public void reset() {
		termWidth = term.getColumnCount();
		termHeight = term.getRowCount();
		charWidth = term.getCharWidth();
		charHeight = term.getCharHeight();
		region_y1 = 1;
		region_y2 = termHeight;
	}

	byte[] buf = new byte[1024];
	int bufs = 0;
	int buflen = 0;

	byte getChar() throws java.io.IOException {
		if (buflen == 0) {
			fillBuf();
		}
		buflen--;

		// System.out.println("getChar: "+new
		// Character((char)buf[bufs])+"["+Integer.toHexString(buf[bufs]&0xff)+"]");

		return buf[bufs++];
	}

	void fillBuf() throws java.io.IOException {
		buflen = bufs = 0;
		buflen = in.read(buf, bufs, buf.length - bufs);
		/*
		 * System.out.println("fillBuf: "); for(int i=0; i<buflen; i++){ byte
		 * b=buf[i]; System.out.print(new
		 * Character((char)b)+"["+Integer.toHexString(b&0xff)+"], "); }
		 * System.out.println("");
		 */
		if (buflen <= 0) {
			buflen = 0;
			throw new IOException("fillBuf");
		}
	}

	void pushChar(byte foo) throws java.io.IOException {
		// System.out.println("pushChar: "+new
		// Character((char)foo)+"["+Integer.toHexString(foo&0xff)+"]");
		buflen++;
		buf[--bufs] = foo;
	}

	int getASCII(int len) throws java.io.IOException {
		// System.out.println("bufs="+bufs+", buflen="+buflen+", len="+len);
		if (buflen == 0) {
			fillBuf();
		}
		if (len > buflen)
			len = buflen;
		int foo = len;
		byte tmp;
		while (len > 0) {
			tmp = buf[bufs++];
			if (0x20 <= tmp && tmp <= 0x7f) {
				buflen--;
				len--;
				continue;
			}
			bufs--;
			break;
		}
		// System.out.println(" return "+(foo-len));
		return foo - len;
	}

	protected int termWidth = 80;
	protected int termHeight = 24;

	protected int x = 0;
	protected int y = 0;

	protected int charWidth;
	protected int charHeight;

	private int region_y2;
	private int region_y1;

	protected int tab = 8;

	// Reverse scroll
	protected void scrollReverse() {
		term.drawCursor();
		term.scrollArea(0, (region_y1 - 1) * charHeight, termWidth * charWidth,
				(region_y2 - region_y1) * charHeight, 0, charHeight);
		term.clearArea(x, y - charHeight, termWidth * charWidth, y);
		term.redraw(0, 0, termWidth * charWidth, termHeight * charHeight
				- charHeight);

		term.drawCursor();
	}

	// Normal scroll one line
	protected void scrollForward() {
		term.drawCursor();
		term.scrollArea(0, (region_y1 - 1) * charHeight, termWidth * charWidth,
				(region_y2 - region_y1 + 1) * charHeight, 0, -charHeight);
		term.clearArea(0, region_y2 * charHeight - charHeight, termWidth
				* charWidth, region_y2 * charHeight);
		term.redraw(0, (region_y1 - 1) * charHeight, termWidth * charWidth,
				(region_y2 - region_y1 + 1) * charHeight);
		term.drawCursor();
	}

	// Save cursor position
	protected void saveCursor() {

	}

	// Enable alternate character set
	protected void ena_acs() {
	}

	protected void exit_alt_charset_mode() {
	}

	protected void enter_alt_charset_mode() {
	}

	protected void reset_2string() {
	}

	protected void exit_attribute_mode() {
		term.resetAllAttributes();
	}

	protected void exit_standout_mode() {
		term.resetAllAttributes();
	}

	protected void exit_underline_mode() {

	}

	protected void enter_bold_mode() {
		term.setBold();
	}

	protected void enter_underline_mode() {
		term.setUnderline();
	}

	protected void enter_reverse_mode() {
		term.setReverse();
	}

	protected void change_scroll_region(int y1, int y2) {
		region_y1 = y1;
		region_y2 = y2;
	}

	protected void cursor_address(int r, int c) {
		term.drawCursor();
		x = (c - 1) * charWidth;
		y = r * charHeight;
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void parm_down_cursor(int lines) {
		term.drawCursor();
		y += (lines) * charHeight;
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void parm_left_cursor(int chars) {
		term.drawCursor();
		x -= (chars) * charWidth;
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void parm_right_cursor(int chars) {
		term.drawCursor();
		x += (chars) * charWidth;
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void clr_eol() {
		term.drawCursor();
		term.clearArea(x, y - charHeight, termWidth * charWidth, y);
		term.redraw(x, y - charHeight, (termWidth) * charWidth - x, charHeight);
		term.drawCursor();
	}

	protected void clr_bol() {
		term.drawCursor();
		term.clearArea(0, y - charHeight, x, y);
		term.redraw(0, y - charHeight, x, charHeight);
		term.drawCursor();
	}

	protected void clr_eos() {
		term.drawCursor();
		term.clearArea(x, y - charHeight, termWidth * charWidth, termHeight
				* charHeight);
		term.redraw(x, y - charHeight, termWidth * charWidth - x, termHeight
				* charHeight - y + charHeight);
		term.drawCursor();
	}

	protected void parm_up_cursor(int lines) {
		term.drawCursor();
		// x=0;
		// y-=char_height;
		y -= (lines) * charHeight;
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void bell() {
		term.beep();
	}

	protected void tab() {
		term.drawCursor();
		x = (((x / charWidth) / tab + 1) * tab * charWidth);
		if (x >= termWidth * charWidth) {
			x = 0;
			y += charHeight;
		}
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void carriage_return() {
		term.drawCursor();
		x = 0;
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void cursor_left() {
		term.drawCursor();
		x -= charWidth;
		if (x < 0) {
			y -= charHeight;
			x = termWidth * charWidth - charWidth;
		}
		term.setCursor(x, y);
		term.drawCursor();
	}

	protected void cursor_down() {
		term.drawCursor();
		y += charHeight;
		term.setCursor(x, y);
		term.drawCursor();

		check_region();
	}

	private byte[] b2 = new byte[2];
	private byte[] b1 = new byte[1];

	protected void draw_text() throws java.io.IOException {

		int rx;
		int ry;
		int w;
		int h;

		check_region();

		rx = x;
		ry = y;

		byte b = getChar();
		term.drawCursor();
		// System.out.print(new
		// Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
		if ((b & 0x80) != 0) {
			term.clearArea(x, y - charHeight, x + charWidth * 2, y);
			b2[0] = b;
			b2[1] = getChar();
			term.drawString(new String(b2, 0, 2, "EUC-JP"), x, y);
			x += charWidth;
			x += charWidth;
			w = charWidth * 2;
			h = charHeight;
		} else {
			pushChar(b);
			int foo = getASCII(termWidth - (x / charWidth));
			if (foo != 0) {
				// System.out.println("foo="+foo+" "+x+", "+(y-char_height)+" "+(x+foo*char_width)+" "+y+" "+buf+" "+bufs+" "+b+" "+buf[bufs-foo]);
				// System.out.println("foo="+foo+" ["+new String(buf, bufs-foo,
				// foo));
				term.clearArea(x, y - charHeight, x + foo * charWidth, y);
				term.drawBytes(buf, bufs - foo, foo, x, y);
			} else {
				foo = 1;
				term.clearArea(x, y - charHeight, x + foo * charWidth, y);
				b1[0] = getChar();
				term.drawBytes(b1, 0, foo, x, y);
				// System.out.print("["+Integer.toHexString(bar[0]&0xff)+"]");
			}
			x += (charWidth * foo);
			w = charWidth * foo;
			h = charHeight;
		}
		term.redraw(rx, ry - charHeight, w, h);
		term.setCursor(x, y);
		term.drawCursor();
	}

	private void check_region() {

		if (x >= termWidth * charWidth) {
			// System.out.println("!! "+new
			// Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
			x = 0;
			y += charHeight;
			// System.out.println("@1: ry="+ry);
		}

		if (y > region_y2 * charHeight) {
			while (y > region_y2 * charHeight) {
				y -= charHeight;
			}
			term.drawCursor();
			term.scrollArea(0, region_y1 * charHeight, termWidth * charWidth,
					(region_y2 - region_y1) * charHeight, 0, -charHeight);
			term.clearArea(0, y - charHeight, termWidth * charWidth, y);
			System.err.println("Emulator.check_region():" + region_y2 + " /"
					+ this.termHeight);
			term.redraw(0, (region_y2 - 1) * charHeight, termWidth * charWidth,
					charHeight);
			term.setCursor(x, y);
			term.drawCursor();
		}
	}
}
