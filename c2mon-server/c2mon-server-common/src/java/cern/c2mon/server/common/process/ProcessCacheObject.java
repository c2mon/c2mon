/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005-2011 CERN. This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.process;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cern.c2mon.server.common.equipment.AbstractSupervisedCacheObject;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity;

// imported many methods from TIM1

/**
 * Cache object representing the C2MON DAQ processes. This object usually resides in the cache when accessed, in which
 * case care must be taken with synchronization:
 * <p>
 * Methods exposed by the Process interface are thread safe and can safely be used to access details about the process,
 * whether in or out the cache. Setter methods are usually not exposed, and modifications to the process objects in the
 * cache should preferably be made using the ProcessFacade bean. Setter methods not exposed in the Process interface are
 * in general NOT thread safe and should therefore only be used on clones of the Process object residing outside the
 * cache.
 */
public class ProcessCacheObject extends AbstractSupervisedCacheObject implements Process, Cacheable, Cloneable {

    private static final long serialVersionUID = -2235204911515127976L;

    public static final String JMS_QUEUE_JNDI_ROOT = "jms/process/destinations/queues/processmessage/";

    public static final String JMS_QCF = "jms/process/factories/QCF";

    public static final String JMS_TOPIC_PREFIX = "tim.process.";

    public static final Pattern PROCESS_NAME_PATTERN = Pattern.compile("P_([A-Z_0-9])+", Pattern.CASE_INSENSITIVE);
    
    public static final String LOCAL_CONFIG = "Y";
    
    public static final String SERVER_CONFIG = "N";

    /**
     * LOG4J Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ProcessCacheObject.class);

    /**
     * A description of the process.
     */
    private String description;

    /**
     * JMS user name used for old Sonic connection.
     */
    private String jmsUserName;

    /**
     * JMS password used for old Sonic connection.
     */
    private String jmsPassword;

    /**
     * JNDI Name of the JMS queue for old Sonic connection (not used for ActiveMQ).
     */
    private String jmsMessageQueue;

    /**
     * Connection factory for old Sonic JMS connection.
     */
    private String jmsQueueConnectionFactory;

    /**
     * Max number of updates in a single message from the DAQ process.
     */
    private int maxMessageSize = 100;

    /**
     * Max delay between reception of update by a DAQ and sending it to the server.
     */
    private int maxMessageDelay = 1000;

    /**
     * Equipments under this Process.
     */
    private ArrayList<Long> equipmentIds = new ArrayList<Long>();

    /**
     * Name of the temporary topic on which the process listens for server requests. Currently set after loading or in
     * facade creation.
     */
    private String jmsListenerTopic;

    /**
     * Host the DAQ process is running on.
     */
    private String currentHost;

    /**
     * Time the DAQ process was started up.
     */
    private Timestamp startupTime;

    /**
     * Indicates the DAQ needs rebooting to obtain the latest configuration from the server.
     */
    private Boolean requiresReboot = Boolean.FALSE;
    
    /** 
     *  Process Identifier Key (PIK) per DAQ instance
     */
    private Long processPIK;
    
    /**
     * Enum for describing configuration type
     * 
     * Y for LOCAL_CONFIG or N for SERVER_CONFIG
     */
    public static enum LocalConfig {
      Y("LOCAL_CONFIG"), N("SERVER_CONFIG");
      
      private String configType;
    
      LocalConfig(String configType) {
        this.configType = configType;
      }
      
      public String getConfigType() {
        return this.configType;
      }
    }
    
    /**
     * The configuration type can be whether Y (LOCAL_CONFIG) or N (SERVER_CONFIG)
     */
    private LocalConfig localConfig;

    /**
     * Constructor with minimal number of non-null fields. Used when loading from the DB at start up.
     * 
     * @param id the id of the process
     * @param name the name of the process
     * @param stateTagId the id of the state tag
     * @param maxMessageSize the max number of updates per message from DAQ layer
     * @param maxMessageDelay the max delay at DAQ layer before sending the updates
     */
    public ProcessCacheObject(Long id, String name, Long stateTagId, Integer maxMessageSize, Integer maxMessageDelay) {
        super(id, stateTagId);
        setName(name);
        this.maxMessageSize = maxMessageSize;
        this.maxMessageDelay = maxMessageDelay;
    }

    /**
     * Constructor only setting id.
     * 
     * @param id process id
     */
    public ProcessCacheObject(final Long id) {
        super(id);
    }

    /**
     * Initalises the Sonic JMS configuration properties, which are all derived from the process name. Only the
     * jmsListenerTopic is used by the ActiveMQ implementation and is no longer set here...
     * <p>
     * TODO remove/modify this and all related fields once Sonic has gone...
     */
    private void initJmsProperties() {
        this.jmsUserName = getName();

        // Assuming that the process name follows the pattern P_XXX, the jms
        // password will be XXX_P

        String[] parts = getName().split("_");
        if (parts.length > 1) {
            this.jmsPassword = parts[1] + "_P";
        } else {
            this.jmsPassword = getName();
        }

        this.jmsMessageQueue = JMS_QUEUE_JNDI_ROOT + getName();

        this.jmsQueueConnectionFactory = JMS_QCF;

    }

    /**
     * Get an optional free-text description for the process.
     * 
     * @return
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the user name to be used by the process for connecting to the JMS provider.
     * 
     * @return
     */
    public String getJMSUserName() {
        return this.jmsUserName;
    }

    /**
     * Get the user name to be used by the process for connecting to the JMS provider.
     * 
     * @return
     */
    public String getJMSPassword() {
        return this.jmsPassword;
    }

