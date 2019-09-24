/* RdesktopCanvas.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.8 $
 * Author: $Author: telliott $
 * Date: $Date: 2005/09/27 14:15:39 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Canvas component, handles drawing requests from server,
 *          and passes user input to Input class.
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 * 
 */
package org.jopenray.rdp;

import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import org.apache.log4j.Logger;
import org.jopenray.rdp.keymapping.KeyCode;
import org.jopenray.rdp.keymapping.KeyCodeFileBased;
import org.jopenray.rdp.orders.BoundsOrder;
import org.jopenray.rdp.orders.Brush;
import org.jopenray.rdp.orders.DestBltOrder;
import org.jopenray.rdp.orders.LineOrder;
import org.jopenray.rdp.orders.MemBltOrder;
import org.jopenray.rdp.orders.PatBltOrder;
import org.jopenray.rdp.orders.PolyLineOrder;
import org.jopenray.rdp.orders.RectangleOrder;
import org.jopenray.rdp.orders.ScreenBltOrder;
import org.jopenray.rdp.orders.TriBltOrder;

// import org.apache.log4j.NDC;

public abstract class RDPCanvas extends Canvas {
	static Logger logger = Logger.getLogger(RDPCanvas.class);

	private RasterOp rop = null;

	protected WrappedImage backstore;

	// Graphics backstore_graphics;

	private Cursor previous_cursor = null; // for setBusyCursor and
	// unsetBusyCursor

	private Input input = null;

	public static final int ROP2_COPY = 0xc;

	private static final int ROP2_XOR = 0x6;

	private static final int ROP2_AND = 0x8;

	private static final int ROP2_NXOR = 0x9;

	private static final int ROP2_OR = 0xe;

	private static final int MIX_TRANSPARENT = 0;

	private static final int MIX_OPAQUE = 1;

	private static final int TEXT2_VERTICAL = 0x04;

	private static final int TEXT2_IMPLICIT_X = 0x20;

	public KeyCode keys = null;

	public KeyCodeFileBased fbKeys = null;

	public String sKeys = null;

	public int width = 0;

	public int height = 0;

	// private int[] colors = null; // needed for integer backstore

	protected IndexColorModel colormap = null;

	private Cache cache = null;

	public Rdp rdp = null;

	// protected int[] backstore_int = null;
	// Clip region
	protected int top = 0;

	protected int left = 0;

	protected int right = 0;

	protected int bottom = 0;

	public RDPCanvas() {
		super();
	}

