/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 * TODO write a script to extract the text file with alarm definitions and "," in problem description with ##comma##
 * TODO test and write tests 
 * TODO comment and document
 * 
 * @author mbuttner
 */
public class MobicallAlarm {

    static final Logger LOG = LoggerFactory.getLogger(MobicallAlarm.class);
    
    public static final String CONFIG_FILE = "conf/mobicall_alarms.txt";
    
    private String systemName;              
    private String identifier;              
    private int faultCode;                  
    private String mobicallId;              
    private String problemDescription;      
    
    private static ConcurrentHashMap<String, MobicallAlarm> alarms;
    private static Thread configuratorThread;
    private static MobicallConfigurator configurator;
    
    public static void init() {
        loadConfig();
        configurator = new MobicallConfigurator();
        configuratorThread = new Thread(configurator);
        configuratorThread.start();
    }
    
    public static void stop() {
        configurator.stop();
        try {
            configuratorThread.join(5 * 1000);
        } catch (InterruptedException ie) {
            // exit anyway!
        }
    }
    
    
    public static synchronized MobicallAlarm find(AlarmValue av) {
        return alarms.get(MobicallAlarmsPublisher.getAlarmId(av));
    }
    
    public static synchronized void loadConfig() {
        try (BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String ligne = null;
            while ((ligne = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(ligne, ",");
                MobicallAlarm ma = new MobicallAlarm();
                ma.systemName = st.nextToken();
                ma.identifier = st.nextToken();
                ma.faultCode = Integer.parseInt(st.nextToken());
                ma.mobicallId = st.nextToken();
                ma.problemDescription = st.nextToken().replaceAll("{{comma}}", ",");
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
        return this.mobicallId;
    }

    public String getProblemDescription() {
        return this.problemDescription;
    }

}

class MobicallConfigurator implements Runnable {

    private boolean cont;
    private long ts;
    
    public MobicallConfigurator() {
        ts = getTimestamp();
    }
    
    private long getTimestamp() {
        File f = new File(MobicallAlarm.CONFIG_FILE);
        return f.lastModified();
    }
    
    @Override
    public void run() {
        cont = true;
        while (cont) {
            long newTs = getTimestamp();
            if (ts < newTs) { 
                MobicallAlarm.LOG.warn("Config outdated, reloading ...");
                MobicallAlarm.loadConfig();
                ts = newTs;
            }
            if (cont) {
                try {
                    Thread.sleep(5 * 1000);
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
