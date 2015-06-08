/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.shared.common.datatag.ISourceDataTag;


/**
 * The Spectrum->LASER gateway
 * 
 * Converts alarms sent by Spectrum scripts (managed by IT-CS) to us into LASER alarms. The message format is:
 * action ip date time model spectrum_alarm_id model_handle pb_description
 * with:
 * - action is CLR (clar an alarm), SET (activate an alarm), UPD (update an alarm), RST (Spectrum restart), 
 *   or KAL (keep alive, expected from time to time).
 * - ip (the address) is converted into a hostname and used as search criteria against the valid alarm 
 *   definitions registered in alarmdefs.txt
 * - date has to be in format mm/dd/yyyy
 * - time has to be in format hh:mi:ss
 * - model, spectrum_alarm_id and model_handle are used to generate the Spectrum URL for help 
 * - problem description is inserted into LASER alarms
 * 
 * To enable a Spectrum hosts, it must be registered in the "hosts" file. Only Spectrum alarms matching 
 * LASER definitions found in alarmdefs.txt are transmitted. The config files are passed as arguments to 
 * the main programme:
 *      -Dlaser.alarmdefs=/opt/laser/etc/alarmdefs.txt -Dspectrum.hosts=/opt/laser/etc/hosts
 * 
 * The hosts file must be edited manually, the alarmdefs.txt must be re-generated after each update using getdefs.pl
 * 
 * This class:
 * - loads config files 
 * - re-establishes the list of active alarms from /tmp/alarmbuffer files
 * - starts server threads, one to listen to incoming connections ("Server") and register the messages
 *      as instances of Event, one to consume these events (MainThread). This is done asynchronously to 
 *      avoid blocking incoming messages. 
 * - loops to check for config changes
 * 
 * NOTES:
 * - Spectrum might use several alarm ids for one single equipment, and therefore match a single LASER alarm.
 *   To correctly handle activate and terminate actions for LASER alarms, we keep a reference count in the
 *   class ActiveAlarm (list of Spectrum ids active for one single LASER alarm). When the reference count
 *   is 0, we can terminate the LASER aarm.
 *
 * 
 * Class overview:
 * - MainEvent          Config load, thread start and event consuming thread
 * - Server             the thread that listens to incoming messages
 * - Event              describes a message from Spectrum and makes events available as queue
 * - SpectrumServer     stores active alarms by Spectrum server and synchronizes with LASER based
 *                      on the ActiveAlarm's
 * - ActiveAlarm        Spectrum reference counter to LASER alarms
 * 
 * @author mbuttner
 * @version 0.1-000, 26 May 2010
 * 
 */
@ManagedResource(objectName = "cern.c2mon.daq.spectrum:name=SpectrumEventProcessor", 
                    description = "Spectrum alarms event processor")

public class SpectrumEventProcessor extends SpectrumConfig implements Runnable {
        
    public static final String BUFFER_NAME = "/tmp/dmn-daq-spectrum.buffer";
    
    static final Logger LOG = LoggerFactory.getLogger("SpectrumEventProcessor");
    public static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    public static final SimpleDateFormat DATE_FMT = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    public static final long BACKUP_DELAY = 7 * 60 * 1000; // 5mn in reality, but let them some delay ...
    
    private IEquipmentMessageSender equipmentMessageSender;
    private ConcurrentHashMap<String, SpectrumAlarm> monitoredHosts = new ConcurrentHashMap<String, SpectrumAlarm>();
    private boolean cont = true;
    
    private long lastEventPrimary = 0;
    private long lastEventSecondary = 0;
    private long lastKalPrimary = System.currentTimeMillis();
    private long lastKalSecondary = System.currentTimeMillis();
    private boolean primaryOk = true;
    private boolean secondaryOk = true;

    private LinkedBlockingQueue<SpectrumEvent> eventQueue = new LinkedBlockingQueue<SpectrumEvent>();

    
    //
    // ---  PUBLIC METHODS ---------------------------------------------------------------------------
    //
    public void setSender(IEquipmentMessageSender equipmentMessageSender) {        
        this.equipmentMessageSender = equipmentMessageSender;
    }
        
    public void shutdown()
    {
        LOG.warn("Going down: need to dump the buffer!");        
        ArrayList<String> result = new ArrayList<String>();
        for (String hostname : monitoredHosts.keySet()) {
            SpectrumAlarm alarm = monitoredHosts.get(hostname);
            if (alarm.isAlarmOn()) {                
                result.add(hostname);
            }
        }
        Collections.sort(result);

        PrintWriter pw = null;
        try
        {
            pw = new PrintWriter(SpectrumEventProcessor.BUFFER_NAME);
            for (String hostname : result)
            {
                pw.print(hostname);
                SpectrumAlarm alarm = monitoredHosts.get(hostname);
                for (Long l : alarm.getAlarmIds())
                {
                    pw.print("," + l);
                }
                pw.println();
            }
        }
        catch (IOException ie)
        {
            SpectrumEventProcessor.LOG.error("Failed to dump alarm buffer!", ie);
        }
        finally
        {
            if (pw != null)
            {
                pw.close();
            }
        }
     }
    
