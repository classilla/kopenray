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

package org.jopenray.server.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jopenray.server.card.Card;
import org.jopenray.server.card.CardManager;
import org.jopenray.server.thinclient.ThinClient;
import org.jopenray.server.thinclient.ThinClientManager;

public class Session {
	public static final int RDP = 0;
	public static final int RFB = 1;
	public static final int IMAGE = 2;
	public static final int SSH = 3;
	public static final int TETPNC = 4;
	private String name;

	private boolean cardRequired = true;
	private boolean filterCard = false;
	private Set<String> allowedCardIds = new HashSet<String>();

	private int protocol = RDP;
	private String server;
	private String login;
	private String password;

	private boolean filterClient = false;
	private Set<String> allowedClientIds = new HashSet<String>();
	private int id;
	private static int nextId = 0;

	private boolean hardwareCursorUsed = true;

	public boolean isHardwareCursorUsed() {
		return hardwareCursorUsed;
	}

	public void setHardwareCursorUsed(boolean hardwareCursorUsed) {
		this.hardwareCursorUsed = hardwareCursorUsed;
	}

	public Session(String n, int id) {
		this.name = n;
		this.id = id;

		if (id >= nextId) {
			nextId = id + 1;
		}
	}

	public Session(String string) {
		this.name = string;
		this.id = nextId;
		nextId++;
	}

	public String getProtocolName(int proto) {
		switch (proto) {
		case RDP:
			return "RDP";
		case RFB:
			return "RFB";
		case SSH:
			return "SSH";
		case IMAGE:
			return "Image";
		case TETPNC:
			return "Tetpnc";
		default:
			return "???";
		}
	}

	public String getName() {

		if (this.name == null) {
			String name = "RDP";
			if (protocol == RFB) {
				name = "RFB";
			}
			this.name = name;
		}
		return this.name;
	}

	public boolean isCardRequired() {
		return cardRequired;
	}

	public void setCardRequired(boolean cardRequired) {
		this.cardRequired = cardRequired;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isFilterClient() {
		return filterClient;
	}

	public void setFilterClient(boolean filterClient) {
		this.filterClient = filterClient;
	}

	public boolean isFilterCard() {
		return filterCard;
	}

	public void setFilterCard(boolean filterCard) {
		this.filterCard = filterCard;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void addAllowedCardId(String id) {
		if (!this.allowedCardIds.contains(id))
			this.allowedCardIds.add(id);
	}

	public void removeAllowedCardId(String id) {
		this.allowedCardIds.remove(id);
	}

	public void addAllowedClientId(String id) {
		this.allowedClientIds.add(id);
	}

	public List<Card> getAllowedCards() {
		List<Card> l = new ArrayList<Card>(this.allowedCardIds.size());

		for (String id : this.allowedCardIds) {
			Card c = CardManager.getInstance().getCardFromId(id);
			l.add(c);
		}

		return l;
	}

	public List<ThinClient> getAllowedClients() {
		List<ThinClient> l = new ArrayList<ThinClient>(this.allowedClientIds
				.size());
		for (String id : this.allowedClientIds) {
			ThinClient c = ThinClientManager.getInstance().getClientFromId(id);
			l.add(c);
		}

		return l;
	}

	public String getAllInfo() {

		return this.getProtocolName(this.getProtocol()) + " "
				+ this.getServer() + " " + this.getLogin() + "/"
				+ this.getPassword() + " Card:" + this.getAllowedCards();
	}

}
