package cern.c2mon.toolkit;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import cern.accsoft.security.rba.RBAToken;
import cern.rba.util.lookup.RbaTokenLookup;

/**
 * Checks the overall health of a C2MON platform.
 * 
 * <p>This includes:
 * <ul>
 * <li>C2MON servers
 * <li>ActiveMQ brokers
 * <li>Terracotta servers
 * <li>C2MON clients (using the C2MON client API; this included publishers)
 * </ul>
 * 
 * <p>The items to monitor need specifying in a properties file; the Java
 * system property -Dc2mon.healthchecker.config.location must then point to
 * where this file is located (can use Spring resource format).
 * 
 * <p>Monitoring is done via JMX, so for each component a JMX host:port
 * must be provided. A single user/password is then expected for each
 * component category. For example, specifying a cluster of C2MON servers
 * is done using:
 * 
 * <p>c2mon.server.url=host1:port1,host2:port2<br/>
 * c2mon.server.user=username<br/>
 * c2mon.server.pwd=password
 * 
 * <p>For JMS brokers, both the host and broker name need specifying:
 * 
 * <p>jms.urls=broker_name:host:port,...</br>
 * jms.user=...</br>
 * jms.pwd=...
 * 
 * <p>Notice the DAQ processes are monitored by the C2MON platform itself.
 * 
 * @author Mark Brightwell
 *
 */
public class HealthChecker {

  
  private enum Status {OK, WARN}
  
  /**
   * For script version. Output is printed to sysout.
   * 
   * @param args
   * @throws Exception 
   * @throws IOException 
   */
  public static void main(String[] args) {
    try {
      System.out.println(checkHealth());
    } catch (Exception e) {
      System.err.println("Exception caught while running the C2MON health checker");
      e.printStackTrace();
      System.exit(1);
    }    
  }
  
  /**
   * Runs the health check on the C2MON platform.
   * 
   * @return the report of the current health
   * @throws IOException 
   * @throws NullPointerException 
   * @throws MalformedObjectNameException 
   * @throws ReflectionException 
   * @throws MBeanException 
   * @throws InstanceNotFoundException 
   */
  public static String checkHealth() throws Exception {
    StringBuilder healthReport = new StringBuilder();
    
    String location = System.getProperty("c2mon.healthchecker.config.location");
    if (location == null) {
      throw new NullPointerException("No configuration file specified (system property c2mon.healthchecker.config.location)");
    }
    
    Properties properties = new Properties();
    
    if (location.startsWith("file:")) {
      String parsedLocation = location.split(":")[1];
      properties.load(new FileReader(parsedLocation));
    } else if (location.startsWith("classpath:")) {
      String parsedLocation = location.split(":")[1];
      properties.load(HealthChecker.class.getResourceAsStream(parsedLocation));
    } else {
      properties.load(HealthChecker.class.getResourceAsStream(location));
    }
       
    healthReport.append("====================================================\n");
    healthReport = checkServers(healthReport, properties);   
    healthReport = checkBrokers(healthReport, properties);
            
    return healthReport.toString();
  }

  private static StringBuilder checkBrokers(StringBuilder healthReport, Properties properties) throws Exception {
    List<String> brokerUrls = parseUrls(properties.getProperty("jms.urls"));
    for (String url : brokerUrls) {  
      String c2monUser = properties.getProperty("jms.user");
      String c2monPwd= properties.getProperty("jms.pwd");
      Map<String, Object> credentialsMap = null;
      if (c2monUser == null || c2monUser.equalsIgnoreCase("")) {
       credentialsMap = createRBACCredentials();
      }
      else {
       credentialsMap = createCredentials(c2monUser, c2monPwd);
      }
      List<String> urlParts = splitUrl(url);
      healthReport.append("Health report for JMS broker " + urlParts.get(0) + " at " + urlParts.get(1) + ":" + urlParts.get(2) +  "\n\n");
      try {
        MBeanServerConnection mBeanConnection = createConnection(urlParts.get(1) + ":" + urlParts.get(2), credentialsMap);
        ObjectName objectName = new ObjectName("org.apache.activemq:BrokerName=" + urlParts.get(0) + ",Type=Broker");
        Integer memoryPercentUsage = Integer.valueOf(mBeanConnection.getAttribute(objectName, "MemoryPercentUsage").toString());        
        healthReport.append("Memory percent usage: " + memoryPercentUsage + " - " + (memoryPercentUsage.equals(0) ? Status.OK : Status.WARN) + "\n");
        Integer storePercentUsage = Integer.valueOf(mBeanConnection.getAttribute(objectName, "StorePercentUsage").toString());        
        healthReport.append("Store percent usage: " + storePercentUsage + " - " + (storePercentUsage.equals(0) ? Status.OK : Status.WARN) + "\n");
        Integer tempPercentUsage = Integer.valueOf(mBeanConnection.getAttribute(objectName, "TempPercentUsage").toString());
        healthReport.append("Temp percent usage: " + tempPercentUsage + " - " + (tempPercentUsage.equals(0) ? Status.OK : Status.WARN) + "\n");        
      } catch (IOException e) {
        healthReport.append("Unable to connect to JMS broker " + urlParts.get(0) + " at " + urlParts.get(1) + ":" + urlParts.get(2) + "\n\n");
      }
      healthReport.append("====================================================\n");
    }
    return healthReport;
  }

