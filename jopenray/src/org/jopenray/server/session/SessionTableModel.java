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

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class SessionTableModel extends AbstractTableModel implements
		SessionManagerListener {

	private static final long serialVersionUID = -564688414777763911L;
	private JTable table;

	public SessionTableModel() {
		SessionManager.getInstance().addListUpdatedListener(this);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Boolean.class;
		case 2:
			return Integer.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0)
			return "Sessions";
		return "";
	}

	@Override
	public int getRowCount() {
		return SessionManager.getInstance().getSessionCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Session session = SessionManager.getInstance().getSession(
				rowIndex);
		switch (columnIndex) {
		case 0:
			return session.getName();
		case 1:

			return session.isFilterClient();
		case 2:

			if (session.isFilterCard()) {
				return 2;
			}
			if (session.isCardRequired()) {
				return 1;
			}
			return 0;
		default:
			return "??";

		}

	}

	public Session getSession(int rowIndex) {
		return SessionManager.getInstance().getSession(rowIndex);

	}

	public void setTable(JTable t) {
		this.table = t;

	}

	@Override
	public void sessionListUpdated() {
		int selectedRow = this.table.getSelectedRow();
		Session c = null;
		try {
			if (selectedRow >= 0) {
				c = this.getSession(selectedRow);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		this.fireTableDataChanged();
		if (c != null) {
			for (int i = 0; i < this.getRowCount(); i++) {
				if (this.getSession(i).equals(c)) {
					table.getSelectionModel().setSelectionInterval(i, i);
				}

			}
		}

	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
