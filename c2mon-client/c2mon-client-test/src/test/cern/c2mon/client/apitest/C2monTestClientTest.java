package cern.c2mon.client.apitest;

import org.junit.Test;

/**
 * Tests C2monTestClient starts up correctly, using the C2MON test system.
 * 
 * @author Mark Brightwell
 *
 */
public class C2monTestClientTest {

  @Test
  public void testSubscription() {
    System.setProperty("c2mon.client.test.tagids.location", "classpath:resources/c2mon-test-client-tagids.txt");
    System.setProperty("c2mon.client.test.subscription.number", "5");
    System.setProperty("c2mon.client.process.name", "test-client-tags");
    System.setProperty("c2mon.client.conf.url", "url:http://timweb/test/conf/c2mon-client.properties");
    String[] args = new String[0];
    C2monTestClient.main(args);
  }
  
}
