import code.messy.net.radius.attribute.CallingStationID;
import code.messy.net.radius.attribute.CiscoVSA;
import code.messy.net.radius.attribute.FramedIPAddress;
import code.messy.net.radius.attribute.FramedIPNetmask;
import code.messy.net.radius.attribute.FramedIPv6Address;
import code.messy.net.radius.attribute.NASIPAddress;
import code.messy.net.radius.attribute.NASIPv6Address;
import code.messy.net.radius.attribute.NASPort;
import code.messy.net.radius.attribute.NASPortID;
import code.messy.net.radius.attribute.NASPortType;
import code.messy.net.radius.attribute.ServiceType;
import code.messy.net.radius.attribute.UserName;
import code.messy.net.radius.attribute.UserPassword;
import code.messy.net.radius.packet.AccessRequest;
import code.messy.net.radius.packet.RadiusPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

public class RadiusAuthenticationOriginal {

	final static String USERNAME = System.getProperty("USERNAME", "user1");
	final static String PASSWORD = System.getProperty("PASSWORD", "Lab123");
	final static String CALLING_STATION_ID = System.getProperty("CALLING_STATION_ID", "00:aa:bb:99:66:77");
	final static String AUDIT_SESSION_ID = System.getProperty("AUDIT_SESSION_ID", "101");
	final static String RADIUS_SECRET = System.getProperty("RADIUS_SECRET", "secret");
	final static String NAS_IP_ADDRESS = System.getProperty("NAS_IP_ADDRESS", "10.0.0.1");
	final static String FRAMED_IP_ADDRESS = System.getProperty("FRAMED_IP_ADDRESS");
	final static String FRAMED_IP_MASK = System.getProperty("FRAMED_IP_MASK", "255.255.255.0");
	final static String NAS_PORT = System.getProperty("NAS_PORT");
	final static String NAS_PORT_ID = System.getProperty("NAS_PORT_ID");
	final static String NAS_PORT_TYPE = System.getProperty("NAS_PORT_TYPE");
	final static String FRAMED_IPV6_ADDRESS = System.getProperty("FRAMED_IPV6_ADDRESS");
	final static String NAS_IPV6_ADDRESS = System.getProperty("NAS_IPV6_ADDRESS");

	static byte[] requestAuthenticator = new byte[16];

	/**
	 * Main <ip>
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String MUD_URI = System.getProperty("mud-uri", "mud-uri=https://www.philips.com/mud-uri/light/bulb/002");

		RadiusPacket resp = null;

		RadiusAuthenticationOriginal radiusClient = new RadiusAuthenticationOriginal();

		/*
		InetAddress address = InetAddress.getByName(args[0]);
		int port = 1812;
		InetSocketAddress sa = new InetSocketAddress(address, port);
		DatagramChannel channel = DatagramChannel.open();
		channel.connect(sa);
		RadiusPacket resp = login(channel, USERNAME, PASSWORD);
		*/

		if (System.getProperty("mud-uri")  == null)
				resp = radiusClient.sendRadiusPackets ( true, args[0], CALLING_STATION_ID, USERNAME, PASSWORD, false, null);
		else
				resp = radiusClient.sendRadiusPackets ( true, args[0], CALLING_STATION_ID, USERNAME, PASSWORD, false, "mud-uri=https://www.philips.com/mud-uri/light/bulb/003");

		//RadiusPacket resp = mab(channel);

		System.out.println(resp.toString());
		