    public Queue<SpectrumEvent> getQueue() {
        return eventQueue;
    }
    
    public void del(String hostname) {
        monitoredHosts.remove(hostname);
    }

    public void add(String hostname, ISourceDataTag tag) {
        monitoredHosts.put(hostname.toUpperCase(), new SpectrumAlarm(tag));
        equipmentMessageSender.sendTagFiltered(tag, Boolean.FALSE, System.currentTimeMillis());
    }    

    public boolean isInteresting(SpectrumEvent event) {
        if (event.getType() == SpectrumEventType.RST)
        {
            return true;
        }
        if (event.getType() == SpectrumEventType.KAL)
        {
            return false;
        }
        event.prepare();
        LOG.info("Checking if event for host {} is of interest", event.getHostname());
        if (monitoredHosts.get(event.getHostname()) != null)
        {
            return true;
        }        
        return false;
    }

    public SpectrumAlarm getAlarm(String hostname) {
        return monitoredHosts.get(hostname.toUpperCase());
    }
    
    @ManagedAttribute
    public List<String> getActiveTriplets() {
        ArrayList<String> result = new ArrayList<String>();
        for (String hostname : monitoredHosts.keySet()) {
            SpectrumAlarm alarm = monitoredHosts.get(hostname);
            if (alarm.isAlarmOn()) {                
                result.add(hostname + "=" + alarm.getTag().getId());
            }
        }
        Collections.sort(result);
        return result;
    }

    @ManagedAttribute
    public List<String> getConnectionStatus() {
        ArrayList<String> result = new ArrayList<String>();
        String s1 = getPrimaryServer() + " " + 
                Boolean.valueOf(primaryOk).toString() + " " + 
                DATE_FMT.format(new Date(this.lastEventPrimary)) + " " + 
                DATE_FMT.format(new Date(this.lastKalPrimary));
        result.add(s1);
        String s2 = getSecondaryServer() + " " + 
                Boolean.valueOf(secondaryOk).toString() + " " + 
                DATE_FMT.format(new Date(this.lastEventSecondary)) + " " + 
                DATE_FMT.format(new Date(this.lastKalSecondary));
        result.add(s2);
        return result;
    }
    
