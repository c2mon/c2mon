/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.spectrum.SpectrumAlarm;
import cern.c2mon.daq.spectrum.SpectrumEventProcessor;

/**
 * Persist active alarms to a disk file in /tmp, and provide the mean to restore the status of the
 * alarms on restart. This allows to overcome, with a good probability, a stop/start of the process
 * without loss of data. For long interruptions, it is better to ask IT for a reset (This should
 * be triggered automatically by them if the see that they lost connections).
 * 
 * @author mbuttner
 */
public class DiskBuffer {

    private static final Logger LOG = LoggerFactory.getLogger(DiskBuffer.class);
    public static final String BUFFER_NAME = "/tmp/dmn-daq-spectrum.buffer";

    /**
     * Save data to the disk file
     */
    public static void write(ConcurrentHashMap<String, SpectrumAlarm> monitoredHosts) {
        ArrayList<String> result = new ArrayList<String>();
        for (String hostname : monitoredHosts.keySet()) {
            SpectrumAlarm alarm = monitoredHosts.get(hostname);
            if (alarm.isAlarmOn()) {
                result.add(hostname);
            }
        }
        Collections.sort(result);

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(BUFFER_NAME);
            for (String hostname : result) {
                SpectrumAlarm alarm = monitoredHosts.get(hostname);
                pw.print(alarm.getSource());
                pw.print("," + hostname);
                pw.print("," + alarm.getUserTimestamp());
                for (Long l : alarm.getAlarmIds()) {
                    pw.print("," + l);
                }
                pw.println();
            }
        } catch (IOException ie) {
            LOG.error("Failed to dump alarm buffer!", ie);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
    
    /**
     * Update the status of the configured alarms based on the content of the disk buffer.
     */
    public static void loadBuffer(SpectrumEventProcessor proc) {
        LOG.info("Start to load active alarms from disk buffer ...");
        BufferedReader inp = null;
        try {
            File f = new File(BUFFER_NAME);
            if (f.exists()) {
                inp = new BufferedReader(new FileReader(f));
                String ligne = null;
                while ((ligne = inp.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(ligne, ",");
                    String source = st.nextToken();
                    String hostname = st.nextToken();
                    LOG.info("Work on [" + hostname + "] activated by " + source + " ...");
                    SpectrumAlarm alarm = proc.getAlarm(hostname);
                    if (alarm != null) {
                        alarm.setSource(source);
                        LOG.info(" ... source set to " + source);
                        long ts = Long.parseLong(st.nextToken());
                        if (ts == 0)
                        {
                            ts = System.currentTimeMillis();
                        }
                        alarm.setUserTimestamp(ts);
                        LOG.info("Alarm timestamp restored: " + (new Date(ts)).toString());
                        while (st.hasMoreTokens()) {
                            long alarmId = Long.parseLong(st.nextToken());
                            LOG.debug("<<  " + alarm.getTag().getName() + " < " + alarmId);
                            alarm.activate(alarmId);
                            LOG.info("" + alarm.getTag().getName() + " -> " + alarm.getAlarmCount());
                        }
                    }
                    else
                    {
                        LOG.warn("Buffered alarm not found in cache!");
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to load " + BUFFER_NAME + " (buffer from prior run)", e);
        } finally {
            try {
                if (inp != null) {
                    inp.close();
                }
            } catch (IOException ie) {
                LOG.warn("Problem when closing disk buffer", ie);
            }
        }
        LOG.info("Load completed..");
    }

}
