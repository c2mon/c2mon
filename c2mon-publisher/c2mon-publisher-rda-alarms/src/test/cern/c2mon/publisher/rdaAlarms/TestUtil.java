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

public class TestUtil {

    public static final String EXISTING_ALARM_ID = "FF:FM:1";
    public static final String EXISTING_SOURCE_ID = "TSOURCE";
    public static final String NOT_EXISTING_ALARM_ID = "XX:YY:Z";
    
    protected Logger log;
    
    @Autowired
    ApplicationContext applicationContext;
    
    @BeforeClass
    public static void init() {
        System.setProperty("c2mon.client.conf.url", "file:client.properties");
        System.setProperty("provider.properties", "file:provider.properties");
        
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configure(log4jConfigFile);
    }

    protected Logger getLogger() {
        if (log == null) {
            log = LoggerFactory.getLogger(this.getClass());
        }
        return log;
    }
}
