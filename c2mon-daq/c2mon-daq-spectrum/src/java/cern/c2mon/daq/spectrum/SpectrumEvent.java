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
public class SpectrumEvent {
    
    public static int MESSAGE_FORMAT_VERSION = 1;

    enum SpectrumEventType {     
        INVALID_MESSAGE,     // in case the code found in the message is not known
        KAL,                 // server keep-alive
        CLR,                 // alarm termination in Spectrum wording
        UPD,
        SET,                // alarm activation in Spectrum words
        RST                // server asks to reset all alarms
    }

    /**
     * The date format as transmitted by Spectrum, DO NOT CHANGE without prior discussion with IT!
     */
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    
    private static final Logger LOG = Logger.getLogger(SpectrumEvent.class);    
    private static final Logger LOG_COMM = Logger.getLogger("LOG_COMM");

    // Message data sent over TCP by the Spectrum server
    private boolean prepared = false;
    private String serverName;                  // Hostname of the Spectrum server at the origin of this event
    private String msg;                         // The raw data received over TCP socket
    private SpectrumEventType eventType;        // The event type might be CLR, SET, UPD or RESET
    
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

    public enum HostnameQuality {FOUND, UNKNOWN, NOT_FOUND, IP_NOT_SPECIFIED}
    private HostnameQuality hqual;

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
    public SpectrumEvent(String serverName, String msg) {       
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
        if (!prepared)
        {
            synchronized(lock) {
                StringTokenizer st = new StringTokenizer(msg);
                try {
                    LOG_COMM.debug(">>>" + msg + "<<<");
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
                        LOG.warn("Failed to parse alarm id " + sAlarmId + " to long value.", nfe);
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
                            LOG_COMM.debug("Message format " + MESSAGE_FORMAT_VERSION + "]: replaced alarm id " 
                                + oldAlarmId + " with " + alarmId + " based on cause id " + causeId);
                        }
    //                }
                    // end change 29 July 2010
                    
                    // optional fields model handle and problem description
                    if (st.hasMoreTokens()) {
                        modelHandle = st.nextToken();
                        if (alarmId > 0) {
                            contextURL ="http://spectrum.cern.ch/spectrum/oneclick.jnlp?alarm=" + modelHandle + "@" + alarmId;
                            LOG.debug("Spectrum URL is [" + contextURL + "]");
                        }
                    }
                    if (st.hasMoreTokens()) 
                    {
                        LOG.info("Trying to retrieve the problem description ...");
                        int fromPos = msg.indexOf('"') + 1;
                        int toPos = msg.lastIndexOf('"');
                        if (fromPos > 0 && toPos > fromPos)
                        {
//                        problemDescription = st.nextToken("\"");
                            problemDescription = msg.substring(fromPos, toPos);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Parsing of message into alarm structure failed",e);
                }
                prepared = true;
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
        try {   
            this.eventType = SpectrumEventType.valueOf(eventType);
        } catch (Exception e) {
            this.eventType = SpectrumEventType.INVALID_MESSAGE;
            LOG.error("{} is not a valid message type code!");
        }
    }
    
    /***
     * Set the IP address for this alarm event. The data is used to derive the hostname, which is in general
     * the alarm key for network alarms
     * 
     * @param ipAddress <code>String</code> as sent by Spectrum
     */
    private void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        if (ipAddress != null) 
        {
            this.ipAddress = ipAddress.trim();
        }
        if (this.ipAddress.length() > 0) 
        {
            try 
            {
                java.net.InetAddress inetAdd = java.net.InetAddress.getByName(ipAddress); 
                LOG.info("Hostname is [" + inetAdd.getCanonicalHostName() + "]");
                hostname =  inetAdd.getCanonicalHostName();
                if (hostname != null && !hostname.equals(ipAddress)) 
                {
                    hostname = hostname.toUpperCase();
                    int domainPos = hostname.indexOf(".CERN.CH"); 
                    if ( domainPos > 0) 
                    {
                        hostname = hostname.substring(0, domainPos);
                    }
                    hqual = HostnameQuality.FOUND;
                }
                else
                {
                    LOG.warn("Failed to convert [" + ipAddress + "] into a valid hostname");
                    hqual = HostnameQuality.NOT_FOUND;
                }
            } catch(java.net.UnknownHostException uhe) 
            {
                hqual = HostnameQuality.UNKNOWN;
                LOG.warn("Unknown host for IP [" + ipAddress + "]");
            }
        } 
        else 
        {
            hqual = HostnameQuality.IP_NOT_SPECIFIED;
        }
        LOG.debug("Hostname derived from [" + ipAddress + "] is [" + hostname+ "]");
    }
    
    /**
     * @return <code>String</code> the hostname derived from the IP address, might be "unknown"
     */
    public String getHostname() {
        return this.hostname;
    }
    
    public HostnameQuality getHostnameQuality()
    {
        return this.hqual;
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
            LOG.error("Failed to parse date string [" + dtStr + "]", e);
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
        if (this.eventType == SpectrumEventType.KAL) {
            return true;
        }
        return false;       
    }
    
    /**
     * @return <code>boolean</code> true for SET and UPD events, false otherwise
     */
    public boolean toActivate() {
        if (this.eventType == SpectrumEventType.SET || this.eventType == SpectrumEventType.UPD) {
            return true;
        }
        return false;
    }
    
    /**
     * @return <code>boolean</code> true for CLR events, false otherwise
     */
    public boolean toTerminate() {
        if (this.eventType == SpectrumEventType.CLR) {
            return true;
        }
        return false;
    }
    
    /**
     * @return <code>boolean</code> true for RESET, false otherwise
     */
    public boolean toReset() {
        if (this.eventType == SpectrumEventType.RST) {
            return true;
        }
        return false;       
    }

    public SpectrumEventType getType() {
        return this.eventType;
    }
    
    
}
