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

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class UserTableModel extends AbstractTableModel implements
		UserManagerListener {

	private static final long serialVersionUID = -564688414777763911L;
	private JTable table;

	public UserTableModel() {
		UserManager.getInstance().addListUpdatedListener(this);
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
		return "Registered users";
	}

	@Override
	public int getRowCount() {
		return UserManager.getInstance().getUserCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return UserManager.getInstance().getUser(rowIndex).getName();
	}

	@Override
	public void userListUpdated() {
		int selectedRow = this.table.getSelectedRow();
		User c = null;
		if (selectedRow >= 0) {
			c = this.getUser(selectedRow);
		}
		this.fireTableDataChanged();
		if (c != null) {
			for (int i = 0; i < this.getRowCount(); i++) {
				if (this.getUser(i).equals(c)) {
					table.getSelectionModel().setSelectionInterval(i, i);
				}

			}
		}
	}

	public User getUser(int rowIndex) {
		return UserManager.getInstance().getUser(rowIndex);

	}

	public void setTable(JTable t) {
		this.table = t;

	}

}
