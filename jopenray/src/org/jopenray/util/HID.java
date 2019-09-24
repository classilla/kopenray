/*
 *  Copyright 2010 jOpenRay, ILM Informatique  
 *  Copyright 2014 Matthew Martin
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

package org.jopenray.util;

import java.awt.event.KeyEvent;

public class HID {
	public static int hidToKeyCode(int hid) {
		// QWERTY keyboard
		int r = 0;
		switch (hid) {
		case 4:
			r = KeyEvent.VK_A;
			break;
		case 5:
			r = KeyEvent.VK_B;
			break;
		case 6:
			r = KeyEvent.VK_C;
			break;
		case 7:
			r = KeyEvent.VK_D;
			break;
		case 8:
			r = KeyEvent.VK_E;
			break;
		case 9:
			r = KeyEvent.VK_F;
			break;
		case 10:
			r = KeyEvent.VK_G;
			break;
		case 11:
			r = KeyEvent.VK_H;
			break;
		case 12:
			r = KeyEvent.VK_I;
			break;
		case 13:
			r = KeyEvent.VK_J;
			break;
		case 14:
			r = KeyEvent.VK_K;
			break;
		case 15:
			r = KeyEvent.VK_L;
			break;
		case 16:
			r = KeyEvent.VK_M;
			break;
		case 17:
			r = KeyEvent.VK_N;
			break;
		case 18:
			r = KeyEvent.VK_O;
			break;
		case 19:
			r = KeyEvent.VK_P;
			break;
		case 20:
			r = KeyEvent.VK_Q;
			break;
		case 21:
			r = KeyEvent.VK_R;
			break;
		case 22:
			r = KeyEvent.VK_S;
			break;
		case 23:
			r = KeyEvent.VK_T;
			break;
		case 24:
			r = KeyEvent.VK_U;
			break;
		case 25:
			r = KeyEvent.VK_V;
			break;
		case 26:
			r = KeyEvent.VK_W;
			break;
		case 27:
			r = KeyEvent.VK_X;
			break;
		case 28:
			r = KeyEvent.VK_Y;
			break;
		case 29:
			r = KeyEvent.VK_Z;
			break;
		case 30:
			r = KeyEvent.VK_1;
			break;
		case 31:
			r = KeyEvent.VK_2;
			break;
		case 32:
			r = KeyEvent.VK_3;
			break;
		case 33:
			r = KeyEvent.VK_4;
			break;
		case 34:
			r = KeyEvent.VK_5;
			break;
		case 35:
			r = KeyEvent.VK_6;
			break;
		case 36:
			r = KeyEvent.VK_7;
			break;
		case 37:
			r = KeyEvent.VK_8;
			break;
		case 38:
			r = KeyEvent.VK_9;
			break;
		case 39:
			r = KeyEvent.VK_0;
			break;
		case 40:
			r = KeyEvent.VK_ENTER;
			break;
		case 41:
			r = KeyEvent.VK_ESCAPE;
			break;
		case 42:
			r = KeyEvent.VK_BACK_SPACE;
			break;
		case 43:
			r = KeyEvent.VK_TAB;
			break;
		case 44:
			r = KeyEvent.VK_SPACE;
			break;
		case 45:
			r = KeyEvent.VK_MINUS;
			break;
		case 46:
			r = KeyEvent.VK_EQUALS;
			break;
		case 47:
			r = KeyEvent.VK_OPEN_BRACKET;
			break;
		case 48:
			r = KeyEvent.VK_CLOSE_BRACKET;
			break;
		case 49:
			r = KeyEvent.VK_BACK_SLASH;
			break;
		case 51:
			r = KeyEvent.VK_SEMICOLON;
			break;
		case 52:
			r = KeyEvent.VK_QUOTE;
			break;
		case 53:
			r = KeyEvent.VK_BACK_QUOTE;
			break;
		case 54:
			r = KeyEvent.VK_COMMA;
			break;
		case 55:
			r = KeyEvent.VK_PERIOD;
			break;
		case 56:
			r = KeyEvent.VK_SLASH;
			break;
		case 57:
			r = KeyEvent.VK_CAPS_LOCK;
			break;
		case 58:
			r = KeyEvent.VK_F1;
			break;
		case 59:
			r = KeyEvent.VK_F2;
			break;
		case 60:
			r = KeyEvent.VK_F3;
			break;
		case 61:
			r = KeyEvent.VK_F4;
			break;
		case 62:
			r = KeyEvent.VK_F5;
			break;
		case 63:
			r = KeyEvent.VK_F6;
			break;
		case 64:
			r = KeyEvent.VK_F7;
			break;
		case 65:
			r = KeyEvent.VK_F8;
			break;
		case 66:
			r = KeyEvent.VK_F9;
			break;
		case 67:
			r = KeyEvent.VK_F10;
			break;
		case 68:
			r = KeyEvent.VK_F11;
			break;
		case 69:
			r = KeyEvent.VK_F11;
			break;
		case 70:
			r = KeyEvent.VK_PRINTSCREEN;
			break;
		case 71:
			r = KeyEvent.VK_SCROLL_LOCK;
			break;
		case 72:
			r = KeyEvent.VK_PAUSE;
			break;
		case 73:
			r = KeyEvent.VK_INSERT;
			break;
		case 74:
			r = KeyEvent.VK_HOME;
			break;
		case 75:
			r = KeyEvent.VK_PAGE_UP;
			break;
		case 76:
			r = KeyEvent.VK_DELETE;
			break;
		case 77:
			r = KeyEvent.VK_END;
			break;
		case 78:
			r = KeyEvent.VK_PAGE_DOWN;
			break;
		case 79:
			r = KeyEvent.VK_RIGHT;
			break;
		case 80:
			r = KeyEvent.VK_LEFT;
			break;
		case 81:
			r = KeyEvent.VK_DOWN;
			break;
		case 82:
			r = KeyEvent.VK_UP;
			break;
		case 83:
			r = KeyEvent.VK_NUM_LOCK;
			break;
		case 89:
			r = KeyEvent.VK_NUMPAD1;
			break;
		case 90:
			r = KeyEvent.VK_NUMPAD2;
			break;
		case 91:
			r = KeyEvent.VK_NUMPAD3;
			break;
		case 92:
			r = KeyEvent.VK_NUMPAD4;
			break;
		case 93:
			r = KeyEvent.VK_NUMPAD5;
			break;
		case 94:
			r = KeyEvent.VK_NUMPAD6;
			break;
		case 95:
			r = KeyEvent.VK_NUMPAD7;
			break;
		case 96:
			r = KeyEvent.VK_NUMPAD8;
			break;
		case 97:
			r = KeyEvent.VK_NUMPAD9;
			break;
		case 98:
			r = KeyEvent.VK_NUMPAD0;
			break;
		case 117:
			r = KeyEvent.VK_HELP;
			break;
		case 118:
			r = KeyEvent.VK_PROPS;
			break;
		case 120:
			r = KeyEvent.VK_STOP;
			break;
		case 121:
			r = KeyEvent.VK_AGAIN;
			break;
		case 122:
			r = KeyEvent.VK_CANCEL;
			break;
		case 123:
			r = KeyEvent.VK_CUT;
			break;
		case 124:
			r = KeyEvent.VK_COPY;
			break;
		case 125:
			r = KeyEvent.VK_PASTE;
			break;
		case 126:
			r = KeyEvent.VK_FIND;
			break;
		default:
			System.err.println("Unknown Convert HID " + hid + " to " + r);
			break;
		}
		System.err.println("Convert HID " + hid + " to " + r);
		return r;
	}
}
