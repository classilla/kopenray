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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CardPropertiesPanel extends JPanel {

	private static final long serialVersionUID = 6917316987752961439L;
	private Card currentCard;

	public CardPropertiesPanel(final Card card) {
		if (card == null) {
			throw new IllegalArgumentException("Card null");
		}
		this.currentCard = card;

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;

		//
		c.gridx = 0;
		c.weightx = 0;
		// TODO: ouvrir une list des utilisateurs.
		this.add(new JLabel("Identifier"), c);
		// Line 1
		c.gridx = 0;
		c.weightx = 0;
		this.add(new JLabel("Identifier"), c);
		c.weightx = 1;
		c.gridx++;
		this.add(new JLabel(card.getId() + " (" + card.getType() + ")"), c);
		// Line 2
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		this.add(new JLabel("Name"), c);
		c.weightx = 1;
		c.gridx++;
		final JTextField textName = new JTextField(card.getName());
		this.add(textName, c);
		// Line 3
		c.gridx = 0;
		c.gridy++;
		c.weightx = 0;
		this.add(new JLabel("Status"), c);
		c.weightx = 1;
		c.gridx++;
		final JCheckBox checkEnabled = new JCheckBox("enabled");
		checkEnabled.setSelected(card.isEnabled());
		this.add(checkEnabled, c);

		// Line 4
		c.gridx = 1;
		c.gridy++;
		c.weighty = 1;

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHEAST;
		this.add(new JPanel(), c);
		// Listeners
		textName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
				card.setName(textName.getText());
				CardManager.getInstance().update(card);

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);

			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);

			}
		});

		checkEnabled.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				card.setEnabled(checkEnabled.isSelected());
				CardManager.getInstance().update(card);

			}
		});
	}

	public Card getCurrentCard() {
		return this.currentCard;
	}

}
