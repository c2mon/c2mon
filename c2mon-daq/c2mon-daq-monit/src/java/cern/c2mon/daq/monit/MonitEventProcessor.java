/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.monit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * 
 * @author mbuttner
 */
@ManagedResource(objectName = "cern.c2mon.daq.monit:name=MonitEventProcessor", description = "Monit data processor")
public class MonitEventProcessor implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger("MonitEventProcessor");
    public static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    public static final SimpleDateFormat DATE_FMT = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    public static final long BACKUP_DELAY = 7 * 60 * 1000; // 5mn in reality, but let them some delay ...
    
    private ConcurrentHashMap<String, MonitUpdateEvent> monitoredHosts = new ConcurrentHashMap<String, MonitUpdateEvent>();
    private boolean cont = true;
    

    private LinkedBlockingQueue<MonitUpdateEvent> eventQueue = new LinkedBlockingQueue<MonitUpdateEvent>();
    
    //
    // --- PUBLIC METHODS ---------------------------------------------------------------------------
    //
    
    public void refreshAll()
    {
        LOG.info("Start refresh tag value operation ...");
        for (MonitUpdateEvent event : monitoredHosts.values()) {
            event.sendUpdate();
        }        
        LOG.info("Refreshed all data tags.");        
    }
    
    public void shutdown() {
        LOG.warn("Going down: need to dump the buffer!");
    }
    
    public Queue<MonitUpdateEvent> getQueue() {
        return eventQueue;
    }

    public void del(String hostname) {
        monitoredHosts.remove(hostname.toUpperCase());
    }

    public void add(String hostname, String metric, ISourceDataTag tag) {
        monitoredHosts.put(hostname.toUpperCase(), new MonitUpdateEvent(hostname, metric, tag));
    }

    public MonitUpdateEvent getAlarm(String hostname) {
        return monitoredHosts.get(hostname.toUpperCase());
    }

    public void init()
    {
        //
    }
    
    /**
     * Process queue entries, send data updates, check that we received what we need, initiate polling when
     * required.
     */
    @Override
    public void run() {
        cont = true;

        try
        {
            Thread.sleep(2000);
            this.refreshAll();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        long loopcounter = 0;
        while (cont) {
            try {
                // if no event is available, we will sleep for 2 seconds ...
                Thread.sleep(1 * 1000);

                // time check the server status here (once per 2 minutes)
                // ...
                //   equipmentMessageSender.confirmEquipmentStateIncorrect();
                // } else {
                //        equipmentMessageSender.confirmEquipmentStateOK();

                loopcounter++;
                LOG.trace("Processor loop " + loopcounter);

                // ... and than process whatever is available on the event queue.
                while (!eventQueue.isEmpty()) {
                    MonitUpdateEvent event = eventQueue.poll();
                    event.sendUpdate();
                    LOG.info("Update sent for {}/{}", event.getHostname(), event.getMetricname());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //
    // --- JMX ------------------------------------------------------------------------------------------
    //
    @ManagedOperation
    public void clearAlarm() {
        //
    }
    
    @ManagedAttribute
    public List<String> getActiveTriplets() {
        ArrayList<String> result = new ArrayList<String>();
        for (String hostname : monitoredHosts.keySet()) {
            result.add(hostname + "=" + hostname);
        }
        Collections.sort(result);
        return result;
    }


}