		//channel.close();
	}

	public  RadiusPacket sendRadiusPackets(
			boolean isMab,
			String hostName,
			String macAddress,
			String userName,
			String password,
			boolean isMudURI,
			String mudURI ) throws Exception {

		//InetAddress address = InetAddress.getByName(hostName);
		//int port = 1812;
		//InetSocketAddress sa = new InetSocketAddress(address, port);
		//DatagramChannel channel = DatagramChannel.open();
		//channel.connect(sa);

		//RadiusPacket resp = mab(channel, macAddress, isMudURI, mudURI);

		RadiusPacket resp = sendRadiusRequest (hostName, isMab, macAddress, userName, password, isMudURI, mudURI);

		System.out.println("Radius Response " + resp.toString());

		return resp;

	}

	private RadiusPacket sendRadiusRequest(
			String hostName,
			boolean isMab,
			String macAddress,
			String username,
			String password,
			boolean isMudURI,
			String mudURI) throws Exception {

		AccessRequest req = buildAccessRequest(isMab, macAddress, username, password);

		if (isMudURI) {

			if (null == mudURI)
				req.add(new CiscoVSA("mud-uri=https://www.philips.com/mud-uri/light/bulb/001"));
			else
				req.add(new CiscoVSA(mudURI));

		}

		if (FRAMED_IP_ADDRESS != null) {
			req.add(new FramedIPAddress(InetAddress.getByName(FRAMED_IP_ADDRESS).getAddress()));
			req.add(new FramedIPNetmask(InetAddress.getByName(FRAMED_IP_MASK).getAddress()));
		}

		if (NAS_PORT != null) {
			req.add(new NASPort(Integer.parseInt(NAS_PORT)));
		}

		if (NAS_PORT_ID != null) {
			req.add(new NASPortID(NAS_PORT_ID));
		}

		if (NAS_PORT_TYPE != null) {
			req.add(new NASPortType(Integer.parseInt(NAS_PORT_TYPE)));
		}

		if (FRAMED_IPV6_ADDRESS != null) {
			String[] addresses = FRAMED_IPV6_ADDRESS.split(",");
			for (String address : addresses) {
				req.add(new FramedIPv6Address(InetAddress.getByName(address).getAddress()));
			}
		}

		if (NAS_IPV6_ADDRESS != null) {
			req.add(new NASIPv6Address(InetAddress.getByName(NAS_IPV6_ADDRESS).getAddress()));
		}

		System.out.println ("\t Radius Request payload \t\t" + req.getPayload());

		InetAddress address = InetAddress.getByName(hostName);
		int port = 1812;
		InetSocketAddress sa = new InetSocketAddress(address, port);
		DatagramChannel channel = DatagramChannel.open();
		channel.connect(sa);

		channel.write(req.getPayload());

		ByteBuffer bb = ByteBuffer.allocate(10 * 1024);
		channel.read(bb);
		bb.flip();

		//RadiusPacket resp = mab(channel, macAddress, isMudURI, mudURI);
		//System.out.println("\n\n \t Radius Response " + resp.toString());

		RadiusPacket packet = RadiusPacket.create(bb);
		channel.close();

		return packet;

	}


	static RadiusPacket login(DatagramChannel channel, String macAddress, String username, String password) throws Exception {

		if (null == macAddress) {
			macAddress = CALLING_STATION_ID;
		}

		/*AccessRequest req = new AccessRequest(RADIUS_SECRET, requestAuthenticator);
		req.add(new UserName(username));
		req.add(new UserPassword(password, RADIUS_SECRET, requestAuthenticator));
		req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
		req.add(new CallingStationID(CALLING_STATION_ID));
		*/

		AccessRequest req = buildAccessRequest(false, macAddress, username, password);

		//req.add(new CiscoVSA("audit-session-id=" + AUDIT_SESSION_ID));
		req.add(new CiscoVSA("mud-uri=https://www.philips.com/mud-uri/light/bulb/001"));

		if (FRAMED_IP_ADDRESS != null) {
			req.add(new FramedIPAddress(InetAddress.getByName(FRAMED_IP_ADDRESS).getAddress()));
			req.add(new FramedIPNetmask(InetAddress.getByName(FRAMED_IP_MASK).getAddress()));
		}
		if (NAS_PORT != null) {
			req.add(new NASPort(Integer.parseInt(NAS_PORT)));
		}
		if (NAS_PORT_ID != null) {
			req.add(new NASPortID(NAS_PORT_ID));
		}
		if (NAS_PORT_TYPE != null) {
			req.add(new NASPortType(Integer.parseInt(NAS_PORT_TYPE)));
		}
		if (FRAMED_IPV6_ADDRESS != null) {
			String[] addresses = FRAMED_IPV6_ADDRESS.split(",");
			for (String address : addresses) {
				req.add(new FramedIPv6Address(InetAddress.getByName(address).getAddress()));
			}
		}
		if (NAS_IPV6_ADDRESS != null) {
			req.add(new NASIPv6Address(InetAddress.getByName(NAS_IPV6_ADDRESS).getAddress()));
		}
		
		channel.write(req.getPayload());

		ByteBuffer bb = ByteBuffer.allocate(10 * 1024);
		channel.read(bb);
		bb.flip();
		
		return RadiusPacket.create(bb);
	}


	static RadiusPacket mab (DatagramChannel channel, String macAddress, boolean isMudURI, String mudURI) throws Exception {

		if (null == macAddress) {
			macAddress = CALLING_STATION_ID;
		}

		System.out.println  ("\t Sending MAB request for mac address " + macAddress);

		//AccessRequest req = new AccessRequest(RADIUS_SECRET, requestAuthenticator);
		//req.add(new UserName(macAddress));
		//req.add(new UserPassword(macAddress, RADIUS_SECRET, requestAuthenticator));
		//req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
		//req.add(new CallingStationID(macAddress));
		//req.add((new ServiceType(10)));

		AccessRequest req = buildAccessRequest(true, macAddress, null, null);

		Random rand = new Random();
		int n = rand.nextInt (1068765) + 1;

		req.add(new CiscoVSA("audit-session-id=" + n));

		if (isMudURI) {
			if (null == mudURI)
				req.add(new CiscoVSA("mud-uri=https://www.philips.com/mud-uri/light/bulb/001"));
			else
				req.add(new CiscoVSA(mudURI));
		}

		if (FRAMED_IP_ADDRESS != null) {
			req.add(new FramedIPAddress(InetAddress.getByName(FRAMED_IP_ADDRESS).getAddress()));
			req.add(new FramedIPNetmask(InetAddress.getByName(FRAMED_IP_MASK).getAddress()));
		}
		if (NAS_PORT != null) {
			req.add(new NASPort(Integer.parseInt(NAS_PORT)));
		}
		if (NAS_PORT_ID != null) {
			req.add(new NASPortID(NAS_PORT_ID));
		}
		if (NAS_PORT_TYPE != null) {
			req.add(new NASPortType(Integer.parseInt(NAS_PORT_TYPE)));
		}
		if (FRAMED_IPV6_ADDRESS != null) {
			String[] addresses = FRAMED_IPV6_ADDRESS.split(",");
			for (String address : addresses) {
				req.add(new FramedIPv6Address(InetAddress.getByName(address).getAddress()));
			}
		}
		if (NAS_IPV6_ADDRESS != null) {
			req.add(new NASIPv6Address(InetAddress.getByName(NAS_IPV6_ADDRESS).getAddress()));
		}

		System.out.println ("\t MAB Request payload \t\t" + req.getPayload());
		channel.write(req.getPayload());

		ByteBuffer bb = ByteBuffer.allocate(10 * 1024);
		channel.read(bb);
		bb.flip();

		return RadiusPacket.create(bb);
	}

	private static AccessRequest buildAccessRequest(boolean isMab, String macAddress, String username, String password) throws  Exception {

		AccessRequest req = new AccessRequest(RADIUS_SECRET, requestAuthenticator);

		if (isMab) {

			req.add(new UserName(macAddress));
			req.add(new UserPassword(macAddress, RADIUS_SECRET, requestAuthenticator));
			req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
			req.add(new CallingStationID(macAddress));
			req.add((new ServiceType(10)));

		} else {

			req.add(new UserName(username));
			req.add(new UserPassword(password, RADIUS_SECRET, requestAuthenticator));
			req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
			req.add(new CallingStationID(CALLING_STATION_ID));
		}

		Random rand = new Random();
		int n = rand.nextInt (1068765) + 1;
		req.add(new CiscoVSA("audit-session-id=" + n));

		return req;

	}


}