  /**
   * Splits along ":"
   */
  private static List<String> splitUrl(String url) {
    String delimiter = ":";
    return Arrays.asList(url.split(delimiter));
  }

  private static StringBuilder checkServers(StringBuilder healthReport, Properties properties) throws Exception {    
    List<String> c2monServerUrls = parseUrls(properties.getProperty("c2mon.server.urls"));            
    String c2monUser = properties.getProperty("c2mon.server.user");
    String c2monPwd= properties.getProperty("c2mon.server.pwd");
    Map<String, Object> credentialsMap = null;
    if (c2monUser == null || c2monUser.equalsIgnoreCase("")) {
     credentialsMap = createRBACCredentials();
    }
    else {
     credentialsMap = createCredentials(c2monUser, c2monPwd);
    }
              
    for (String url : c2monServerUrls) {
      healthReport.append("Health report for C2MON server at " + url + "\n\n");
      try {
        MBeanServerConnection mBeanConnection = createConnection(url, credentialsMap);
        
        ObjectName objectName = new ObjectName("cern.c2mon:name=sourceUpdateManager");
        Integer updateThreads = Integer.valueOf(mBeanConnection.getAttribute(objectName, "ActiveUpdateThreads").toString());
        healthReport.append("Number of threads currently processing DAQ updates: " + updateThreads + " - " + (updateThreads < 10 ? Status.OK : Status.WARN) + "\n");
        objectName = new ObjectName("cern.c2mon:name=processJmsContainerManager");
        Integer jmsThreads = Integer.valueOf(mBeanConnection.invoke(objectName, "getNumActiveThreads", null, null).toString());
        healthReport.append("Number of JMS container threads: " + jmsThreads + " - " + (jmsThreads < 200 ? Status.OK : Status.WARN) + "\n");
        objectName = new ObjectName("cern.c2mon:type=LaserPublisher,name=LaserPublisher");
        Boolean hasUnpublishedAlarms = Boolean.valueOf(mBeanConnection.invoke(objectName, "hasUnpublishedAlarms", null, null).toString());
        healthReport.append("Problems publishing alarms to LASER: " + hasUnpublishedAlarms + "\n");
      } catch (IOException e) {
        healthReport.append("Unable to connect to C2MON server at " + url + "\n");
      }
      healthReport.append("====================================================\n");
    }
    
    return healthReport;    
  }

  /**
   * Creates the JMX connection.
   */
  private static MBeanServerConnection createConnection(String url, Map<String, Object> credentialsMap) throws Exception {
    JMXServiceURL jmxServiceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + url + "/jmxrmi");
    //System.out.println(jmxServiceURL);
    JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, credentialsMap);
    MBeanServerConnection mBeanConnection = jmxConnector.getMBeanServerConnection();
    return mBeanConnection;
  }
  
  /**
   * Creates the JMX connection.
   */
  private static Map<String, Object> createRBACCredentials() throws Exception {
    HashMap<String, Object> credentialsMap = new HashMap<String, Object >();
    
    RBAToken rbaToken = RbaTokenLookup.findRbaToken();
    if (rbaToken == null) {
        throw new IllegalStateException("No RBAC by location token found. Please specify user and password instead in the property file.");
    }
    
    credentialsMap.put(JMXConnector.CREDENTIALS, rbaToken);
    return credentialsMap;
  }

  /**
   * Creates the credentials map.
   */
  private static Map<String, Object> createCredentials(String user, String pwd) {
    String[] credentials = new String[]{user, pwd}; 
    HashMap<String, Object> credentialsMap = new HashMap<String, Object >();
    credentialsMap.put(JMXConnector.CREDENTIALS, credentials);
    return credentialsMap;
  }

  /**
   * Parses a list of URLs delimited by ",".
   */
  private static List<String> parseUrls(String c2monUrls) {
    String delimiter = ",";
    return Arrays.asList(c2monUrls.split(delimiter));
  }
  
 

}
