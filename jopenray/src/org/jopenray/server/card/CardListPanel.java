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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

public class CardListPanel extends JPanel {

	private static final long serialVersionUID = -8987431536818466922L;
	private JSplitPane split;

	public CardListPanel() {
		final CardTableModel dm = new CardTableModel();
		final JTable t = new JTable(dm);
		dm.setTable(t);
		t.getColumnModel().getColumn(0).setCellRenderer(
				new DefaultTableCellRenderer() {

					private static final long serialVersionUID = -9109954835956521771L;

					@Override
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						if (value instanceof Card) {
							Card c = (Card) value;
							value = c.getName();
							Component comp = super
									.getTableCellRendererComponent(table,
											value, isSelected, hasFocus, row,
											column);
							if (!isSelected) {
								if (!c.isEnabled()) {
									comp.setForeground(Color.GRAY);
								} else {
									comp.setForeground(Color.BLACK);
								}
							}
							return comp;
						} else {
							return super.getTableCellRendererComponent(table,
									value, isSelected, hasFocus, row, column);
						}
					}

				});

		this.setLayout(new BorderLayout());

		JPanel tools = new JPanel();
		tools.setOpaque(false);
		tools.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 3, 2);
		c.gridx = 0;
		/*
		 * c.gridy=0; tools.add(new JButton("Remove"),c); c.gridx++;
		 * tools.add(new JButton("Assign to user"),c);
		 */
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

		t.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {

						if (!e.getValueIsAdjusting()) {
							int first = t.getSelectedRow();
							if (first >= 0) {
								Card c = dm.getCard(first);
								if (c != null) {
									if (split.getRightComponent() instanceof CardPropertiesPanel) {
										CardPropertiesPanel up = (CardPropertiesPanel) split
												.getRightComponent();
										if (up.getCurrentCard().equals(c)) {
											return;
										}
									}

									split
											.setRightComponent(new CardPropertiesPanel(
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
	}

}
