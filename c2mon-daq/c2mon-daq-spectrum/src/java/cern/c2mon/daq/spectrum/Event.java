/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;


import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/***
 * Spectrum events. Each instance of this class represents an alarm setting or clearing as
 * announced by Spectrum. 
 * 
 * @author mbuttner
 * @version 29 May 2009
 * @version 28 July 2010 added cause id in the Spectrum message, we will simply replace our alarm id field by it
 *                       because this is the field that allows to identify what we need to clear on Spectrum
 *                       alarm termination. IMPORTANT: The property cause id is already present in the class
 *                       but not used. Instead, we ignore the alarm id in the incoming message and put the 
 *                       cause id into the field alarm id (avoids any other code changes)
 */
public class Event {
    
    public static int MESSAGE_FORMAT_VERSION = 1;
    
    /**
     * Identifier for the "keep alive" message for a given Spectrum server
     */
    public static final String EVENT_KEEP_ALIVE = "KAL";
    
    /**
     * A Spectrum alarm is cleared, which does not mean that a LASER alarm has to be terminated!
     */
    public static final String EVENT_CLEAR_ALARM = "CLR";
    
    /**
     * A Spectrum alarm is updated, should not really affect our system except for problem description
     */
    public static final String EVENT_UPDATE_ALARM = "UPD";
        
    /**
     * A Spectrum alarm is activated, note that the LASER alarm might already be up
     */
    public static final String EVENT_SET_ALARM = "SET";
    
    /**
     * Sent by the Spectrum server when it restarts, so that we that all alarms can first be terminated.
     */
    public static final String EVENT_RESET = "RST";

    /**
     * The date format as transmitted by Spectrum
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    
    private static Logger log = Logger.getLogger(Event.class);    
    private static Logger logComm = Logger.getLogger("LOG_COMM");

    // Message data sent over TCP by the Spectrum server
    private String serverName;                  // Hostname of the Spectrum server at the origin of this event
    private String msg;                         // The raw data received over TCP socket
    private String eventType;                   // The event type might be CLR, SET, UPD or RESET
    private String ipAddress;                   // in the form aaa.bbb.ccc.dddd sent over TCP by Spectrum 
    private boolean spectrumNotifierOk = true;  // maybe false (when keep alive from Spectrum signals an error)
    private String dateString;                  // in the form yyyy/mm/dd
    private String timeString;                  // in the form hh:mi:ss
    private String model;                       
    private String modelHandle;
    private long alarmId;                       // The Spectrum one!
    
    /**
     * The Spectrum cause id, real identifier for a device and a problem as the alarm id might by
     * changed when another Spectrum server takes over
     */
    private String causeId;
    
    private String problemDescription;
    private String contextURL;
    private String hostname;

    private Object lock = new Object();
    
    //
    // --- CONSTRUCTION --------------------------------------------------------------------------------
    //
    
    /**
     * Constructor. Only stores the raw data and extracts the event type. This is a choice in order to 
     * keep the action carried out synchronously by the Server class as short as possible. Later on,
     * before using the Event object, one should call prepare() to set all other properties based on
     * the raw data.
     * 
     * @param serverName <code>String</code>    the name of the Spectrum server sending the event
     * @param msg <code>String</code>   the raw event data sent over TCP by the Spectrum server
     */
    public Event(String serverName, String msg) {       
        this.msg = msg;
        this.serverName = serverName;
        StringTokenizer st = new StringTokenizer(msg);
        setEventType(st.nextToken());
    }
    
    //
    // --- PUBLIC METHODS -------------------------------------------------------------------------------
    //
    /**
     * Hack the event properties out of the raw data. Should be called on event consuming, not generating !
     */
    public void prepare() {
        synchronized(lock) {
            StringTokenizer st = new StringTokenizer(msg);
            try {
                logComm.info(">>>" + msg + "<<<");
                setEventType(st.nextToken());
                
                //
                // If this is a keepalive message, we check the status of the notifier
                // else, the second field is the IP address of the host concerned by the alarm
                if (this.isKeepAlive()) {
                    String status = st.nextToken();
                    if (status.equals("OK")) this.spectrumNotifierOk = true;
                    else this.spectrumNotifierOk = false;
                } else setIpAddress(st.nextToken());
                
                //
                // Standard fields valid for all messages
                dateString =  st.nextToken();
                timeString = st.nextToken();
                model = st.nextToken();
                                
                String sAlarmId = st.nextToken();
                try {
                    // Parse alarmId only for if the message is not of type "keepalive"
                    if (this.isKeepAlive()) alarmId = 0;
                    else alarmId = Long.parseLong(sAlarmId);
                } catch (NumberFormatException nfe) {
                    log.warn("Failed to parse alarm id " + sAlarmId + " to long value.", nfe);
                }
                
                //
                // version 29 July 2010: ignore the alarm id in favor of the new cause id!
                // note KAL (keepalive) messages do not have ids, skip this par
                //
//                if (MESSAGE_FORMAT_VERSION > 1)
//                {
                    causeId = st.nextToken(); 
                    if (!this.isKeepAlive()) {
                        long oldAlarmId = alarmId;
                        alarmId = Long.parseLong(causeId, 16);
                        logComm.info("Message format " + MESSAGE_FORMAT_VERSION + "]: replaced alarm id " 
                            + oldAlarmId + " with " + alarmId + " based on cause id " + causeId);
                    }
//                }
                // end change 29 July 2010
                
                // optional fields model handle and problem description
                if (st.hasMoreTokens()) {
                    modelHandle = st.nextToken();
                    if (alarmId > 0) {
                        contextURL ="http://spectrum.cern.ch/spectrum/oneclick.jnlp?alarm=" + modelHandle + "@" + alarmId;
                        log.debug("Spectrum URL is [" + contextURL + "]");
                    }
                }
                if (st.hasMoreTokens()) problemDescription = st.nextToken("\"");
            } catch (Exception e) {
                log.error("Parsing of message into alarm structure failed",e);
            }
        }
    }
    
    
    /**
     * @return <code>long</code> the Spectrum cause id used as identifier to clear all problems for a device
     */
    public String getCauseId() {
        return this.causeId;
    }
    
    
    /**
     * The alarm id from Spectrum is used to identify multiple alarms on the same host active at the same time.
     * A LASER alarm should be terminated only if all Spectrum alarms for a host are cleared!
     * 
     * @return <code>long</code> the Spectrum alarm id
     */
    public long getAlarmId() {
        return this.alarmId;
    }
    
