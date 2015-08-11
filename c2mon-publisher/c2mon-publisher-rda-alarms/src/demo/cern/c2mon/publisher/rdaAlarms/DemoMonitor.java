/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.Selector;
import cern.japc.SimpleParameterValue;
import cern.japc.SubscriptionHandle;
import cern.japc.factory.ParameterFactory;
import cern.japc.factory.ParameterValueFactory;

/**
 * Demonstrates how to monitor an alarm source for given set of alarms.
 *
 * @author mbuttner 
 */
public class DemoMonitor implements ParameterValueListener
{
    /**
     * The source. This is something you need to take from the alarm system configuration
     */
    static final String sourceId = "PSB";
    
    /**
     * The alarm as identified in the alarm system. The alarm will only be correctly received
     * if it is sent by the source used to subscribe.
     */
    static final String[] laserAlarmId = {
            "XENERICSAMPLER_2106:BA.SAGSHARM:4000",
            "XENERICSAMPLER_2106:BA.SAGSHARM:5000",
            "XENERICSAMPLER_2106:BA.SAGSHARM:6000",
            "XENERICSAMPLER_2106:BA.SAGSHARM:7000",
            "XENERICSAMPLER_2106:BR4.QH4-ST:6000" 
            };
    
    /**
     * The internal id in your application.
     */
    static final String clientAlarmId[] = { "X1", "X2", "X3", "X4", "OSC" };

    /**
     * The URL used to create the JAPC parameter.
     */
//    static final String paramUrl = "rda3:///DMN.RDA.ALARMS/" + sourceId;
    
    private static Log log;
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {        
        System.setProperty("app.name", "japc-ext-laser DemoMonitor");
        System.setProperty("app.version", "0.0.1");
        
        // standard logging setup stuff
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configureAndWatch(log4jConfigFile, 60 * 1000);   
        log = LogFactory.getLog(DemoMonitor.class);
        log.info("Starting " + DemoMonitor.class.getName() + " ...");

        
        // create the filter parameter: This is a list of pairs internal alarm id / alarm sys alarm id
        Map<String, SimpleParameterValue> filterParams = new HashMap<String, SimpleParameterValue>();        
        for (int i = 0; i < clientAlarmId.length; i++)
        {   
            filterParams.put(clientAlarmId[i], ParameterValueFactory.newParameterValue(laserAlarmId[i]));        
        }
        // ... add others as needed ...
        MapParameterValue filter = ParameterValueFactory.newParameterValue(filterParams);
        
        // selector The id value is used as dely to re-publish (!?)
        Selector selector = ParameterValueFactory.newSelector("5000", filter, false);           

        try
        {
            //
            // create the parameter, do the GET operation and extract the status of the alarm
            // from the result.
            DemoMonitor mon = new DemoMonitor();
            Parameter param = ParameterFactory.newInstance().newParameter("DMN.RDA.ALARMS", sourceId);
            SubscriptionHandle sh = param.createSubscription(selector, mon);            
            sh.startMonitoring();            
            
            log.info("Waiting 5 minutes for data ...");
            Thread.sleep(5 *60 * 1000); // let's run it for 5 minutes.

            log.info("Go down ...");
            sh.stopMonitoring();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        log.info(DemoMonitor.class.getName()  + " completed");
        System.exit(0);
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException pe)
    {
        log.error(parameterName + " (" + description + ")", pe);
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue avalue)
    {
        try
        {
            log.info("Value received for " + parameterName + " :\n" + avalue.getValue());
            MapParameterValue value = (MapParameterValue)avalue.getValue();
   
            String firstAlarmId = clientAlarmId[0];
            SimpleParameterValue spv = value.get(firstAlarmId);
            if (spv != null)
            {
                log.info(firstAlarmId + ": " + spv.getString());
            }
            else
            {
                log.warn("Alarm " + firstAlarmId + " not found in " + parameterName);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();            
        }
    }

    
}
