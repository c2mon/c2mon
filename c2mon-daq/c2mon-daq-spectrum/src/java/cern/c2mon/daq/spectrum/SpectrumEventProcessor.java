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

package cern.c2mon.daq.spectrum;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.spectrum.util.DiskBuffer;
import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * The Spectrum->LASER gateway Converts alarms sent by Spectrum scripts (managed by IT-CS) to us into LASER alarms. The
 * message format is: action ip date time model spectrum_alarm_id model_handle pb_description with: - action is CLR
 * (clar an alarm), SET (activate an alarm), UPD (update an alarm), RST (Spectrum restart), or KAL (keep alive, expected
 * from time to time). - ip (the address) is converted into a hostname and used as search criteria against the valid
 * alarm definitions registered in alarmdefs.txt - date has to be in format mm/dd/yyyy - time has to be in format
 * hh:mi:ss - model, spectrum_alarm_id and model_handle are used to generate the Spectrum URL for help - problem
 * description is inserted into LASER alarms To enable a Spectrum hosts, it must be registered in the "hosts" file. Only
 * Spectrum alarms matching LASER definitions found in alarmdefs.txt are transmitted. The config files are passed as
 * arguments to the main programme: -Dlaser.alarmdefs=/opt/laser/etc/alarmdefs.txt -Dspectrum.hosts=/opt/laser/etc/hosts
 * The hosts file must be edited manually, the alarmdefs.txt must be re-generated after each update using getdefs.pl
 * This class: - loads config files - re-establishes the list of active alarms from /tmp/alarmbuffer files - starts
 * server threads, one to listen to incoming connections ("Server") and register the messages as instances of Event, one
 * to consume these events (MainThread). This is done asynchronously to avoid blocking incoming messages. - loops to
 * check for config changes NOTES: - Spectrum might use several alarm ids for one single equipment, and therefore match
 * a single LASER alarm. To correctly handle activate and terminate actions for LASER alarms, we keep a reference count
 * in the class ActiveAlarm (list of Spectrum ids active for one single LASER alarm). When the reference count is 0, we
 * can terminate the LASER aarm. Class overview: - MainEvent Config load, thread start and event consuming thread -
 * Server the thread that listens to incoming messages - Event describes a message from Spectrum and makes events
 * available as queue - SpectrumServer stores active alarms by Spectrum server and synchronizes with LASER based on the
 * ActiveAlarm's - ActiveAlarm Spectrum reference counter to LASER alarms
 * 
 * @author mbuttner
 * @version 0.1-000, 26 May 2010
 */
