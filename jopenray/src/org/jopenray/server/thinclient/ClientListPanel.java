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

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ClientListPanel extends JPanel {

	private static final long serialVersionUID = 5189150642711327382L;
	private JSplitPane split;

	public ClientListPanel() {
		final ClientTableModel dm = new ClientTableModel();
		final JTable t = new JTable(dm);
		dm.setTable(t);
		/*
		 * this.setLayout(new GridBagLayout()); GridBagConstraints c = new
		 * GridBagConstraints(); c.gridx = 0; c.gridy = 0; c.fill =
		 * GridBagConstraints.BOTH; c.weightx = 0; c.weighty = 1;
		 */
		this.setLayout(new GridLayout(1, 1));
		split = new JSplitPane();
		JScrollPane comp = new JScrollPane(t);
		comp.setMinimumSize(new Dimension(200, 200));
		comp.setPreferredSize(new Dimension(200, 200));
		split.setLeftComponent(comp);
		split.setRightComponent(new JPanel());

		this.add(split);

		t.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {

						if (!e.getValueIsAdjusting()) {
							int first = t.getSelectedRow();
							if (first >= 0) {
								ThinClient c = dm.getClient(first);
								if (c != null) {
									split
											.setRightComponent(new ThinClientPropertiesPanel(
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
