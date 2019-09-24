/* Rdp5.java
 * Component: ProperJavaRDP
 * 
 * Revision: $Revision: 1.7 $
 * Author: $Author: telliott $
 * Date: $Date: 2005/09/27 14:15:40 $
 *
 * Copyright (c) 2005 Propero Limited
 *
 * Purpose: Handle RDP5 orders
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

package org.jopenray.rdp.rdp5;

import org.jopenray.rdp.*;
import org.jopenray.rdp.crypto.*;


public class Rdp5 extends Rdp {

    private VChannels channels;

    /**
     * Initialise the RDP5 communications layer, with specified virtual channels
     * 
     * @param channels
     *            Virtual channels for RDP layer
     */
    public Rdp5(VChannels channels) {
        super(channels);
        this.channels = channels;
    }

    /**
     * Process an RDP5 packet
     * 
     * @param s
     *            Packet to be processed
     * @param e
     *            True if packet is encrypted
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public void rdp5_process(RdpPacket_Localised s, boolean e)
            throws RdesktopException, OrderException, CryptoException {
        rdp5_process(s, e, false);
    }

    /**
     * Process an RDP5 packet
     * 
     * @param s
     *            Packet to be processed
     * @param encryption
     *            True if packet is encrypted
     * @param shortform
     *            True if packet is of the "short" form
     * @throws RdesktopException
     * @throws OrderException
     * @throws CryptoException
     */
    public void rdp5_process(RdpPacket_Localised s, boolean encryption,
            boolean shortform) throws RdesktopException, OrderException,
            CryptoException {
        logger.debug("Processing RDP 5 order");

        int length, count;
        int type;
        int next;

        if (encryption) {
            s.incrementPosition(shortform ? 6 : 7 /* XXX HACK */); /* signature */
            byte[] data = new byte[s.size() - s.getPosition()];
            s.copyToByteArray(data, 0, s.getPosition(), data.length);
            byte[] packet = SecureLayer.decrypt(data);
        }

        // printf("RDP5 data:\n");
        // hexdump(s->p, s->end - s->p);

        while (s.getPosition() < s.getEnd()) {
            type = s.get8();
            length = s.getLittleEndian16();
            /* next_packet = */next = s.getPosition() + length;
            logger.debug("RDP5: type = " + type);
            switch (type) {
            case 0: /* orders */
                count = s.getLittleEndian16();
                orders.processOrders(s, next, count);
                break;
            case 1: /* bitmap update (???) */
                s.incrementPosition(2); /* part length */
                processBitmapUpdates(s);
                break;
            case 2: /* palette */
                s.incrementPosition(2);
                processPalette(s);
                break;
            case 3: /* probably an palette with offset 3. Weird */
                break;
            case 5:
                process_null_system_pointer_pdu(s);
                break;
            case 6: // default pointer
                break;
            case 9:
                process_colour_pointer_pdu(s);
                break;
            case 10:
                process_cached_pointer_pdu(s);
                break;
            default:
                logger.warn("Unimplemented RDP5 opcode " + type);
            }

            s.setPosition(next);
        }
    }

    /**
     * Process an RDP5 packet from a virtual channel
     * @param s Packet to be processed
     * @param channelno Channel on which packet was received
     */
    void rdp5_process_channel(RdpPacket_Localised s, int channelno) {
        VChannel channel = channels.find_channel_by_channelno(channelno);
        if (channel != null) {
            try {
                channel.process(s);
            } catch (Exception e) {
            }
        }
    }

}
