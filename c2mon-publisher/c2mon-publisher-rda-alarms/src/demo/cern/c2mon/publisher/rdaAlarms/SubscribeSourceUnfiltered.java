/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import cern.c2mon.publisher.rdaAlarms.RdaAlarmProperty.AlarmState;
import cern.japc.AcquiredParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.Parameter;
import cern.japc.ParameterException;
import cern.japc.ParameterValueListener;
import cern.japc.SubscriptionHandle;
import cern.japc.factory.ParameterFactory;

/**
 * Subscribes to a source (to be provided as first command line argument) and prints the number
 * of active/terminated alarms each time an update us received.
 *
 * @author mbuttner 
 */
public class SubscribeSourceUnfiltered implements ParameterValueListener
{
    
    private static Log log;
    
    public static void main(String[] args)
    {        
        System.setProperty("app.name", "japc-ext-laser DemoMonitor");
        System.setProperty("app.version", "0.0.1");
        
        // standard logging setup stuff
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configureAndWatch(log4jConfigFile, 60 * 1000);   
        log = LogFactory.getLog(SubscribeSourceUnfiltered.class);
        log.info("Starting " + SubscribeSourceUnfiltered.class.getName() + " ...");
        
        try
        {
            //
            // create the parameter, do the GET operation and extract the status of the alarm
            // from the result.
            SubscribeSourceUnfiltered mon = new SubscribeSourceUnfiltered();
            Parameter param = ParameterFactory.newInstance().newParameter("DMN.RDA.ALARMS", args[0]);
            SubscriptionHandle sh = param.createSubscription(null, mon);            
            sh.startMonitoring();            
            
            log.info("Press enter in this window to stop ...");
            System.in.read();

            log.info("Go down ...");
            sh.stopMonitoring();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        log.info(SubscribeSourceUnfiltered.class.getName()  + " completed");
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
        int active = 0;
        int terminate =0;
        int invalid = 0;
        MapParameterValue value = (MapParameterValue)avalue.getValue();
        for (String alarmId : value.getNames()) {
            try {
                AlarmState state = AlarmState.valueOf(value.getString(alarmId));
                if (state == AlarmState.ACTIVE) {
                    active++;
                } else {
                    if (state == AlarmState.TERMINATE) {
                        terminate++;
                    }
                }
            } catch (Exception e) {
                    invalid++;
            }
        }
        log.info("Value received for " + parameterName + " :" + value.size() + 
                "(A/T/I " + active + "," + terminate + "," + invalid +")");
    }

    
}
