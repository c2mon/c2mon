/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:cern/c2mon/publisher/rdaAlarms/alarms_publisher.xml")
@ActiveProfiles(profiles = "TEST")
public class TestBaseClass {

    public static final String RDA_TEST_DEVICE = "DMN.RDA.ALARMS.TEST";
    
    public static final String ALARM_ID = "FF:FM:1";
    public static final String ALARM_ID_bis = "FF:FM:3";
    public static final String SOURCE_ID = "TSOURCE";
    public static final String MISSING_ALARM_ID = "XX:YY:Z";
    public static final String SOURCE_ID_bis = "TSOURCE_2";
    
    public static final String  SAMPLE_FF = "FF";
    public static final String  SAMPLE_FM = "FM";
    public static final int     SAMPLE_FC = 2;
    public static final String  SAMPLE_ALARM_ID = SAMPLE_FF + ":" + SAMPLE_FM + ":" + SAMPLE_FC;
    
    private Logger log;
    private static RdaAlarmsPublisher publisher;
    
    @Autowired
    ApplicationContext applicationContext;
    
    @BeforeClass
    public static void init() {
        System.setProperty("c2mon.client.conf.url", "file:client.properties");
        System.setProperty("provider.properties", "file:provider.properties");
        System.setProperty("app.version", "0.1");
        System.setProperty("app.name", "test");
        System.setProperty("cmw.rda3.transport.server.multiThreadedPublisher","true");
//        System.setProperty("cmw.directory.client.serverList", "cmw-dir-test:5021");
        
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configure(log4jConfigFile);
    }

    protected Logger getLogger() {
        if (log == null) {
            log = LoggerFactory.getLogger(this.getClass());
        }
        return log;
    }
    
    protected void startTestPublisher() {
        publisher = RdaAlarmsPublisher.getPublisher();
        if (!publisher.isRunning()) {
            publisher.setServerName("DMN-RDA-ALARMS-TEST");
            publisher.start();       
        }
    }
    
    protected static void stopTestPublisher() {
        publisher.shutdown();
    }
}
