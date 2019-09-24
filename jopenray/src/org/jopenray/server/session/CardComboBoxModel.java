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

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.jopenray.server.card.CardManager;
import org.jopenray.server.card.CardManagerListener;

public class CardComboBoxModel extends AbstractListModel implements
		ComboBoxModel, CardManagerListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4031486954747080284L;
	private Object selectedObject;

	CardComboBoxModel() {
		CardManager.getInstance().addListUpdatedListener(this);
	}

	public void setSelectedItem(Object anObject) {
		if ((selectedObject != null && !selectedObject.equals(anObject))
				|| selectedObject == null && anObject != null) {
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	public Object getSelectedItem() {
		return selectedObject;
	}

	@Override
	public Object getElementAt(int index) {
		return CardManager.getInstance().getCard(index);
	}

	@Override
	public int getSize() {
		return CardManager.getInstance().getCardCount();
	}

	@Override
	public void cardListUpdated() {
		fireContentsChanged(this, -1, -1);
	}

}
