/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */
package cern.c2mon.publisher.rda;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.publisher.core.PublisherKernel;
import cern.cmw.rda3.client.core.AccessPoint;
import cern.cmw.rda3.client.service.ClientService;
import cern.cmw.rda3.client.service.ClientServiceBuilder;
import cern.cmw.rda3.client.subscription.NotificationListener;
import cern.cmw.rda3.client.subscription.Subscription;
import cern.cmw.rda3.common.data.AcquiredData;
import cern.cmw.rda3.common.data.UpdateType;
import cern.cmw.rda3.common.exception.RdaException;

public class RdaPublisherTest {

    /** Is set to true, if an update was received by the RDA test client */
    private volatile boolean subscriptionTestOK = false;

    /** If an error occurred */
    private volatile boolean error = false;

    /** if first update was received **/
    private volatile boolean firstUpdateFlag = false;

    @Before
    public void beforeTest() {
        System.setProperty("app.name", "TIM-RDA-SERVER-TEST");
        System.setProperty("app.version", "1.0");
        System.setProperty("c2mon.client.conf.url", "http://timweb/conf/c2mon-client.properties");
        System.setProperty("log4j.configuration", "cern/c2mon/publisher/rda/log4j.xml");
        System.setProperty("c2mon.publisher.tid.location", "src/test/cern/c2mon/publisher/rda/test.tid");
        System.setProperty("c2mon.publisher.rda.server.name", "TIM-RDA-SERVER-TEST");
        System.setProperty("c2mon.publisher.rda.device.name", "TIM.RDA.DEVICE.TEST");
    }

    /**
     * Tests, whether it is possible to subscribe to the RDA publisher
     */
    @Test
    public void testSubscription() {
        PublisherKernel.main(new String[] {});

        ClientService client = null;
        Subscription sub = null;

        try {

            client = ClientServiceBuilder.newInstance().build();
            AccessPoint accessPoint = client.getAccessPoint(System.getProperty("c2mon.publisher.rda.device.name"),
                    "EY.L04.EMD801_4R:POSITION");

            sub = accessPoint.subscribe(new NotificationListener() {

                @Override
                public void errorReceived(Subscription subscription, RdaException exception, UpdateType updateType) {
                    System.out.println("---- error ----");
                    error = true;
                    exception.printStackTrace();
                }

                @Override
                public void dataReceived(Subscription subscription, AcquiredData value, UpdateType updateType) {
                    System.out.println("---- update ----");
                    System.out.println(value.getData());
                    long id = value.getData().getLong("id");
                    if (id != 161027) {
                        subscriptionTestOK = false;
                        error = true;
                        if (updateType == UpdateType.UT_FIRST_UPDATE) {
                            firstUpdateFlag = true;
                        }
                    } else {
                        subscriptionTestOK = true;
                    }
                }

            });

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // nothing to be done here
            }

        } catch (Exception ex) {
            error = true;
            ex.printStackTrace();
        } finally {
            if (client != null) {
                client.close();
            }

        }

        assertTrue(subscriptionTestOK);
        assertFalse("A RDA3 subscription error occured", error);
        assertFalse("First update was NOT received but was expected", firstUpdateFlag);
    }
}
