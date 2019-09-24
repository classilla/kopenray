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

package org.jopenray.server.user;

import java.util.ArrayList;
import java.util.List;

import org.jopenray.server.card.Card;

public class User {

	private String firstName;
	private String lastName;
	private final List<Card> cards = new ArrayList<Card>();
	private int id;
	private static int nextId = 0;

	public User(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.id = nextId;
		nextId++;
	}

	public User(int userId, String firstName, String lastName) {
		this.id = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		if (id >= nextId) {
			nextId = id + 1;
		}
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User u = (User) obj;
			return u.id == this.id;
		}
		return super.equals(obj);
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getName() {

		return this.getFirstName() + " " + this.getLastName();
	}

	public void setFirstName(String text) {
		text = text.trim();

	}

	public void setLastName(String text) {
		text = text.trim();

	}

}
