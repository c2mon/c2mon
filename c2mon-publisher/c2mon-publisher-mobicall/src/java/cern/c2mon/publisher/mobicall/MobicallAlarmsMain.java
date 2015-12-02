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
 *
 * @author mbuttner
 */
public class MobicallAlarmsMain extends Thread {

    private static Logger log;
    private static MobicallAlarmsPublisher client;

    public static void main(String[] args)
    {
        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configureAndWatch(log4jConfigFile, 60 * 1000);
        log = LoggerFactory.getLogger(MobicallAlarmsMain.class);
        log.info("Logging system ready ({})...", log4jConfigFile);

        Runtime.getRuntime().addShutdownHook(new MobicallAlarmsMain());
        
        try {
             client = new MobicallAlarmsPublisher(new C2monConnection());
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
     * This is the method called when ArchiverMain is declared to be its own shutdown
     * hook. In the deployed version, this will be activated in case of ctrl-c or
     * wreboot of the process (will NOT work from within Eclipse!).
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