	protected void init(int width, int height) {
		rop = new RasterOp();
		this.width = width;
		this.height = height;
		this.right = width - 1;
		this.bottom = height - 1;
		setSize(width, height);

		backstore = new WrappedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	public void paint(Graphics g) {
		update(g);

	}

	public abstract void update(Graphics g);

	/**
	 * Register a colour palette with this canvas
	 * 
	 * @param cm
	 *            Colour model to be used with this canvas
	 */
	public void registerPalette(IndexColorModel cm) {
		this.colormap = cm;

		backstore.setIndexColorModel(cm);
	}

	/**
	 * Register the Rdp layer to act as the communications interface to this
	 * canvas
	 * 
	 * @param rdp
	 *            Rdp object controlling Rdp layer communication
	 */
	public void registerCommLayer(Rdp rdp) {
		this.rdp = rdp;
		if (fbKeys != null)
			input = new Input(this, rdp, fbKeys);

	}

	/**
	 * Register keymap
	 * 
	 * @param keys
	 *            Keymapping object for use in handling keyboard events
	 */
	public void registerKeyboard(KeyCodeFileBased keys) {
		this.fbKeys = keys;
		if (rdp != null) {
			// rdp and keys have been registered...
			input = new Input(this, rdp, keys);
		}
	}

	/**
	 * Set cache for this session
	 * 
	 * @param cache
	 *            Cache to be used in this session
	 */
	public void registerCache(Cache cache) {
		this.cache = cache;
	}

	/**
	 * Display a compressed bitmap direct to the backstore NOTE: Currently not
	 * functioning correctly, see Bitmap.decompressImgDirect Does not call
	 * repaint. Image is drawn to canvas on next update
	 * 
	 * @param x
	 *            x coordinate within backstore for drawing of bitmap
	 * @param y
	 *            y coordinate within backstore for drawing of bitmap
	 * @param width
	 *            Width of bitmap
	 * @param height
	 *            Height of bitmap
	 * @param size
	 *            Size (bytes) of compressed bitmap data
	 * @param data
	 *            Packet containing compressed bitmap data at current read
	 *            position
	 * @param Bpp
	 *            Bytes-per-pixel for bitmap
	 * @param cm
	 *            Colour model currently in use, if any
	 * @throws RdesktopException
	 */
	public void displayCompressed(int x, int y, int width, int height,
			int size, RdpPacket_Localised data, int Bpp, IndexColorModel cm)
			throws RdesktopException {
		backstore = Bitmap.decompressImgDirect(width, height, size, data, Bpp,
				cm, x, y, backstore);
	}

	/**
	 * Draw an image object to the backstore, does not call repaint. Image is
	 * drawn to canvas on next update.
	 * 
	 * @param img
	 *            Image to draw to backstore
	 * @param x
	 *            x coordinate for drawing location
	 * @param y
	 *            y coordinate for drawing location
	 * @throws RdesktopException
	 */
	public void displayImage(Image img, int x, int y) throws RdesktopException {

		Graphics g = backstore.getGraphics();
		g.drawImage(img, x, y, null);
		/* Useful test for identifying image boundaries */
		// g.setColor(Color.RED);
		// g.drawRect(x,y,data.getWidth(null),data.getHeight(null));
		g.dispose();

	}

	/**
	 * Draw an image (from an integer array of colour data) to the backstore,
	 * does not call repaint. Image is drawn to canvas on next update.
	 * 
	 * @param data
	 *            Integer array of pixel colour information
	 * @param w
	 *            Width of image
	 * @param h
	 *            Height of image
	 * @param x
	 *            x coordinate for drawing location
	 * @param y
	 *            y coordinate for drawing location
	 * @param cx
	 *            Width of drawn image (clips, does not scale)
	 * @param cy
	 *            Height of drawn image (clips, does not scale)
	 * @throws RdesktopException
	 */
	public void displayImage(int[] data, int w, int h, int x, int y, int cx,
			int cy) throws RdesktopException {

		backstore.setRGBNoConversion(x, y, cx, cy, data, 0, w);

		// repaint(x, y, cx, cy);

		/* Useful test for identifying image boundaries */
		// Graphics g = backstore.getGraphics();
		// g.drawImage(data,x,y,null);
		// g.setColor(Color.RED);
		// g.drawRect(x,y,cx,cy);
		// g.dispose();
	}

	/**
	 * Retrieve an image from the backstore, as integer pixel information
	 * 
	 * @param x
	 *            x coordinate of image to retrieve
	 * @param y
	 *            y coordinage of image to retrieve
	 * @param cx
	 *            width of image to retrieve
	 * @param cy
	 *            height of image to retrieve
	 * @return Requested area of backstore, as an array of integer pixel colours
	 */
	public int[] getImage(int x, int y, int cx, int cy) {

		int[] data = new int[cx * cy];

		data = backstore.getRGB(x, y, cx, cy, null, // no existing image data to
				// add to
				0, // retrieving as complete image, no offset needed
				cx);

		return data;
	}

	/**
	 * Draw an image (from an integer array of colour data) to the backstore,
	 * also calls repaint to draw image to canvas
	 * 
	 * @param x
	 *            x coordinate at which to draw image
	 * @param y
	 *            y coordinate at which to draw image
	 * @param cx
	 *            Width of drawn image (clips, does not scale)
	 * @param cy
	 *            Height of drawn image (clips, does not scale)
	 * @param data
	 *            Image to draw, represented as an array of integer pixel
	 *            colours
	 */
	public void putImage(int x, int y, int cx, int cy, int[] data) {

		backstore.setRGBNoConversion(x, y, cx, cy, data, 0, // drawing entire
				// image, no
				// offset needed
				cx);

		this.repaint(x, y, cx, cy);
	}

	/**
	 * Reset clipping boundaries for canvas
	 */
	public void resetClip() {
		Graphics g = this.getGraphics();
		Rectangle bounds = this.getBounds();
		g.setClip(bounds.x, bounds.y, bounds.width, bounds.height);
		this.top = 0;
		this.left = 0;
		this.right = this.width - 1; // changed
		this.bottom = this.height - 1; // changed
	}

	/**
	 * Set clipping boundaries for canvas, based on a bounds order
	 * 
	 * @param bounds
	 *            Order defining new boundaries
	 */
	public void setClip(BoundsOrder bounds) {
		Graphics g = this.getGraphics();
		g.setClip(bounds.getLeft(), bounds.getTop(), bounds.getRight()
				- bounds.getLeft(), bounds.getBottom() - bounds.getTop());
		this.top = bounds.getTop();
		this.left = bounds.getLeft();
		this.right = bounds.getRight();
		this.bottom = bounds.getBottom();
	}

	/**
	 * Move the mouse pointer (only available in Java 1.3+)
	 * 
	 * @param x
	 *            x coordinate for mouse move
	 * @param y
	 *            y coordinate for mouse move
	 */
	public void movePointer(int x, int y) {
		// need Java 1.3+ to move mouse with Robot
	}

	/**
	 * Draw a filled rectangle to the screen
	 * 
	 * @param x
	 *            x coordinate (left) of rectangle
	 * @param y
	 *            y coordinate (top) of rectangle
	 * @param cx
	 *            Width of rectangle
	 * @param cy
	 *            Height of rectangle
	 * @param color
	 *            Colour of rectangle
	 */
	public void fillRectangle(int x, int y, int cx, int cy, int color) {
		// clip here instead
		if (x > this.right || y > this.bottom)
			return; // off screen

		int Bpp = Options.Bpp;

		// convert to 24-bit colour
		color = Bitmap.convertTo24(color);

		// correction for 24-bit colour
		if (Bpp == 3)
			color = ((color & 0xFF) << 16) | (color & 0xFF00)
					| ((color & 0xFF0000) >> 16);

		// Perform standard clipping checks, x-axis
		int clipright = x + cx - 1;
		if (clipright > this.right)
			clipright = this.right;
		if (x < this.left)
			x = this.left;
		cx = clipright - x + 1;

		// Perform standard clipping checks, y-axis
		int clipbottom = y + cy - 1;
		if (clipbottom > this.bottom)
			clipbottom = this.bottom;
		if (y < this.top)
			y = this.top;
		cy = clipbottom - y + 1;

		// construct rectangle as integer array, filled with color
		int[] rect = new int[cx * cy];
		for (int i = 0; i < rect.length; i++)
			rect[i] = color;
		// draw rectangle to backstore
		backFill(x, y, cx, cy, color);
		backstore.setRGBNoConversion(x, y, cx, cy, rect, 0, cx);

		// if(logger.isInfoEnabled()) logger.info("rect
		// \t(\t"+x+",\t"+y+"),(\t"+(x+cx-1)+",\t"+(y+cy-1)+")");
		// this.repaint(x, y, cx, cy); // seems to be faster than
		// Graphics.fillRect
		// according to JProbe
	}

	public void backFill(int x, int y, int cx, int cy, int color) {
		// TODO Auto-generated method stub

	}

	/**
	 * Draw a line to the screen
	 * 
	 * @param x1
	 *            x coordinate of start point of line
	 * @param y1
	 *            y coordinate of start point of line
	 * @param x2
	 *            x coordinate of end point of line
	 * @param y2
	 *            y coordinate of end point of line
	 * @param color
	 *            colour of line
	 * @param opcode
	 *            Operation code defining operation to perform on pixels within
	 *            the line
	 */
	public void drawLine(int x1, int y1, int x2, int y2, int color, int opcode) {
		// convert to 24-bit colour
		color = Bitmap.convertTo24(color);

		if (x1 == x2 || y1 == y2) {
			drawLineVerticalHorizontal(x1, y1, x2, y2, color, opcode);
			return;
		}

		int deltax = Math.abs(x2 - x1); // The difference between the x's
		int deltay = Math.abs(y2 - y1); // The difference between the y's
		int x = x1; // Start x off at the first pixel
		int y = y1; // Start y off at the first pixel
		int xinc1, xinc2, yinc1, yinc2;
		int num, den, numadd, numpixels;

		if (x2 >= x1) { // The x-values are increasing
			xinc1 = 1;
			xinc2 = 1;
		} else { // The x-values are decreasing
			xinc1 = -1;
			xinc2 = -1;
		}

		if (y2 >= y1) { // The y-values are increasing
			yinc1 = 1;
			yinc2 = 1;
		} else { // The y-values are decreasing
			yinc1 = -1;
			yinc2 = -1;
		}

		if (deltax >= deltay) { // There is at least one x-value for every
			// y-value
			xinc1 = 0; // Don't change the x when numerator >= denominator
			yinc2 = 0; // Don't change the y for every iteration
			den = deltax;
			num = deltax / 2;
			numadd = deltay;
			numpixels = deltax; // There are more x-values than y-values
		} else { // There is at least one y-value for every x-value
			xinc2 = 0; // Don't change the x for every iteration
			yinc1 = 0; // Don't change the y when numerator >= denominator
			den = deltay;
			num = deltay / 2;
			numadd = deltax;
			numpixels = deltay; // There are more y-values than x-values
		}

		for (int curpixel = 0; curpixel <= numpixels; curpixel++) {
			setPixel(opcode, x, y, color); // Draw the current pixel
			num += numadd; // Increase the numerator by the top of the fraction
			if (num >= den) { // Check if numerator >= denominator
				num -= den; // Calculate the new numerator value
				x += xinc1; // Change the x as appropriate
				y += yinc1; // Change the y as appropriate
			}
			x += xinc2; // Change the x as appropriate
			y += yinc2; // Change the y as appropriate
		}

		int x_min = x1 < x2 ? x1 : x2;
		int x_max = x1 > x2 ? x1 : x2;
		int y_min = y1 < y2 ? y1 : y2;
		int y_max = y1 > y2 ? y1 : y2;

		this.repaint(x_min, y_min, x_max - x_min + 1, y_max - y_min + 1);
	}

	/**
	 * Helper function for drawLine, draws a horizontal or vertical line using a
	 * much faster method than used for diagonal lines
	 * 
	 * @param x1
	 *            x coordinate of start point of line
	 * @param y1
	 *            y coordinate of start point of line
	 * @param x2
	 *            x coordinate of end point of line
	 * @param y2
	 *            y coordinate of end point of line
	 * @param color
	 *            colour of line
	 * @param opcode
	 *            Operation code defining operation to perform on pixels within
	 *            the line
	 */
	public void drawLineVerticalHorizontal(int x1, int y1, int x2, int y2,
			int color, int opcode) {
		int pbackstore;
		int i;
		// only vertical or horizontal lines
		if (y1 == y2) { // HORIZONTAL
			if (y1 >= this.top && y1 <= this.bottom) { // visible
				if (x2 > x1) { // x inc, y1=y2
					if (x1 < this.left)
						x1 = this.left;
					if (x2 > this.right)
						x2 = this.right;
					pbackstore = y1 * this.width + x1;
					for (i = 0; i < x2 - x1; i++) {
						rop.do_pixel(opcode, backstore, x1 + i, y1, color);
						pbackstore++;
					}
					repaint(x1, y1, x2 - x1 + 1, 1);
				} else { // x dec, y1=y2
					if (x2 < this.left)
						x2 = this.left;
					if (x1 > this.right)
						x1 = this.right;
					pbackstore = y1 * this.width + x1;
					for (i = 0; i < x1 - x2; i++) {
						rop.do_pixel(opcode, backstore, x2 + i, y1, color);
						pbackstore--;
					}
					repaint(x2, y1, x1 - x2 + 1, 1);
				}
			}
		} else { // x1==x2 VERTICAL
			if (x1 >= this.left && x1 <= this.right) { // visible
				if (y2 > y1) { // x1=x2, y inc
					if (y1 < this.top)
						y1 = this.top;
					if (y2 > this.bottom)
						y2 = this.bottom;
					pbackstore = y1 * this.width + x1;
					for (i = 0; i < y2 - y1; i++) {
						rop.do_pixel(opcode, backstore, x1, y1 + i, color);
						pbackstore += this.width;
					}
					repaint(x1, y1, 1, y2 - y1 + 1);
				} else { // x1=x2, y dec
					if (y2 < this.top)
						y2 = this.top;
					if (y1 > this.bottom)
						y1 = this.bottom;
					pbackstore = y1 * this.width + x1;
					for (i = 0; i < y1 - y2; i++) {
						rop.do_pixel(opcode, backstore, x1, y2 + i, color);
						pbackstore -= this.width;
					}
					repaint(x1, y2, 1, y1 - y2 + 1);
				}
			}
		}
		// if(logger.isInfoEnabled()) logger.info("line
		// \t(\t"+x1+",\t"+y1+"),(\t"+x2+",\t"+y2+")");
	}

	/**
	 * Draw a line to the screen
	 * 
	 * @param line
	 *            LineOrder describing line to be drawn
	 */
	public void drawLineOrder(LineOrder line) {
		int x1 = line.getStartX();
		int y1 = line.getStartY();
		int x2 = line.getEndX();
		int y2 = line.getEndY();

		int fgcolor = line.getPen().getColor();

		int opcode = line.getOpcode() - 1;
		drawLine(x1, y1, x2, y2, fgcolor, opcode);
	}

	/**
	 * Perform a dest blt
	 * 
	 * @param destblt
	 *            DestBltOrder describing the blit to be performed
	 */
	public void drawDestBltOrder(DestBltOrder destblt) {
		int x = destblt.getX();
		int y = destblt.getY();

		if (x > this.right || y > this.bottom)
			return; // off screen

		int cx = destblt.getCX();
		int cy = destblt.getCY();

		int clipright = x + cx - 1;
		if (clipright > this.right)
			clipright = this.right;
		if (x < this.left)
			x = this.left;
		cx = clipright - x + 1;

		int clipbottom = y + cy - 1;
		if (clipbottom > this.bottom)
			clipbottom = this.bottom;
		if (y < this.top)
			y = this.top;
		cy = clipbottom - y + 1;

		rop.do_array(destblt.getOpcode(), backstore, this.width, x, y, cx, cy,
				null, 0, 0, 0);
		this.repaint(x, y, cx, cy);

	}

	/**
	 * Perform a screen blit
	 * 
	 * @param screenblt
	 *            ScreenBltOrder describing the blit to be performed
	 */
	public void drawScreenBltOrder(ScreenBltOrder screenblt) {
		int x = screenblt.getX();
		int y = screenblt.getY();

		if (x > this.right || y > this.bottom)
			return; // off screen

		int cx = screenblt.getCX();
		int cy = screenblt.getCY();
		int srcx = screenblt.getSrcX();
		int srcy = screenblt.getSrcY();

		int clipright = x + cx - 1;
		if (clipright > this.right)
			clipright = this.right;
		if (x < this.left)
			x = this.left;
		cx = clipright - x + 1;

		int clipbottom = y + cy - 1;
		if (clipbottom > this.bottom)
			clipbottom = this.bottom;
		if (y < this.top)
			y = this.top;
		cy = clipbottom - y + 1;

		srcx += x - screenblt.getX();
		srcy += y - screenblt.getY();
		// System.out.println("RdesktopCanvas.drawScreenBltOrder():" + x + "," +
		// y
		// + "," + cx + "," + cy);
		rop.do_array(screenblt.getOpcode(), backstore, this.width, x, y, cx,
				cy, null, this.width, srcx, srcy);
		// this.repaint(x, y, cx, cy);
		blit(x, y, cx, cy, srcx, srcy);
	}

	public void blit(int x, int y, int w, int h, int srcx, int srcy) {

	}

	/**
	 * Perform a memory blit
	 * 
	 * @param memblt
	 *            MemBltOrder describing the blit to be performed
	 */
	public void drawMemBltOrder(MemBltOrder memblt) {
		int x = memblt.getX();
		int y = memblt.getY();

		if (x > this.right || y > this.bottom)
			return; // off screen

		int cx = memblt.getCX();
		int cy = memblt.getCY();
		int srcx = memblt.getSrcX();
		int srcy = memblt.getSrcY();

		// Perform standard clipping checks, x-axis
		int clipright = x + cx - 1;
		if (clipright > this.right)
			clipright = this.right;
		if (x < this.left)
			x = this.left;
		cx = clipright - x + 1;

		// Perform standard clipping checks, y-axis
		int clipbottom = y + cy - 1;
		if (clipbottom > this.bottom)
			clipbottom = this.bottom;
		if (y < this.top)
			y = this.top;
		cy = clipbottom - y + 1;

		srcx += x - memblt.getX();
		srcy += y - memblt.getY();

		// if (logger.isInfoEnabled())
		System.out.println("MEMBLT x=" + x + " y=" + y + " cx=" + cx + " cy="
				+ cy + " srcx=" + srcx + " srcy=" + srcy + " opcode="
				+ memblt.getOpcode());
		try {
			Bitmap bitmap = cache.getBitmap(memblt.getCacheID(), memblt
					.getCacheIDX());
			// IndexColorModel cm = cache.get_colourmap(memblt.getColorTable());
			// should use the colormap, but requires high color backstore...
			rop.do_array(memblt.getOpcode(), backstore, this.width, x, y, cx,
					cy, bitmap.getBitmapData(), bitmap.getWidth(), srcx, srcy);

			this.repaint(x, y, cx, cy);
		} catch (RdesktopException e) {
		}
	}

	/**
	 * Draw a pattern to the screen (pattern blit)
	 * 
	 * @param opcode
	 *            Code defining operation to be performed
	 * @param x
	 *            x coordinate for left of blit area
	 * @param y
	 *            y coordinate for top of blit area
	 * @param cx
	 *            Width of blit area
	 * @param cy
	 *            Height of blit area
	 * @param fgcolor
	 *            Foreground colour for pattern
	 * @param bgcolor
	 *            Background colour for pattern
	 * @param brush
	 *            Brush object defining pattern to be drawn
	 */
	public void patBltOrder(int opcode, int x, int y, int cx, int cy,
			int fgcolor, int bgcolor, Brush brush) {

		// convert to 24-bit colour
		fgcolor = Bitmap.convertTo24(fgcolor);
		bgcolor = Bitmap.convertTo24(bgcolor);

		// Perform standard clipping checks, x-axis
		int clipright = x + cx - 1;
		if (clipright > this.right)
			clipright = this.right;
		if (x < this.left)
			x = this.left;
		cx = clipright - x + 1;

		// Perform standard clipping checks, y-axis
		int clipbottom = y + cy - 1;
		if (clipbottom > this.bottom)
			clipbottom = this.bottom;
		if (y < this.top)
			y = this.top;
		cy = clipbottom - y + 1;

		int i;
		int[] src = null;
		switch (brush.getStyle()) {
		case 0: // solid
			// make efficient version of rop later with int fgcolor and boolean
			// usearray set to false for single colour
			src = new int[cx * cy];
			for (i = 0; i < src.length; i++)
				src[i] = fgcolor;

			// System.out.println("RdesktopCanvas.patBltOrder():" + x + "," + y
			// + "," + cx + "," + cy);
			rop.do_array(opcode, backstore, this.width, x, y, cx, cy, src, cx,
					0, 0);
			this.repaint(x, y, cx, cy);

			break;

		case 2: // hatch
			System.out.println("hatch");
			break;

		case 3: // pattern
			int brushx = brush.getXOrigin();
			int brushy = brush.getYOrigin();
			byte[] pattern = brush.getPattern();
			byte[] ipattern = pattern;

			/*
			 * // not sure if this inversion is needed byte[] ipattern = new
			 * byte[8]; for(i=0;i<ipattern.length;i++) {
			 * ipattern[ipattern.length-1-i] = pattern[i]; }
			 */

			src = new int[cx * cy];
			int psrc = 0;
			for (i = 0; i < cy; i++) {
				for (int j = 0; j < cx; j++) {
					if ((ipattern[(i + brushy) % 8] & (0x01 << ((j + brushx) % 8))) == 0)
						src[psrc] = fgcolor;
					else
						src[psrc] = bgcolor;
					psrc++;
				}
			}
			rop.do_array(opcode, backstore, this.width, x, y, cx, cy, src, cx,
					0, 0);
			this.repaint(x, y, cx, cy);
			break;
		default:
			logger.warn("Unsupported brush style " + brush.getStyle());
		}
	}

	/**
	 * Perform a pattern blit on the screen
	 * 
	 * @param patblt
	 *            PatBltOrder describing the blit to be performed
	 */
	public void drawPatBltOrder(PatBltOrder patblt) {
		Brush brush = patblt.getBrush();
		int x = patblt.getX();
		int y = patblt.getY();

		if (x > this.right || y > this.bottom)
			return; // off screen

		int cx = patblt.getCX();
		int cy = patblt.getCY();
		int fgcolor = patblt.getForegroundColor();
		int bgcolor = patblt.getBackgroundColor();
		int opcode = patblt.getOpcode();

		patBltOrder(opcode, x, y, cx, cy, fgcolor, bgcolor, brush);
	}

	/**
	 * Perform a tri blit on the screen
	 * 
	 * @param triblt
	 *            TriBltOrder describing the blit
	 */
	public void drawTriBltOrder(TriBltOrder triblt) {
		int x = triblt.getX();
		int y = triblt.getY();

		if (x > this.right || y > this.bottom)
			return; // off screen

		int cx = triblt.getCX();
		int cy = triblt.getCY();
		int srcx = triblt.getSrcX();
		int srcy = triblt.getSrcY();
		int fgcolor = triblt.getForegroundColor();
		int bgcolor = triblt.getBackgroundColor();
		Brush brush = triblt.getBrush();

		// convert to 24-bit colour
		fgcolor = Bitmap.convertTo24(fgcolor);
		bgcolor = Bitmap.convertTo24(bgcolor);

		// Perform standard clipping checks, x-axis
		int clipright = x + cx - 1;
		if (clipright > this.right)
			clipright = this.right;
		if (x < this.left)
			x = this.left;
		cx = clipright - x + 1;

		// Perform standard clipping checks, y-axis
		int clipbottom = y + cy - 1;
		if (clipbottom > this.bottom)
			clipbottom = this.bottom;
		if (y < this.top)
			y = this.top;
		cy = clipbottom - y + 1;

		try {
			Bitmap bitmap = cache.getBitmap(triblt.getCacheID(), triblt
					.getCacheIDX());
			switch (triblt.getOpcode()) {
			case 0x69: // PDSxxn
				rop.do_array(ROP2_XOR, backstore, this.width, x, y, cx, cy,
						bitmap.getBitmapData(), bitmap.getWidth(), srcx, srcy);
				patBltOrder(ROP2_NXOR, x, y, cx, cy, fgcolor, bgcolor, brush);
				break;
			case 0xb8: // PSDPxax
				patBltOrder(ROP2_XOR, x, y, cx, cy, fgcolor, bgcolor, brush);
				rop.do_array(ROP2_AND, backstore, this.width, x, y, cx, cy,
						bitmap.getBitmapData(), bitmap.getWidth(), srcx, srcy);
				patBltOrder(ROP2_XOR, x, y, cx, cy, fgcolor, bgcolor, brush);
				break;
			case 0xc0: // PSa
				rop.do_array(ROP2_COPY, backstore, this.width, x, y, cx, cy,
						bitmap.getBitmapData(), bitmap.getWidth(), srcx, srcy);
				patBltOrder(ROP2_AND, x, y, cx, cy, fgcolor, bgcolor, brush);
				break;

			default:
				logger
						.warn("Unimplemented Triblt opcode:"
								+ triblt.getOpcode());
				rop.do_array(ROP2_COPY, backstore, this.width, x, y, cx, cy,
						bitmap.getBitmapData(), bitmap.getWidth(), srcx, srcy);
			}
		} catch (RdesktopException e) {
		}
	}

	/**
	 * Parse a delta co-ordinate in polyline order form
	 * 
	 * @param buffer
	 * @param offset
	 * @return
	 */
	static int parse_delta(byte[] buffer, int[] offset) {
		int value = buffer[offset[0]++] & 0xff;
		int two_byte = value & 0x80;

		if ((value & 0x40) != 0) /* sign bit */
			value |= ~0x3f;
		else
			value &= 0x3f;

		if (two_byte != 0)
			value = (value << 8) | (buffer[offset[0]++] & 0xff);

		return value;
	}

	/**
	 * Draw a multi-point set of lines to the screen
	 * 
	 * @param polyline
	 *            PolyLineOrder describing the set of lines to draw
	 */
	public void drawPolyLineOrder(PolyLineOrder polyline) {
		int x = polyline.getX();
		int y = polyline.getY();
		int fgcolor = polyline.getForegroundColor();
		int datasize = polyline.getDataSize();
		byte[] databytes = polyline.getData();
		int lines = polyline.getLines();

		// convert to 24-bit colour
		fgcolor = Bitmap.convertTo24(fgcolor);

		// hack - data as single element byte array so can pass by ref to
		// parse_delta
		// see http://www.rgagnon.com/javadetails/java-0035.html

		int[] data = new int[1];
		data[0] = ((lines - 1) / 4) + 1;
		int flags = 0;
		int index = 0;

		int opcode = polyline.getOpcode() - 1;

		for (int line = 0; (line < lines) && (data[0] < datasize); line++) {
			int xfrom = x;
			int yfrom = y;

			if (line % 4 == 0)
				flags = databytes[index++];

			if ((flags & 0xc0) == 0)
				flags |= 0xc0; /* none = both */

			if ((flags & 0x40) != 0)
				x += parse_delta(databytes, data);

			if ((flags & 0x80) != 0)
				y += parse_delta(databytes, data);
			// logger.info("polyline
			// "+line+","+xfrom+","+yfrom+","+x+","+y+","+fgcolor+","+opcode);

			drawLine(xfrom, yfrom, x, y, fgcolor, opcode);
			flags <<= 2;
		}
	}

	/**
	 * Draw a rectangle to the screen
	 * 
	 * @param rect
	 *            RectangleOrder defining the rectangle to be drawn
	 */
	public void drawRectangleOrder(RectangleOrder rect) {
		// if(logger.isInfoEnabled()) logger.info("RectangleOrder!");
		fillRectangle(rect.getX(), rect.getY(), rect.getCX(), rect.getCY(),
				rect.getColor());
	}

	/**
	 * Perform an operation on a pixel in the backstore
	 * 
	 * @param opcode
	 *            ID of operation to perform
	 * @param x
	 *            x coordinate of pixel
	 * @param y
	 *            y coordinate of pixel
	 * @param color
	 *            Colour value to be used in operation
	 */
	public void setPixel(int opcode, int x, int y, int color) {
		int Bpp = Options.Bpp;

		// correction for 24-bit colour
		if (Bpp == 3)
			color = ((color & 0xFF) << 16) | (color & 0xFF00)
					| ((color & 0xFF0000) >> 16);

		if ((x < this.left) || (x > this.right) || (y < this.top)
				|| (y > this.bottom)) { // Clip
		} else {
			rop.do_pixel(opcode, backstore, x, y, color);
		}
	}

	/**
	 * Draw a single glyph to the screen
	 * 
	 * @param mixmode
	 *            0 for transparent background, specified colour for background
	 *            otherwide
	 * @param x
	 *            x coordinate on screen at which to draw glyph
	 * @param y
	 *            y coordinate on screen at which to draw glyph
	 * @param cx
	 *            Width of clipping area for glyph
	 * @param cy
	 *            Height of clipping area for glyph
	 * @param data
	 *            Set of values defining glyph's pattern
	 * @param bgcolor
	 *            Background colour for glyph pattern
	 * @param fgcolor
	 *            Foreground colour for glyph pattern
	 */
	public void drawGlyph(int mixmode, int x, int y, int cx, int cy,
			byte[] data, int bgcolor, int fgcolor) {

		int pdata = 0;
		int index = 0x80;

		int bytes_per_row = (cx - 1) / 8 + 1;
		int newx, newy, newcx, newcy;

		int Bpp = Options.Bpp;

		// convert to 24-bit colour
		fgcolor = Bitmap.convertTo24(fgcolor);
		bgcolor = Bitmap.convertTo24(bgcolor);

		// correction for 24-bit colour
		if (Bpp == 3) {
			fgcolor = ((fgcolor & 0xFF) << 16) | (fgcolor & 0xFF00)
					| ((fgcolor & 0xFF0000) >> 16);
			bgcolor = ((bgcolor & 0xFF) << 16) | (bgcolor & 0xFF00)
					| ((bgcolor & 0xFF0000) >> 16);
		}

		// clip here instead

		if (x > this.right || y > this.bottom)
			return; // off screen

		int clipright = x + cx - 1;
		if (clipright > this.right)
			clipright = this.right;
		// if (x < this.left)
		// newx = this.left;
		// else
		newx = x;
		newcx = clipright - x + 1; // not clipright - newx - 1

		int clipbottom = y + cy - 1;
		if (clipbottom > this.bottom)
			clipbottom = this.bottom;
		if (y < this.top)
			newy = this.top;
		else
			newy = y;

		newcy = clipbottom - newy + 1;

		int pbackstore = (newy * this.width) + x;
		pdata = bytes_per_row * (newy - y); // offset y, but not x

		if (mixmode == MIX_TRANSPARENT) { // FillStippled
			for (int i = 0; i < newcy; i++) {
				for (int j = 0; j < newcx; j++) {
					if (index == 0) { // next row
						pdata++;
						index = 0x80;
					}

					if ((data[pdata] & index) != 0) {
						if ((x + j >= newx) && (newx + j > 0) && (newy + i > 0))
							// since haven't offset x
							backstore.setRGB(newx + j, newy + i, fgcolor);
					}
					index >>= 1;
				}
				pdata++;
				index = 0x80;
				pbackstore += this.width;
				if (pdata == data.length) {
					pdata = 0;
				}
			}
		} else { // FillOpaqueStippled
			for (int i = 0; i < newcy; i++) {
				for (int j = 0; j < newcx; j++) {
					if (index == 0) { // next row
						pdata++;
						index = 0x80;
					}

					if (x + j >= newx) {
						if ((x + j > 0) && (y + i > 0)) {
							if ((data[pdata] & index) != 0)
								backstore.setRGB(x + j, y + i, fgcolor);
							else
								backstore.setRGB(x + j, y + i, bgcolor);
						}
					}
					index >>= 1;
				}
				pdata++;
				index = 0x80;
				pbackstore += this.width;
				if (pdata == data.length) {
					pdata = 0;
				}
			}
		}

		// if(logger.isInfoEnabled()) logger.info("glyph
		// \t(\t"+x+",\t"+y+"),(\t"+(x+cx-1)+",\t"+(y+cy-1)+")");

		// System.out.println("RdesktopCanvas.drawGlyph():" + x + "," + y + ","
		// + cx + "," + cy);
		this.repaint(newx, newy, newcx, newcy);
	}

	/**
	 * Create an AWT Cursor object
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param andmask
	 * @param xormask
	 * @param cache_idx
	 * @return Created Cursor
	 */
	public Cursor createCursor(int x, int y, int w, int h, byte[] andmask,
			byte[] xormask, int cache_idx) {
		int pxormask = 0;
		int pandmask = 0;
		Point p = new Point(x, y);
		int size = w * h;
		int scanline = w / 8;
		int offset = 0;
		byte[] mask = new byte[size];
		int[] cursor = new int[size];
		int pcursor = 0, pmask = 0;

		offset = size;

		for (int i = 0; i < h; i++) {
			offset -= w;
			pmask = offset;
			for (int j = 0; j < scanline; j++) {
				for (int bit = 0x80; bit > 0; bit >>= 1) {
					if ((andmask[pandmask] & bit) != 0) {
						mask[pmask] = 0;
					} else {
						mask[pmask] = 1;
					}
					pmask++;
				}
				pandmask++;
			}
		}

		offset = size;
		pcursor = 0;

		for (int i = 0; i < h; i++) {
			offset -= w;
			pcursor = offset;
			for (int j = 0; j < w; j++) {
				cursor[pcursor] = ((xormask[pxormask + 2] << 16) & 0x00ff0000)
						| ((xormask[pxormask + 1] << 8) & 0x0000ff00)
						| (xormask[pxormask] & 0x000000ff);
				pxormask += 3;
				pcursor++;
			}

		}

		offset = size;
		pmask = 0;
		pcursor = 0;
		pxormask = 0;

		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				if ((mask[pmask] == 0) && (cursor[pcursor] != 0)) {
					cursor[pcursor] = ~(cursor[pcursor]);
					cursor[pcursor] |= 0xff000000;
				} else if ((mask[pmask] == 1) || (cursor[pcursor] != 0)) {
					cursor[pcursor] |= 0xff000000;
				}
				pcursor++;
				pmask++;
			}
		}

