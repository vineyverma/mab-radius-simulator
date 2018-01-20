import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;


public class Util {

  public static List<String> read ( FileInputStream fis, String  encoding ) throws IOException {

    List<String> macList = new ArrayList<>();

    try ( BufferedReader br = new BufferedReader( new InputStreamReader(fis, encoding ))) {

      String line;
      while(( line = br.readLine()) != null ) {
        macList.add(line);
      }
      return macList;
    }
  }


  public List<String> getFileContent (String fileName) throws IOException  {

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(fileName).getFile());
    FileInputStream fileInputStream = new FileInputStream(file);

    List<String> macList = new ArrayList<>();

    try ( BufferedReader br = new BufferedReader( new InputStreamReader(fileInputStream, "UTF-8" ))) {

      String line;
      while(( line = br.readLine()) != null ) {
        macList.add(line);
      }
    }
    return macList;
  }

  public void readFileMethod2 (String fileName) throws IOException  {

    String expectedData = "Hello World from fileTest.txt!!!";
    InputStream inputStream = getClass().getResourceAsStream(fileName);
    String data = readFromInputStream(inputStream);
    System.out.println ("Data " + data);

  }

  private String readFromInputStream(InputStream inputStream) throws IOException {

    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br
        = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    }
    return resultStringBuilder.toString();
  }

  static String randomMACAddress() {
    Random rand = new Random();
    byte[] macAddr = new byte[6];
    rand.nextBytes(macAddr);

    macAddr[0] = (byte) (macAddr[0] & (byte) 254); // zeroing last 2 bytes
    // to make it unicast and
    // locally adminstrated

    StringBuilder sb = new StringBuilder(18);
    for (byte b : macAddr) {

      if (sb.length() > 0)
        sb.append(":");

      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  public static void main (String [] args) {

    for (int i=0; i<10;i++)
      System.out.println(getRandomMalformedURL());
    //testRegEx();
    for (int i=0; i<=10;i++) {
      System.out.println (randomMACAddress());
    }
  }

  private static void testRegEx() {

    String REGEX = "^(https|http)://[^\\s/$.?#].[^\\s]*$@iS";

    String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    // and finally, a word boundary or end of
    // input.  This is to stop foo.sure from
    // matching as foo.su

    String s1 = "https://www.xxx.com/mud/v1/A007-IOT-Z01-07";
    String s2 = "http://www.xxx.com/.well-known/mud/v1/A007-IOT-Z01-07";
    String s3 = "http://www.xxx.com//mud/v1/A007-IOT-Z01-07";
    String s4 = "http://abc123//mud/v1/A007-IOT-Z01-07";
    String s5 = "http:abc123//mud/v1/A007-IOT-Z01-07";
    String s6 = "https://www.xxx.com/.well-known/mud/005/A007-IOT-Z01-07";
    String s7 = "https://www.xxx.com//mud/005/A007-IOT-Z01-07";
    String s8 = "https://www.xxx.com/.well-known/mud/v1/794857489357";
    String s9 = "https://www.xxx.com/.well-known/mud/v1/#$%^&&&";
    String s10 = "https://www.xxx.com.SOMETHING/.well-known/mud/v2/#$%^&&&";
    String s11 = "ftp//www.xxx.com/.well-known/mud/v1/#$%^&&&";

    List<String> list = new ArrayList<>();
    list.add(s1);
    list.add(s2);
    list.add(s3);
    list.add(s4);
    list.add(s5);
    list.add(s6);
    list.add(s7);
    list.add(s8);
    list.add(s9);
    list.add(s10);
    list.add(s11);

    for (String s: list) {

      if (s.matches(regex)) {

        System.out.println ("PASSED " + s);

      } else {

        System.out.println ("FAILED " + s);

      }
    }

    int index1 = s2.indexOf("//");
    String token1 = s2.substring(0, index1);
    System.out.println ("token1=>" + token1);

    String token2 = s2.substring(index1 + s2.indexOf("/"));
    System.out.println ("token2=>" + token2);

    String[] splitted = s1.split("/");

    System.out.println ("Length => " + splitted.length);

    for (int i=0; i< splitted.length; i++) {
      System.out.println (splitted[i]);
    }
    // regex str[0] -> http | https
    // regex str[1] -> URL
    // regex str[2] -> mud
    // regex str[3] -> v1
    // regex str[4] -> no underscores
    androidPattern(list);
  }

  private static void androidPattern (List<String> list) {

    System.out.println ("android pattern");
    for (String s: list) {
      boolean isMatched = URLPattern.WEB_URL.matcher(s).matches();
      if (isMatched) {
        System.out.println ("PASSED " + s);
      } else {
        System.out.println ("FAILED " + s);
      }
    }
  }

  public static String getRandomMalformedURL () {

    String s4 = "http://abc123//mud/v1/A007-IOT-Z01-07";
    String s5 = "http:abc123//mud/v1/A007-IOT-Z01-07";
    String s6 = "https://www.xxx.com.khkk/.well-known/mud/005/A007-IOT-Z01-07";
    String s7 = "https://www.xxx.&*(//mud/005/A007-&*(IOT-Z01-07";
    String s8 = "https://www.xxx.com/.well-known/mud/v1/794857489357";
    String s9 = "https://www.xxx.com/.well-known/mud/v1/#$%^&&&";
    String s10 = "https://www.xxx.com.SOMETHING/.well-known/mud/v2/#$%^&&&";
    String s11 = "ftp//www.xxx.com/.well-known/mud/v1/#$%^&&&";

    List<String> list = new ArrayList<>();
    list.add(s4);
    list.add(s5);
    list.add(s6);
    list.add(s7);
    list.add(s8);
    list.add(s9);
    list.add(s10);
    list.add(s11);

    Random rand = new Random();
    int index = rand.nextInt(list.size());
    return list.get(index);

  }

}
