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

package org.jopenray.authentication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

import net.percederberg.tetris.Game;

import org.jopenray.server.card.Card;
import org.jopenray.server.card.CardManager;
import org.jopenray.server.event.Event;
import org.jopenray.server.event.EventManager;
import org.jopenray.server.session.Session;
import org.jopenray.server.session.SessionManager;
import org.jopenray.server.thinclient.ThinClient;
import org.jopenray.server.thinclient.ThinClientManager;
import org.jopenray.util.MessageImage;

public class AuthenticationThread extends Thread {
	BufferedReader plec;
	PrintWriter pred;
	private Socket socket;

	public AuthenticationThread(final Socket soc) throws IOException {
		this.socket = soc;
		// Un BufferedReader permet de lire par ligne.
		plec = new BufferedReader(new InputStreamReader(soc.getInputStream()));

		// Un PrintWriter possède toutes les opérations print classiques.
		// En mode auto-flush, le tampon est vidé (flush) à l'appel de println.
		pred = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc
				.getOutputStream())), true);
		this.setDaemon(true);
	}

	@Override
	public void run() {
		ThinClient client = new ThinClient(null);
		try {

			while (isAlive()) {
				ThinClientManager.getInstance().addOrUpdate(client);
				String str;

				System.out.println(this + " Waiting for message...");
				str = plec.readLine();
				System.out.println(this + " Message received: " + str); // trace

				// This can pull in a tight loop. If we get
				// repeated null messages, the socket
				// probably died, and we should disconnect.
				if (str == null) {
					break;
				}
				AuthenticationMessage m = new AuthenticationMessage();
				List<String> l = m.readFrom(str);
				m.put("remoteIP", remoteIP());
				if (client.getSerialNumber() == null) {
					String sn = m.get("sn");
					if (sn != null) {
						ThinClient knownClient = ThinClientManager
								.getInstance().getClientFromId(sn);
						if (knownClient != null) {
							client = knownClient;
						} else {
							System.out.println("New client connected:" + sn);
						}
					}
				}
				if (m.get("id") != null) {
					// Filter id "0" and "pseudo" (no card)
					EventManager.getInstance().add(
							new Event("Authentication received", l.toString(),
									Event.TYPE_INFO));
					if (m.get("type") != null && m.get("id").length() > 1) {
						if (!m.get("type").equals("pseudo")) {
							CardManager.getInstance().addOrUpdate(
									new Card(m.get("id"), m.get("type")));
						}
					}
				}

				if (m.getType().equals("infoReq")) {

					pred
							.println("connInf useReal=true encUpType=none tokenSeq="
									+ m.get("tokenSeq")
									+ " module=StartSession.m3 access=allowed token=pseudo.00212839eff9 encDownType=none");

					if (m.isCardRemoved()) {
						client.updateStateFrom(m);
						client.clearScreen();
					} else {
						client.updateStateFrom(m);
					}
				} else if (m.getType().equals("keepAliveReq")) {
					client.updateStateFrom(m);
					pred.println("keepAliveCnf");

				} else if (m.getType().equals("connRsp")) {
					client.updateStateFrom(m);
					client.setAllowed(true);

					Session s = SessionManager.getInstance()
							.getAvailableSessionFor(client);
					if (s == null) {
						String desc = client.getName();
						if (client.getCardId() != null) {
							desc += " CardId: " + client.getCardId();
						}
						EventManager.getInstance().add(
								new Event("No session configured", desc,
										Event.TYPE_WARNING));
						client.initConnection();
						client.getWriter().sendImage(
								MessageImage.createImage(
										"No session configured", client
												.getNativeWidth(), client
												.getNativeHeight()), 0, 0);

						final Game game = new Game(client);
						client.getReader().addInputListener(game);
						game.handleStart();

					} else {
						client.connectDisplay(s);
					}
				} else {
					client.updateStateFrom(m);
					System.out.println(this + " Unknown message received: "
							+ str);
				}
				// TODO : sessionReq _=1 event=insert type=pseudo namespace=MD5
				// id=5d05f4585dae4d651a6542e1b7f30386
				// sw=Sun:SunRayS1:MINGW32_NT-5.1:1.0.41 hw=SunRayS1
				// state=disconnected realIP=7F000001 tokenSeq=2 cause=insert
				// sn=5d05f4585dae4d651a6542e1b7f30386 MTU=1500
				// startRes=1359x1019:1359x1019
				// clientRand=xWKi8X7wu2IIiLbDMmWtnwbWi3I5Gr01gyqDyK2R/Hm
				// firstServer=0100007F keyTypes=dsa-sha1-x1,dsa-sha1
			}

			plec.close();
			pred.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

        public String remoteIP() {
		SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
		String state = "";
		if (remoteSocketAddress != null) {
			state = ((InetSocketAddress) remoteSocketAddress).getAddress()
					.getHostAddress();
		}
		return state;
	}

	@Override
	public String toString() {
		String state = remoteIP();
		if (state == "")
			state = "Disconnected";
		return "AuthenticationThread [" + state + "]";
	}

}
