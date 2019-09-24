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

package org.jopenray.server.card;

import java.util.Date;

public class Card {
	private final String id;
	private String name;
	private String type;
	private Date expirationDate; // null if never expire
	private boolean isEnabled;

	public Card(String id, String type) {
		this.id = id;
		this.type = type;
		this.name = id;
		expirationDate = null;
		isEnabled = true;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Card) {
			Card c = (Card) obj;
			return this.id.equals(c.id) && this.type.equals(c.type);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "Card:" + this.name + "[" + this.type + " " + this.id + "]";
	}
}
