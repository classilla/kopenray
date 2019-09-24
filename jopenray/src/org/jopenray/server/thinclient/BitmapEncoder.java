/*
 *  Copyright 2019 Cameron Kaiser
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2005 Propero Limited
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
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.List;

import org.jopenray.operation.BitmapOperation;
import org.jopenray.operation.BitmapRGBOperation;
import org.jopenray.operation.FlushOperation;
import org.jopenray.operation.FillOperation;
import org.jopenray.util.BitArray;
import org.jopenray.util.TIntArrayList;
import org.jopenray.util.Util;

import java.util.Vector;

public class BitmapEncoder {
	private static final int NO_COLOR = 888;
	private DisplayWriterThread writer;
	private int toX, toY;
	private List<BitmapLine> l = new ArrayList<BitmapLine>(1200);
	private int[] pixels;
	private int bitmapWidth;
	private int bitmapHeight;

	public BitmapEncoder(DisplayWriterThread writer) {
		this.writer = writer;
	}

	public void setDestination(int toX, int toY) {
		this.toX = toX;
		this.toY = toY;

	}

	long encodedBitmap;
	long smalBitmapEncodedBitmap;
	private long smalBitmapEncodedBitmapByFill;
	private long smalBitmapEncodedBitmapByBiColo;
	private long encodedBitmapByBiColor;
	private long encodedBitmapByRGBColor;
	private long encodedBitmapByFill;

	public String getStats() {
		return "Encoded bitmap:" + encodedBitmap + " [Small: "
				+ smalBitmapEncodedBitmapByFill + ":"
				+ smalBitmapEncodedBitmapByBiColo + "]" + encodedBitmapByFill
				+ ":" + encodedBitmapByBiColor + ":" + encodedBitmapByRGBColor;
	}

	public void encode(int[] pixels, int bitmapWidth, int bitmapHeight, boolean flush) {

		encodedBitmap++;
		this.pixels = pixels;
		this.bitmapWidth = bitmapWidth;
		this.bitmapHeight = bitmapHeight;

		// Dump old screens if we are updating en masse. Partial
		// updates may need to preserve history, though.
		if (flush) {
			DisplayMessage mflush = new DisplayMessage(writer);
			mflush.addOperation(new FlushOperation());
			writer.addMessage(mflush);
		}

		// Test small bitmap
		if (bitmapWidth * bitmapHeight <= 2048) {
			if (encodeSmall()) {
				return;
			}
		}
		if (bitmapWidth < 5 && bitmapHeight > 64) {
			DisplayMessage message = new DisplayMessage(writer);
			sendFullColorRaw(0, message, bitmapHeight, bitmapWidth);
			writer.addMessage(message);
			return;
		}

		analyseLines();

		DisplayMessage message = new DisplayMessage(writer);

		BitmapLine currentLine = l.get(0);
		int first = 0;
		// System.out.println(currentLine);

		long t1 = System.nanoTime();

		for (int i = 1; i < bitmapHeight; i++) {
			BitmapLine line = l.get(i);

			if (!currentLine.canBeMergedWith(line)) {
				// Encode les merged
				
				encodeLines(first, i - first, message);
				// 
				// System.out.println("Cannot be merged");
				currentLine = line;
				first = i;
			}
			// System.out.println(line);
		}
		encodeLines(first, bitmapHeight - first, message);
		long t2 = System.nanoTime();
		// System.out.println("BitmapEncoder.encode(): in " + (t2 - t1) /
		// 1000000
		// + " ms");
		writer.addMessage(message);

	}

	private boolean encodeSmall() {
		smalBitmapEncodedBitmap++;
		int stop = bitmapHeight * bitmapWidth;
		int col0 = pixels[0];
		int col1 = NO_COLOR;
		boolean fill = true;
		boolean biColor = true;
		for (int i = 0; i < stop; i++) {
			if (pixels[i] != col0) {
				if (col1 == NO_COLOR) {
					col1 = pixels[i];
					fill = false;
				}

				if (pixels[i] != col1) {
					biColor = false;
					break;
				}
			}
		}
		if (fill) {
			DisplayMessage m = new DisplayMessage(writer);
			m.addOperation(new FillOperation(toX, toY, bitmapWidth,
					bitmapHeight, new Color(col0)));
			writer.addMessage(m);
			smalBitmapEncodedBitmapByFill++;
			// System.out.println("Bitmap replaced by fill");
			return true;
		} else if (biColor) {

			DisplayMessage m = new DisplayMessage(writer);
			BitArray b = BitmapOperation.getBytes(pixels, bitmapWidth, 0, 0,
					bitmapWidth, bitmapHeight, col1);
			m.addOperation(new BitmapOperation(toX, toY, bitmapWidth,
					bitmapHeight, new Color(col0), new Color(col1), b));
			writer.addMessage(m);
			smalBitmapEncodedBitmapByBiColo++;
			// System.out.println("Bitmap replaced by bicolor");
			return true;

		}

		return false;
	}

	private final void analyseLines() {
		l.clear();
		for (int i = 0; i < bitmapHeight; i++) {
			l.add(new BitmapLine(pixels, bitmapWidth, i));
		}
	}

	private void encodeLines(int first, int height, DisplayMessage m) {

		BitmapLine firstLine = l.get(first);
		// System.out.println("Encode à partir de la ligne " + first
		// + " hauteur: " + height + "  [premiere ligne:" + firstLine
		// + "]");

		int width = firstLine.getWidth();

		if (firstLine.getType() == BitmapLine.TYPE_MONOCOLOR) {

			m.addOperation(new FillOperation(toX, toY + first, width, height,
					new Color(firstLine.getColor0())));
			encodedBitmapByFill++;
			return;
		}
		if (firstLine.getType() == BitmapLine.TYPE_BICOLOR) {
			encodedBitmapByBiColor++;
			sendBiColor(first, m, height, width, firstLine);
			return;
		}

		encodedBitmapByRGBColor++;
		sendFullColorOptimized(first, m, height, width);
	}

	
	private void sendFullColorRaw(int first, DisplayMessage m, int height,
			int width) {
		// Compute Widths
		int nbColor = width;
		int MAX = (1448 - 128) / 4;
		int nbSegmentW = (int) Math.ceil((double) nbColor / MAX);

		int limitedWidth = (int) Math.ceil((double) width / nbSegmentW);
		if (limitedWidth > width) {
			limitedWidth = width;
		} else if (limitedWidth < 1) {
			limitedWidth = 1;
		}
		// System.out.println("widht:" + width + " nbSegm:" + nbSegmentW
		// + " limit width to:" + limitedWidth + " MAX:" + MAX);

		int[] widths = Util.split(width, limitedWidth);

		// Compute Heights
		nbColor = limitedWidth * height;
		int nbSegmentH = (int) Math.ceil((double) nbColor / MAX);
		int limitedHeight = (int) Math.floor((double) height / nbSegmentH);
		if (limitedHeight < 1) {
			limitedHeight = 1;
		} else if (limitedHeight > height) {
			limitedHeight = height;
		}
		int[] heights = Util.split(height, limitedHeight);

		// System.out.println("height:" + height + " nbSegm:" + nbSegmentH
		// + " limit height to:" + limitedHeight + " MAX:" + MAX);
		for (int j = 0; j < heights.length; j++) {
			int newHeight = heights[j];

			for (int i = 0; i < widths.length; i++) {
				int newWidth = widths[i];
				byte[] bytes = BitmapRGBOperation.getBytes(pixels, width,
						limitedWidth * i, first + limitedHeight * j, newWidth,
						newHeight);
				m.addOperation(new BitmapRGBOperation(toX + limitedWidth * i,
						toY + first + limitedHeight * j, newWidth, newHeight,
						bytes));
			}
		}

	}

	private void sendFullColorOptimized(int first, DisplayMessage m,
			int height, int width) {
		// On envoi en full
		// System.out.println("RGB: " + toX + " , " + (toY + first) + " "
		// + width + " x " + height);

		for (int i = first; i < first + height; i++) {
			sendFullColorLine(m, i);
		}

	}

	private void sendFullColorLineAsRGB(int first, DisplayMessage m,
			int height, int width) {
		// On envoi en full
		// System.out.println("RGB: " + toX + " , " + (toY + first) + " "
		// + width + " x " + height);

		for (int i = first; i < first + height; i++) {
			sendFullColorLineAsRGB(m, i);
		}

	}

	private void sendBiColor(int first, DisplayMessage m, int height,
			int width, BitmapLine firstLine) {

		// On envoi en bi
		// System.out.println("BI: " + toX + " , " + (toY + first) + " " + width
		// + " x " + height);
		if (width > 32) {
			for (int i = 0; i < height; i++) {
				sendBiColorLine(pixels, width, i + first, toX, toY, firstLine
						.getColor0(), firstLine.getColor1(), m);
			}
			return;
		}

		// Compute Widths
		int nbColor = width;
		int MAX = (1448 - 64);
		int nbSegmentW = (int) Math.ceil((double) nbColor / MAX);

		int limitedWidth = (int) Math.ceil((double) width / nbSegmentW);
		if (limitedWidth > width) {
			limitedWidth = width;
		} else if (limitedWidth < 1) {
			limitedWidth = 1;
		}
		// System.out.println("widht:" + width + " nbSegm:" + nbSegmentW
		// + " limit width to:" + limitedWidth + " MAX:" + MAX);

		int[] widths = Util.split(width, limitedWidth);

		// Compute Heights
		nbColor = limitedWidth * height;
		int nbSegmentH = (int) Math.ceil((double) nbColor / MAX);
		int limitedHeight = (int) Math.floor((double) height / nbSegmentH);
		if (limitedHeight < 1) {
			limitedHeight = 1;
		} else if (limitedHeight > height) {
			limitedHeight = height;
		}
		int[] heights = Util.split(height, limitedHeight);

		// System.out.println("height:" + height + " nbSegm:" + nbSegmentH
		// + " limit height to:" + limitedHeight + " MAX:" + MAX);
		for (int j = 0; j < heights.length; j++) {
			int newHeight = heights[j];

			for (int i = 0; i < widths.length; i++) {
				int newWidth = widths[i];
				BitArray bytes = BitmapOperation.getBytes(pixels, width,
						limitedWidth * i, first + limitedHeight * j, newWidth,
						newHeight, firstLine.getColor1());
				m.addOperation(new BitmapOperation(toX + limitedWidth * i, toY
						+ first + limitedHeight * j, newWidth, newHeight,
						new Color(firstLine.getColor0()), new Color(firstLine
								.getColor1()), bytes));
			}
		}
	}

	private void sendBiColorLine(int[] pixels, int width, int y, int toX,
			int toY, int c0, int c1, DisplayMessage m) {
		int offset = y * width;
		int count = 0;// nombre de pixel contigues

		int currentColor = pixels[offset];
		Vector<Integer> counts = new Vector<Integer>();
		// System.out.println("Analysing line");
		for (int i = 0; i < width; i++) {
			int p1 = pixels[offset];
			offset++;
			// System.out.print(p1 + " ,");
			if (p1 == currentColor) {
				count++;
			} else {
				counts.addElement(count);
				count = 1;
				currentColor = p1;
			}

		}
		if (count > 0) {
			counts.addElement(count);
		}
		// System.out.println();
		// System.out.println("Counts:");
		int offsetX = 0;
		int lastSentX = 0;
		// for (int i = 0; i < counts.size(); i++) {
		// int nb = counts.elementAt(i);
		// System.out.print(nb + " ,");
		// }
		// System.out.println();
		int size = counts.size();
		for (int i = 0; i < size; i++) {
			int nb = counts.elementAt(i);

			if (nb > 16) {
				// Envoi le mono
				int nbMono = offsetX - lastSentX;
				if (nbMono > 0) {
					// System.out.println("Bi "+(toX + lastSentX)+","+(toY +
					// y)+" "+nbMono+"x1");
					BitArray b = BitmapOperation.getBytes(pixels, width,
							lastSentX, y, nbMono, 1, c1);
					BitmapOperation bOp = new BitmapOperation(
							(toX + lastSentX), (toY + y), nbMono, 1, new Color(
									c0), new Color(c1), b);
					m.addOperation(bOp);
				}
				// Envoi le fill
				// System.out.println("Fill rect:"+(toX + offsetX)+","+(toY +
				// y)+" "+nb+"x1");
				FillOperation fOp = new FillOperation(toX + offsetX, toY + y,
						nb, 1, new Color(pixels[y * width + offsetX]));
				m.addOperation(fOp);
				lastSentX = offsetX + nb;
			}
			offsetX += nb;

		}
		int nbMono = offsetX - lastSentX;
		if (nbMono > 0) {
			// System.out.println("Bi "+(toX + lastSentX)+","+(toY +
			// y)+" "+nbMono+"x1");
			BitArray b = BitmapOperation.getBytes(pixels, width, lastSentX, y,
					nbMono, 1, c1);
			BitmapOperation bOp = new BitmapOperation((toX + lastSentX),
					(toY + y), nbMono, 1, new Color(c0), new Color(c1), b);
			m.addOperation(bOp);
		}
		// System.out.println();
	}

	public void encode(BufferedImage image, int toX, int toY, boolean flush) {

		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels,
				0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

	
		encode(toX, toY, width, height, pixels, flush);
		// int r = sendBitmap_new(pixels, width, height, toX, toY);
		
	}

	public void encode(int toX, int toY, int width, int height, int[] pixels, boolean flush) {
		//long t1 = System.nanoTime();
		setDestination(toX, toY);
		encode(pixels, width, height, flush);
		//long t2 = System.nanoTime();
		//System.out.println("Bitmap "+width+"x"+height+" at "+toX+","+toY+" encoded in ms:" + (t2 - t1) / 1000000);

	}

	private static final boolean DEBUG = false;
	private static final int MAX_SIZE = 128;

	private static final int HASH_SIZE = 1283; // Prime,optimus :)

	private final int getColorCountForLine(int y, int max) {
		// TIntHashSet colors = new TIntHashSet();
		TIntArrayList colors = new TIntArrayList(64);
		int ref[] = new int[HASH_SIZE * 2];
		int nbColor = 0;
		for (int x = y * bitmapWidth; x < y * bitmapWidth + bitmapWidth; x++) {
			int c = pixels[x];
			int hash = HASH_SIZE + c % HASH_SIZE;
			boolean isNewColor = (ref[hash] == 0);
			if (!isNewColor) {
				isNewColor = !colors.contains(c);
			}

			if (!isNewColor) {
				// jamais eu
				nbColor++;
				ref[hash] = 1;
				colors.add(c);
				if (nbColor > max) {
					break;
				}
			}
		}
		return nbColor;
	}

	private void sendFullColorLine(DisplayMessage m, int y) {
		if (bitmapWidth < 3) {
			Thread.dumpStack();
			System.exit(0);
		}
		// Nb color analysis
		if (bitmapWidth > 0 /* 32 */) {

			int maxColor = Math.min(2 + bitmapWidth / 10, 32);
			int nbColor = getColorCountForLine(y, maxColor);
			if (nbColor > maxColor) {

				sendFullColorLineAsRGB(m, y);

				return;
			}
		}

		Vector<Integer> counts = new Vector<Integer>(); // IntVector
		int currentCount = 0;

		int offset = y * bitmapWidth;

		int c1 = NO_COLOR;
		int c2 = NO_COLOR;
		int startX = 0;
		if (DEBUG) {
			System.err.println("\nLine:" + y);
			for (int x = 0; x < bitmapWidth; x++) {

				int c = pixels[offset];// image.getRGB(x, y);
				offset++;
				System.err.print(c + ",");
				if (offset % 10 == 0) {
					System.err.println();
				}
			}
			System.err.println();
		}
		boolean isC1 = true;
		currentCount = 0;
		counts.removeAllElements();

		int encodedPixel = 0;

		for (int x = 0; x < bitmapWidth; x++) {
			if (DEBUG) {
				System.err.println("Reading pixel index:" + x + " (encoded:"
						+ encodedPixel + ")");
			}
			int c = pixels[offset];// image.getRGB(x, y);
			offset++;
			if (c1 == NO_COLOR) {
				c1 = c;
			} else if (c != c1 && c2 == NO_COLOR) {
				// on decouvre une 2eme couleur
				c2 = c;
			}
			if (c == c1) {
				if (isC1) {
					currentCount++;
				} else {
					counts.addElement(currentCount);
					currentCount = 1;
					isC1 = true;
				}

			} else if (c == c2) {
				if (!isC1) {
					currentCount++;

				} else {
					counts.addElement(currentCount);
					currentCount = 1;
					isC1 = false;
				}

			}
			if (c != c1 && c != c2 || x == bitmapWidth - 1) {
				// On decouvre une 3eme couleur ou on est en fin de ligne
				counts.addElement(currentCount);
				int size = counts.size();
				if (DEBUG) {
					System.err.println("==============  Changement de couleur");
					dumpCounts(y, counts, c1, c2, startX, x, size);
				}
				// Creation des operations
				int cumulatedCount = 0; // longeur du segment cummulé
				int s = 0; // debut du segment cummulé courant
				int nbCumulated = 0;// nombre de segment cumulés
				int lastSentSegment = -2;
				for (int i = 0; i < size; i++) {
					int v = counts.elementAt(i);

					if (cumulatedCount + v > MAX_SIZE) {
						// envoi jusqu'a i-1
						if (cumulatedCount > 0) {
							//System.err.println("cumulated:" + nbCumulated);
							if (nbCumulated == 1) {
								/*System.err.println("mono de " + s + " l:"
										+ (cumulatedCount) + " color: isC1:"
										+ (i % 2 == 1));*/
								if (i % 2 == 1) {
									m.addOperation(new FillOperation(startX + s
											+ toX, y + toY, cumulatedCount, 1,
											getColorFrom(c1)));
									encodedPixel += cumulatedCount;
								} else {
									m.addOperation(new FillOperation(startX + s
											+ toX, y + toY, cumulatedCount, 1,
											getColorFrom(c2)));
									encodedPixel += cumulatedCount;
								}
							} else {
								//System.err.println("bi de " + s + " l:"
								//		+ (cumulatedCount));
								m.addOperation(getBiColorBitmapOperation(
										pixels, startX + s, cumulatedCount,
										bitmapWidth, y, c1, c2, toX, toY));
								encodedPixel += cumulatedCount;
							}
						}
						nbCumulated = 0;
						s += cumulatedCount;
						cumulatedCount = 0;
						lastSentSegment = i;
					}

					cumulatedCount += v;
					nbCumulated++;

				}
				if (lastSentSegment <= size) {
					// la fin

					if (nbCumulated == 1) {
					//	System.err.println("fin mono de " + s + " l:"
					//			+ (cumulatedCount) + " color: isC1:"
					//			+ (lastSentSegment % 2 == 0));

						if (lastSentSegment % 2 == 0) {
							m.addOperation(new FillOperation(startX + s + toX,
									y + toY, cumulatedCount, 1,
									getColorFrom(c1)));
							encodedPixel += cumulatedCount;
						} else {
							m.addOperation(new FillOperation(startX + s + toX,
									y + toY, cumulatedCount, 1,
									getColorFrom(c2)));
							encodedPixel += cumulatedCount;
						}

					} else {
					//	System.err.println("fin bi de " + s + " l:"
					//			+ (cumulatedCount));
						m.addOperation(getBiColorBitmapOperation(pixels, startX
								+ s, cumulatedCount, bitmapWidth, y, c1, c2,
								toX, toY));
						encodedPixel += cumulatedCount;
					}
					nbCumulated = 0;
					s = cumulatedCount;
					cumulatedCount = 0;
				}

				if (DEBUG) {
					System.err.println("============== Clear count");
					dumpCounts(y, counts, c1, c2, startX, x, size);
				}
				counts.removeAllElements();
				currentCount = 1;
				isC1 = true;
				startX = x;
				c1 = c;
				c2 = NO_COLOR;
				if (DEBUG) {
					System.err.println("============== Encoded pixels:"
							+ encodedPixel + " / " + bitmapWidth);

				}
			}

		}

		if (encodedPixel != bitmapWidth /* && lastPainted == bitmapWidth - 1 */) {

			final int width = bitmapWidth - encodedPixel;
			if (width != 1) {

				throw new IllegalStateException("More than 1 pixel missing");
			}
			m.addOperation(new FillOperation(toX + encodedPixel, y + toY,
					width, 1, getColorFrom(c1)));
			encodedPixel += 1;
		}

	}

	private void dumpCounts(int y, Vector<Integer> counts, int c1, int c2,
			int startX, int x, int size) {

		for (int i = 0; i < size; i++) {
			System.err.print(counts.elementAt(i));
			if (i < size - 1) {
				System.err.print(", ");
			}
		}
		System.err.println("--");
		for (int i = bitmapWidth * y + startX; i < bitmapWidth * y + x; i++) {
			if (pixels[i] == c1) {
				System.err.print("0");
			} else {
				System.err.print("1");
			}
		}
		System.err.println(":" + (x - startX) + " color:" + c1 + "/" + c2);
	}

	private void sendFullColorLineAsRGB(DisplayMessage m, int y) {
		int MAX = 480;// (1448 - 64) / 4;
		int nbSegmentW = (int) Math.ceil((double) bitmapWidth / MAX);

		int limitedWidth = (int) Math.ceil((double) bitmapWidth / nbSegmentW);
		if (limitedWidth > bitmapWidth) {
			limitedWidth = bitmapWidth;
		} else if (limitedWidth < 1) {
			limitedWidth = 1;
		}
		// System.out.println("widht:" + width + " nbSegm:" + nbSegmentW
		// + " limit width to:" + limitedWidth + " MAX:" + MAX);

		int[] widths = Util.split(bitmapWidth, limitedWidth);
		for (int i = 0; i < widths.length; i++) {
			int newWidth = widths[i];
			byte[] bytes = BitmapRGBOperation.getBytes(pixels, bitmapWidth,
					limitedWidth * i, y, newWidth, 1);
			m.addOperation(new BitmapRGBOperation(toX + limitedWidth * i, toY
					+ y, newWidth, 1, bytes));
		}
	}

	private BitmapOperation getBiColorBitmapOperation(int[] pixels, int startX,
			int pixelToSend, int bitmapWidth, int y, int c1, int c2, int toX,
			int toY) {
		// On envoi un setRectBitmap
		// System.out.println("c:" + c + " c1:" + c1 + "c2:" +
		// c2);
		// System.out.println("x:" + x + " start:" + startX);

		// int LIMIT = 112;

		int nbBitsToSend = pixelToSend;
		if (nbBitsToSend % 32 > 0) {
			nbBitsToSend = nbBitsToSend + 32 - nbBitsToSend % 32;
		}

		BitArray b = new BitArray(nbBitsToSend);
		int index = 0;
		int counter = startX + y * bitmapWidth;

		for (int i = 0; i < pixelToSend; i++) {
			int col = pixels[counter + i];// ! out of bound
			if (col == c2) {

				// System.out.print(1);
				b.set(index);
			}
			index++;

		}

		return new BitmapOperation(startX + toX, y + toY, pixelToSend, 1,
				getColorFrom(c1), getColorFrom(c2), b);
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

}