    /**
     * The event consumer thread. Spectrum events are taken from the event queue and converted as possible
     * into LASER alarms, action handled by the SpectrumServer class. The thread is a kind of buffer between
     * the events queue and the SpectrumServer for LASER communication.
     */
    @Override
    public void run() {
        cont = true;
        lastKalPrimary = System.currentTimeMillis();
        lastKalSecondary = System.currentTimeMillis();

        // attempt to read buffer and activate stuff from prior run
        loadBuffer();
        
        
        long loopcounter = 0;
        while (cont) {
            try {                
                // if no event is available, we will sleep for 2 seconds ...
                Thread.sleep(1 * 1000);
                
                // time check the server status here (once per 2 minutes)
                if (loopcounter % 60 == 0) {
                    long now = System.currentTimeMillis();
                    if ((!this.primaryOk && !secondaryOk) ||
                        ((now - this.lastKalPrimary > BACKUP_DELAY) && (now - this.lastKalSecondary > BACKUP_DELAY))
                            ){
                        LOG.warn("Lost connecion with both Spectrum servers");
                        equipmentMessageSender.confirmEquipmentStateIncorrect();
                    } else {
                        LOG.info("Spectrum connection check OK");
                        equipmentMessageSender.confirmEquipmentStateOK();                    
                    }
                }

                loopcounter++;
                LOG.trace("Processor loop " + loopcounter);
                
                // ... and than process whatever is available on the event queue.
                while (!eventQueue.isEmpty()) {
                    SpectrumEvent event = eventQueue.poll();

                    LOG.debug("Processing event {}", event.toString());
                    
                    // 
                    // For a "reset" event (manually sent if detected here), we have to clear all active alarms
                    if (event.toReset()) {
                        LOG.warn("Spectrum sent a RST event, terminate all active alarms..." );
                        terminateAll();
                        LOG.warn("All alarms terminated (RESET event)." );
                    } else {                        
                        // set attributes of the event based on the raw data stored on construction
                        event.prepare();
                        LOG.debug("RECEIVED:"  + event.getType() + " ["+ event.getServerName() + "] ");
                        
                        if (event.isKeepAlive()) {
                            setLastKeepAliveTs(event.getServerName(), event.isSpectrumNotifierOk());
                            LOG.info("RECEIVED: keep-alive [" + event.getServerName() + "] ");
                        } else {
                        
                            String hostname = event.getHostname();
                            SpectrumAlarm alarm = null;
                            synchronized(monitoredHosts) {
                                alarm = monitoredHosts.get(hostname);
                            }
                        
                            // if we have a valid LASER alarm for the Spectrum event ...
                            if (alarm != null) {
                            
                                // the timestamp based reset procedure: an event older than the previously received one means
                                // that Spectrum was restarted and that we have to clean (because we will receive all active
                                // alarms now
                                long uts = event.getUserTimestamp();
                                setLastEventTs(event.getServerName(), uts);  
                            
                                String descr = event.getProblemDescription() + " (" + event.getContextURL() + ")";
                                long ts = System.currentTimeMillis();
                                if (event.toActivate()) {
                                    LOG.info("ACTIVATE: [" + event.getServerName() + "] " + event);
                                    alarm.activate(event.getAlarmId());
                                    equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.TRUE, ts, descr);
                                }
                                if (event.toTerminate()) {
                                    LOG.info("TERMINATE: [" + event.getServerName() + "] " + event);
                                    alarm.terminate(event.getAlarmId());
                                    if (!alarm.isAlarmOn())
                                    {
                                        equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.FALSE, ts, descr);
                                    }
                                }
                            } else {
                                LOG.warn("No alarm definition found for host " + hostname + ", event discarded");
                            }
                        }
                        LOG.debug("Consumed event [" + event + "]");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
   

    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------------
    //
    private void loadBuffer()
    {
        BufferedReader inp = null;
        try
        {
            File f = new File(BUFFER_NAME);
            if (f.exists())
            {
                inp = new BufferedReader(new FileReader(f));        
                String ligne = null;
                long ts = System.currentTimeMillis();
                while ((ligne = inp.readLine()) != null)
                {
                    StringTokenizer st = new StringTokenizer(ligne, ",");
                    String hostname = st.nextToken();
                    SpectrumAlarm alarm = monitoredHosts.get(hostname);
                    if (alarm != null)
                    {
                        while (st.hasMoreTokens())
                        {
                            long alarmId = Long.parseLong(st.nextToken());
                            alarm.activate(alarmId);
                        }
                        equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.TRUE, ts, "from buffer reload");
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.warn("Failed to load " + BUFFER_NAME + " (buffer from prior run)", e);
        }
        finally
        {
            try
            {
                if (inp != null)
                {
                    inp.close();                    
                }
            }
            catch (IOException ie)
            {
                LOG.warn("Problem when closing disk buffer", ie);
            }
        }

    }
    
    /**
     * 
     * @param serverName
     * @param spectrumNotifierOk
     */
    private void setLastKeepAliveTs(String serverName, boolean spectrumNotifierOk) {
        boolean valid = false;
        LOG.info("KAL for {} -> {}", serverName, Boolean.valueOf(spectrumNotifierOk).toString());
        if (serverName.equals(getPrimaryServer())) {
            this.lastKalPrimary = System.currentTimeMillis();
            primaryOk = spectrumNotifierOk;
            valid = true;
        }
        if (serverName.equals(getSecondaryServer())) {
            this.lastKalSecondary = System.currentTimeMillis();
            secondaryOk = spectrumNotifierOk;            
            valid = true;
        }        
        if (!valid) {
            LOG.error("Message not valid for actual configuration: (1) {} (2) {}", 
                    getPrimaryServer(), getSecondaryServer());            
        }
    }

    private void terminateAll()
    {
        LOG.warn("Terminate all procedure triggered ...");
        for (SpectrumAlarm alarm : monitoredHosts.values())
        {
            if (alarm.isAlarmOn())
            {
                equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.FALSE, System.currentTimeMillis());
                alarm.clear();
            }
        }
        LOG.info("Terminate all procedure completed.");
    }

    private void setLastEventTs(String serverName, long uts) {
        if (serverName.equals(getPrimaryServer())) {
            if (uts < this.lastEventPrimary)
            {
                LOG.warn("{} sent messages in unconsistent chronological order ...", serverName);
                terminateAll();
            }
            this.lastEventPrimary = uts;
        }
        if (serverName.equals(getSecondaryServer())) {
            if (uts < this.lastEventSecondary)
            {
                LOG.warn("{} sent messages in unconsistent chronological order ...", serverName);
                terminateAll();
            }
            this.lastEventSecondary = uts;
        }                
    }

}
