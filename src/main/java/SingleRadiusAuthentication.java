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
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

public class SingleRadiusAuthentication {

  static final String USERNAME = System.getProperty("USERNAME", "user1");
  static final String PASSWORD = System.getProperty("PASSWORD", "Lab123");
  static final String CALLING_STATION_ID = System.getProperty("CALLING_STATION_ID", "00:aa:bb:99:66:77");
  static final String AUDIT_SESSION_ID = System.getProperty("AUDIT_SESSION_ID", "101");
  static final String RADIUS_SECRET = System.getProperty("RADIUS_SECRET", "secret");
  static final String NAS_IP_ADDRESS = System.getProperty("NAS_IP_ADDRESS", "10.0.0.1");
  static final String FRAMED_IP_ADDRESS = System.getProperty("FRAMED_IP_ADDRESS");
  static final String FRAMED_IP_MASK = System.getProperty("FRAMED_IP_MASK", "255.255.255.0");
  static final String NAS_PORT = System.getProperty("NAS_PORT");
  static final String NAS_PORT_ID = System.getProperty("NAS_PORT_ID");
  static final String NAS_PORT_TYPE = System.getProperty("NAS_PORT_TYPE");
  static final String FRAMED_IPV6_ADDRESS = System.getProperty("FRAMED_IPV6_ADDRESS");
  static final String NAS_IPV6_ADDRESS = System.getProperty("NAS_IPV6_ADDRESS");
  static final String HOSTNAME = System.getProperty("HOSTNAME", "tb071-ise1.cisco.com");
  static final String isMud = System.getProperty("isMud", "true");
  static byte[] requestAuthenticator = new byte[16];

  public static void main(String[] args) throws Exception {

    String MUD_URI = System.getProperty("mud_uri",
        "mud-uri=https://www.iot-cisco.com/.well-known/mud/v1/Cisco-Camera-ACA-1.0");

    RadiusPacket resp = null;

    String userName = USERNAME;
    String pass = PASSWORD;

    SingleRadiusAuthentication radiusClient = new SingleRadiusAuthentication();

    if (isMud.equalsIgnoreCase("true")) {

      resp = radiusClient
          .sendRadiusPackets(HOSTNAME, CALLING_STATION_ID, true,
              MUD_URI, userName, pass);
    } else {

      resp = radiusClient
          .sendRadiusPackets(HOSTNAME, CALLING_STATION_ID, false,
              MUD_URI, userName, pass);
    }
    System.out.println(resp.toString());

  }

  public RadiusPacket sendRadiusPackets(
      String hostName,
      String macAddress,
      boolean isMudURI,
      String mudURI,
      String userName,
      String pass) throws Exception {

    System.out.println(
        "Sending radius packet for mac " + macAddress + " user: " + userName + " pass: "
            + pass);

    RadiusPacket resp = sendRadiusRequest(hostName, macAddress, isMudURI,
        mudURI, userName, pass);

    System.out.println("Radius Response " + resp.toString());

    return resp;

  }

  private RadiusPacket sendRadiusRequest(
      String hostName,
      String macAddress,
      boolean isMudURI,
      String mudURI,
      String userName,
      String password) throws Exception {

    AccessRequest req = new AccessRequest(RADIUS_SECRET, requestAuthenticator);

    if (userName == null)
      req.add(new UserName(USERNAME));
    else
      req.add(new UserName(userName));

    if (password == null) {
      req.add(new UserPassword(password, RADIUS_SECRET, requestAuthenticator));
    } else {
      req.add(new UserPassword(PASSWORD, RADIUS_SECRET, requestAuthenticator));
    }


    req.add(new NASIPAddress(InetAddress.getByName(NAS_IP_ADDRESS).getAddress()));
    req.add(new CallingStationID(macAddress));

    Random rand = new Random();
    int n = rand.nextInt(1068765) + 1;
    req.add(new CiscoVSA("audit-session-id=" + n));

    //String LLDP_MUD_PREFIX = "lldp-tlv=\\000\\177\\0007\\000\\000^\\001";
    //String LLDP_MUD = "lldp-tlv=\u0000\u007F\u0000\u0032\u0000\u0000\u005E\u0001https://mud.example.com/.well-known/mud/v1/mudfile";
    String LLDP_MUD = "lldp-tlv=\u0000\u007F\u0000\u004c\u0000\u0000\u005E\u0001https://cisco.mudservice.org/.well-known/mud/v1/Molex-LEDlight-transcend";

    if (isMudURI) {

      if (null == mudURI) {
       req.add(
             new CiscoVSA(LLDP_MUD));
              //new CiscoVSA("mud-uri=https://www.philips.com/mud-uri/light/bulb/Philips-Bulb-A2-2.1"));

      } else {
        req.add(new CiscoVSA(mudURI));
      }

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

    System.out.println("\t Radius Request payload \t\t" + req.getPayload().toString());

    InetAddress address = InetAddress.getByName(hostName);
    int port = 1812;
    InetSocketAddress sa = new InetSocketAddress(address, port);
    DatagramChannel channel = DatagramChannel.open();
    channel.connect(sa);

    channel.write(req.getPayload());

    ByteBuffer bb = ByteBuffer.allocate(10 * 1024);
    channel.read(bb);
    bb.flip();

    RadiusPacket packet = RadiusPacket.create(bb);

    try {

      channel.close();

    } catch (IOException er) {

        System.err.println ("ERROR closing stream " + er);

    }

    return packet;

  }

}
