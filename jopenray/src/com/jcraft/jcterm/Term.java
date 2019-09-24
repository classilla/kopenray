/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2007 ymnk, JCraft,Inc.
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

package com.jcraft.jcterm;

public interface Term {

	void start(Connection connection);

	int getRowCount();

	int getColumnCount();

	int getCharWidth();

	int getCharHeight();

	void setCursor(int x, int y);

	void clear();

	void drawCursor();

	void redraw(int x, int y, int width, int height);

	void clearArea(int x1, int y1, int x2, int y2);

	void scrollArea(int x, int y, int w, int h, int dx, int dy);

	void drawBytes(byte[] buf, int s, int len, int x, int y);

	void drawString(String str, int x, int y);

	void beep();

	void setDefaultForeGround(Object foreground);

	void setDefaultBackGround(Object background);

	void setForeGround(Object foreground);

	void setBackGround(Object background);

	void setBold();

	void setUnderline();

	void setReverse();

	void resetAllAttributes();

	int getTermWidth();

	int getTermHeight();

	Object getColor(int index);
}
