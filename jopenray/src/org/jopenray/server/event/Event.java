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

package org.jopenray.server.event;

public class Event {
	public static final int TYPE_INFO = 0;
	public static final int TYPE_WARNING = 1;
	public static final int TYPE_ERROR = 2;

	private final int type;
	private final String title;
	private final String description;
	private final long date;

	public Event(String title, String desc, int type) {
		this.title = title;
		this.description = desc;
		this.type = type;
		this.date = System.currentTimeMillis();
	}

	public int getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public long getDate() {
		return date;
	}

}
