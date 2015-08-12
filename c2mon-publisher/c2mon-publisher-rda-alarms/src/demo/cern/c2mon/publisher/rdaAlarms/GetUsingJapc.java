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
 * Demonstrates a simple "alarm GET" operation using JAPC
 *
 * Notes:
 * - if you do not provide a selector with filter, all alarms with an event since startup of the publisher
 *   will be returned.
 * - if you do provide a selector with filter, only the alarms matching the ids in the filter list will 
 *   be returned, and this only if they really belong to the alarm source specified as property name.
 * - the use of the filter also allows the renaming of alarms (see "client alarm id" vs "laser alarm id")
 *   for internal use by the application.
 * 
 * @author mbuttner 
 */
public class GetUsingJapc {

    /**
     * The RDA device publishing the alarms
     */
    static final String DEVICE = "DMN.RDA.ALARMS";
    
    /**
     * The source. This is something you need to take from the alarm system configuration
     */
    static final String sourceId = "TIMOPALARM";
        
    /**
     * The alarm as identified in the alarm system. The alarm will only be correctly received
     * if it is sent by the source. The alarm id acts as a filter on the property, which  contains
     * all (at least the active) alarms sent by the source. 
     */
    static final String laserAlarmId = "SECU_FEU_LHC:SFDIN-00279:1663";
        
    /**
     * The internal alarm id in your application.
     */
    static final String clientAlarmId = "MY_ALARM";


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
        Logger log = LoggerFactory.getLogger(GetUsingJapc.class);
        log.info("Starting " + GetUsingJapc.class.getName() + " ...");
            
        // create the filter parameter: This is a list of pairs internal alarm id / alarm sys alarm id
        Map<String, SimpleParameterValue> filterParams = new HashMap<String, SimpleParameterValue>();        
        filterParams.put(clientAlarmId, ParameterValueFactory.newParameterValue(laserAlarmId));        
        filterParams.put("MY_FAKE", ParameterValueFactory.newParameterValue("FF:FM:FC"));        
        // ... add others as needed ...
        MapParameterValue filter = ParameterValueFactory.newParameterValue(filterParams);
            
        // selector The id value is used as dely to re-publish (!?)
        Selector selector = ParameterValueFactory.newSelector("5000", filter);           

        try
        {
            //
            // create the parameter, do the GET operation and extract the status of the alarm
            // from the result.
            Parameter param = ParameterFactory.newInstance().newParameter("DMN.RDA.ALARMS","TIMOPALARM");
            AcquiredParameterValue avalue = param.getValue(selector);
            MapParameterValue value = (MapParameterValue)avalue.getValue();
            log.info("Initial status of alarm " + clientAlarmId + "= " + value.toString());
        }
        catch ( ParameterException e )
        {
            e.printStackTrace();
        }
        log.info("Completed");
        System.exit(0);
    }

               
}


