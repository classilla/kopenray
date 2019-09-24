/* Pen.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.7 $
 * Author: $Author: telliott $
 * Date: $Date: 2005/09/27 14:15:40 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: 
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 * 
 * (See gpl.txt for details of the GNU General Public License.)
 * 
 */
package org.jopenray.rdp.orders;

public class Pen {
    
    private int style = 0;
    private int width = 0;
    private int color = 0;

    public Pen() {
    }

    public int getStyle() {
	return this.style;
    }
    
    public int getWidth() {
	return this.width;
    }
    
    public int getColor() {
	return this.color;
    }

    public void setStyle(int style) {
	this.style = style;
    }

    public void setWidth(int width) {
	this.width = width;
    }

    public void setColor(int color) {
	this.color = color;
    }

    public void reset() {
	style = 0;
	width = 0;
	color = 0;
    }
}
