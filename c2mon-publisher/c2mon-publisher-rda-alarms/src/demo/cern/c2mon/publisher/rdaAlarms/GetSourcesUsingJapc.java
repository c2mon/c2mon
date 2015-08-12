/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.factory.ParameterFactory;

/**
 * 
 * @author mbuttner 
 */
public class GetSourcesUsingJapc {

    static final String DEVICE = "DMN.RDA.ALARMS";
    static final String PROP = "_SOURCES";
        
    //
    // --- MAIN -------------------------------------------------------------------------------------
    //
    public static void main(String[] args)
    {        
        System.setProperty("app.name", "japc-ext-laser DemoGet");
        System.setProperty("app.version", "0.0.1");
        
        // standard logging setup stuff
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configureAndWatch(log4jConfigFile, 60 * 1000);   
        Logger log = LoggerFactory.getLogger(GetSourcesUsingJapc.class);
        log.info("Starting " + GetSourcesUsingJapc.class.getName() + " ...");
            
        try
        {
            Parameter param = ParameterFactory.newInstance().newParameter(DEVICE, PROP);
            AcquiredParameterValue avalue = param.getValue(null);
            MapParameterValue value = (MapParameterValue) avalue.getValue();
            log.info("Sources declared in publisher:");
            for (String sourceId : value.getNames()) {
                log.info(" - {}", sourceId);
            }
        }
        catch ( ParameterException e )
        {
            e.printStackTrace();
        }
        log.info("Completed");
        System.exit(0);
    }

               
}


