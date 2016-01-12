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

package cern.c2mon.publisher.mobicall;

import java.io.IOException;
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

/**
 * The implementation to send alarm messages to Mobicall over SNMP. The behavior of this
 * class is widely dependant on the configuration parameters in mobicall.properties (prototocol,
 * min time between two messages, target servers, ...)
 * 
 * @author mbuttner
 */
public class SenderSnmpImpl implements SenderIntf {

    private static final Logger LOG = LoggerFactory.getLogger(SenderSnmpImpl.class);
    private static final Logger JRN = LoggerFactory.getLogger("MOBICALL_JOURNAL");

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
    // --- Implements SenderIntf ----------------------------------------------------------------
    //
    @Override
    public void setup() throws IOException {
        LOG.info("... loading Mobicall settings ...");
        MobicallServers = new Vector<String>();
    
        snmpTargets = new Vector<CommunityTarget>();
        snmpConfig = new Properties();
        snmpConfig.load(this.getClass().getResourceAsStream("/mobicall.properties"));
        this.delay = Integer.parseInt(snmpConfig.getProperty("mobicall.delay"));
        this.protocol = snmpConfig.getProperty("mobicall.protocol");
        MobicallServers.add(snmpConfig.getProperty("mobicall.server.main"));
        MobicallServers.add(snmpConfig.getProperty("mobicall.server.backup"));

        if (snmpConfig.getProperty("mobicall.server.debug") != null) {
            MobicallServers.add(snmpConfig.getProperty("mobicall.server.debug"));
        }
        sendTraps = Boolean.parseBoolean(snmpConfig.getProperty("mobicall.active"));
        LOG.info("-> " + snmpConfig.toString());
        
        LOG.info("... setting up SNMP communication ...");
        this.setupSNMP();
    }


    @Override
    public void send(String mobicallId, String message) {
        if (sendTraps) {
            LOG.warn("SENDING message to PRODUCTION Mobicall!");
            sendTrap(mobicallId, message);            
            JRN.info("SENT>   {}", message);
        } else {
            LOG.info(message);
            JRN.info("logged> {}", message);
        }        
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
        LOG.info("Mobicall setup (" + MobicallAlarmsPublisher.class.getName() + ") ...");
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
        LOG.info("Mobicall setup completed with " + counter + " servers");
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
      
        LOG.debug("Sending Mobicall message for alarmNumber '" + alarmNumber + "' with text '" + message + "'");
        LOG.debug("Message: " + pdu.toString());

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

}
