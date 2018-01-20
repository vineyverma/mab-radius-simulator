import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RadiusExecutor {

  String numRequests = System.getProperty("numberOfRequests", "1");
  String hostName = System.getProperty("hostName", "tb071-ise1.cisco.com");
  String baseURI = System.getProperty("base_mud_uri", "https://www.philips.com/mud-uri/light/bulb/001");

  final static String REQUEST_TYPE = System.getProperty("request_type", "mab"); // mab or radius
  final static String WITH_MUD = System.getProperty("isMud", "false"); // mab or radius

  private static List<String> macList = new ArrayList<>();
  private static List<String> userList = new ArrayList<>();

  public List<Callable<String>> createTasks () {

    List<Callable<String>> callableTasks = new ArrayList<>();

    if (REQUEST_TYPE.equalsIgnoreCase("mab")) {
      for (int i = 0; i < Integer.parseInt(numRequests); i++) {
        callableTasks.add(callableTaskMab);
      }
    } else {
       for (int i = 0; i < Integer.parseInt(numRequests); i++) {
          callableTasks.add(callableTaskRadius);
        }
      }

    return callableTasks;
  }

  Callable<String> callableTaskMab = () -> {

    System.out.println("Sending MAB packets ..");

    Random rand = new Random();
    int n = rand.nextInt (macList.size()) + 1;

    String mac = macList.get(n);
    System.out.println("Picked mac " + n + " mac: " + mac);

    if (WITH_MUD.equalsIgnoreCase("true"))
      MABAuthentication.sendPackets(hostName, mac, true, baseURI + mac);
    else
      MABAuthentication.sendPackets(hostName, mac, false, baseURI + mac);

    return macList.get(n);

  };

  Callable<String> callableTaskRadius = () -> {

    System.out.println("Sending radius packet ..");

    Random rand = new Random();
    int n = rand.nextInt (macList.size()) + 1;

    String mac = macList.get(n);

    n = rand.nextInt (userList.size()) + 1;

    String[] userPass = userList.get(n).split("=");
    System.out.println ("Sending radius packet for mac " + mac + " user: " + userPass[0] + " pass: " + userPass[1]);

    //RadiusAuthentication authentication = new RadiusAuthentication();

    MockRadius authentication = new MockRadius();
    authentication.sendRadiusPackets(false, hostName, mac, userPass[0], userPass[1], true, baseURI + mac);

    return userList.get(n);

  };


  public static void main (String[] args)  {

    System.out.println (" Running main..on following mac addreses.");
    Util fileUtils = new Util();
    RadiusExecutor threader = new RadiusExecutor();
    ExecutorService executorService = Executors.newFixedThreadPool(10);
    List<Callable<String>> tasks = null;//threader.createTasks();

    if (REQUEST_TYPE.equalsIgnoreCase("mab")) {

      try {
           macList = fileUtils.getFileContent("src/main/resources/mac_list.txt");
           System.out.println (" Mac List:" + macList);
      } catch (Exception er) {
          er.printStackTrace();
      }

      tasks = threader.createTasks();

      try {
        executorService.invokeAll(tasks);
      } catch (Exception er) {
        er.printStackTrace();
      }

    } else {

        try {

          userList = fileUtils.getFileContent("radius_users.txt");
          System.out.println (" User Password List:" + userList);

          Callable task1 = threader.callableTaskRadius;
          Callable task2 = threader.callableTaskRadius;

          List<Callable<String>> callables = new ArrayList<>();

          callables.add(task1);
          callables.add(task2);

          executorService.submit(task1);
          executorService.submit(task2);

          executorService.invokeAll(callables);

        } catch (Exception er) {
          er.printStackTrace();
        }
    }

    executorService.shutdown();

  }

}