    /**
     * Get the JNDI name of the JMS message queue on which the DAQ process is expected to send its value updates.
     * 
     * @return the JNDI name of the DAQ's JMS message queue
     */
    public String getJmsMessageQueue() {
        return this.jmsMessageQueue;
    }

    /**
     * Get the JNDI name of the queue connection factory to be used by the DAQ process to connect to the JMS broker.
     * 
     * @return the JNDI name of the DAQ's queue connection factory
     */
    public String getJmsQueueConnectionFactory() {
        return this.jmsQueueConnectionFactory;
    }

    /**
     * Get the maximum number of value updates to be sent by the DAQ at once. If the DAQ wants to send more value
     * updates than maxMessageSize (e.g. on start-up), the updates have to be split into several JMS messages.
     * 
     * @return max number of updates as int
     */
    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }

    /**
     * Get the maximum number of milliseconds a value update message may be delayed by the DAQ in order to bundle
     * several value updates together. If only one value update is to be sent by the DAQ, the DAQ may wait up to
     * maxMessageDelay milliseconds before sending the message to the application server.
     * 
     * @return max delay in milliseconds
     */
    public int getMaxMessageDelay() {
        return this.maxMessageDelay;
    }

    /**
     * If the DAQ process is currently (known to be) running, this method will return the start-up time of the DAQ
     * process. If the DAQ process is believed to be DOWN, this method will return null.
     * 
     * @return
     */
    public Timestamp getStartupTime() {
        return this.startupTime;
    }

    /**
     * If the DAQ process is currently (believed to be) running, this method will return the name of the host on which
     * the DAQ process has been started. If the DAQ process is believed to be DOWN, the method will return null.
     * 
     * @return the host
     */
    public String getCurrentHost() {
        return this.currentHost;
    }
    
    /**
     * Sets the PIK of the process.
     * 
     * @param processPIK The process PIK
     */
    public void setProcessPIK(final Long processPIK) {
      this.processPIK = processPIK;
    }

    /**
     * Returns the process PIK
     * 
     * @return The process PIK
     */
    @Override
    public Long getProcessPIK() {
      return this.processPIK;
    }
    
    /**
     * Sets the Configuration type to Local or Server
     * In the data base is call LocalConfig
     * 
     * @param localConfig Y(LOCAL_CONFIG)/N(SERVER_CONFIG)
     */
    public void setLocalConfig(final LocalConfig localConfig) {
        this.localConfig = localConfig;
    }

    /** 
     * @return the Configuration type => Y(LOCAL_CONFIG)/N(SERVER_CONFIG)
     */
    public LocalConfig getLocalConfig() {
      return this.localConfig;
    }

    @Override
    public Collection<Long> getEquipmentIds() {
        return equipmentIds;
    }

    /**
     * Setter method.
     * 
     * @param name
     */
    public void setJmsUserName(String name) {
        this.jmsUserName = name;
    }

    /**
     * @param jmsPassword the jmsPassword to set
     */
    public void setJmsPassword(String jmsPassword) {
        this.jmsPassword = jmsPassword;
    }

    /**
     * @param jmsMessageQueue the jmsMessageQueue to set
     */
    public void setJmsMessageQueue(String jmsMessageQueue) {
        this.jmsMessageQueue = jmsMessageQueue;
    }

    /**
     * @param jmsQueueConnectionFactory the jmsQueueConnectionFactory to set
     */
    public void setJmsQueueConnectionFactory(String jmsQueueConnectionFactory) {
        this.jmsQueueConnectionFactory = jmsQueueConnectionFactory;
    }

    /**
     * Sets the name of the process and (re-)sets the associated JMS properties.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        super.setName(name);
        initJmsProperties();
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param maxMessageSize the maxMessageSize to set
     */
    public void setMaxMessageSize(final int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * @param maxMessageDelay the maxMessageDelay to set
     */
    public void setMaxMessageDelay(final int maxMessageDelay) {
        this.maxMessageDelay = maxMessageDelay;
    }

    /**
     * @param currentHost the currentHost to set
     */
    public void setCurrentHost(final String currentHost) {
        this.currentHost = currentHost;
    }

    /**
     * @param startupTime the startupTime to set
     */
    public void setStartupTime(final Timestamp startupTime) {
        this.startupTime = startupTime;
    }

    /**
     * @param equipmentIds the equipmentIds to set
     */
    public void setEquipmentIds(final ArrayList<Long> equipmentIds) {
        this.equipmentIds = equipmentIds;
    }

    @Override
    public String getJmsListenerTopic() {
        return jmsListenerTopic;
    }

    /**
     * Setter method.
     * 
     * @param requiresReboot
     */
    public void setRequiresReboot(final Boolean requiresReboot) {
        this.requiresReboot = requiresReboot;
    }

    @Override
    public Boolean getRequiresReboot() {
        return requiresReboot;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Process clone() {
        ProcessCacheObject clone = (ProcessCacheObject) super.clone();
        clone.equipmentIds = (ArrayList<Long>) equipmentIds.clone();
        if (this.startupTime != null) {
            clone.startupTime = (Timestamp) this.startupTime.clone();
        }
        
        return clone;
    }

    @Override
    public SupervisionEntity getSupervisionEntity() {
        return SupervisionEntity.PROCESS;
    }

    @Override
    public void setJmsListenerTopic(String jmsListenerTopic) {
        this.jmsListenerTopic = jmsListenerTopic;
    }

}
