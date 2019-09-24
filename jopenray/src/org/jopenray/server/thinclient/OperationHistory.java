/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2019 Cameron Kaiser
 *  All rights reserved.
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

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import org.jopenray.operation.Operation;
import org.jopenray.operation.PadOperation;
import org.jopenray.operation.UnknownACOperation;

public class OperationHistory {
	private static final boolean DEBUG = false;
	LinkedList<Operation> l = new LinkedList<Operation>();
	int maxHistory;

	public OperationHistory() {
		maxHistory = 524288;
	}

	public void add(Operation o) {
		synchronized (l) {
			l.add(o);
			if (l.size() > maxHistory) {
				System.out.println("OVERFLOWED HISTORY!\n");
				l.removeFirst();
			}
		}

	}


	public void resend(DisplayWriterThread displayWriterThread, int from, int to) {
		if (DEBUG)
		System.out.println("Resend: from :" + from + " to " + to);

		// The buffer is empty, we can't resend anything.
		if (l.size() == 0) return;

		DisplayMessage m = new DisplayMessage(displayWriterThread, true /* resending */);
		ArrayList<Operation> operations = new ArrayList<Operation>(256);
		int toFind = to - from + 1; // Nombre de packet a trouver
		int found = 0;
		int i = from;

		// Profiling shows this is VERY, VERY hot.
		synchronized (l) {
			ListIterator<Operation> it = l.listIterator();
			Operation o;

			while (it.hasNext()) {
				o = (Operation) it.next();
				if (o.getSequence() == i) {
					//System.out.println("OperationHistory.resend() found: seq"
					//+ i);
					operations.add(o);
					i++;
					found++;
				}
				if (found == toFind) {
					break;
				}
			}
		}
		if (found == 0) {
			if (DEBUG)
			System.out.println("NACK FAILED to find "+i);
			return;
		}

		// Add the found operations en masse.
		m.addOperations(operations);

		displayWriterThread.addHighPriorityMessage(m);
		DisplayMessage mstatus = new DisplayMessage(displayWriterThread);
		mstatus.addOperation(new PadOperation());
		mstatus.addOperation(new UnknownACOperation(0, 1, 0, to));
		displayWriterThread.addHighPriorityMessage(mstatus);

		// m.dump();

	}

	public void clear() {
		synchronized (l) {
			l.clear();
		}
	}
}
