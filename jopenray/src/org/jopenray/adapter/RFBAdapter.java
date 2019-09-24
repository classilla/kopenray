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

package org.jopenray.adapter;

import org.jopenray.rfb.VncViewer;
import org.jopenray.server.session.Session;
import org.jopenray.server.thinclient.InputListener;
import org.jopenray.server.thinclient.ThinClient;

public class RFBAdapter implements InputListener {
	VncViewer v;

	public void start(final ThinClient displayClient, Session session) {
		System.err.println("DisplayClient:" + displayClient);

		v = new VncViewer(displayClient, new String[] { "HOST",
				session.getServer(), "PASSWORD", session.getPassword() });

		v.init();
		v.start();
	}

	public void stop() {
		v.stop();
	}

	@Override
	public void keyPressed(int key, boolean shift, boolean ctrl, boolean alt,
			boolean meta, boolean altGr) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(int key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(int button, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(int button, int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelDown(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelUp(int mouseX, int mouseY) {
		// TODO Auto-generated method stub

	}

}
