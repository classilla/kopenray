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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class EventManager {
	private final List<Event> l = new ArrayList<Event>();
	private static EventManager instance;
	private final File f = new File("Configurations/events.xml");
	private List<EventManagerListener> listeners = new ArrayList<EventManagerListener>(
			128);

	public static synchronized EventManager getInstance() {
		if (instance == null) {
			instance = new EventManager();
			instance.load();
		}

		return instance;
	}

	private void load() {

		if (f.exists()) {
			try {
				SAXBuilder builder = new SAXBuilder();

				Document doc = builder.build(f);
				// TODO

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public synchronized void add(Event e) {
		l.add(e);
		fireClardListUpdated();
	}

	void save() {
		Document doc = new Document();
		Element root = new Element("cards");
		doc.addContent(root);

		/*
		 * for (int i = 0; i < this.getCardCount(); i++) { Element card = new
		 * Element("card"); Element id = new Element("id");
		 * id.setText(this.getCard(i).getId()); Element type = new
		 * Element("type"); type.setText(this.getCard(i).getType()); Element
		 * name = new Element("name"); name.setText(this.getCard(i).getName());
		 * Element expiration = new Element("expiration"); Date expirationDate =
		 * this.getCard(i).getExpirationDate(); if (expirationDate != null) {
		 * expiration.setText(String.valueOf(expirationDate.getTime())); }
		 * Element enabled = new Element("enabled"); if
		 * (this.getCard(i).isEnabled()) { enabled.setText("true"); } else {
		 * enabled.setText("false"); } card.addContent(id);
		 * card.addContent(type); card.addContent(name);
		 * card.addContent(expiration); card.addContent(enabled);
		 * root.addContent(card); }
		 */

		FileOutputStream fOp;
		try {
			fOp = new FileOutputStream(f);
			// Raw output
			XMLOutputter outp = new XMLOutputter();
			outp.output(doc, fOp);
			fOp.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public synchronized void addListUpdatedListener(EventManagerListener l) {
		this.listeners.add(l);

	}

	private synchronized void fireClardListUpdated() {
		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i).eventListUpdated();
		}
	}

	public synchronized int getEventCount() {
		return this.l.size();
	}

	public synchronized Event getEvent(int index) {
		return this.l.get(index);
	}
}
