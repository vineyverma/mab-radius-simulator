/**
 * Created by vinverma on 11/4/17.
 */
public class MockRadius {

    public void sendRadiusPackets(boolean isRadius,
        String hostName,
        String mac,
        String user,
        String password,
        boolean isFlag,
        String baseURI) {

      System.out.println ("SendRadiusPackets called for " + user + ": pass: " + password + " : " + baseURI);

    }

}
