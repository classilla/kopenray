/* ImageSelection.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.4 $
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
package org.jopenray.rdp.rdp5.cliprdr;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.jopenray.rdp.Common;
import org.jopenray.rdp.Utilities_Localised;

	 public class ImageSelection
	    implements Transferable 
	  {
	    // the Image object which will be housed by the ImageSelection
	    private Image image;

	    public ImageSelection(Image image) {
	      this.image = image;
	    }

	    // Returns the supported flavors of our implementation
	    public DataFlavor[] getTransferDataFlavors() 
	    {
	      return new DataFlavor[] {Utilities_Localised.imageFlavor};
	    }
	    
	    // Returns true if flavor is supported
	    public boolean isDataFlavorSupported(DataFlavor flavor) 
	    {
	      return Utilities_Localised.imageFlavor.equals(flavor);
	    }

	    // Returns Image object housed by Transferable object
	    public Object getTransferData(DataFlavor flavor)
	      throws UnsupportedFlavorException,IOException 
	    {
	      if (!Utilities_Localised.imageFlavor.equals(flavor)) 
	      {
	        throw new UnsupportedFlavorException(flavor);
	      }
	      // else return the payload
	      return image;
	    }
	  }