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

package org.jopenray.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class OpenRayClient {
	public OpenRayClient() {

	}

	public void connectTo(String server) throws IOException {

		Socket socket = new Socket();
		socket.setKeepAlive(true);

		socket.setReuseAddress(true);

		socket.setTcpNoDelay(true);

		socket.connect(new InetSocketAddress(server, 7009), 30 * 1000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));

		PrintWriter writer = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream())), true);

		RendererListener renderer = new RendererListener();

		System.out.println("Send Init on port:" + renderer.getPort());
		renderer.start();
		writer
				.println("infoReq _=1 event=insert fw=FW1 hw=HW1 namespace=IEEE802 id=005056c00008 pn="
						+ renderer.getPort()
						+ " sn=005056c00008 state=disconnected type=pseudo cause=insert initState=1 startRes=800x600 tokenSeq=1");

		// recoit
		// connInf useReal=true encUpType=none tokenSeq=1 module=StartSession.m3
		// access=allowed token=pseudo.005056c00008 encDownType=none
		System.out.println("Waiting for answer");
		String s = reader.readLine();

		System.out.println("receiving:" + s);

		// Send connection

		writer.println("connRsp _=1 access=allowed fw=FW1 hw=HW1 pn="
				+ renderer.getPort()
				+ " sn=005056c00008 namespace=IEEE802 state=connected");

		while (true) {
			try {
				Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Stat stat = renderer.getStats();
			writer
					.println("keepAliveReq _=1 fw=FW1 hw=HW1 pn="
							+ renderer.getPort()
							+ " sn=005056c00008 namespace=IEEE802 state=connected byteCount="
							+ stat.getByteCount() + " connTime="
							+ stat.getConnectionTime() + " idleTime="
							+ stat.getIdleTime() + " lossCount="
							+ stat.getLossCount() + "pktCount="
							+ stat.getPacketCount());

			s = reader.readLine();
			System.out.println("receiving:" + s);// keepAliveCnf

		}
	}

	public static void main(String[] args) {
		OpenRayClient client = new OpenRayClient();
		try {
			client.connectTo("192.168.1.13");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
