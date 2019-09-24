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

import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import org.jopenray.server.card.Card;

public class CardSessionListModel extends AbstractListModel implements
		ListModel, SessionManagerListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2357465461187203953L;
	private Session session;
	private List<Card> cards;

	public CardSessionListModel(Session session) {
		this.session = session;
		cards = session.getAllowedCards();
		SessionManager.getInstance().addListUpdatedListener(this);
	}

	@Override
	public Object getElementAt(int index) {
		return cards.get(index);
	}

	@Override
	public int getSize() {
		return cards.size();
	}

	@Override
	public void sessionListUpdated() {
		cards = session.getAllowedCards();
		fireContentsChanged(this, 0, cards.size());

	}

}
