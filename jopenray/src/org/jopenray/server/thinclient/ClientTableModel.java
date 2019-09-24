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

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class ClientTableModel extends AbstractTableModel implements
		ThinClientManagerListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6784201841899339306L;
	private JTable table;

	ClientTableModel() {
		ThinClientManager.getInstance().addListUpdatedListener(this);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return "Client";
	}

	@Override
	public int getRowCount() {
		return ThinClientManager.getInstance().getClientCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return ThinClientManager.getInstance().getClient(rowIndex).getName();
	}

	@Override
	public void clientListUpdated() {
		int selectedRow = this.table.getSelectedRow();
		ThinClient c = null;
		if (selectedRow >= 0) {
			c = this.getClient(selectedRow);
		}
		this.fireTableDataChanged();
		if (c != null) {
			for (int i = 0; i < this.getRowCount(); i++) {
				if (this.getClient(i).equals(c)) {
					table.getSelectionModel().setSelectionInterval(i, i);
				}

			}
		}
	}

	public ThinClient getClient(int rowIndex) {
		return ThinClientManager.getInstance().getClient(rowIndex);

	}

	public void setTable(JTable t) {
		this.table = t;

	}

}
