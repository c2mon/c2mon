package cern.c2mon.publisher.rda;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.publisher.core.PublisherKernel;
import cern.cmw.Data;
import cern.cmw.DataEntry;
import cern.cmw.IOError;
import cern.cmw.TypeMismatch;
import cern.cmw.rda.client.Subscription;

public class RdaPublisherTest {

  /** Is set to true, if an update was received by the RDA test client */
  private volatile boolean subscriptionTestOK = false;
  
  /** If an error occured */
  private volatile boolean error = false;
  
  @Before
  public void beforeTest() {
    System.setProperty("app.name", "TIM-RDA-SERVER-TEST");
    System.setProperty("app.version", "1.0");
    System.setProperty("c2mon.client.conf.url", "http://timweb/conf/c2mon-client.properties");
    System.setProperty("log4j.configuration", "cern/c2mon/publisher/rda/log4j.xml");
    System.setProperty("publisher.tid.location", "src/test/cern/c2mon/publisher/rda/test.tid");
    System.setProperty("rda.device.name", "TIM.RDA.DEVICE.TEST");
  }

  /**
   * Tests, whether it is possible to subscribe to the RDA publisher 
   */
  @Test
  public void testSubscription() {
    PublisherKernel.main(new String[] {});

    Subscription s = new Subscription(System.getProperty("rda.device.name"), "EY.L04.EMD801_4R:POSITION", "1000") {
      @Override
      protected void handleValue(Data value) {
        try {
          DataEntry idEntry = value.get("id");
          long id = idEntry.extractLong();
          if (value.get("id").extractLong() != 161027) {
            subscriptionTestOK = false;
            error = true;
          }
          else {
            subscriptionTestOK = true;
          }
        }
        catch (TypeMismatch e) {
          error = true;
        }
      }

      @Override
      protected void handleError(IOError ex) {
        error = true;
      }
    };
    s.start();
    
    try {Thread.sleep(3000);} catch (InterruptedException e) {}
    
    assertTrue(subscriptionTestOK);
    assertFalse("A RDA subscription error occured", error);
  }

}
