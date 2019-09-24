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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class UserPropertiesPanel extends JPanel implements DocumentListener {

	private User currentUser;
	private JTextField textLastName;
	private JTextField textFirstName;

	public UserPropertiesPanel(User user) {
		this.currentUser = user;
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 4, 2, 2);

		c.gridy = 0;
		// Line 1
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(new JLabel("First name"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		textFirstName = new JTextField();
		textFirstName.setText(this.currentUser.getFirstName());
		this.add(textFirstName, c);

		//
		// Line 2
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Last name"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		textLastName = new JTextField();
		textLastName.setText(this.currentUser.getLastName());
		this.add(textLastName, c);
		// Line 2
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.weighty = 1;
		c.anchor = GridBagConstraints.SOUTHWEST;
		this.add(new JLabel("Id:" + currentUser.getId()), c);
		textFirstName.getDocument().addDocumentListener(this);
		textLastName.getDocument().addDocumentListener(this);

	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		currentUser.setFirstName(textFirstName.getText());
		currentUser.setLastName(textLastName.getText());

		UserManager.getInstance().update(currentUser);

	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		changedUpdate(e);

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		changedUpdate(e);

	}

	public User getCurrentUser() {
		return currentUser;
	}
}
