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

package org.jopenray.server;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import org.jopenray.server.card.CardListPanel;
import org.jopenray.server.event.EventListPanel;
import org.jopenray.server.session.SessionListPanel;
import org.jopenray.server.thinclient.ClientListPanel;
import org.jopenray.server.user.UserListPanel;
import org.jopenray.util.JImage;

public class ServerFrame extends JFrame {

	ServerFrame() {
		setTitle("kOpenRay Server Console");
		JPanel p = new JPanel();
		ImageIcon i = new ImageIcon();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;

		p.add(new JImage(this.getClass().getResource("logo.png")), c);
		JPanel blank = new JPanel();
		blank.setBackground(Color.WHITE);
		blank.setOpaque(true);
		c.gridx++;
		c.weightx = 1;
		p.add(blank, c);
		// Separator
		c.gridx = 0;

		c.gridy++;
		c.gridwidth = 2;
		p.add(new JSeparator(JSeparator.HORIZONTAL), c);
		// Tab
		c.gridx = 0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridy++;
		JTabbedPane tab = new JTabbedPane();
		tab.setOpaque(false);

		// Clients
		ClientListPanel component = new ClientListPanel();
		component.setOpaque(false);
		tab.add("Clients", component);
		// Cards
		CardListPanel component2 = new CardListPanel();
		component2.setOpaque(false);
		tab.add("Cards", component2);
		// Users
		// Sessions
		UserListPanel component3 = new UserListPanel();
		component3.setOpaque(false);
		tab.add("Users", component3);

		// Sessions
		SessionListPanel component4 = new SessionListPanel();
		component4.setOpaque(false);
		tab.add("Sessions", component4);

		// Events
		EventListPanel component5 = new EventListPanel();
		component5.setOpaque(false);
		tab.add("Events", component5);

		c.weighty = 1;
		p.add(tab, c);
		this.setContentPane(p);

		setSize(600, 800);
	}
}