@ManagedResource(objectName = "cern.c2mon.daq.spectrum:name=SpectrumEventProcessor", description = "Spectrum alarms event processor")
public class SpectrumEventProcessor extends SpectrumConfig implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger("SpectrumEventProcessor");
    public static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    public static final SimpleDateFormat DATE_FMT = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    public static final long BACKUP_DELAY = 7 * 60 * 1000; // 5mn in reality, but let them some delay ...

    private int tolerance;      // only if incoming message is older by more than a minute
                                // compared to the previous, we "terminate all".
    
    private IEquipmentMessageSender equipmentMessageSender;
    private ConcurrentHashMap<String, SpectrumAlarm> monitoredHosts = new ConcurrentHashMap<String, SpectrumAlarm>();
    private boolean cont = true;

    private long lastEventPrimary = 0;
    private long lastEventSecondary = 0;
    private long lastKalPrimary = System.currentTimeMillis();
    private long lastKalSecondary = System.currentTimeMillis();
    private boolean primaryOk = true;
    private boolean secondaryOk = true;
    
    private boolean connectionOk = true;

    private LinkedBlockingQueue<SpectrumEvent> eventQueue = new LinkedBlockingQueue<SpectrumEvent>();

    private int backupControlFreq = 60;
    private long bkpDelay = BACKUP_DELAY;
    
    //
    // --- PUBLIC METHODS ---------------------------------------------------------------------------
    //
    /**
     * All values to be provided in seconds
     * @param freq  <code>int</code>    default is 60 (seconds)
     * @param delay <code>int</code>    default is 300 (seconds), or in other words 5mn
     */
    public void setBackupControl(int freq, int delay)
    {
        this.backupControlFreq = freq;
        this.bkpDelay = delay * 1000;        
    }
    
    public void refreshAll()
    {
        LOG.info("Start refresh tag value operation ...");
        for (SpectrumAlarm alarm : monitoredHosts.values()) {
            if (alarm.isAlarmOn())
            {
                LOG.info("Alarm on: " + alarm.getHostname());
            }
            equipmentMessageSender.sendTagFiltered(alarm.getTag(), alarm.isAlarmOn(), alarm.getUserTimestamp(),"from buffer");
        }        
        LOG.info("Refreshed all data tags.");        
    }
    
    /**
     * Set value for backrolling timestamps sent by Spectrum. This is only needed for the JMS listener
     * as usually the delivery is really in sequence. Note that the parameter value is expressed in
     * seconds, but the internal storage is in ms!
     * 
     * @param tolerance <code>int</code> the number of seconds a ts can be before the one of the previous message
     */ 
    public void setTolerance(int tolerance)
    {
        this.tolerance = tolerance * 1000;
        LOG.info("Tolerance (backward timestamped messages): " + tolerance + "s");
    }
    
    public int getTolerance()
    {
        return this.tolerance;
    }
    
    public void setSender(IEquipmentMessageSender equipmentMessageSender) {
        this.equipmentMessageSender = equipmentMessageSender;
    }

    public void shutdown() {
        LOG.warn("Going down: need to dump the buffer!");
        DiskBuffer.write(monitoredHosts);
    }

    public boolean isConnectionOk()
    {
        return this.connectionOk;
    }
    
    public Queue<SpectrumEvent> getQueue() {
        return eventQueue;
    }

    public void del(String hostname) {
        monitoredHosts.remove(hostname.toUpperCase());
    }

    public void add(String hostname, ISourceDataTag tag) {
        monitoredHosts.put(hostname.toUpperCase(), new SpectrumAlarm(hostname, tag));
    }

    public SpectrumAlarm getAlarm(String hostname) {
        return monitoredHosts.get(hostname.toUpperCase());
    }

    public void init()
    {
        // attempt to read buffer and activate stuff from prior run
        DiskBuffer.loadBuffer(this);        
    }
    
    /**
     * The event consumer thread. Spectrum events are taken from the event queue and converted as possible into LASER
     * alarms, action handled by the SpectrumServer class. The thread is a kind of buffer between the events queue and
     * the SpectrumServer for LASER communication.
     */
    @Override
    public void run() {
        cont = true;
        lastKalPrimary = System.currentTimeMillis();
        lastKalSecondary = System.currentTimeMillis();

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
                if (loopcounter % backupControlFreq == 0) {
                    long now = System.currentTimeMillis();
                    if ((!this.primaryOk && !secondaryOk)
                            || ((now - this.lastKalPrimary > bkpDelay) && (now - this.lastKalSecondary > bkpDelay))) {
                        LOG.warn("Lost connecion with both Spectrum servers");
                        equipmentMessageSender.confirmEquipmentStateIncorrect();
                        connectionOk = false;
                    } else {
                        LOG.info("Spectrum connection check OK");
                        equipmentMessageSender.confirmEquipmentStateOK();
                        connectionOk = true;
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
                        LOG.warn("Spectrum sent a RST event, terminate all active alarms...");
                        terminateAll(event.getServerName());
                        LOG.warn("All alarms terminated (RESET event).");
                    } else {
                        // set attributes of the event based on the raw data stored on construction
                        event.prepare();
                        LOG.debug("RECEIVED:" + event.getType() + " [" + event.getServerName() + "] ");

                        if (event.isKeepAlive()) {
                            setLastKeepAliveTs(event.getServerName(), event.isSpectrumNotifierOk());
                            LOG.info("RECEIVED: keep-alive [" + event.getServerName() + "] ");
                        } else {

                            String hostname = event.getHostname();
                            SpectrumAlarm alarm = null;
                            if (hostname != null) {
                                synchronized (monitoredHosts) {
                                    LOG.info("Retrieving alarm info for hostname [" + hostname + "]");
                                    alarm = monitoredHosts.get(hostname);
                                }
                            }
                            // if we have a valid LASER alarm for the Spectrum event ...
                            if (alarm != null) {

                                // the timestamp based reset procedure: an event older than the previously received one
                                // means
                                // that Spectrum was restarted and that we have to clean (because we will receive all
                                // active
                                // alarms now
                                long uts = event.getUserTimestamp();
                                setLastEventTs(event.getServerName(), uts);

                                String descr = event.getProblemDescription() + " (" + event.getContextURL() + ")";
                                long ts = System.currentTimeMillis();
                                if (event.toActivate()) {
                                    LOG.info("ACTIVATE: [" + event.getServerName() + "] " + event);
                                    alarm.activate(event.getAlarmId());
                                    alarm.setSource(event.getServerName());
                                    equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.TRUE, ts, descr);
                                    LOG.info("-> tag updated");
                                }
                                if (event.toTerminate()) {
                                    LOG.info("TERMINATE: [" + event.getServerName() + "] " + event);
                                    alarm.terminate(event.getAlarmId());
                                    equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.FALSE, ts, descr);
                                    LOG.info("-> tag updated");
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
    // --- JMX ------------------------------------------------------------------------------------------
    //
    @ManagedOperation
    public void clearAlarm(String hostname) {
        SpectrumAlarm alarm = monitoredHosts.get(hostname);
        if (alarm != null && alarm.isAlarmOn()) {
            equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.FALSE, System.currentTimeMillis());
            alarm.clear();
        }
    }

    @ManagedOperation
    public void setEquipDown() {
        equipmentMessageSender.confirmEquipmentStateIncorrect("Equipment manually disconnected by JMX backdoor");
    }
    
    @ManagedOperation
    public void setEquipUp() {
        equipmentMessageSender.confirmEquipmentStateOK();        
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
        String s1 = getPrimaryServer() + " " + Boolean.valueOf(primaryOk).toString() + " "
                + DATE_FMT.format(new Date(this.lastEventPrimary)) + " "
                + DATE_FMT.format(new Date(this.lastKalPrimary));
        result.add(s1);
        String s2 = getSecondaryServer() + " " + Boolean.valueOf(secondaryOk).toString() + " "
                + DATE_FMT.format(new Date(this.lastEventSecondary)) + " "
                + DATE_FMT.format(new Date(this.lastKalSecondary));
        result.add(s2);
        return result;
    }


    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------------
    //

    /**
     * Keep trace of the last keep alive message from each of the Spectrum servers.
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
            LOG.error("Message not valid for actual configuration: (1) {} (2) {}", getPrimaryServer(),
                    getSecondaryServer());
        }
    }

    /**
     * In case of reset message from the client scripts, all alarms are terminated and their internal
     * list of fautl codes is cleared. 
     * @param spectrumServer 
     */
    private void terminateAll(String spectrumServer) {
        Logger log = LoggerFactory.getLogger("special");
        log.warn("Terminate all procedure triggered ...");
        for (SpectrumAlarm alarm : monitoredHosts.values()) {
            if (alarm.isAlarmOn() && alarm.getSource().equals(spectrumServer)) {
                equipmentMessageSender.sendTagFiltered(alarm.getTag(), Boolean.FALSE, System.currentTimeMillis());
                alarm.clear();
            }
        }
        log.info("Terminate all procedure completed.");
    }

    /**
     * 
     * @param serverName <code>String</code> name of the server sending the message
     * @param uts <code>long</code> timestamp
     */
    private void setLastEventTs(String serverName, long uts) {
        if (serverName.equals(getPrimaryServer())) {
            if (uts < (this.lastEventPrimary - tolerance)) {
                LOG.warn("{} sent messages in unconsistent chronological order ...", serverName);
                LOG.info("Previous message was: " + (new Date(this.lastEventPrimary)).toString());
                LOG.info("Incoming:             " + (new Date(uts)).toString());
                terminateAll(serverName);
            }
            this.lastEventPrimary = uts;
        }
        if (serverName.equals(getSecondaryServer())) {
            if (uts < (this.lastEventSecondary - tolerance)) {
                LOG.warn("{} sent messages in unconsistent chronological order ...", serverName);
                LOG.info("Previous message was: " + (new Date(this.lastEventSecondary)).toString());
                LOG.info("Incoming:             " + (new Date(uts)).toString());
                terminateAll(serverName);
            }
            this.lastEventSecondary = uts;
        }
    }

}
