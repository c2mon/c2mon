/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package cern.c2mon.publisher.mobicall;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load/reload mobicall linked alarm definitions from the database directly (for production use). The configuration
 * of this class requires a file database.properties at the root of the classpath. This file should contain
 * all properties for the database connection, except the password. To avoid presence in SVN, a hidden file
 * with the password property must be created in the working directory of the app.
 * 
 * @author mbuttner
 */
public class MobicallConfigLoaderDB implements MobicallConfigLoaderIntf {

    private static final String ORACLE_NET_TNS_ADMIN = "oracle.net.tns_admin";

    private static final String ALARM_JDBC_PASSWORD = "alarm.jdbc.password";

    private static final String ALARM_JDBC_USER = "alarm.jdbc.user";

    private static final String ALARM_JDBC_URL = "alarm.jdbc.url";

    private static final String ALARM_JDBC_DRIVER_CLASS = "alarm.jdbc.driverClass";

    private static MobicallConfigLoaderDB loader;
    
    static final Logger LOG = LoggerFactory.getLogger(MobicallAlarm.class);
    
    private static final String SYS_PROP_DATABASE_PROPERTIES = "database.properties";
    
    private ConcurrentHashMap<String, MobicallAlarm> alarms;
    private Connection conn;
    private boolean pbNotified;
    
    //
    // --- CONSTRUCTION ------------------------------------------------------------------------
    //
    private MobicallConfigLoaderDB() throws Exception {
        alarms = new ConcurrentHashMap<String, MobicallAlarm>();
        
        Properties dbProps = new Properties();
        
        if (System.getProperty(SYS_PROP_DATABASE_PROPERTIES) != null) {
            File file = new File(System.getProperty(SYS_PROP_DATABASE_PROPERTIES));
            
            try (InputStream is = new FileInputStream(file)) {
                dbProps.load(is);
            }
        } else {
            throw new IOException("Please set the location of the database properties file using -D" + SYS_PROP_DATABASE_PROPERTIES);
        }
        
        if (((String)dbProps.get(ALARM_JDBC_DRIVER_CLASS)).toLowerCase().contains("oracle") && System.getProperty(ORACLE_NET_TNS_ADMIN) == null) {
            LOG.info("Careful => No TNS_HOME set but you are using Oracle. You might like to set -D" + ORACLE_NET_TNS_ADMIN);
        }
        
        LOG.info("Connecting to database using user {} on {} ", 
                dbProps.getProperty(ALARM_JDBC_USER), 
                dbProps.getProperty(ALARM_JDBC_URL));
        
        Class.forName(dbProps.getProperty(ALARM_JDBC_DRIVER_CLASS));   
        conn = DriverManager.getConnection(
                dbProps.getProperty(ALARM_JDBC_URL),
                dbProps.getProperty(ALARM_JDBC_USER),
                dbProps.getProperty(ALARM_JDBC_PASSWORD));
    }
    
    public static MobicallConfigLoaderIntf getLoader() throws Exception {
        if (loader == null) {
            loader = new MobicallConfigLoaderDB();
        }
        return loader;
    }

    //
    // --- Implements MobicallConfigLoaderIntf -----------------------------------------------------------
    // 
    /**
     * Let clients check if a given alarm is in the map of alarms with Mobicall reference
     * @param alarmId <code>String</code> the triplet identifying the alarm
     * @return <code>MobicallAlarm</code> object matching the alarmId, or null if not found
     */
    @Override
    public synchronized MobicallAlarm find(String alarmId) {
        synchronized(alarms) {
            return alarms.get(alarmId);
        }
    }
    
    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.warn("Failed to close DB connection", e);
            }
        }
    }
    
    /**
     * Read Mobicall alarm definitions from the database. The result is loaded into a temporary
     * Map. The result is moved (in sync'd way) to the persistent map only if at least an alarm 
     * was found and no exception happened during the whole DB access. Otherwise, the result is
     * ignored and we go on with the old config. 
     */
    @Override
    public void loadConfig() {
        
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
            int count = 0;
            while (rs.next()) {
                MobicallAlarm ma = new MobicallAlarm(rs.getString("alarm_id"));
                ma.setSystemName(rs.getString("system_name"));
                ma.setIdentifier(rs.getString("identifier"));
                ma.setFaultCode(rs.getInt("fault_code"));
                ma.setNotificationId(rs.getString("notification_id"));
                ma.setProblemDescription(rs.getString("problem_description"));
                tmpAlarms.put(ma.getAlarmId(), ma);
                LOG.debug("Added {} to configuration", ma.getAlarmId());
                count++;
            }            
            if (count > 0) {
                synchronized(alarms) {
                    alarms = tmpAlarms;
                    LOG.info("New Mobicall alarm map size : {} ", alarms.size());
                }
            } else {
                LOG.warn("No alarm definitions found, keep previous configuration.");                
                if (!pbNotified) {
                    pbNotified = true;
                    DiamonSupport.getSupport().notifyTeam("Mobicall: Empty DB configuration", 
                            "The database returned an empty set of Mobicall alarms, config ignored. \n" + 
                            "You will not receive any further mail about such a problem until you \n" +
                            " restart the process.");
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to reload the mobicall alarm definitions", e);
            if (!pbNotified) {
                pbNotified = true;
                DiamonSupport.getSupport().notifyTeam("Mobicall: DB access exception", 
                    "The database access completed with exception [" + e.getMessage() + "]. \n" + 
                    "You will not receive any further mail about such a problem until you \n" +
                    " restart the process.");
            }
        }                    
    }

    
}
