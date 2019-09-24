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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jopenray.server.card.Card;

public class SessionPropertiesPanel extends JPanel implements DocumentListener,
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -415155945748614934L;
	private Session currentSession;
	private JTextField textName, textServer;

	private JTextField textLogin;
	private JPasswordField textPassword;
	private JCheckBox checkCardRequired;
	private JComboBox comboType;
	private JCheckBox checkRetrictCard;
	private JCheckBox checkRetrictClient;
	private JCheckBox checkHidePointer;

	public SessionPropertiesPanel(final Session session) {
		this.currentSession = session;
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 4, 2, 2);
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		// Line 1
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Name"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		textName = new JTextField();
		textName.setText(this.currentSession.getName());
		this.add(textName, c);
		// Line 2
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;

		this.add(new JLabel("Type"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.NONE;
		comboType = new JComboBox(new String[] { "RDP", "RFB", "Image", "SSH", "Tetpnc" });
		comboType.setSelectedIndex(this.currentSession.getProtocol());
		this.add(comboType, c);
		// Line 3
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		final JLabel comp = new JLabel("Server");
		comp.setFont(comp.getFont().deriveFont(Font.BOLD));
		this.add(comp, c);

		// Line 2
		c.gridy++;
		c.gridwidth = 1;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;

		this.add(new JLabel("Server address"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		textServer = new JTextField();
		textServer.setText(this.currentSession.getServer());
		this.add(textServer, c);
		// Line 2
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Login"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		textLogin = new JTextField();
		textLogin.setText(this.currentSession.getLogin());
		this.add(textLogin, c);
		// Line 2
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Password"), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		textPassword = new JPasswordField();
		textPassword.setText(this.currentSession.getPassword());
		this.add(textPassword, c);
		// Line 3
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		final JLabel comp2 = new JLabel("Security");
		comp2.setFont(comp.getFont());
		this.add(comp2, c);
		// Line 4

		c.gridwidth = 2;
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(new JLabel(" "), c);
		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		checkCardRequired = new JCheckBox("a card is a required");
		checkCardRequired.setSelected(this.currentSession.isCardRequired());
		this.add(checkCardRequired, c);
		// Line 4

		c.gridwidth = 2;
		c.gridy++;
		c.gridx = 0;

		c.weightx = 1;

		c.fill = GridBagConstraints.HORIZONTAL;
		checkRetrictCard = new JCheckBox("restrict to the following cards :");
		checkRetrictCard.setSelected(this.currentSession.isFilterCard());
		this.add(checkRetrictCard, c);
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;

		ListModel listModel = new CardSessionListModel(session);

		final JList cardList = new JList(listModel);
		cardList.setCellRenderer(new CardListCellRenderer());
		this.add(new JScrollPane(cardList), c);
		//
		c.gridx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridy++;
		c.weighty = 0;

		this.add(createCardTools(), c);
		//
		c.gridwidth = 2;
		c.gridy++;
		c.gridx = 0;
		c.weighty = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		checkRetrictClient = new JCheckBox(
				"restrict to the following clients :");
		checkRetrictClient.setSelected(this.currentSession.isFilterClient());
		this.add(checkRetrictClient, c);
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JScrollPane(new JList(new String[] { "" })), c);

		// Line 3
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.weighty = 0;
		final JLabel comp3 = new JLabel("Misc");
		comp3.setFont(comp.getFont());
		this.add(comp3, c);
		// Line 4

		c.gridwidth = 2;
		c.gridy++;
		c.gridx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		checkHidePointer = new JCheckBox(
				"mouse pointer is managed by the operating system");
		checkHidePointer.setSelected(!this.currentSession
				.isHardwareCursorUsed());
		this.add(checkHidePointer, c);

		// Line 2
		c.gridy++;
		c.gridx = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.SOUTHWEST;
		this.add(new JLabel("Id:" + currentSession.getId()), c);
		textName.getDocument().addDocumentListener(this);

		comboType.addActionListener(this);
		textServer.getDocument().addDocumentListener(this);
		textLogin.getDocument().addDocumentListener(this);
		textPassword.getDocument().addDocumentListener(this);
		checkCardRequired.addActionListener(this);
		checkRetrictCard.addActionListener(this);
		checkRetrictClient.addActionListener(this);
		checkHidePointer.addActionListener(this);
	}

	private void commitChanges() {
		currentSession.setName(textName.getText());
		currentSession.setProtocol(comboType.getSelectedIndex());
		currentSession.setServer(textServer.getText());
		currentSession.setLogin(textLogin.getText());
		currentSession.setPassword(new String(textPassword.getPassword()));
		currentSession.setCardRequired(checkCardRequired.isSelected());
		currentSession.setFilterCard(checkRetrictCard.isSelected());
		currentSession.setFilterClient(checkRetrictClient.isSelected());
		currentSession.setHardwareCursorUsed(!checkHidePointer.isSelected());

		SessionManager.getInstance().update(currentSession);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		commitChanges();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		commitChanges();

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		commitChanges();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		commitChanges();

	}

	public Session getCurrentSession() {

		return this.currentSession;
	}

	public JComponent createCardTools() {
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 2, 2);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weighty = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		final JComboBox comboCards = new JComboBox(new CardComboBoxModel());
		comboCards.setRenderer(new CardListCellRenderer());
		p.add(comboCards, c);
		c.weightx = 0;
		c.gridx++;
		JButton bAddCard = new JButton("Add");
		bAddCard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object selectedCard = comboCards.getSelectedItem();
				if (selectedCard != null) {
					currentSession.addAllowedCardId(((Card) selectedCard)
							.getId());
					SessionManager.getInstance().update(currentSession);
				}
			}
		});
		p.add(bAddCard, c);
		c.gridx++;
		JButton bRemoveCard = new JButton("Remove");
		bRemoveCard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object selectedCard = comboCards.getSelectedItem();
				if (selectedCard != null) {
					currentSession.removeAllowedCardId(((Card) selectedCard)
							.getId());
					SessionManager.getInstance().update(currentSession);
				}
			}
		});
		p.add(bRemoveCard, c);
		return p;
	}
}
