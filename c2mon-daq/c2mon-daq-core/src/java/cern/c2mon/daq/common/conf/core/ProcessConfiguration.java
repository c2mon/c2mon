/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.conf.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible for storing DAQ-specific configuration parameters.
 * 
 * It accesses the server to retrieve the DAQ XML configuration file (using the
 * ProcessRequestHandler) and loads the associated configuration details. This
 * is done in the configure() method.
 */
public class ProcessConfiguration {

  /**
   *  The name of the daq process
   */
    private String name;
    
    /**
     * The unique process identifier (UID)
     * (is set when XML conf is loaded by ProcessConfiguration object)
     */
    private Long processID;
    
    /**  
     *  Process Identifier Key (PIK) per DAQ instance
     */
    private Long processPIK;

    /**
     * Private queue on which the daq will permanently listen for incoming
     * messages from the application server.
     */
    private String jmsDaqCommandQueue;

    /**
     * Identifier of the data tag that shall be used as an alive tag for this
     * process
     */
    private long aliveTagId;

    /**
     * Interval in milliseconds at which the process is expected to send "alive"
     * messages to the application server
     */
    private int aliveInterval;

    /**
     * Maximum number of tag values to be packed into a single JMS message sent
     * to the server.
     */
    private long maxMessageSize;

    /**
     * Interval in milliseconds at which messages are to be sent to the server
     * if (1) there are tag updates to be processed (2) the max-message-size is
     * not reached
     */
    private long maxMessageDelay;
    
    /**
     * True if this configuration is local else false.
     */
    private boolean localConfiguration;
    
    /**
     * Map of equipment unit configurations of this process.
     */
    private final ConcurrentHashMap<Long, EquipmentConfiguration> equipmentConfigurations = 
        new ConcurrentHashMap<Long, EquipmentConfiguration>();
    
    /**
     *  The DAQ process Host Name
     */
      private String hostName;

    /**
     * This method sets the Alive Tag identifier
     * 
     * @param id the Alive Tag identifier
     */
    public void setAliveTagID(final long id) {
        aliveTagId = id;
    }

    /**
     * This method gets the AliveTag identifier
     * 
     * @return The id of the alive tag of this DAQ.
     */
    public long getAliveTagID() {
        return aliveTagId;
    }

    /**
     * This method sets the AliveTag interval
     * 
     * @param interval the alive tag interval (in milliseconds)
     */
    public void setAliveInterval(final int interval) {
        aliveInterval = interval;
    }

    /**
     * This method gets the AliveTag millisecond interval
     * 
     * @return The alive interval of this DAQ in ms.
     */
    public int getAliveInterval() {
        return aliveInterval;
    }

    /**
     * This method sets the maximum allowed size of the SourceDataTag's message
     * 
     * @param size the maximum size of the message
     */
    public void setMaxMessageSize(final long size) {
        maxMessageSize = size;
    }

    /**
     * This method gets the maximum allowed size of the SourceDataTag's message
     * 
     * @return The maximum size of the source data tag message.
     */
    public long getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * This method sets the maximum allowed delay for the SourceDataTag's
     * message
     * 
     * @param delay The delay (in milliseconds)
     */
    public void setMaxMessageDelay(final long delay) {
        maxMessageDelay = delay;
    }

    /**
     * This method gets the maximum allowed delay for the SourceDataTag's
     * message
     * 
     * @return The maximum delay for a source data tag message.
     */
    public long getMaxMessageDelay() {
        return maxMessageDelay;
    }

    /**
     * This method sets the queue name for sending commands to the ProcessMessageReceiver
     * 
     * @param jmsDaqCommandQueue the queue name
     */
    public void setJmsDaqCommandQueue(final String jmsDaqCommandQueue) {
          this.jmsDaqCommandQueue = jmsDaqCommandQueue;
    }

    /**
     * This method gets the queue name for the ProcessMessageReceiver
     * 
     * @return The listener queue for the process message receiver.
     */
    public final String getJmsDaqCommandQueue() {
        return this.jmsDaqCommandQueue;
    }

    /**
     * Sets the process name of the configuration.
     * 
     * @param processName The name of the process.
     */
    public void setProcessName(final String processName) {
        this.name = processName;
    }

    /**
     * Returns the process name of the configuration.
     * 
     * @return The process name.
     */
    public String getProcessName() {
        return name;
    }

    /**
     * Sets the process id of the configuration.
     * 
     * @param processID The process id.
     */
    public void setProcessID(final Long processID) {
        this.processID = processID;
    }

    /**
     * Returns the process id.
     * 
     * @return The process id.
     */
    public Long getProcessID() {
        return processID;
    }

    /**
     * Returns the live map of equipment configuration keys and values.
     * All changes will (add/remove/clear...) be made to the real
     * map. It is never null.
     * @return The live map of sub equipment commFault keys and values.
     */
    public Map<Long, EquipmentConfiguration> getEquipmentConfigurations() {
        return equipmentConfigurations;
    }
    
    
    public void addEquipmentConfiguration(EquipmentConfiguration econf) {
        this.equipmentConfigurations.put(econf.getId(),econf);
    }
    
    public void removeEquipmentConfiguration(final long equipmentId) {
        this.equipmentConfigurations.remove(equipmentId);       
    }
    
    
    
    /**
     * Returns the equipment configuration with the provided id.
     * @param equipmentId The id of the desired equipment.
     * @return The equipment configuration with the provided id.
     */
    public EquipmentConfiguration getEquipmentConfiguration(final long equipmentId) {
        return equipmentConfigurations.get(equipmentId);
    }

    /**
     * Sets if this configuration should be treated as local.
     * @param localConfiguration True local, false from server.
     */
    public void setLocalConfiguration(final boolean localConfiguration) {
        this.localConfiguration = localConfiguration;
    }

    /**
     * Returns if this process configuration is local or retrieved from the server.
     * @return True if the configuration is local else false.
     */
    public boolean isLocalConfiguration() {
        return localConfiguration;
    }
    
    /**
     * Sets the PIK of the process.
     * 
     * @param processPIK The process PIK
     */
    public final void setprocessPIK(final Long processPIK) {
        this.processPIK = processPIK;
    }

    /**
     * Returns the process PIK
     * 
     * @return The process PIK
     */
    public Long getprocessPIK() {
        return this.processPIK;
    }
    /**
     * Sets the DAQ process Host Name
     * 
     * @param hostName The DAQ process Host Name
     */
    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    /**
     * Returns the DAQ process Host Name
     * 
     * @return The DAQ process Host Name
     */
    public String getHostName() {
        return this.hostName;
    }
}
