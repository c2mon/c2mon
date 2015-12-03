/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Mobicall alarms publisher acts as a C2MON client working with all alarm tags in the server.
 * Based on configuration data, alarms are forwarded to the Mobicall system through a SNMP trap.
 *
 * Notification of technical problems: Remember to set diamon.support property to the mail address
 * of the team. By default, mails go to the author!
 * 
 * Notes:
 * - alarms are notified to mobicall if they have a "notification id" in the alarm definition. This id
 *   comes from Mobicall and should first be declared there.
 *   
 * - when this application starts, it loads the list of alarms to be notified, and refreshes the
 *   list once per a given number of minutes. The delay can be configured (see mobicall.properties 
 *   at the root of the classpath). The default value is 5 minutes. The loading is done asynchronously, 
 *   failing is accepted (and only logged). When the config load is failing, the app continues with
 *   the known list.
 *   
 * - validity of notified alarms: to avoid wrong notifications, the app checks for each alarm to be
 *   notified if the underlying datatag is valid. Only alarms for valid datatags are notified.
 *   
 * - no redundant notifications: at startup, only the alarms activated within a short period 
 *   before the start are notified. The delay can be configured, default is 60s. This means that when
 *   the application is restarted only the alarms activated within the last minute will be notified,
 *   and are therefore subject to redundant notifications. IF THE SYSTEM IS STOPPED for a longer
 *   period (not a simple restart), one should consider change the configuration of the delay before
 *   restarting in order to adjust it to the duration of the outage
 * 
 * - start/stop: the main method uses a shutdown hook to ask all threads (alarm listener and configuration
 *   loader) to go down in a clean way. The configuration defines a latency time (default 5s). When
 *   threads sleep, they do it for this time. When stopping, the caller should wait for exactly this 
 *   delay to make sure the threads have the required time to stop.  
 *   
 * - this application integrates the C2MON client, the alarm definitions from the database and the
 *   sending of SNMP traps. For unit tests, these 3 external elements are mock'd (to allow testing
 *   of only the internal logic). 
 *
 * TODO safety mechanism if configuration mechanism dies (notification !?)
 * TODO deployment project
 *
 * @author mbuttner
 */
public class MobicallAlarmsMain extends Thread {

    private static Logger log;
    private static MobicallAlarmsPublisher client;

    public static void main(String[] args)
    {
        // configure log4j
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configureAndWatch(log4jConfigFile, 60 * 1000);
        log = LoggerFactory.getLogger(MobicallAlarmsMain.class);
        log.info("Logging system ready ({})...", log4jConfigFile);
        
        // add the shutdown hook. When called, this initiates the clean stop of the app 
        // (usually done from ctrl-c in interactive mode, or wreboot on deployed version)
        Runtime.getRuntime().addShutdownHook(new MobicallAlarmsMain());

        // start the publisher and wait for its "exit" notification
        try {
             client = new MobicallAlarmsPublisher(new C2monConnection(), 
                     MobicallConfigLoaderDB.getLoader(), 
                     new SenderSnmpImpl());
             
             client.connect();
             synchronized (client) {
                 client.wait();
             }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        log.info("Halt.");
        System.exit(0);
    }

    //
    // --- Implements Runnable -----------------------------------------------------------
    //
    /**
     * For shutdown hook. In the deployed version, this will be activated in case of ctrl-c or
     * wreboot of the process (will NOT work from within Eclipse!). The publisher is asked 
     * to stop, once this is done, it will notify the main method.
     */
    @Override
    public void run()
    {
        log.info("Going down ...");
        if (client != null) {
            client.close();
        }
    }

}