    /**
     * @return <code>String</code> use a subset of the fields to provide console output for an event
     */
    @Override
    public String toString() {
        return this.ipAddress + " " + this.dateString + " " + this.timeString + " " + this.model + " " + this.alarmId;
    }
    
    /**
     * @param eventType <code>String</code> should be one of CLR, SET, UPD, RESET
     */
    private void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    /***
     * Set the IP address for this alarm event. The data is used to derive the hostname, which is in general
     * the alarm key for network alarms
     * 
     * @param ipAddress <code>String</code> as sent by Spectrum
     */
    private void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        if (ipAddress != null) this.ipAddress = ipAddress.trim();
        if (this.ipAddress.length() > 0) {
            try {
                java.net.InetAddress inetAdd = java.net.InetAddress.getByName(ipAddress); 
                hostname =  inetAdd.getHostName();
                if (hostname != null) {
                    hostname = hostname.toUpperCase();
                    int domainPos = hostname.indexOf(".CERN.CH"); 
                    if ( domainPos > 0) hostname = hostname.substring(0, domainPos);
                }
            } catch(java.net.UnknownHostException uhe) {
                hostname = "Unknown host for IP " + ipAddress;
            }
        } else hostname = "not specified";
        log.debug("Hostname derived from " + ipAddress + " is " + hostname);
    }
    
    /**
     * @return <code>String</code> the hostname derived from the IP address, might be "unknown"
     */
    public String getHostname() {
        return this.hostname;
    }
    
    /**
     * @return <code>String</code> the problem description as sent by Spectrum
     */
    public String getProblemDescription() {
        return this.problemDescription;
    }
    
    /**
     * @return <code>String</code> the Spectrum server that sent the alarm
     */
    public String getServerName() {
        return this.serverName;
    }
    
    /**
     * @return <code>long</code> the timestamp value derived from Spectrum's date and time, 0 in case of failure
     */
    public long getUserTimestamp() {
        String dtStr = dateString + " " + timeString;
        long uts = 0;
        try {
            uts = sdf.parse(dtStr).getTime();
        } catch (Exception e) {
            log.error("Failed to parse date string [" + dtStr + "]", e);
        }
        return uts;
    }

    /**
     * @return  <code>String</code> direct pointer to Spectrum URL
     */
    public String getContextURL() {
        return this.contextURL;
    }
    
    /**
     * @return <code>boolean</code> true if the notifier status signaled by this KAL event is ok, false otherwise
     */
    public boolean isSpectrumNotifierOk() {
        return this.spectrumNotifierOk;
    }
    
    /**
     * @return <code>boolean</code> true for KAL events, false otherwise
     */
    public boolean isKeepAlive() {
        if (this.eventType.equals(EVENT_KEEP_ALIVE)) return true;
        return false;       
    }
    
    /**
     * @return <code>boolean</code> true for SET and UPD events, false otherwise
     */
    public boolean toActivate() {
        if (this.eventType.equals(EVENT_SET_ALARM)) return true;
        if (this.eventType.equals(EVENT_UPDATE_ALARM)) return true;
        return false;
    }
    
    /**
     * @return <code>boolean</code> true for CLR events, false otherwise
     */
    public boolean toTerminate() {
        if (this.eventType.equals(EVENT_CLEAR_ALARM)) return true;
        return false;
    }
    
    /**
     * @return <code>boolean</code> true for RESET, false otherwise
     */
    public boolean toReset() {
        if (this.eventType.equals(EVENT_RESET)) return true;
        return false;       
    }
}
