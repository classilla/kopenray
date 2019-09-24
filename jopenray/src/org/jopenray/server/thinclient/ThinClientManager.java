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

package org.jopenray.server.thinclient;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class ThinClientManager {
	private final List<ThinClient> l = new ArrayList<ThinClient>();
	private final Map<String, ThinClient> map = new HashMap<String, ThinClient>();
	private static ThinClientManager instance;
	private final File f = new File("Configurations/clients.xml");

	public static ThinClientManager getInstance() {
		if (instance == null) {
			instance = new ThinClientManager();
			instance.load();
		}
		return instance;
	}

	private final List<ThinClientManagerListener> listeners = new ArrayList<ThinClientManagerListener>();

	public synchronized void addOrUpdate(ThinClient client) {
		if (client.getSerialNumber() == null) {
			return;
		}
		if (!l.contains(client)) {
			addClient(client);

		}
		// Update
		if (client.getSerialNumber() != null
				&& client.getSerialNumber().length() > 0) {
			map.put(client.getSerialNumber(), client);
		}
		save();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireClientListUpdated();

			}
		});

	}

	private void addClient(ThinClient client) {
		l.add(client);

	}

	public synchronized void addListUpdatedListener(ThinClientManagerListener l) {
		this.listeners.add(l);

	}

	private synchronized void fireClientListUpdated() {
		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i).clientListUpdated();
		}
	}

	public synchronized int getClientCount() {
		return this.l.size();
	}

	public synchronized ThinClient getClient(int index) {
		return this.l.get(index);
	}

	public void check() {
		// TODO Auto-generated method stub

	}

	public ThinClient getClientFromId(String id) {
		return map.get(id);
	}

	private void load() {

		if (f.exists()) {
			try {
				SAXBuilder builder = new SAXBuilder();

				Document doc = builder.build(f);
				List<Element> l = doc.getRootElement().getChildren("client");
				for (Element element : l) {

					String id = element.getChildText("id");
					String name = element.getChildText("name");
					String sWidth = element.getChildText("screenWidth");
					String sHeight = element.getChildText("screenHeight");
					int w = 1280;
					int h = 1024;
					if (sWidth != null) {
						try {
							w = Integer.parseInt(sWidth);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (sHeight != null) {
						try {
							h = Integer.parseInt(sHeight);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (id.length() > 0) {
						ThinClient client = new ThinClient(name, id, w, h);

						if (!this.l.contains(client)) {
							this.l.add(client);
							if (client.getSerialNumber() != null
									&& client.getSerialNumber().length() > 0) {
								map.put(client.getSerialNumber(), client);
							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	void save() {
		Document doc = new Document();
		Element root = new Element("clients");
		doc.addContent(root);

		final int clientCount = this.getClientCount();
		for (int i = 0; i < clientCount; i++) {
			ThinClient client = this.getClient(i);

			Element elementClient = new Element("client");

			Element elementId = new Element("id");
			elementId.setText(client.getSerialNumber());
			elementClient.addContent(elementId);

			Element elementName = new Element("name");
			elementName.setText(client.getName());
			elementClient.addContent(elementName);
			if (client.getSerialNumber() != null
					&& client.getSerialNumber().length() > 0) {
				root.addContent(elementClient);
			}

			Element elementScreenWidth = new Element("screenWidth");
			elementScreenWidth.setText(String.valueOf(client.getScreenWidth()));
			elementClient.addContent(elementScreenWidth);

			Element elementScreenHeight = new Element("screenHeight");
			elementScreenHeight.setText(String
					.valueOf(client.getScreenHeight()));
			elementClient.addContent(elementScreenHeight);

		}
		File fDir = new File("Configurations");

		if (!fDir.exists()) {
			fDir.mkdir();
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

	public void update(ThinClient client) {
		if (client.getSerialNumber() == null) {
			return;
		}
		if (!l.contains(client)) {
			addClient(client);

		}
		// Update
		if (client.getSerialNumber() != null
				&& client.getSerialNumber().length() > 0) {
			map.put(client.getSerialNumber(), client);
		}
		save();

	}

}
