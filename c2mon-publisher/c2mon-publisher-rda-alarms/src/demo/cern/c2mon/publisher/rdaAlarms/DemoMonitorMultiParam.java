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
 * Demonstrates how to listen to alarms coming from different alarm sources.
 *
 * @author mbuttner 
 */
public class DemoMonitorMultiParam implements ParameterValueListener
{    
    private static Log log;
    
    private static String alarmId_1 = "PSBS:DE1.STP26:1000";
    private static String sourceId_1 = "CPS";

    //
    // --- MAIN -----------------------------------------------------------------------------------
    //
    public static void main(String[] args)
    {        
        System.setProperty("app.name", "japc-ext-laser DemoMonitor");
        System.setProperty("app.version", "0.0.1");
        
        // standard logging setup stuff
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configureAndWatch(log4jConfigFile, 60 * 1000);   
        log = LogFactory.getLog(DemoMonitorMultiParam.class);
        log.info("Starting " + DemoMonitorMultiParam.class.getName() + " ...");
        
        // create the filter parameter: This is a list of pairs internal alarm id / alarm sys alarm id
        Map<String, SimpleParameterValue> psbFilterParams = new HashMap<String, SimpleParameterValue>();        
        psbFilterParams.put(sourceId_1, ParameterValueFactory.newParameterValue(alarmId_1));        
        MapParameterValue psbFilter = ParameterValueFactory.newParameterValue(psbFilterParams);
        Selector psbSelector = ParameterValueFactory.newSelector("5000", psbFilter, false);           

        Map<String, SimpleParameterValue> leiFilterParams = new HashMap<String, SimpleParameterValue>();        
        leiFilterParams.put("LEI", ParameterValueFactory.newParameterValue("XENERICSAMPLER_2106:ER.SCBSYNTH:4000"));        
        MapParameterValue leiFilter = ParameterValueFactory.newParameterValue(leiFilterParams);
        Selector leiSelector = ParameterValueFactory.newSelector("5000", leiFilter, false);           

        try
        {
            DemoMonitorMultiParam mon = new DemoMonitorMultiParam();
            Parameter p1 = ParameterFactory.newInstance().newParameter("laser:///laser/" + sourceId_1);
            SubscriptionHandle sh1 = p1.createSubscription(psbSelector, mon);            
            sh1.startMonitoring();            

            Parameter p2 = ParameterFactory.newInstance().newParameter("laser:///laser/LEI");
            SubscriptionHandle sh2 = p2.createSubscription(leiSelector, mon);            
            sh2.startMonitoring();            
                        
            Thread.sleep(2 * 60 * 1000); // let's run it for 2 minutes.

            sh1.stopMonitoring();
            sh2.stopMonitoring();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        log.info(DemoMonitorMultiParam.class.getName()  + " completed");
        System.exit(0);
    }

    @Override
    public void exceptionOccured(String parameterName, String description, ParameterException pe)
    {
        pe.printStackTrace();
    }

    @Override
    public void valueReceived(String parameterName, AcquiredParameterValue avalue)
    {
        try
        {
            String value = avalue.getValue().toString();
            log.info("Value received for " + parameterName + ": " + value.trim());
        }
        catch (Exception e)
        {
            log.error("Failed to retrieve value for " + parameterName, e);            
        }
    }

    
}
