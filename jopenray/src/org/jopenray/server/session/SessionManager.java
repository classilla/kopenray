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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jopenray.server.card.Card;
import org.jopenray.server.thinclient.ThinClient;

public class SessionManager {
	private final List<Session> l = new ArrayList<Session>();
	private static SessionManager instance;
	private final File f = new File("Configurations/sessions.xml");
	private final List<SessionManagerListener> listeners = new ArrayList<SessionManagerListener>();

	public static synchronized SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
			instance.load();
		}

		return instance;
	}

	private void load() {

		if (f.exists()) {
			try {
				SAXBuilder builder = new SAXBuilder();

				Document doc = builder.build(f);
				List<Element> l = doc.getRootElement().getChildren("session");
				for (Element element : l) {

					int id = Integer.valueOf(element.getChildText("id"));
					int type = Integer.valueOf(element.getChildText("type"));
					String name = element.getChildText("name");
					String server = element.getChildText("server");
					String login = element.getChildText("login");
					String password = element.getChildText("password");
					boolean isCardRequired = Boolean.parseBoolean(element
							.getChildText("cardRequired"));
					boolean isCardRestricted = Boolean.parseBoolean(element
							.getChildText("cardRestricted"));
					boolean isClientRestricted = Boolean.parseBoolean(element
							.getChildText("clientRestricted"));
					boolean isHardwareCursorUsed = Boolean.parseBoolean(element
							.getChildText("hardwareCursorUsed"));
					Session currentSession = new Session(name, id);
					currentSession.setProtocol(type);
					currentSession.setServer(server);
					currentSession.setLogin(login);
					currentSession.setPassword(password);
					currentSession.setCardRequired(isCardRequired);
					currentSession.setFilterCard(isCardRestricted);
					currentSession.setFilterClient(isClientRestricted);
					currentSession.setHardwareCursorUsed(isHardwareCursorUsed);
					List<Element> lIds = element.getChildren("allowedCardId");
					for (Element elementId : lIds) {
						currentSession.addAllowedCardId(elementId.getText());
					}

					List<Element> lClientIds = element
							.getChildren("allowedClientId");
					for (Element elementId : lClientIds) {
						currentSession.addAllowedClientId(elementId.getText());
					}

					if (!this.l.contains(currentSession)) {
						this.l.add(currentSession);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public synchronized void update(Session session) {

		save();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireSessionListUpdated();

			}
		});

	}

	public synchronized void addOrUpdate(Session session) {
		if (!l.contains(session)) {
			l.add(session);
			save();
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireSessionListUpdated();

			}
		});

	}

	void save() {
		Document doc = new Document();
		Element root = new Element("sessions");
		doc.addContent(root);

		final int sessionCount = this.getSessionCount();
		for (int i = 0; i < sessionCount; i++) {
			Session s = this.getSession(i);

			Element elementSession = new Element("session");

			Element elementId = new Element("id");
			elementId.setText(String.valueOf(s.getId()));
			elementSession.addContent(elementId);

			Element elementType = new Element("type");
			elementType.setText(String.valueOf(s.getProtocol()));
			elementSession.addContent(elementType);

			Element elementName = new Element("name");
			elementName.setText(s.getName());
			elementSession.addContent(elementName);

			Element elementServeur = new Element("server");
			elementServeur.setText(s.getServer());
			elementSession.addContent(elementServeur);

			Element elementLogin = new Element("login");
			elementLogin.setText(s.getLogin());
			elementSession.addContent(elementLogin);

			Element elementPassword = new Element("password");
			elementPassword.setText(s.getPassword());
			elementSession.addContent(elementPassword);

			Element elementCardRequired = new Element("cardRequired");
			elementCardRequired.setText(s.isCardRequired() ? "true" : "false");
			elementSession.addContent(elementCardRequired);

			Element elementCardRestricted = new Element("cardRestricted");
			elementCardRestricted.setText(s.isFilterCard() ? "true" : "false");
			elementSession.addContent(elementCardRestricted);

			Element elementClientRestricted = new Element("clientRestricted");
			elementClientRestricted.setText(s.isFilterClient() ? "true"
					: "false");
			elementSession.addContent(elementClientRestricted);

			List<Card> cards = s.getAllowedCards();
			for (Card card : cards) {
				Element elementCard = new Element("allowedCardId");
				elementCard.setText(card.getId());
				elementSession.addContent(elementCard);
			}
			List<ThinClient> clients = s.getAllowedClients();
			for (ThinClient client : clients) {
				Element elementClient = new Element("allowedClientId");
				elementClient.setText(client.getSerialNumber());
				elementSession.addContent(elementClient);
			}

			//
			Element elementHardwareCursorUsed = new Element(
					"hardwareCursorUsed");
			elementHardwareCursorUsed.setText(s.isHardwareCursorUsed() ? "true"
					: "false");
			elementSession.addContent(elementHardwareCursorUsed);

			root.addContent(elementSession);
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

	public synchronized void addListUpdatedListener(SessionManagerListener l) {
		this.listeners.add(l);

	}

	private synchronized void fireSessionListUpdated() {
		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i).sessionListUpdated();
		}
	}

	public synchronized int getSessionCount() {
		return this.l.size();
	}

	public synchronized Session getSession(int index) {
		return this.l.get(index);
	}

	public void check() {
		// TODO Auto-generated method stub

	}

	public Session getAvailableSessionFor(ThinClient client) {

		final int sessionCount = this.getSessionCount();
		for (int i = 0; i < sessionCount; i++) {
			Session s = this.getSession(i);
			List<Card> cards = s.getAllowedCards();
			for (Card card : cards) {
				if (card.getId().equals(client.getCardId())) {
					return s;
				}
			}
		}
		for (int i = 0; i < sessionCount; i++) {
			Session s = this.getSession(i);
			if (!s.isFilterCard()) {
				return s;
			}
		}

		return null;
	}

	public void remove(Session session) {
		if (l.contains(session)) {
			l.remove(session);
			save();
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireSessionListUpdated();

			}
		});

	}
}
