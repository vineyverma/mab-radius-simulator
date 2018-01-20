//~--- JDK imports ------------------------------------------------------------

import code.messy.net.radius.packet.RadiusPacket;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

// Implements the Callable interface parameterized with the String class.
public class ExecutableTask implements Callable<String> {

  private static final Logger logger = Logger.getLogger("ExecutableTask");
  String MAB_OR_DOT1X = System.getProperty("MAB_OR_DOT1X", "mab");


  // It will store the name of the task.
  private String name;

  // Implement the constructor of the class to initialize the name of the task.
  public ExecutableTask(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  // Put the task to sleep for a random period of time and return a message
  // with the name of the task.
  @Override
  public String call() throws Exception {

    String macAddress = null;

    try {
      long duration = (long) (Math.random() * 10);
      macAddress = Util.randomMACAddress();
      logger.info(String.format("MAC Address :%s", macAddress));

      if (!"mab".equalsIgnoreCase(MAB_OR_DOT1X)) {
        sendPacket("10.0.10.130", macAddress);
      }
      else {
        sendMABPacket("10.0.10.130", macAddress);
      }

      TimeUnit.SECONDS.sleep(duration);

    } catch (InterruptedException ie) {
      logger.log(Level.SEVERE, ie.getLocalizedMessage());
      ie.printStackTrace(System.err);
    }
    return macAddress;
  }

  private void sendPacket(String host, String macAddress) throws Exception {

    String isMud = "true";
    String MUD_URI = null;

    int userId = 1 + (int) (Math.random() * ((16 - 1) + 1));
    System.out.println("userId =>" + userId);
    String userName = "mud_user" + userId;
    String pass = "Lab123";

    if (userId % 2 == 0) {
      MUD_URI =
          "mud-url=https://www.company.com/.well-known/mud/v1/CREE-device-AA" + "1." + userId;
    } else {
      MUD_URI = Util.getRandomMalformedURL();
    }

    System.out.println("==== MUD-URL === ");
    System.out.println("Mac Address: " + macAddress + " mud-url: " + MUD_URI);

    RadiusPacket resp = null;
    SingleRadiusAuthentication radiusClient = new SingleRadiusAuthentication();

    resp = radiusClient
          .sendRadiusPackets(host, macAddress, true,
              MUD_URI, userName, pass);

    System.out.println(resp.toString());
  }

  private void sendMABPacket(String host, String macAddress) throws Exception {

    String isMud = "true";
    String mudURL = null;

    int userId = 1 + (int) (Math.random() * ((16 - 1) + 1));
    System.out.println("userId =>" + userId);

    if (userId % 2 == 0) {
      mudURL =
          "mud-url=https://www.company.com/.well-known/mud/v1/CREE-device-AA" + "1." + userId;
    } else {
      mudURL = Util.getRandomMalformedURL();
    }

    System.out.println("Mac Address: " + macAddress + " mud-url: " + mudURL);

    RadiusPacket resp = null;
    System.out.println(" Sending MAB request for mac address " + macAddress);
    MABAuthentication.sendPackets(host, macAddress, true, mudURL);
  }
}
