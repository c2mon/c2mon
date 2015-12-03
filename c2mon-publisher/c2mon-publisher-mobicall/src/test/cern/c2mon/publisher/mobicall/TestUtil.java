/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import org.apache.log4j.PropertyConfigurator;

/**
 * Some static utility methods for the JUnit tests: start the publisher based on the
 * instances of mock classes, stop the publisher, configure log4j.
 * 
 * @author mbuttner
 */
public class TestUtil {

    protected static MobicallAlarmsPublisher publisher;
    protected static MobicallConfigLoaderMock loader;
    protected static C2monConnectionMock c2mon;
    protected static SenderMockImpl sender;
    
    // flag used to aoivd starting the publisher twice
    private static boolean running;

    /**
     * Prepare standard properties and setup logging
     */
    public static void init() {
        System.setProperty("c2mon.client.conf.url", "file:client.properties");
        System.setProperty("app.version", "0.1");
        System.setProperty("app.name", "test");

        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configure(log4jConfigFile);
    }

    /**
     * Start the publisher with intances of the mocks for all components.
     * @throws Exception
     */
    protected static void startTestPublisher() throws Exception {
        if (!running) {
            loader = new MobicallConfigLoaderMock();
            c2mon = new C2monConnectionMock();
            sender = new SenderMockImpl();
            publisher = new MobicallAlarmsPublisher(c2mon, loader, sender);
            publisher.connect();
        }
    }
    
    /**
     * Stop the publisher.
     */
    protected static void stopTestPublisher() {
        publisher.close();
        running = false;
    }
}