		Image wincursor = this.createImage(new MemoryImageSource(w, h, cursor,
				0, w));
		return createCustomCursor(wincursor, p, "", cache_idx);
	}

	/**
	 * Create an AWT Cursor from an image
	 * 
	 * @param wincursor
	 * @param p
	 * @param s
	 * @param cache_idx
	 * @return Generated Cursor object
	 */
	protected Cursor createCustomCursor(Image wincursor, Point p, String s,
			int cache_idx) {
		if (cache_idx == 1)
			return Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		return Cursor.getDefaultCursor();
	}

	/**
	 * Handle the window losing focus, notify input classes
	 */
	public void lostFocus() {
		if (input != null)
			input.lostFocus();
	}

	/**
	 * Handle the window gaining focus, notify input classes
	 */
	public void gainedFocus() {
		if (input != null)
			input.gainedFocus();
	}

	/**
	 * Notify the input classes that the connection is ready for sending
	 * messages
	 */
	public void triggerReadyToSend() {
		input.triggerReadyToSend();
	}

	public void repaint(int x, int y, int width, int height) {
		// System.err.println("RdesktopCanvas.repaint():" + x + "," + y + ","
		// + width + "," + height);
		// Thread.dumpStack();
		repaint(0, x, y, width, height);
	}

	public void setCursorFromIdx(int cache_idx) {
		// TODO Auto-generated method stub

	}

}
