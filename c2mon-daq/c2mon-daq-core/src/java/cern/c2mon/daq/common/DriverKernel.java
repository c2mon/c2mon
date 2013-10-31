/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationFactory;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationHandler;
import cern.c2mon.daq.common.conf.core.ProcessConfiguration;
import cern.c2mon.daq.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.daq.common.messaging.JmsLifecycle;
import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.messaging.impl.ProcessMessageSender;
import cern.c2mon.daq.common.messaging.impl.RequestController;
import cern.c2mon.daq.filter.FilterConnectorThread;
import cern.c2mon.daq.tools.StackTraceHelper;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.daq.tools.processexceptions.ConfRejectedTypeException;
import cern.c2mon.daq.tools.processexceptions.ConfUnknownTypeException;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.config.EquipmentUnitAdd;
import cern.tim.shared.daq.config.EquipmentUnitRemove;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;

/**
 * This Kernel is the main class of the driver. It aggregates other classes specialized in taking individual actions,
 * like EquipmentMessageHandlers, ProcessRequestHandler, ProcessMessageSender. Try to keep Spring code out of this class
 * to keep it generic! (only @Service at the moment)
 */
public class DriverKernel implements ApplicationContextAware {

    /**
     * The driver's internal logger
     */
    private static final Logger LOGGER = Logger.getLogger(DriverKernel.class);

    /**
     * The name of the equipment message sender in the Spring context.
     */
    public static final String EQUIPMENT_MESSAGE_SENDER = "equipmentMessageSender";

    /**
     * The reference to the static ProcessMessageSender object
     */
    private ProcessMessageSender processMessageSender;

    /**
     * Reference to the static FilterMessageSender object
     */
    private JmsLifecycle filterMessageSender;

    /**
     * The reference to the primary ProcessRequestSender: the primary sender also requests the XML config at startup.
     */
    private ProcessRequestSender primaryRequestSender;

    /**
     * Ref to the secondary request sender, which only sends out disconnect notifications. !May be null!
     */
    private ProcessRequestSender secondaryRequestSender;

    /**
     * The reference to the ProcessMessageReceiver beans.
     */
    private List<ProcessMessageReceiver> processMessageReceivers = new ArrayList<ProcessMessageReceiver>();

    /**
     * This hashtable contains all registered EquipmentMessageHandlers
     */
    private final ConcurrentMap<Long, EquipmentMessageHandler> eqLookupTable = new ConcurrentHashMap<Long, EquipmentMessageHandler>();

    /**
     * The Kernel's Shutdown-hook (defines an action to be taken on kernel's termination)
     */
    private final DriverKernel.KernelShutdownHook ksh = new KernelShutdownHook();

    /**
     * The spring application context.
     */
    private ApplicationContext applicationContext;

    /**
     * The ConfigurationController managing the configuration life cycle and allows to access and change the current
     * configuration.
     */
    private ConfigurationController configurationController;

    /**
     * This controller is to forward request to different parts of the core.
     */
    private RequestController requestController;

    private EquipmentConfigurationFactory equipmentConfigurationFactory;

    /**
     * This class coordinates the shutdown of the DAQ. This involves, in the following order:
     * <ol>
     * <li>Stop listening for message from the server.
     * <li>Close the connection of the EMH to the equipment.
     * <li>Shutdown the JMS beans sending updates to the server.
     * <li>Shutdown the filter connection.
     * </ol>
     * <p>
     * Notice that the DAQ does not rely on the Spring shutdown hook to close down. This means all shutdown methods must
     * be called explicitly in the appropriate methods.
     */
    public class KernelShutdownHook extends Thread {
        /**
         * Creates a new KernelShutdownHook
         */
        public KernelShutdownHook() {
            super("KernelShutdownHook");
        }

