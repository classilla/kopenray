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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jopenray.server.thinclient.ThinClient;

public class CardPropertiesTableModel extends AbstractTableModel implements
		PropertyChangeListener {
	ThinClient c;

	public CardPropertiesTableModel(ThinClient c) {
		if (c == null) {
			throw new IllegalArgumentException("DisplayClient null");
		}
		this.c = c;
		this.c.addPropertyChangeListeneer(this);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -4707314332094004994L;

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return "Name";
		}
		return "Value";
	}

	@Override
	public int getRowCount() {
		return c.getPropertyCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return c.getPropertyName(rowIndex);
		}
		return c.getPropertyValue(rowIndex);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		this.fireTableDataChanged();

	}

}
