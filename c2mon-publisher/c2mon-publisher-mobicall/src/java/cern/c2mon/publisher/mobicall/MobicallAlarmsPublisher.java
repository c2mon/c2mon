/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.publisher.mobicall;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import cern.c2mon.client.jms.AlarmListener;
import cern.c2mon.shared.client.alarm.AlarmValue;

/**
 */
public final class MobicallAlarmsPublisher implements Runnable, AlarmListener {

    static final Logger LOG = LoggerFactory.getLogger(MobicallAlarmsPublisher.class);
    @SuppressWarnings("unused")
    private static final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY HH:MM:SS");

    private Thread daemonThread;
    private C2monConnectionIntf c2mon;

    private Vector<String> MobicallServers;
    private final String MOBICALL_ALARM_NR_OID = "0.0.0.0";
    private final String MOBICALL_ADDITIONAL_INFO_OID = "0.0.0.1";

    private Snmp snmp;
    private Vector<CommunityTarget> snmpTargets;

    private Properties snmpConfig;
    private int delay;
    private String protocol;
    private boolean sendTraps;
    
    
    //
    // --- CONSTRUCTION ----------------------------------------------------------------
    //
    public MobicallAlarmsPublisher(C2monConnectionIntf c2mon) throws IOException {
        LOG.warn("Publisher instance created ...");
        MobicallServers = new Vector<String>();
    
        snmpTargets = new Vector<CommunityTarget>();
        snmpConfig = new Properties();
        snmpConfig.load(this.getClass().getResourceAsStream("mobicall.properties"));
        this.delay = Integer.parseInt(snmpConfig.getProperty("mobicall.delay"));
        this.protocol = snmpConfig.getProperty("mobicall.protocol");
        MobicallServers.add(snmpConfig.getProperty("mobicall.server.main"));
        MobicallServers.add(snmpConfig.getProperty("mobicall.server.backup"));

        if (snmpConfig.getProperty("mobicall.server.debug") != null) {
            MobicallServers.add(snmpConfig.getProperty("mobicall.server.debug"));
        }
        sendTraps = Boolean.parseBoolean(snmpConfig.getProperty("mobicall.active"));
        setupSNMP();
        this.c2mon = c2mon;
        
        MobicallAlarm.init();
    }


    //
    // --- PUBLIC METHODS ---------------------------------------------------------------
            
    static String getAlarmId(AlarmValue av) {
        return av.getFaultFamily() + ":" + av.getFaultMember() + ":" + av.getFaultCode();
    }
    


    //
    // --- DAEMON -----------------------------------------------------------------------
    //
    /**
     * This method has to be called in order to start the RDA publisher
     */
    public void start() {
        daemonThread = new Thread(this);
        daemonThread.start();
    }

    @Override
    public void run() {
        try {
            LOG.info("Starting RDA device server");

            c2mon.start();
            Collection<AlarmValue> activeAlarms = c2mon.getActiveAlarms();
            LOG.info("... now listening to incoming alarms.");
            
            c2mon.connectListener();
            for (AlarmValue av : activeAlarms) {
                this.onAlarmUpdate(av);
            }
            LOG.info("Started with initial selection of " + activeAlarms.size() + " alarms.");
            // everything ready, start the RDA server for publishung
        } catch (Exception e) {
            LOG.error("A major problem occured while running the RDA server. Stopping publisher!", e);
        }
    }

    public void join() throws InterruptedException {
        this.daemonThread.join();
    }

    public void shutdown() {        
        LOG.info("Stopping C2MON ...");
        c2mon.stop();
        try {
            daemonThread.join();
        } catch (InterruptedException e) {
            LOG.warn("InterruptedException caught", e);
        }
        LOG.info("RDA publisher stopped.");
    }

    //
    // --- Implements AlarmListener -----------------------------------------------------------
    //
    @Override
    public void onAlarmUpdate(AlarmValue av) {
        String alarmId = getAlarmId(av);
        LOG.debug(" RECEIVED    > " + alarmId + " is active:" + av.isActive());        
        MobicallAlarm ma = MobicallAlarm.find(av);
        if (ma != null) {
            if (sendTraps) {
                sendTrap(ma.getMobicallId(), composeTrapMessage(ma, av));            
            } else {
                LOG.info(composeTrapMessage(ma, av));
            }
        }
        LOG.debug(" PROCESSED    > " + alarmId);
    }

    

