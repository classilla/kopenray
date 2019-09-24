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

package org.jopenray.authentication;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jopenray.server.thinclient.ThinClient;

public class AuthenticationMessage {

	String type;
	List<String> keys = new ArrayList<String>();
	List<String> values = new ArrayList<String>();

	public AuthenticationMessage(String string) {
		this.type = string;
	}

	public AuthenticationMessage() {
		// TODO Auto-generated constructor stub
	}

	public String getType() {
		return type;
	}

	public List<String> readFrom(String input) {
		List<String> l = fastSplit(input, ' ');
		type = l.get(0);
		for (int i = 1; i < l.size(); i++) {
			add(l.get(i));

		}
		return l;
	}

	private void add(String string) {
		// System.out.println("add:"+string);
		int i = string.indexOf('=');
		put(string.substring(0, i), string.substring(i + 1));

	}

	public void put(String key, String val) {
		keys.add(key);
		values.add(val);

	}

	public void print(PrintWriter out) {
		out.print(this.type);

		for (int i = 0; i < this.keys.size(); i++) {
			out.print(" " + this.keys.get(i) + "=" + this.values.get(i));
		}
		out.flush();
	}

	public static final List<String> fastSplit(final String string,
			final char sep) {
		final List<String> l = new ArrayList<String>();
		final int length = string.length();
		final char[] cars = string.toCharArray();
		int rfirst = 0;

		for (int i = 0; i < length; i++) {
			if (cars[i] == sep) {
				l.add(new String(cars, rfirst, i - rfirst));
				rfirst = i + 1;
			}
		}

		if (rfirst < length) {
			l.add(new String(cars, rfirst, length - rfirst));
		}
		return l;
	}

	public String get(String key) {
		String r = null;
		int i = this.keys.indexOf(key);
		if (i >= 0) {
			r = this.values.get(i);
		}
		return r;

	}

	public void exportProperties(ThinClient displayClient) {
		for (int i = 0; i < this.keys.size(); i++) {
			displayClient.put(this.keys.get(i), this.values.get(i));
		}

	}

	public boolean isCardRemoved() {
		if (get("event") != null) {
			return get("event").equals("remove");
		}
		return false;
	}

	public int getServerPort() {
		if (get("pn") != null) {
			return Integer.valueOf(get("pn"));
		}
		return -1;
	}
}