        /**
         * The thread's run method
         */
        @SuppressWarnings("synthetic-access")
        public void run() {
            LOGGER.info("calling DAQ shutdown() ...");

            LOGGER.debug("Stopping DAQ alive timer.");
            processMessageSender.stopAliveTimer();
            
            LOGGER.debug("\tstopping listener for server commands/requests...");

            for (ProcessMessageReceiver receiver : processMessageReceivers) {
                receiver.shutdown();
            }

            LOGGER.debug("\tcalling ProcessRequestSender's sendProcessDisconnection()..");
            if (primaryRequestSender != null) {
              // If noPIK we call old Process Disconnection method. Else the new one
              if (configurationController.getRunOptions().isNoPIK()) {
                primaryRequestSender.old_sendProcessDisconnection();
              } else {
                primaryRequestSender.sendProcessDisconnectionRequest();
              }
            }
            
            //send in separate thread as may block if broker problem
            if (secondaryRequestSender != null) {
                Thread disconnectSend = new Thread(new Runnable() {                  
                  @Override
                  public void run() {
                    // If noPIK we call old Process Disconnection method. Else the new one
                    if (configurationController.getRunOptions().isNoPIK()) {
                      secondaryRequestSender.old_sendProcessDisconnection();
                    } else {
                      secondaryRequestSender.sendProcessDisconnectionRequest();
                    }
                  }
                });
                disconnectSend.setDaemon(true);
                disconnectSend.start();
            }

            LOGGER.debug("\tcalling disconnectFromDataSource interface of each of registered EMHs");
            Iterator<EquipmentMessageHandler> eqIt = getEquipmentMessageHandlersTable().values().iterator();
            while (eqIt.hasNext()) {
                EquipmentMessageHandler emhandler = eqIt.next();
                try {
                    emhandler.shutdown();
                } catch (EqIOException ex) {
                    LOGGER
                            .warn("a problem occured while calling disconnectFromDataSourc() of EquipmentMessageHandler id :"
                                    + emhandler.getEquipmentConfiguration().getId()
                                    + ", name :"
                                    + emhandler.getEquipmentConfiguration().getName());
                }
            }

            LOGGER.debug("\tdisconnecting FilterMessageSenders...");
            if (filterMessageSender != null)
                filterMessageSender.shutdown();
            LOGGER.debug("\tdisconnecting JmsSenders for tag update connection...");
            // ActiveMQ JmsSender also closes all the ActiveMQ JMS connections so keep as last
            if (processMessageSender != null)
                processMessageSender.shutdown();
            LOGGER.info("DAQ shutdown completed successfully");
        }
    }

    /**
     * Unique constructor used to construct the DriverKernel bean.
     * 
     * @param processMessageSender the ProcessMessageSender
     * @param filterMessageSender the FilterMessageSender
     * @param configurationController The configuration controller to access and manage the configuration.
     * @param processRequestSender the ProcessRequestSender
     * @param requestController The message handler which handles incoming messages.
     */
    @Autowired
    public DriverKernel(final ProcessMessageSender processMessageSender, final JmsLifecycle filterMessageSender,
            final ConfigurationController configurationController, final RequestController requestController,
            EquipmentConfigurationFactory equipmentConfigurationFactory) {
        super();
        this.processMessageSender = processMessageSender;
        this.filterMessageSender = filterMessageSender;
        this.configurationController = configurationController;
        this.requestController = requestController;
        this.equipmentConfigurationFactory = equipmentConfigurationFactory;

    }

    /**
     * Initialization of the DriverKernel at startup, once all dependencies have been set.
     * 
     * @throws ConfRejectedTypeException Throws a configuration rejected exception if the server rejected the
     *             configuration request of the DAQ.
     * @throws ConfUnknownTypeException Throws a configuration unknown type exception if the configuration requested was
     *             unknown on the server.
     */
    @PostConstruct
    public final void init() throws ConfUnknownTypeException, ConfRejectedTypeException {
        // was originally in main of Driver Kernel
        Runtime.getRuntime().addShutdownHook(this.ksh);
        configure();
    }

