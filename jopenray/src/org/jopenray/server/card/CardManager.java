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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class CardManager {

	private static CardManager instance;
	private final File f = new File("Configurations/cards.xml");
	private final List<CardManagerListener> listeners = new ArrayList<CardManagerListener>();

	// Id (String) , Card
	private Map<String, Card> map = new HashMap<String, Card>();
	private final List<Card> l = new ArrayList<Card>();

	public static synchronized CardManager getInstance() {
		if (instance == null) {
			instance = new CardManager();
			instance.load();
		}

		return instance;
	}

	private void load() {

		if (f.exists()) {
			try {
				SAXBuilder builder = new SAXBuilder();

				Document doc = builder.build(f);
				List<Element> l = doc.getRootElement().getChildren("card");
				for (Element element : l) {

					String id = element.getChildText("id");
					String type = element.getChildText("type");
					String name = element.getChildText("name");
					String expiration = element.getChildText("expiration");
					String enabled = element.getChildText("enabled");

					Card c = new Card(id, type);
					c.setName(name);
					if (expiration != null && expiration.length() > 0) {
						long lDate = Long.parseLong(expiration);
						c.setExpirationDate(new Date(lDate));
					}
					if (enabled != null && enabled.equals("false")) {
						c.setEnabled(false);
					}

					if (!this.l.contains(c)) {
						addCard(c);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void addCard(Card c) {
		this.l.add(c);
		this.map.put(c.getId(), c);
	}

	public synchronized void update(Card card) {

		save();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireClardListUpdated();

			}
		});
	}

	public synchronized void addOrUpdate(Card card) {
		if (!l.contains(card)) {
			addCard(card);
			save();
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireClardListUpdated();

			}
		});

	}

	void save() {
		Document doc = new Document();
		Element root = new Element("cards");
		doc.addContent(root);

		for (int i = 0; i < this.getCardCount(); i++) {
			Element card = new Element("card");
			Element id = new Element("id");
			id.setText(this.getCard(i).getId());
			Element type = new Element("type");
			type.setText(this.getCard(i).getType());
			Element name = new Element("name");
			name.setText(this.getCard(i).getName());
			Element expiration = new Element("expiration");
			Date expirationDate = this.getCard(i).getExpirationDate();
			if (expirationDate != null) {
				expiration.setText(String.valueOf(expirationDate.getTime()));
			}
			Element enabled = new Element("enabled");
			if (this.getCard(i).isEnabled()) {
				enabled.setText("true");
			} else {
				enabled.setText("false");
			}
			card.addContent(id);
			card.addContent(type);
			card.addContent(name);
			card.addContent(expiration);
			card.addContent(enabled);
			root.addContent(card);
		}

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

	public synchronized void addListUpdatedListener(CardManagerListener l) {
		this.listeners.add(l);

	}

	private synchronized void fireClardListUpdated() {
		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i).cardListUpdated();
		}
	}

	public synchronized int getCardCount() {
		return this.l.size();
	}

	public synchronized Card getCard(int index) {
		return this.l.get(index);
	}

	public void check() {
		// TODO Auto-generated method stub

	}

	public Card getCardFromId(String id) {
		return map.get(id);
	}
}
