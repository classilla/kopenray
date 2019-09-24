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

import java.io.IOException;

import org.jopenray.server.thinclient.ThinClient;

public class DebugClient extends ThinClient {
	public DebugClient(String name) {
		super(name);
	}

	long l;

	@Override
	public void sendBytes(byte[] buffer, int bufferLength) throws IOException {
		System.out.println("DebugClient.sendBytes() length:" + bufferLength);
		l += bufferLength;
		System.out.println("Sent:" + l + " bytes -> " + (l / 1024) + " KB");
	}

}
