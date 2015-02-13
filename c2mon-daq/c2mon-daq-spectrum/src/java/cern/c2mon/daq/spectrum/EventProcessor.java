/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.IEquipmentMessageSender;
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
public class EventProcessor implements Runnable {
        
    private static final Logger LOG = LoggerFactory.getLogger(EventProcessor.class);

    private IEquipmentMessageSender equipmentMessageSender;
    private SpectrumEquipConfig config;
    private HashMap<String, SpectrumAlarm> monitoredHosts = new HashMap<String, SpectrumAlarm>();
    private boolean cont = true;
    
    private long lastKalPrimary = 0;
    private long lastKalSecondary = 0;
    
    //
    // --- CONSTRUCTION -------------------------------------------------------------------------------
    //
    public EventProcessor(IEquipmentMessageSender equipmentMessageSender, SpectrumEquipConfig config) {        
        this.equipmentMessageSender = equipmentMessageSender;
        this.config = config;
        LOG.info("Ready to go.");        
    }
    
    public void shutdown()
    {
        cont = false;
    }
    
    public void del(String hostname) {
        monitoredHosts.remove(hostname);
    }

    public void add(String hostname, ISourceDataTag tag) {
        monitoredHosts.put(hostname, new SpectrumAlarm(hostname, tag));
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
        
        while (cont) {
            try {                
                // if no event is available, we will sleep for 5 seconds ...
                Thread.sleep(5 * 1000);
                
                // ... and than process whatever is available on the event queue.
                // TODO from time to time check the server status here!!
                // -> for normal delay ... check legacy code
                
                while (EventQueue.getQueue().available()) {
                    Event event = EventQueue.getQueue().consumeEvent();

                    // 
                    // For a "reset" event (manually sent if detected here), we have to clear all active alarms
                    if (event.toReset()) {
                        LOG.warn("Spectrum sent a RST event, terminate all active alarms..." );
                        SpectrumServer.terminateAll(event.getServerName( )/*, manager*/);
                        LOG.warn("All alarms terminated (RESET event)." );
                    } else {
                        
                        // set attributes of the event based on the raw data stored on construction
                        event.prepare();
                        eventLog.info("RECEIVED: [" + event.getServerName() + "] " + event);
                        
                        if (event.isKeepAlive()) {
                            //
                            SpectrumServer.setLastKeepAliveTs(event.getServerName(), event.isSpectrumNotifierOk());
                            eventLog.info("RECEIVED: [" + event.getServerName() + "] " + event);
                        } else {
                        
                            String hostname = event.getHostname();
                            String alarmId = null;
                            synchronized(laserAlarms) {
                                alarmId = laserAlarms.get(hostname);
                            }
                        
                            // if we have a valid LASER alarm for the Spectrum event ...
                            if (alarmId != null) {
                            
                                // the timestamp based reset procedure: an event older than the previously received one means
                                // that Spectrum was restarted and that we have to clean (because we will receive all active
                                // alarms now
                                long uts = event.getUserTimestamp();
/*
 *  For debugging purposes, check timestamps of data sent by Spectrum
 *                              long lastEventTs = SpectrumServer.getLastEventTs(event.getServerName());
                                if (uts != 0 && (uts + (3 * 60 * 1000) < lastEventTs)) {
                                    log.warn("Spectrum sent a timestamp older than for a previous event, terminate all ..." );
                                    log.warn("Spectrum server:          " + event.getServerName());
                                    log.warn("User timestamp:           " + new Date(uts));
                                    log.warn("Previous event timestamp: " + new Date(lastEventTs));
                                    SpectrumServer.terminateAll(event.getServerName(), manager);
                                    log.warn("All alarms terminated." );
                                    eventLog.info("RESET: by Spectrum timestamp");
                                }
*/                              
                                SpectrumServer.setLastEventTs(event.getServerName(), uts);  // remember the timestamp of the last event from Spectrum
                            
                                //
                                //  Build the alarm and based on the event type try to activate or terminate
                                //
                                AlarmInstance alarm = 
                                    builder.clear()
                                        .setProblemKey(alarmId)
                                        .setUserTimestamp(System.currentTimeMillis())
                                        .setProperty("PB_DESC", event.getProblemDescription())
                                        .setProperty("SPECTRUM_URL", event.getContextURL())
                                        .getInstance();        
                                if (event.toActivate()) {
                                    eventLog.info("ACTIVATE: [" + event.getServerName() + "] " + event);
                                    SpectrumServer.addAlarm(event.getServerName(), alarm, event.getAlarmId(), manager);
                                }
                                if (event.toTerminate()) {
                                    eventLog.info("TERMINATE: [" + event.getServerName() + "] " + event);
                                    SpectrumServer.removeAlarm(event.getServerName(), alarm, event.getAlarmId(), manager);
                                }
                            } else {
                                LOG.warn("No alarm definition found for host " + hostname + ", event discarded");
                                discardedLog.warn("No alarm definition found for host " + hostname + ", event discarded");
                                eventLog.info("DISCARD: [" + event.getServerName() + "] " + event);
                            }
                        }
                        LOG.info("Consumed event [" + event + "]");
                        SpectrumServer.listServers();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
   
}