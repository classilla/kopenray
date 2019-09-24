/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
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

package org.jopenray.test;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.jopenray.server.thinclient.BitmapEncoder;
import org.jopenray.server.thinclient.DisplayMessage;
import org.jopenray.server.thinclient.DisplayWriterThread;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class BenchmarkEncoder {
	static long totalL = 0;
	static long totalTime = 0;
	static long optimums = 0;

	public static void main(String[] args) {

		BitmapEncoder encoder = new BitmapEncoder(
				new DisplayWriterThread(null) {
					@Override
					public void addMessage(DisplayMessage m) {
						totalL += m.getLength();
					}
				});
		try {
			ZipFile f = new ZipFile("test.zip");
			System.out.println("Reading file");
			BufferedImage[] ims = new BufferedImage[/* 115 */6263];
			for (int i = 1; i < ims.length + 1; i++) {
				ZipEntry e = f.getEntry("test/" + i + ".png");
				InputStream in = new BufferedInputStream(f.getInputStream(e));
				BufferedImage image = ImageIO.read(in);
				ims[i - 1] = image;

			}

			System.setOut(new PrintStream(new BufferedOutputStream(
					new ByteOutputStream(1000000))));
			for (int i = 0; i < ims.length; i++) {
				long t1 = System.nanoTime();
				long l1 = totalL;
				BufferedImage image = ims[i];
				encoder.encode(image, 0, 0);
				long t2 = System.nanoTime();
				long l2 = totalL;
				int pixels = image.getWidth() * image.getHeight();
				long encoded = l2 - l1;

				boolean optimum = (encoded == 16);
				if (!optimum && encoded > 9000) {
					System.err.println("Image " + (i + 1) + " : "
							+ image.getWidth() + "x" + image.getHeight() + " "
							+ pixels + " pixels encoded in " + encoded
							+ " octets, compression :" + (float) pixels
							/ encoded);

				} else {
					optimums++;
				}
				totalTime += (t2 - t1);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.err.println("Total encoded:" + totalL / 1024 + " ko (" + totalL
				+ ") in " + totalTime / (1000 * 1000) + "ms");
		System.err.println(optimums + " optimum compression");
	}
}