    /**
     * This method is responsible for all cleaning stuff that has to be taken before the DAQ terminates, such as e.g.
     * sending ProcessDisconnectionRequest message to the application server. It is currently only used in the DAQ test web
     * application (the kernel shutdown hook performs the same role for the usual runtime environment).
     */
    public void terminateDAQ() {
        for (ProcessMessageReceiver receiver : processMessageReceivers) {
            receiver.shutdown();
        }
        this.processMessageSender.shutdown();
        this.processMessageSender.closeSourceDataTagsBuffers();

        // disconnect the FilterMessageSender object from JMS
        // and perform and shutdown logic
        filterMessageSender.shutdown();

        // If noPIK we call old Process Disconnection method. Else the new one
        if (configurationController.getRunOptions().isNoPIK()) {
          this.primaryRequestSender.sendProcessDisconnectionRequest();
          if (secondaryRequestSender != null) {
            secondaryRequestSender.sendProcessDisconnectionRequest();
          }
        } else {
          this.primaryRequestSender.old_sendProcessDisconnection();
          if (secondaryRequestSender != null) {
            secondaryRequestSender.old_sendProcessDisconnection();
          }
        }

        LOGGER.info("terminateDAQ - Process terminated gently");
        System.exit(0);
    }
    
    /**
     * This method does all the driver's configuration job, basing on a DOM configuration XML document given as an
     * argument.
     * 
     * @throws ConfUnknownTypeException Thrown if the configuration type is unknown. That means that the server does not
     *             no the requested configuration.
     * @throws ConfRejectedTypeException Thrown if the configuration type indicates that the server rejected the startup
     *             request.
     */
    public void configure() throws ConfUnknownTypeException, ConfRejectedTypeException {
        LOGGER.debug("configure - entering configure()..");

        LOGGER.debug("configure - connecting to ProcessMessageSender");
        // create and initialize ProccessMessageSender
        // processMessageSender = new ProcessMessageSender(processConfiguration,
        // jndiMonitor);
        // try to connect establish ProcessMessageSender's connection
        processMessageSender.connect();

        LOGGER.debug("configure - connecting to FilterMessageSender");
        // create and initialize the FilterMessageSender

        // filterMessageSender = new FilterMessageSender(processConfiguration,
        // jndiMonitor);
        // connect the FilterMessageSender to JMS, using a separate thread
        // (to make sure DAQ starts, even if filter JMS broker is not reachable)
        FilterConnectorThread filterConnectorThread = new FilterConnectorThread(filterMessageSender);
        filterConnectorThread.start();

        EquipmentMessageHandler equnit = null;
        ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();
        boolean dynamicTimeDeadbandEnabled = !configurationController.getCommandParamsHandler().hasParam("-noDeadband");

        int eqUnitsConnectedProperly = 0;
        // for each equipment unit defined in the ProcessConfiguration XML
        for (EquipmentConfiguration conf : processConfiguration.getEquipmentConfigurations().values()) {
            long equipmentId = conf.getId();
            LOGGER.info("configure - Dynamic timedeadband enabled for equipment id: " + equipmentId + " enabled: "
                    + dynamicTimeDeadbandEnabled);
            conf.setDynamicTimeDeadbandEnabled(dynamicTimeDeadbandEnabled);
            EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(conf,
                    processConfiguration, configurationController.getRunOptions());

            EquipmentMessageSender equipmentMessageSender = (EquipmentMessageSender) applicationContext
                    .getBean(EQUIPMENT_MESSAGE_SENDER);
            equipmentMessageSender.setEquipmentConfiguration(conf);
            equipmentMessageSender.setEquipmentLoggerFactory(equipmentLoggerFactory);

            configurationController.addCoreDataTagChanger(equipmentId, equipmentMessageSender);
            try {
                validateDataTags(conf, equipmentMessageSender);
                validateCommandTags(conf, equipmentMessageSender);
                equnit = EquipmentMessageHandler.createEquipmentMessageHandler(conf.getHandlerClassName(),
                        new EquipmentCommandHandler(equipmentId, requestController), new EquipmentConfigurationHandler(
                                equipmentId, configurationController), equipmentMessageSender);
                equnit.setEquipmentLoggerFactory(equipmentLoggerFactory);
            } catch (InstantiationException e) {
                String msg = "Error while instantiating " + conf.getHandlerClassName();
                equipmentMessageSender.confirmEquipmentStateIncorrect(msg + ": " + e.getMessage());
                LOGGER.error(msg, e);
            } catch (IllegalAccessException e) {
                String msg = "Access error while calling constructor of " + conf.getHandlerClassName();  
                equipmentMessageSender.confirmEquipmentStateIncorrect("Error in code: " + msg);
                LOGGER.error(msg, e);
            } catch (ClassNotFoundException e) {
                String msg = "Handler class not found: " + conf.getHandlerClassName();
                equipmentMessageSender.confirmEquipmentStateIncorrect("Error during configuration: " + msg);
                LOGGER.error(msg, e);
            }
            if (equnit != null) {
                if (registerNewEquipmentUnit(equnit)) {
                    eqUnitsConnectedProperly++;
                }
            }
        } // for

        // try to establish ProcessMessageReceiver's JMS topic connection
        for (ProcessMessageReceiver receiver : processMessageReceivers) {
            receiver.connect();
        }

        // LOGGER.info("Number of supervised equipment units : " + eqLookupTable.size());
        LOGGER.info("configure - Number of equipment units configured properly : " + eqUnitsConnectedProperly);

        // start "ticking" of the ProcessMessageSender's AliveTimer
        // as soon as the first AliveTag arives to TIM server, the driver will
        // be
        // considered as operational
        LOGGER.debug("configure - Starting DAQ alive timer.");
        processMessageSender.startAliveTimer();

        LOGGER.info("configure - DAQ initialized and running.");
    }

