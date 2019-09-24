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

public interface InputListener {

	public void mouseReleased(int button, int mouseX, int mouseY);

	public void mousePressed(int button, int mouseX, int mouseY);

	public void mouseMoved(int mouseX, int mouseY);

	public void mouseWheelUp(int mouseX, int mouseY);

	public void mouseWheelDown(int mouseX, int mouseY);

	public void keyPressed(int key, boolean shift, boolean ctrl, boolean alt,
			boolean meta, boolean altGr);

	public void keyReleased(int key);

}
