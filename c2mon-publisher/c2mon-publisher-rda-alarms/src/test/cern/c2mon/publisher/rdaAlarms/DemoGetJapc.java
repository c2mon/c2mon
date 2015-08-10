/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.factory.ParameterFactory;
import cern.japc.factory.ParameterValueFactory;

/**
 * Demonstrates a simple GET operation on japc-ext-laser based JAPC parameter
 *
 * TODO create a mean to redirect dataserver to the localhost, so that we can test compat with new server!!!
 * TODO same of course for monitor and multiparam!
 *
//TODO to test new instance, redirect using this system prop        diamon.alarms.service
 * 
 * @author mbuttner 
 */
public class DemoGetJapc {

    /**
     * The source. This is something you need to take from the alarm system configuration
     */
    static final String sourceId = "TIMOPALARM";
        
    /**
     * The alarm as identified in the alarm system. The alarm will only be correctly received
     * if it is sent by the source used to subscribe.
     */
    static final String laserAlarmId = "SECU_FEU_LHC:SFDIN-00279:1663";
        
    /**
     * The internal id in your application.
     */
    static final String clientAlarmId = "MY_ALARM";

    /**
     * The URL used to create the JAPC parameter.
     */
    static final String paramUrl = "rda3:///DMN.RDA.ALARMS/" + sourceId;
        
    /**
     * @param args
     */
    public static void main(String[] args)
    {        
        System.setProperty("app.name", "japc-ext-laser DemoGet");
        System.setProperty("app.version", "0.0.1");

        // default
        // System.setProperty("diamon.alarms.service",  "http://cs-ccr-dmnp2:19001/data/");
        // test
        // System.setProperty("diamon.alarms.service",  "http://localhost:19001/data/");
        
        // standard logging setup stuff
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configureAndWatch(log4jConfigFile, 60 * 1000);   
        Logger log = LoggerFactory.getLogger(DemoGetJapc.class);
        log.info("Starting " + DemoGetJapc.class.getName() + " ...");
            
        // create the filter parameter: This is a list of pairs internal alarm id / alarm sys alarm id
        Map<String, SimpleParameterValue> filterParams = new HashMap<String, SimpleParameterValue>();        
        filterParams.put(clientAlarmId, ParameterValueFactory.newParameterValue(laserAlarmId));        
        // ... add others as needed ...
        MapParameterValue filter = ParameterValueFactory.newParameterValue(filterParams);
            
        // selector The id value is used as dely to re-publish (!?)
        Selector selector = ParameterValueFactory.newSelector("5000", filter);           

        try
        {
            //
            // create the parameter, do the GET operation and extract the status of the alarm
            // from the result.
//            Parameter param = ParameterFactory.newInstance().newParameter(paramUrl);
            Parameter param = ParameterFactory.newInstance().newParameter("DMN.RDA.ALARMS","TIMOPALARM");
            AcquiredParameterValue avalue = param.getValue(selector);
            MapParameterValue value = (MapParameterValue)avalue.getValue();
//            SimpleParameterValue spv = value.get(clientAlarmId);           
            log.info("Initial status of alarm " + clientAlarmId + ":" + value.toString());
        }
        catch ( ParameterException e )
        {
            e.printStackTrace();
        }
        log.info("Completed");
        System.exit(0);
    }

               
}


