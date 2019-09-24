# kOpenRay

kOpenRay is a fork of _[jOpenRay](http://www.jopenray.org/)_, an open-source Java server for [SunRay 2](https://en.wikipedia.org/wiki/Sun_Ray) devices. Unfortunately, jOpenRay hasn't been updated since 2010 (though there have been [modest attempts](https://github.com/collegiumv/jopenray), some of which is in this codebase), and it has various issues with newer SunRay hardware and later JDKs. kOpenRay is a possibly intermittently potentially incompletely successful attempt to fix these problems.

Like jOpenRay, kOpenRay runs anywhere Java does (including Windows, macOS, Linux and Solaris), and is free and open-source under the GNU Public License. This version of kOpenRay is only suitable for exploratory use on a secured LAN and should not be used on a production external-facing interface.

## How to use

  1. You need to install Java 1.8 or later (OpenJDK is fine). JavaFX is not required.
  1. Configure your network as per recommendations in the [Sun Ray Software Administration Guide](https://docs.oracle.com/cd/E25749_01/E25745/html/). These pieces are external to kOpenRay.
    * Depending on your client hardware you may need a DHCP server on your network, and that DHCP server may need to serve [additional SunRay-specific information](https://docs.oracle.com/cd/E22662_01/E22661/html/Alternate-Client-Initialization-Reqs-Using-DHCP.html). Alternatively, you may be able to serve names like `sunray-config-servers` and `sunray-servers` that map to the desired IP address(es) from a local DNS resolver. If you define multiple addresses, note that the client may pick one at random.
    *  If your client requires firmware to bootstrap, you may also need a TFTP server to offer it. There is no firmware here for any compatible client, and I don't know where you can get it (it used to be part of the server software for Solaris until Ellison pulled a Larry and decommissioned the whole SunRay project).
    *  Fortunately many more recent SunRay clients don't require firmware to start, and can be configured with a static IP and/or pointed directly at kOpenRay's IP address without DHCP assistance. You can see if your client offers a configuration menu usually with a key combination like Stop-M or Menu-M.
  2. Unzip the software. _Don't move anything around in the directory structure that results._
  3. Start the Java interface with `java -jar kOpenRay.jar` from within the directory structure that results (or, on many OSes, you can just double-click the JAR to start it). Right now the server listens on any interface, so do **not** run this on an externally-facing system.
  4. The server starts automatically and awaits a connection. The configuration tabs are self-explanatory; the clients tab self-populates as different client configurations are seen. By default no sessions exist, so any clients that connect get an informational message and can play Tetpnc on an orange background. As you make configuration changes, they are saved to the `Configurations/` directory.
  5. If you start kOpenRay from a terminal or command line session, substantial debugging input is displayed, which is occasionally useful to the developers but can be rather spammy.
  6. You can quit it from whatever interface you like, including pressing CTRL-C from the terminal you launched it in if you did so.

## A lot of things DON'T WORK

See [our Github page](https://github.com/classilla/kopenray) for everything, in fact, that does indeed not work. Some things that don't work may have worked a long time ago in jOpenRay but they don't work anymore, or may work for some clients but not others. Here are the major ones:

  - The ALP implementation requires a lot of bandwidth and resends are unnecessarily frequent, even considering it's UDP.
  - SSH doesn't scroll properly yet, and may not connect to newer servers that restrict key exchange and encryption methods.
  - RDP is not tested.
  - RFB/VNC is currently not supported. But hey! Tetpnc and images!

Please don't report these issues because we are already well aware of them. This software is a spare-time project, so they may or may not be fixed as our interest and schedule permit. You use this at your own risk, remember?

## License and Credits

These list all credits for various components of kOpenRay.

Copyright &copy;2019 Cameron Kaiser  
Copyright &copy;2014 Matthew Martin  
Copyright &copy;2010 jOpenRay, ILM Informatique  
Copyright &copy;2008 IsmAvatar  
Copyright &copy;2005 Propero Limited  
Copyright &copy;2003 Per Cederberg  
Copyright &copy;2002-2010 ymnk, JCraft, Inc.  
Copyright &copy;2002-2006 Constantin Kaplinsky  
Copyright &copy;2002-2005 RealVNC Ltd.  
Copyright &copy;2002 Cendio Systems  
Copyright &copy;2001-2004 HorizonLive.com  
Copyright &copy;2000 Tridia Corporation  
Copyright &copy;1999 AT&T Laboratories Cambridge  
Copyright &copy;1996 Jef Poskanzer  
Copyright &copy;1996 Widget Workshop, Inc.  
All rights reserved.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
