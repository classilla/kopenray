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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class UserManager {
	private final List<User> l = new ArrayList<User>();
	private static UserManager instance;
	private final File f = new File("Configurations/users.xml");

	public static synchronized UserManager getInstance() {
		if (instance == null) {
			instance = new UserManager();
			instance.load();
		}
		return instance;
	}

	public void check() {

	}

	private void load() {

		if (f.exists()) {
			try {
				SAXBuilder builder = new SAXBuilder();

				Document doc = builder.build(f);
				List<Element> l = doc.getRootElement().getChildren("user");
				for (Element element : l) {

					String id = element.getChildText("id");
					int userId = Integer.valueOf(id);
					String fName = element.getChildText("firstname");
					String lName = element.getChildText("lastname");

					User c = new User(userId, fName, lName);

					if (!this.l.contains(c)) {
						this.l.add(c);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private final List<UserManagerListener> listeners = new ArrayList<UserManagerListener>();

	public synchronized void addOrUpdate(User user) {
		if (!l.contains(user)) {
			l.add(user);
			save();
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireUserListUpdated();

			}
		});

	}

	public synchronized void update(User user) {

		save();

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				fireUserListUpdated();

			}
		});

	}

	void save() {
		Document doc = new Document();
		Element root = new Element("users");
		doc.addContent(root);

		for (int i = 0; i < this.getUserCount(); i++) {
			final User user = this.getUser(i);
			Element userElement = new Element("user");
			Element id = new Element("id");
			id.setText(String.valueOf(user.getId()));
			Element firstname = new Element("firstname");
			firstname.setText(user.getFirstName());
			Element lastname = new Element("lastname");
			lastname.setText(user.getLastName());
			userElement.addContent(id);
			userElement.addContent(firstname);
			userElement.addContent(lastname);

			root.addContent(userElement);
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

	public synchronized void addListUpdatedListener(UserManagerListener l) {
		this.listeners.add(l);

	}

	private synchronized void fireUserListUpdated() {
		for (int i = 0; i < this.listeners.size(); i++) {
			this.listeners.get(i).userListUpdated();
		}
	}

	public synchronized int getUserCount() {
		return this.l.size();
	}

	public synchronized User getUser(int index) {
		return this.l.get(index);
	}
}
