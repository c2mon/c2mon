/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.spectrum;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.spectrum.SpectrumEvent.SpectrumEventType;
import cern.c2mon.daq.spectrum.listener.SpectrumListenerJunit;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

public class SpectrumTestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SpectrumTestUtil.class);
    private static final SimpleDateFormat DTE_FMT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    private SpectrumTestUtil() {        
    }

    public static void trySleepSec(int n) {
        try {
            Thread.sleep(n * 1000);
        } catch (Exception e) {
            LOG.warn("Sleep interrupted", e);
        }
    }

    
    public static boolean getValue(ISourceDataTag iSourceDataTag) {
        SourceDataTagValue v = iSourceDataTag.getCurrentValue();
        Boolean b = (Boolean) v.getValue();
        return b.booleanValue();
    }

    public static boolean getValue(EquipmentMessageHandler handler, long tagId) {
        SourceDataTagValue v = handler.getEquipmentConfiguration().getSourceDataTag(tagId).getCurrentValue();
        Boolean b = (Boolean) v.getValue();
        return b.booleanValue();
    }
    
    public static void sendKeepAlive(String server, String status) {
        // KAL OK 2015-01-29 11:05:01 - - - - [AlarmNotifier process is running on cs-srv-44]
        StringBuffer buffer = new StringBuffer();
        buffer.append(SpectrumEventType.KAL.toString() + " ");
        buffer.append(status + " ");

        Date now = new Date(System.currentTimeMillis());
        buffer.append(DTE_FMT.format(now) + " - - - - ");
        buffer.append("[AlarmNotifier process is running on " + server + "]");
        SpectrumListenerJunit.getListener().sendMessage(server, buffer.toString()); 
    }
    
    public static void sendMessage(String server, SpectrumEventType cmd, String hostname, int causeId) {

        StringBuffer buffer = new StringBuffer();
        buffer.append(cmd.toString() + " ");

        // convert hostname to ip address
        InetAddress address = null;
        try {
            address = InetAddress.getByName(hostname);
            buffer.append(address.getHostAddress() + " ");
        } catch (UnknownHostException e) {
            LOG.error("Unknown host {}", hostname, e);
            buffer.append(hostname + " ");
        }
        
        Date now = new Date(System.currentTimeMillis());
        buffer.append(DTE_FMT.format(now) + " x 1 ");       // fake model and alarm id
        buffer.append(causeId + " ");
        buffer.append("0x20661c0 \"TEST PB DESC ...\"");    // constant model handle and pb desc
        SpectrumListenerJunit.getListener().sendMessage(server, buffer.toString()); 
     // "CLR 172.18.227.106 01/29/2015 11:03:47 u2485-r-pb14-bhp42-1 21082124 10009 0x20661c0 \"DEVICE HAS ...\"");

    }
}