  //
  // -- PRIVATE METHODS ---------------------------------------------
  //
  
  /**
   * For each string in the list of servers, create a valid SNMP ("Community") target to
   * be used for each alarm received in the callback method.
   */
  private void setupSNMP() {
      
      //
      // SNMP global handle setup
      //
      LOG.warn("Mobicall setup (" + MobicallAlarmsPublisher.class.getName() + ") ...");
      TransportMapping transport;
      try {
          if (protocol.equals("UDP")) {
              transport = new DefaultUdpTransportMapping();
          } else {
              transport = new DefaultTcpTransportMapping();
          }
      } catch (IOException e) {
          throw new RuntimeException("Could not create SNMP transport for protocol " + protocol, e);
      }
      snmp = new Snmp(transport);
      
      //
      // Create the list of targets
      //
      int counter = 0;
      for (String target : MobicallServers) {
          counter++;
          LOG.info("Define target for address " + target + " ...");
          CommunityTarget ct = new CommunityTarget();
          ct.setCommunity(new OctetString("public"));
          ct.setAddress(GenericAddress.parse(target));
          ct.setRetries(2);
          ct.setTimeout(1500);
          ct.setVersion(SnmpConstants.version1);
          snmpTargets.add(ct);
      }
      LOG.warn("Mobicall setup completed with " + counter + " servers");
  }

  /**
   * Create the SNMP message (called PDU) and send it to all the configured servers
   * 
   * @param alarmNumber <code>String</code> is the Mobicall id which defines the notifications to trigger
   * @param message <code>String</code> for additional info in Mobicall
   */
  private void sendTrap(String alarmNumber, String message) {
      // creating PDU
      PDUv1 pdu = new PDUv1();
      pdu.setType(PDU.TRAP);

      pdu.add(new VariableBinding(new OID(MOBICALL_ALARM_NR_OID), new OctetString(alarmNumber)));
      pdu.add(new VariableBinding(new OID(MOBICALL_ADDITIONAL_INFO_OID), new OctetString(message)));
    
      if (LOG.isDebugEnabled()) {
          LOG.debug("Sending Mobicall message for alarmNumber '" + alarmNumber + "' with text '" + message + "'");
          LOG.debug("Message: " + pdu.toString());
      }

      int success = 0;
      for (CommunityTarget ct : snmpTargets) {
          try {
              LOG.info("Sending to " + ct.getAddress() + " ... ");
              snmp.trap(pdu, ct);
              if (delay > 0 && delay < 500) {
                  LOG.debug("... apply delay " + delay + " ms between traps to Mobicall ... ");
                  Thread.sleep(delay);
              } else {                  
                  LOG.debug("... sending delay " + delay + " out of valid range 0-500ms, ignored.");
              }
              LOG.debug("... ok for " + ct.getAddress() + ", try next ... ");
              success++;
          } catch (Exception e) {
              LOG.warn("Failed to send Mobicall notification to " + ct.getAddress() + " " + e.getMessage());
              LOG.debug("Stack trace follows", e);
          }   
      }      
      if (success == 0) {
          LOG.error("None of the requested Mobicall notifications succeeded!!!");          
      } else {
          LOG.info("Notification successfully passed to " + success + " Mobicall servers.");
      }
  }

  /**
   * Create the value of the message field in the SNMP trap
   * 
   * @param alarm <code>Alarm</code> objet used to fill the message placeholders
   * @return <code>String</code> the value to be assigned to the "message" OID in the SNMP trap
   */
  private String composeTrapMessage(MobicallAlarm alarm, AlarmValue av) {
    StringBuffer message = new StringBuffer();
    message.append(alarm.getSystemName());
    message.append(" ");
    message.append(alarm.getIdentifier());
    message.append(" ");
    message.append(alarm.getFaultCode() + " [" + alarm.getMobicallId() + "] ");
    message.append(" ");
    message.append(alarm.getProblemDescription());
    message.append(" ");
    message.append(av.isActive() ? "ACTIVE" : "TERMINATE");
    return message.toString();
  }

}
    

