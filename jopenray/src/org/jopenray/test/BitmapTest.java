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

import org.jopenray.server.thinclient.DisplayWriterThread;

public class BitmapTest {

	public static void main(String[] args) {

		System.out.println("Starting");

		DisplayWriterThread w = new DisplayWriterThread(new DebugClient(
				"Debug Client"));

		w.sendImage("698_0.png", 100, 100);
		w.start();
		try {
			Thread.sleep(3600 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
