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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class UserListPanel extends JPanel {

	private static final long serialVersionUID = -8987431536818466922L;
	private JSplitPane split;

	public UserListPanel() {
		final UserTableModel dm = new UserTableModel();
		final JTable t = new JTable(dm);
		dm.setTable(t);

		this.setLayout(new BorderLayout());

		JPanel tools = new JPanel();
		tools.setOpaque(false);
		tools.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 3, 2);
		c.gridx = 0;
		c.gridy = 0;
		tools.add(new JButton("Remove"), c);
		c.gridx++;
		final JButton addUserButton = new JButton("Add");
		tools.add(addUserButton, c);

		JPanel blank = new JPanel();
		blank.setOpaque(false);
		c.weightx = 1;
		c.gridx++;
		tools.add(blank, c);
		this.add(tools, BorderLayout.NORTH);

		split = new JSplitPane();
		JScrollPane comp = new JScrollPane(t);
		comp.setMinimumSize(new Dimension(200, 200));
		comp.setPreferredSize(new Dimension(200, 200));
		split.setLeftComponent(comp);
		split.setRightComponent(new JPanel());

		this.add(split, BorderLayout.CENTER);
		t.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		t.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {

						if (!e.getValueIsAdjusting()) {
							int first = t.getSelectedRow();
							if (first >= 0) {
								User c = dm.getUser(first);

								if (split.getRightComponent() instanceof UserPropertiesPanel) {
									UserPropertiesPanel up = (UserPropertiesPanel) split
											.getRightComponent();
									if (up.getCurrentUser().equals(c)) {
										return;
									}
								}

								if (c != null) {
									split
											.setRightComponent(new UserPropertiesPanel(
													c));
								} else {
									System.err
											.println("ClientListPanel: First index:"
													+ first);
								}
							}
						}

					}
				});
		addUserButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				User u = new User("Anonymous", "User");
				UserManager.getInstance().addOrUpdate(u);

			}
		});

	}

}
