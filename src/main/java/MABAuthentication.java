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

public class MABAuthentication {

	final static String CALLING_STATION_ID = System.getProperty("CALLING_STATION_ID", "66:77:88:99:66:77");
	final static String AUDIT_SESSION_ID = System.getProperty("AUDIT_SESSION_ID", "101");
	final static String RADIUS_SECRET = System.getProperty("RADIUS_SECRET", "secret");
	final static String NAS_IP_ADDRESS = System.getProperty("NAS_IP_ADDRESS", "tb071-ise3.cisco.com");
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

		System.out.println  (" Sending MAB request for mac address " + CALLING_STATION_ID);
		sendPackets(args[0], CALLING_STATION_ID, false, null);

	}

	public static void sendPackets(String hostName, String macAddress, boolean isMudURI, String mudURI) throws Exception {

		InetAddress address = InetAddress.getByName(hostName);
		int port = 1812;
		InetSocketAddress sa = new InetSocketAddress(address, port);
		DatagramChannel channel = DatagramChannel.open();
		channel.connect(sa);

		RadiusPacket resp = mab (channel, macAddress, isMudURI, mudURI);
		System.out.println("Radius Response " + resp.toString());

		channel.close();
	}

	static RadiusPacket mab (DatagramChannel channel, String macAddress, boolean isMudURI, String mudURI) throws Exception {

		if (null == macAddress) {
			macAddress = CALLING_STATION_ID;
		}

		System.out.println  ("\t Sending MAB request for mac address " + macAddress);

		AccessRequest req = new AccessRequest(RADIUS_SECRET, requestAuthenticator);
		req.add(new UserName(macAddress));
		req.add(new UserPassword(macAddress, RADIUS_SECRET, requestAuthenticator));
		req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
		req.add(new CallingStationID(macAddress));

		req.add((new ServiceType(10)));

		Random rand = new Random();
		int n = rand.nextInt (1068765) + 1;

		req.add(new CiscoVSA("audit-session-id=" + n));

		if (isMudURI) {
			if (null == mudURI)
			req.add(new CiscoVSA("mud-uri=https://www.philips.com/.well-known/v1/PHILIPS-Model-A-1.0"));
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


}
