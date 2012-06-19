package cern.c2mon.publisher.core;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.publisher.test.TestPublisher;

public class PublisherKernelTest {

  @Before
  public void beforeTest() {
    System.setProperty("app.name", "C2MON-PUBLISHER-CORE-TEST");
    System.setProperty("app.version", "1.0");
    System.setProperty("c2mon.client.conf.url", "http://timweb/conf/c2mon-client.properties");
    System.setProperty("log4j.configuration", "cern/c2mon/publisher/core/log4j.xml");
    System.setProperty("c2mon.publisher.tid.location", "src/test/cern/c2mon/publisher/core/test.tid");
  }
  
  @Test
  public void testStartup() {
    PublisherKernel.main(new String[]{});
    
    try {Thread.sleep(3000);} catch (InterruptedException e) {}
    
    assertTrue(TestPublisher.getUpdateCounter() >= 9);
  }
}