    private boolean registerNewEquipmentUnit(final EquipmentMessageHandler eqHandler) {
        this.eqLookupTable.put(eqHandler.getEquipmentConfiguration().getId(), eqHandler);
        LOGGER.info("registerNewEquipmentUnit - Number of supervised equipment units : " + eqLookupTable.size());
        return startEquipmentMessageHandler(eqHandler);
    }

    private boolean unregisterEquipmentUnit(final Long eqId) {
        boolean stopStatus = stopEquipmentMessageHandler(eqId); 
        this.eqLookupTable.remove(eqId);
        
        // remove equipment configuratio from the process configuration object
        configurationController.getProcessConfiguration().removeEquipmentConfiguration(eqId);
        
        LOGGER.info("unregisterEquipmentUnit - Number of supervised equipment units : " + eqLookupTable.size());
        return stopStatus; 
    }

    /**
     * This will validate the data tags of this configuration and invalidate them via the equipment message sender if
     * necessary.
     * 
     * @param conf The configuration to use.
     * @param equipmentMessageSender The sender to use.
     */
    private void validateDataTags(final EquipmentConfiguration conf, final EquipmentMessageSender equipmentMessageSender) {
        Iterator<SourceDataTag> dataTagIterator = conf.getDataTags().values().iterator();
        while (dataTagIterator.hasNext()) {
            SourceDataTag sourceDataTag = dataTagIterator.next();
            try {
                LOGGER.debug("validateDataTags - validate DataTag " + sourceDataTag.getId());
                sourceDataTag.validate();
            } catch (ConfigurationException e) {
                LOGGER.error("Error validating configuration for DataTag " + sourceDataTag.getId(), e);
                equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, e
                        .getMessage(), null);
                dataTagIterator.remove();
            }
        }
    }

    /**
     * This will validate the command tags of this configuration and invalidate them via the equipment message sender if
     * necessary.
     * 
     * @param equipmentConfiguration The configuration to use.
     * @param equipmentMessageSender The sender to use. TODO Invalidation of command tags not possible. Find a solution.
     */
    private void validateCommandTags(final EquipmentConfiguration equipmentConfiguration,
            final EquipmentMessageSender equipmentMessageSender) {
        Iterator<SourceCommandTag> commandTagIterator = equipmentConfiguration.getCommandTags().values().iterator();
        while (commandTagIterator.hasNext()) {
            SourceCommandTag sourceDataTag = commandTagIterator.next();
            try {
                LOGGER.debug("validateCommandTags - validate DataTag " + sourceDataTag.getId());
                sourceDataTag.validate();
            } catch (ConfigurationException e) {
                LOGGER.error("Error validating configuration for CommandTag " + sourceDataTag.getId(), e);
                // equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                // e.getMessage(), null);
                commandTagIterator.remove();
            }
        }
    }

    /**
     * Starts the equipment message handler (connecting to data source and refreshing data tags).
     * 
     * @param equipmentMessageHandler The equipment message handler to start.
     * @return True if the start was successful else false.
     */
    private boolean startEquipmentMessageHandler(final EquipmentMessageHandler equipmentMessageHandler) {
        boolean success = true;
        IEquipmentConfiguration equipmentConfiguration = equipmentMessageHandler.getEquipmentConfiguration();
        try {
            equipmentMessageHandler.connectToDataSource();
            equipmentMessageHandler.refreshAllDataTags();                
        } catch (EqIOException ex) {
            LOGGER.error("startEquipmentMessageHandler - Could not connect EquipmentUnit to its data source. EquipmentMessageHandler name :"
                    + equipmentConfiguration.getName() + " id :" + equipmentConfiguration.getId());
            String errMsg = "EqIOException : code = " + ex.getErrorCode() + " message = " + ex.getErrorDescription();
            LOGGER.error(errMsg);
            equipmentMessageHandler.getEquipmentMessageSender().confirmEquipmentStateIncorrect(errMsg);
            success = false;
        } catch (Exception ex) {
            LOGGER.error("startEquipmentMessageHandler - Could not connect EquipmentUnit to its data source. EquipmentMessageHandler name :"
                    + equipmentMessageHandler.getEquipmentConfiguration().getName() + " id :"
                    + equipmentMessageHandler.getEquipmentConfiguration().getId());
            String errMsg = "Unexpected exception caught whilst connecting to equipment: ";
            LOGGER.error(errMsg, ex);
            equipmentMessageHandler.getEquipmentMessageSender().confirmEquipmentStateIncorrect(errMsg + ex.getMessage());
            success = false;
        }
        return success;
    }

    private boolean stopEquipmentMessageHandler(final Long eqId) {
        boolean success = true;

        EquipmentMessageHandler handler = this.eqLookupTable.get(eqId);
        if (handler == null) {
            success = false;
        } else {
            IEquipmentConfiguration conf = handler.getEquipmentConfiguration();
            try {
                handler.disconnectFromDataSource();
                // send commfault tag
                handler.getEquipmentMessageSender().confirmEquipmentStateIncorrect("Equipment has been been stopped");
            } catch (Exception ex) {
                LOGGER.warn("stopEquipmentMessageHandler - Could not discconnect EquipmentUnit from its data source. EquipmentMessageHandler name :"
                        + conf.getName() + " id :" + eqId);
            }
        }

        return success;
    }

    /**
     * This method returns a reference to the EquipmentUnits hashtable
     * 
     * @return java.util.Hashtable
     */
    public Map<Long, EquipmentMessageHandler> getEquipmentMessageHandlersTable() {
        return eqLookupTable;
    }

    /**
     * This method is responsible for changing the log4j's root logger treshold
     * 
     * @param level - the level to set
     */
    public void setRootLoggerLevel(final String level) {
        LOGGER.info("setRootLoggerLevel - changing current process logger's treshold to " + level);
        Level newLevel = stringToLog4jLevel(level);
        if (newLevel != null) {
            Logger.getRootLogger().setLevel(newLevel);
        } else {
            LOGGER.error("setRootLoggerLevel - could not change logging level to : " + level);
        }
    }

    /**
     * This method is responsible for changing the log4j's root logger treshold
     * 
     * @param eqID equipment unit unique identifier
     * @param eqName equipment unit name
     * @param level the level to set
     */
    public void setEqLoggerLevel(final Long eqID, final String eqName, final String level) {
        LOGGER.info("changing eqUnit\'s [" + eqID + "," + eqName + "] logger treshold to " + level);
        Level newLevel = stringToLog4jLevel(level);
        if (newLevel != null) {
            // TODO not perfect will override all loggers of this equipment
            EquipmentLoggerFactory.setLevel(eqName, newLevel);
        } else {
            LOGGER.error("setEqLoggerLevel - could not change logging level of logger [" + eqID + "," + eqName + "] to level : " + level);
        }
    }

    /**
     * This method converts string representation of the logging level to log4j's Level object
     * 
     * @param level the string representation of the logging level
     * @return The Level object or null if the String did not match a log4j level.
     */
    private Level stringToLog4jLevel(final String level) {
        Level result = null;

        if (level.equalsIgnoreCase("OFF"))
            result = Level.OFF;
        else if (level.equalsIgnoreCase("INFO"))
            result = Level.INFO;
        else if (level.equalsIgnoreCase("WARN"))
            result = Level.WARN;
        else if (level.equalsIgnoreCase("ERROR"))
            result = Level.ERROR;
        else if (level.equalsIgnoreCase("FATAL"))
            result = Level.FATAL;
        else if (level.equalsIgnoreCase("DEBUG"))
            result = Level.DEBUG;
        else if (level.equalsIgnoreCase("ALL"))
            result = Level.ALL;

        return result;
    }

    /**
     * Implementation of a spring interface to get the application context.
     * 
     * @param applicationContext The application context of this application.
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * @param processMessageReceivers the processMessageReceivers to set
     */
    public void setProcessMessageReceivers(final List<ProcessMessageReceiver> processMessageReceivers) {
        this.processMessageReceivers = processMessageReceivers;
    }

    /**
     * Sets the reference to the primary ProcessRequestSender: the primary sender also requests the XML config at
     * startup.
     * 
     * @param primaryRequestSender the primaryRequestSender to set
     */
    public void setPrimaryRequestSender(final ProcessRequestSender primaryRequestSender) {
        this.primaryRequestSender = primaryRequestSender;
    }

    /**
     * Ref to the secondary request sender, which only sends out disconnect notifications.
     * 
     * @param secondaryRequestSender the secondaryRequestSender to set
     */
    public void setSecondaryRequestSender(final ProcessRequestSender secondaryRequestSender) {
        this.secondaryRequestSender = secondaryRequestSender;
    }

    /**
     * Updates the DAQ by injecting new Equipment Unit
     * 
     * @param EquipmentUnitAdd The newly injected equipment unit
     * @return A change report with information about the success of the update.
     */

    public ChangeReport onEquipmentUnitAdd(final EquipmentUnitAdd equipmentUnitAdd) {
        LOGGER.debug("onEquipmentUnitAdd - entering onEquipmentUnitAdd()..");
        
        ChangeReport changeReport = new ChangeReport(equipmentUnitAdd);
        changeReport.setState(CHANGE_STATE.SUCCESS);

        ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();

        // // check if equipment unit with same id is not already registered
        if (processConfiguration.getEquipmentConfiguration(equipmentUnitAdd.getEquipmentId()) != null) {
            changeReport.appendError("onEquipmentUnitAdd - Equipment unit id: " + equipmentUnitAdd.getEquipmentId()
                    + " is already registered");
            changeReport.setState(CHANGE_STATE.FAIL);
            return changeReport;
        }

        EquipmentConfiguration conf = null;
        try {
            conf = equipmentConfigurationFactory.createEquipmentConfiguration(equipmentUnitAdd.getEquipmentUnitXml());
        } catch (Exception ex) {
            changeReport.setState(CHANGE_STATE.FAIL);
            changeReport.appendError(StackTraceHelper.getStackTrace(ex));
            return changeReport;
        }

        EquipmentMessageHandler equnit = null;

        boolean dynamicTimeDeadbandEnabled = !configurationController.getCommandParamsHandler().hasParam("-noDeadband");
        conf.setDynamicTimeDeadbandEnabled(dynamicTimeDeadbandEnabled);

        LOGGER.info("onEquipmentUnitAdd - Dynamic timedeadband enabled for equipment id: " + conf.getId() + " enabled: "
                + dynamicTimeDeadbandEnabled);

        EquipmentLoggerFactory equipmentLoggerFactory = EquipmentLoggerFactory.createFactory(conf,
                processConfiguration, configurationController.getRunOptions());

        EquipmentMessageSender equipmentMessageSender = (EquipmentMessageSender) applicationContext
                .getBean(EQUIPMENT_MESSAGE_SENDER);
        equipmentMessageSender.setEquipmentConfiguration(conf);
        equipmentMessageSender.setEquipmentLoggerFactory(equipmentLoggerFactory);

        configurationController.addCoreDataTagChanger(conf.getId(), equipmentMessageSender);

        try {
            validateDataTags(conf, equipmentMessageSender);
            validateCommandTags(conf, equipmentMessageSender);
            equnit = EquipmentMessageHandler.createEquipmentMessageHandler(conf.getHandlerClassName(),
                    new EquipmentCommandHandler(conf.getId(), requestController), new EquipmentConfigurationHandler(
                            conf.getId(), configurationController), equipmentMessageSender);
            equnit.setEquipmentLoggerFactory(equipmentLoggerFactory);

            // put the equipment configuration into the process configuration's map
            processConfiguration.addEquipmentConfiguration(conf);            
        } catch (InstantiationException e) {
          String msg = "Error while instantiating " + conf.getHandlerClassName();
          equipmentMessageSender.confirmEquipmentStateIncorrect(msg + ": " + e.getMessage());
          LOGGER.error(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Access error while calling constructor of " + conf.getHandlerClassName();  
            equipmentMessageSender.confirmEquipmentStateIncorrect("Error in code: " + msg);
            LOGGER.error(msg, e);
        } catch (ClassNotFoundException e) {
            String msg = "Handler class not found: " + conf.getHandlerClassName();
            equipmentMessageSender.confirmEquipmentStateIncorrect("Error during configuration: " + msg);
            LOGGER.error(msg, e);
        }
        
        if (equnit != null) {
            if (!registerNewEquipmentUnit(equnit)) {
                changeReport.setState(CHANGE_STATE.REBOOT);
                changeReport
                        .appendWarn("problem detected while registering new equipment. You need to restart the DAQ");
            }
        }

        return changeReport;
    }

    /**
     * Updates the DAQ by removing a whole EquipmentUnit
     * 
     * @param EquipmentUnitRemove The equipment unit to be removed
     * @return A change report with information about the success of the update.
     */
    public ChangeReport onEquipmentUnitRemove(final EquipmentUnitRemove equipmentUnitRemove) {
        LOGGER.debug("onEquipmentUnitRemove - entering onEquipmentUnitRemove()..");
        
        ChangeReport changeReport = new ChangeReport(equipmentUnitRemove);
        changeReport.setState(CHANGE_STATE.SUCCESS);

        ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();

        // check if equipment unit with same id is not already registered
        if (processConfiguration.getEquipmentConfiguration(equipmentUnitRemove.getEquipmentId()) == null) {
            changeReport.appendError("Equipment unit id: " + equipmentUnitRemove.getEquipmentId() + " is unknown");
            changeReport.setState(CHANGE_STATE.FAIL);
            return changeReport;
        }

        if (!unregisterEquipmentUnit(equipmentUnitRemove.getEquipmentId())) {
            changeReport.setState(CHANGE_STATE.REBOOT);
            changeReport.appendWarn("problem detected while unregistering equipment id: "
                    + equipmentUnitRemove.getEquipmentId() + ". You need to restart the DAQ");
        }

        return changeReport;
    }
}