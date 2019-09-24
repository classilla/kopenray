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

package org.jopenray.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class JImage extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5600905043973642236L;
	private final Image image;
	private boolean centered;

	public JImage(String fileName) {
		this(new ImageIcon(fileName).getImage());
	}

	public JImage(URL url) {
		this(new ImageIcon(url).getImage());
	}

	public JImage(Image img) {
		this.image = img;
	}

	public void check() {
		if (this.image == null || this.image.getHeight(null) <= 0) {
			throw new IllegalStateException();
		}
	}

	protected void paintComponent(Graphics g) {

		g.setColor(this.getBackground());
		int imageW = this.image.getWidth(null);
		if (!centered) {
			g.fillRect(imageW, 0, this.getBounds().width - imageW, this
					.getBounds().height);
			g.drawImage(this.image, 0, 0, null);
		} else {
			int dx = (this.getBounds().width - imageW) / 2;
			g.fillRect(0, 0, dx, this.getBounds().height);
			g.fillRect(0, 0, dx + imageW, this.getBounds().height);
			g.drawImage(this.image, dx, 0, null);
		}
	}

	public Dimension getPreferredSize() {
		return this.getMinimumSize();
	}

	public Dimension getMinimumSize() {
		return new Dimension(this.image.getWidth(null), this.image
				.getHeight(null));
	}

	public void setCenterImage(boolean t) {
		this.centered = true;
	}

}
