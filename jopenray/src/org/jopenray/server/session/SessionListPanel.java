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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.jopenray.server.OpenRayServer;

public class SessionListPanel extends JPanel {

	private static final long serialVersionUID = -8987431536818466922L;
	private JSplitPane split;

	public SessionListPanel() {
		final SessionTableModel dm = new SessionTableModel();
		final JTable t = new JTable(dm);
		dm.setTable(t);
		t.getColumnModel().getColumn(1).setCellRenderer(
				new DefaultTableCellRenderer() {
					/**
					 * 
					 */
					private static final long serialVersionUID = -6163179793076760086L;

					@Override
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						if (((Boolean) value)) {
							this.setIcon(new ImageIcon(OpenRayServer.class
									.getResource("thinclient.png")));
						} else {
							this.setIcon(null);
						}
						value = "";
						return super.getTableCellRendererComponent(table,
								value, isSelected, hasFocus, row, column);
					}

				});
		t.getColumnModel().getColumn(1).setMaxWidth(17);
		t.getColumnModel().getColumn(1).setMaxWidth(17);
		t.getColumnModel().getColumn(1).setPreferredWidth(17);
		t.getColumnModel().getColumn(2).setCellRenderer(
				new DefaultTableCellRenderer() {
					@Override
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						if (((Integer) value) == 1) {
							this.setIcon(new ImageIcon(OpenRayServer.class
									.getResource("card.png")));
						} else if (((Integer) value) == 2) {
							this.setIcon(new ImageIcon(OpenRayServer.class
									.getResource("card_red.png")));
						} else {
							this.setIcon(null);
						}
						value = "";
						return super.getTableCellRendererComponent(table,
								value, isSelected, hasFocus, row, column);
					}

				});
		t.getColumnModel().getColumn(2).setMaxWidth(17);
		t.getColumnModel().getColumn(2).setMaxWidth(17);
		t.getColumnModel().getColumn(2).setPreferredWidth(17);

		this.setLayout(new BorderLayout());

		JPanel tools = new JPanel();
		tools.setOpaque(false);
		tools.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 3, 2);
		c.gridx = 0;
		c.gridy = 0;
		final JButton removeSessionButton = new JButton("Remove");
		tools.add(removeSessionButton, c);
		c.gridx++;
		final JButton addSessionButton = new JButton("Add");
		tools.add(addSessionButton, c);

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
								Session c = dm.getSession(first);
								if (split.getRightComponent() instanceof SessionPropertiesPanel) {
									SessionPropertiesPanel up = (SessionPropertiesPanel) split
											.getRightComponent();
									if (up.getCurrentSession().equals(c)) {
										return;
									}
								}

								if (c != null) {
									split
											.setRightComponent(new SessionPropertiesPanel(
													c));
								} else {
									System.err
											.println("SessionListPanel: First index:"
													+ first);
								}
							}
						}

					}
				});

		addSessionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Session u = new Session("New session");
				SessionManager.getInstance().addOrUpdate(u);

			}
		});
		removeSessionButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int first = t.getSelectedRow();
				if (first >= 0) {
					Session c = dm.getSession(first);
					SessionManager.getInstance().remove(c);
				}

			}
		});

	}
}
