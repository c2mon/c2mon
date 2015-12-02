/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO send mobicall notifications on startup only if alarm was activated in between stop and start!
 * TODO test and write tests 
 * TODO comment and document
 * 
 * @author mbuttner
 */
public class MobicallAlarm {

    public static final int LATENCY = 5; // in seconds, = sleep slice and wait time to let the thread complete on stop
    public static final int REFRESH_INTERVAL = 5; // update alarm definitions once per n minutes
    
    static final Logger LOG = LoggerFactory.getLogger(MobicallAlarm.class);
    
    private static final String DBPWD = ".dbpwd.properties";
    private static final String DBCONN = "/database.properties";
    
    private String alarmId;
    private String systemName;              
    private String identifier;              
    private int faultCode;                  
    private String notificationId;              
    private String problemDescription;      
    
    private static ConcurrentHashMap<String, MobicallAlarm> alarms;
    private static Thread configuratorThread;
    private static MobicallConfigurator configurator;
    private static Connection conn;
    
    /***
     * Configure the database access and start the background thread
     * @throws Exception when connection to DB fails
     */
    public static void init() throws Exception {
        
        alarms = new ConcurrentHashMap<String, MobicallAlarm>();
        configurator = new MobicallConfigurator();
        
        Properties dbProps = new Properties();
        try (FileInputStream fisPwd = new FileInputStream(DBPWD)) {
            System.setProperty("oracle.net.tns_admin", "/etc");
            dbProps.load(configurator.getClass().getResourceAsStream(DBCONN));
            
            // show configuration in LOG without password!
            LOG.info(dbProps.toString());
            dbProps.load(fisPwd);
                
            Class.forName(dbProps.getProperty("alarm.jdbc.driverClass"));   
            conn = DriverManager.getConnection(
                    dbProps.getProperty("alarm.jdbc.url"),
                    dbProps.getProperty("alarm.jdbc.user"),
                    dbProps.getProperty("alarm.jdbc.password"));
        } catch (Exception e) {
            throw e;
        }
        configuratorThread = new Thread(configurator);
        configuratorThread.start();
    }
    
    /**
     * Stop the configuration background thread, close database connection.
     */
    public static void stop() {
        configurator.stop();
        try {
            if (conn != null) {
                conn.close();
            }
            configuratorThread.join(LATENCY * 1000);
        } catch (Exception ie) {
            // exit anyway!
        }
    }
    
    /**
     * Let clients check if a given alarm is in the map of alarms with Mobicall reference
     * @param alarmId <code>String</code> the triplet identifying the alarm
     * @return <code>MobicallAlarm</code> object matching the alarmId, or null if not found
     */
    public static synchronized MobicallAlarm find(String alarmId) {
        synchronized(alarms) {
            return alarms.get(alarmId);
        }
    }
    
    /**
     * Try to connect to database and retrieve the latest set of Mobicall alarm definitions.
     */
    public static void loadConfig() {
        
        final String query = "select " +
                " alarm_id, " +
                " system_name, " +
                " identifier, " +
                " fault_code, " +
                " notification_id, " +
                " problem_Description " +
                "from " +
                " alarm_definitions_v " +
                "where  " +
                " notification_id is not null " +
                "order by 1";
        
        try (
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)
                ) {
            
            ConcurrentHashMap<String, MobicallAlarm> tmpAlarms = new ConcurrentHashMap<String, MobicallAlarm>();
            while (rs.next()) {
                MobicallAlarm ma = new MobicallAlarm();
                ma.alarmId = rs.getString("alarm_id");
                ma.systemName = rs.getString("system_name");
                ma.identifier = rs.getString("identifier");
                ma.faultCode = rs.getInt("fault_code");
                ma.notificationId = rs.getString("notification_id");
                ma.problemDescription = rs.getString("problem_description");
                tmpAlarms.put(ma.alarmId, ma);
                MobicallAlarm.LOG.debug("Added {} to configuration", ma.alarmId);
            }            
            synchronized(alarms) {
                alarms = tmpAlarms;
                LOG.info("Size of Mobicall alarm map: " + alarms.size());
            }
        } catch (Exception e) {
            LOG.warn("Failed to reload the mobicall alarm definitions", e);
        }                    
    }
    
    // 
    // --- GETTERS ---------------------------------------------------------------------
    //
    public String getSystemName() {
        return this.systemName;
    }

    public Object getIdentifier() {
        return this.identifier;
    }

    public int getFaultCode() {
        return this.faultCode;
    }

    public String getMobicallId() {
        return this.notificationId;
    }

    public String getProblemDescription() {
        return this.problemDescription;
    }

}

class MobicallConfigurator implements Runnable {

    private boolean cont;
    
    @Override
    public void run() {
        cont = true;
        long lastLoadTs = 0;
        int failures = 0;
        int success = 0;
        while (cont) {
            if (System.currentTimeMillis() - (30 * 1000) < lastLoadTs) {
                MobicallAlarm.LOG.warn("Skipped a request to reload configuration, was too fast!");
                failures++;
                if (failures > 10) {
                    MobicallAlarm.LOG.warn("Too many failures, stopping the reconfiguration thread!");                    
                }
            } else {
                MobicallAlarm.LOG.warn("Time to reload config ...");
                MobicallAlarm.loadConfig();
                lastLoadTs = System.currentTimeMillis();
                success++;
                if (success > 10) {
                    failures = 0;
                }
                MobicallAlarm.LOG.warn("Config reloaded OK.");
            }
            if (cont) {
                try {
                    int slept = 0;
                    while (cont && slept < MobicallAlarm.REFRESH_INTERVAL * 60) {
                        Thread.sleep(MobicallAlarm.LATENCY * 1000);
                        slept += MobicallAlarm.LATENCY;
                    }
                } catch (InterruptedException e) {
                    MobicallAlarm.LOG.warn("Sleep in MobicallConfigurator thread interrupted");
                }
            }
        }
    }
    
    public void stop() {
        cont = false;
    }
    
}
