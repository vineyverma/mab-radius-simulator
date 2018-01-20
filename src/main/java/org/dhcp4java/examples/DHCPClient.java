/*
 *	This file is part of dhcp4java, a DHCP API for the Java language.
 *	(c) 2006 Stephan Hadinger
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.dhcp4java.examples;

import code.messy.net.radius.packet.RadiusPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

import org.dhcp4java.DHCPPacket;


import static org.dhcp4java.DHCPConstants.*;

/**
 * Example of DHCP Client (under construction).
 *
 * @author Stephan Hadinger
 * @version 1.00
 */
public class DHCPClient {
    private static byte[] macAddress = {
        (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05
    };

    private DHCPClient() {
    	throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        // first send discover
        DHCPPacket discover = new DHCPPacket();
        discover.setOp(BOOTREQUEST);
        discover.setHtype(HTYPE_ETHER);
        discover.setHlen((byte) 6);
        discover.setHops((byte) 0);
        discover.setXid( (new Random()).nextInt() );
        discover.setSecs((short) 0);
        discover.setFlags((short) 0);
        discover.setChaddr(macAddress);

        discover.setSname("tb071-ise3.cisco.com");

        try {
            discover.setSiaddr("172.21.78.184");
        } catch (Exception er) {
            er.printStackTrace();
        }

        System.out.println ("DHCP packet ...");
        System.out.println (discover.toString());

    }

    public static void sendDhcpPacket ( DHCPPacket discover) throws Exception {

        InetAddress address = InetAddress.getByName("tb071-ise3.cisco.com");
        int port = 67;

        InetSocketAddress sa = new InetSocketAddress(address, port);
        DatagramChannel channel = DatagramChannel.open();
        channel.connect(sa);

        channel.write(getPayload(discover.toString()));

        //RadiusPacket resp = start(channel, USERNAME, PASSWORD);
        //System.out.println(resp.toString());
        channel.close();
    }

    public static ByteBuffer getPayload(String value) {

        int length = value.length() + 2 + 4 + 2;
        ByteBuffer bb = ByteBuffer.allocate(length);

        bb.put((byte)26);
        bb.put((byte)length);
        bb.putInt(9);

        bb.put((byte)1);
        byte[] b = value.getBytes();

        bb.put((byte)(b.length + 2));
        bb.put(b);
        bb.flip();

        return bb;
    }
}
