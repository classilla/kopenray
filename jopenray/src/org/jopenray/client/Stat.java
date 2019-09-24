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

package org.jopenray.client;

public class Stat {
	private final long byteCount;
	private final long connectionTime;
	private final long idleTime;
	private final long packetLostCount;
	private final long packetCount;

	Stat(long byteCount, long connectionTime, long idleTime,
			long packetLostCount, long packetCount) {
		this.byteCount = byteCount;
		this.connectionTime = connectionTime;
		this.idleTime = idleTime;
		this.packetLostCount = packetLostCount;
		this.packetCount = packetCount;

	}

	public long getByteCount() {
		return byteCount;
	}

	public long getConnectionTime() {
		return connectionTime;
	}

	public long getIdleTime() {
		return idleTime;
	}

	public long getLossCount() {
		return packetLostCount;
	}

	public long getPacketCount() {
		return packetCount;
	}

}
