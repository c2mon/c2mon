/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class TestUtil {

    private Logger log;
    protected static MobicallAlarmsPublisher publisher;
    protected static MobicallConfigLoaderMock loader;
    protected static C2monConnectionMock c2mon;
    protected static SenderMockImpl sender;
    
    private static boolean running;
    
    @Autowired
    ApplicationContext applicationContext;

    @BeforeClass
    public static void init() {
        System.setProperty("c2mon.client.conf.url", "file:client.properties");
        System.setProperty("app.version", "0.1");
        System.setProperty("app.name", "test");

        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configure(log4jConfigFile);
    }

    protected Logger getLogger() {
        if (log == null) {
            log = LoggerFactory.getLogger(this.getClass());
        }
        return log;
    }

    protected static void startTestPublisher() throws Exception {
        if (!running) {
            loader = new MobicallConfigLoaderMock();
            c2mon = new C2monConnectionMock();
            sender = new SenderMockImpl();
            publisher = new MobicallAlarmsPublisher(c2mon, loader, sender);
            publisher.connect();
        }
    }

    protected static void stopTestPublisher() {
        publisher.close();
        running = false;
    }
}
