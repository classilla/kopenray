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

package org.jopenray.server.event;

import java.util.Date;

import javax.swing.table.AbstractTableModel;

public class EventTableModel extends AbstractTableModel implements
		EventManagerListener {

	private static final long serialVersionUID = -564688414777763911L;

	public EventTableModel() {
		EventManager.getInstance().addListUpdatedListener(this);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Date.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		case 3:
			return String.class;
		}
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Date";
		case 1:
			return "Type";
		case 2:
			return "Title";
		case 3:
			return "Description";
		}
		return "??";
	}

	@Override
	public int getRowCount() {
		return EventManager.getInstance().getEventCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Event e = getEvent(rowIndex);
		switch (columnIndex) {
		case 0:
			return new Date(e.getDate());
		case 1:
			return e.getType();
		case 2:
			return e.getTitle();
		case 3:
			return e.getDescription();
		}
		return "??";

	}

	public Event getEvent(int rowIndex) {
		return EventManager.getInstance().getEvent(rowIndex);

	}

	@Override
	public void eventListUpdated() {
		fireTableDataChanged();
	}

}
