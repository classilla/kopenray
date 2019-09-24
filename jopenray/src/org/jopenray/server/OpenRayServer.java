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

package org.jopenray.server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jopenray.authentication.AuthenticationThread;
import org.jopenray.server.card.CardManager;
import org.jopenray.server.session.SessionManager;
import org.jopenray.server.thinclient.ThinClientManager;
import org.jopenray.server.user.UserManager;

public class OpenRayServer {

	boolean stop = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ThinClientManager.getInstance().check();
		UserManager.getInstance().check();
		CardManager.getInstance().check();
		SessionManager.getInstance().check();

		System.out.println("jOpenRayServer : started");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}

				JFrame f = new ServerFrame();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setVisible(true);

			}
		});

		OpenRayServer srv = new OpenRayServer();
		try {
			srv.startAuthenticationServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void startAuthenticationServer() throws IOException {
		ServerSocket s = null;
		try {
			s = new ServerSocket(7009);
		} catch (final BindException e) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null,
							"A server is already running!");
					System.exit(0);
				}
			});

		}
		while (true) {
			System.out.println("jOpenRayServer : waiting message");
			Socket soc = s.accept();
			System.out.println("jOpenRayServer : client connected");
			AuthenticationThread thr = new AuthenticationThread(soc);
			thr.start();
		}

	}

}
