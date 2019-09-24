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

package org.jopenray.test;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class KeyTest {
	public static void main(String[] args) {
		Runnable r = new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				JPanel p = new JPanel();
				p.setPreferredSize(new Dimension(80, 80));
				f.setContentPane(p);
				p.addKeyListener(new KeyListener() {

					@Override
					public void keyPressed(KeyEvent e) {
						System.out.println(".keyPressed()" + e);
					}

					@Override
					public void keyReleased(KeyEvent e) {
						System.out.println(".keyReleased()" + e);
					}

					@Override
					public void keyTyped(KeyEvent e) {
						System.out.println(".keyTyped()" + e);
					}

				});
				p.requestFocus();
				f.pack();
				f.setVisible(true);
			}
		};
		SwingUtilities.invokeLater(r);
	}
}
