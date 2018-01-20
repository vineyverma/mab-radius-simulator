

import code.messy.net.radius.attribute.MudUri;
import code.messy.net.radius.attribute.ServiceType;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

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
import code.messy.net.radius.attribute.UserName;
import code.messy.net.radius.attribute.UserPassword;
import code.messy.net.radius.packet.AccessRequest;
import code.messy.net.radius.packet.RadiusPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RadiusAuthentication {

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

	final static String REQUEST_TYPE = System.getProperty("request_type", "mab"); // mab or radius

	final static String numRequests = System.getProperty("numberOfRequests", "1");

	final static String isMud = System.getProperty("isMud", "false");

	static byte[] requestAuthenticator = new byte[16];

	private static List<String> macList = new ArrayList<>();
	private static List<String> userList = new ArrayList<>();

	/**
	 * Main <ip>
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		String MUD_URI = System.getProperty("mud_uri", "mud-uri=https://www.iot-cisco.com/.well-known/mud/v1/Cisco-Camera-ACA-1.0");

		RadiusPacket resp = null;

		Util util = new Util ();

		// Read macs from file
		try {
			macList = util.getFileContent("mac_list.txt");
			System.out.println (" Mac List:" + macList);
		} catch (Exception er) {
			er.printStackTrace();
		}

		// Read username and passwords from file. These username and passwords should exist on ISE
		try {
			userList = util.getFileContent("radius_users.txt");
			System.out.println (" user password:" + userList);
		} catch (Exception er) {
			er.printStackTrace();
		}

		RadiusAuthentication  radiusClient = new RadiusAuthentication();

		boolean isMab = false;

		// is this MAB or Radius
		if (REQUEST_TYPE.equalsIgnoreCase("mab")) {
			isMab = true;

			resp = radiusClient
					.sendRadiusPackets(false, args[0], CALLING_STATION_ID, USERNAME, PASSWORD, false, null);

			System.out.println(resp.toString());

		}

		// Multiple RADIUS requests
		if (!isMab && Integer.parseInt(numRequests) >= 1) {

			for (int i = 0; i < Integer.parseInt(numRequests); i++) {

				if (isMud.equalsIgnoreCase("true")) {
					resp = radiusClient
							.sendRadiusPackets(false, args[0], CALLING_STATION_ID, USERNAME, PASSWORD, true,
									MUD_URI);
				} else {
					resp = radiusClient
							.sendRadiusPackets(false, args[0], CALLING_STATION_ID, USERNAME, PASSWORD, false,
									MUD_URI);
				}

				System.out.println(resp.toString());

			}
		}

		//RadiusPacket resp = mab(channel);
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

		Random rand = new Random();
		int n = rand.nextInt (macList.size());

		String mac = macList.get(n);

		n = rand.nextInt (userList.size());

		String[] userPass = userList.get(n).split("=");

		userName = userPass[0];
		password = userPass[1];

		System.out.println ("Sending radius packet for mac " + mac + " user: " + userName + " pass: " + password);

		RadiusPacket resp = sendRadiusRequest (hostName, isMab, mac, userName, password, isMudURI, mudURI);

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

	private static AccessRequest buildAccessRequest(boolean isMab,
			String macAddress,
			String username,
			String password) throws  Exception {

		AccessRequest req = new AccessRequest(RADIUS_SECRET, requestAuthenticator);

		if (isMab) {

			req.add(new UserName(macAddress));
			req.add(new UserPassword(macAddress, RADIUS_SECRET, requestAuthenticator));
			//req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
			//req.add(new CallingStationID(macAddress));
			req.add((new ServiceType(10)));

		} else {

			req.add(new UserName(username));
			req.add(new UserPassword(password, RADIUS_SECRET, requestAuthenticator));
			//req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
			//req.add(new CallingStationID(CALLING_STATION_ID));
		}

		req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
		req.add(new CallingStationID(macAddress));

		Random rand = new Random();
		int n = rand.nextInt (1068765) + 1;
		req.add(new CiscoVSA("audit-session-id=" + n));

		return req;
	}


}
