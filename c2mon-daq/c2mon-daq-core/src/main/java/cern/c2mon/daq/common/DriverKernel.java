/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.daq.common;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationFactory;
import cern.c2mon.daq.common.conf.core.EquipmentConfigurationHandler;
import cern.c2mon.daq.common.impl.EquipmentCommandHandler;
import cern.c2mon.daq.common.impl.EquipmentMessageSender;
import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.c2mon.daq.common.messaging.ProcessRequestSender;
import cern.c2mon.daq.common.messaging.impl.ProcessMessageSender;
import cern.c2mon.daq.common.messaging.impl.RequestController;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.daq.filter.FilterConnectorThread;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.tools.StackTraceHelper;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.command.SourceCommandTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagQualityCode;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.c2mon.shared.daq.config.EquipmentUnitAdd;
import cern.c2mon.shared.daq.config.EquipmentUnitRemove;

/**
 * This Kernel is the main class of the daq. It aggregates other classes
 * specialized in taking individual actions, like EquipmentMessageHandlers,
 * ProcessRequestHandler, ProcessMessageSender. Try to keep Spring code out of
 * this class to keep it generic! (only @Service at the moment)
 */
@Component
@Slf4j
public class DriverKernel implements ApplicationContextAware {

  /**
   * The name of the equipment message sender in the Spring context.
   */
  public static final String EQUIPMENT_MESSAGE_SENDER = "equipmentMessageSender";

  @Autowired
  private DaqProperties properties;

  /**
   * The reference to the static ProcessMessageSender object
   */
  @Autowired
  @Setter
  private ProcessMessageSender processMessageSender;

  /**
   * Reference to the static FilterMessageSender object
   */
  @Autowired
  private IFilterMessageSender filterMessageSender;

  /**
   * The reference to the primary ProcessRequestSender: the primary sender also
   * requests the XML config at startup.
   */
  @Autowired
  @Qualifier("primaryRequestSender")
  private ProcessRequestSender primaryRequestSender;

  /**
   * Ref to the secondary request sender, which only sends out disconnect
   * notifications. !May be null!
   */
  @Autowired(required = false)
  @Qualifier("secondaryRequestSender")
  private ProcessRequestSender secondaryRequestSender;

  /**
   * The reference to the ProcessMessageReceiver beans.
   */
  @Autowired
  private ProcessMessageReceiver processMessageReceiver;

  /**
   * This hashtable contains all registered EquipmentMessageHandlers
   */
  private final ConcurrentMap<Long, EquipmentMessageHandler> eqLookupTable = new ConcurrentHashMap<>();

  /**
   * The Kernel's Shutdown-hook (defines an action to be taken on kernel's
   * termination)
   */
  private final DriverKernel.KernelShutdownHook ksh = new KernelShutdownHook();

  /**
   * The spring application context.
   */
  private ApplicationContext applicationContext;

  /**
   * The ConfigurationController managing the configuration life cycle and
   * allows to access and change the current configuration.
   */
  @Autowired
  private ConfigurationController configurationController;

  /**
   * This controller is to forward request to different parts of the core.
   */
  @Autowired
  private RequestController requestController;

  @Autowired
  private EquipmentConfigurationFactory equipmentConfigurationFactory;

  /**
   * This class coordinates the shutdown of the DAQ. This involves, in the
   * following order:
   * <ol>
   * <li>Stop listening for message from the server.
   * <li>Close the connection of the EMH to the equipment.
   * <li>Shutdown the JMS beans sending updates to the server.
   * <li>Shutdown the filter connection.
   * </ol>
   * <p>
   * Notice that the DAQ does not rely on the Spring shutdown hook to close
   * down. This means all shutdown methods must be called explicitly in the
   * appropriate methods.
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
    @Override
    public void run() {
      doShutdown();
    }
  }

  private synchronized void doShutdown() {
    log.debug("Stopping DAQ alive timer.");
    processMessageSender.stopAliveTimer();

    log.debug("\tstopping listener for server commands/requests...");

    processMessageReceiver.shutdown();

    log.debug("\tcalling ProcessRequestSender's sendProcessDisconnection()..");
    if (primaryRequestSender != null) {
      primaryRequestSender.sendProcessDisconnectionRequest(configurationController.getProcessConfiguration(), configurationController.getStartUp());
    }

    // send in separate thread as may block if broker problem
    if (secondaryRequestSender != null) {
      Thread disconnectSend = new Thread(new Runnable() {
        @Override
        public void run() {
          secondaryRequestSender.sendProcessDisconnectionRequest(configurationController.getProcessConfiguration(), configurationController.getStartUp());
        }
      });
      disconnectSend.setDaemon(true);
      disconnectSend.start();
    }

    log.debug("\tcalling disconnectFromDataSource interface of each of registered EMHs");
    Iterator<EquipmentMessageHandler> eqIt = getEquipmentMessageHandlersTable().values().iterator();
    while (eqIt.hasNext()) {
      EquipmentMessageHandler emhandler = eqIt.next();
      try {
        emhandler.shutdown();
      } catch (EqIOException ex) {
        log.warn("a problem occured while calling disconnectFromDataSourc() of EquipmentMessageHandler id: {}",
                emhandler.getEquipmentConfiguration()
            .getId() + ", name :" + emhandler.getEquipmentConfiguration().getName());
      }
    }

    log.debug("\tdisconnecting FilterMessageSenders...");
    if (filterMessageSender != null) filterMessageSender.shutdown();
    log.debug("\tdisconnecting JmsSenders for tag update connection...");
    // ActiveMQ JmsSender also closes all the ActiveMQ JMS connections so keep
    // as last
    if (processMessageSender != null) processMessageSender.shutdown();
    log.info("DAQ shutdownInternal completed successfully");
  }

  public void shutdown(){
    doShutdown();
    Runtime.getRuntime().removeShutdownHook(this.ksh);
  }

  /**
   * Initialization of the DriverKernel at startup, once all dependencies have
   * been set.
   */
  public final void init()  {
    // was originally in main of Driver Kernel
    Runtime.getRuntime().addShutdownHook(this.ksh);

    processMessageReceiver.setKernel(this);
    processMessageReceiver.setRequestController(requestController);

    configure();
  }

  /**
   * This method is responsible for all cleaning stuff that has to be taken
   * before the DAQ terminates, such as e.g. sending ProcessDisconnectionRequest
   * message to the application server. It is currently only used in the DAQ
   * test web application (the kernel shutdown hook performs the same role for
   * the usual runtime environment).
   */
  public void terminateDAQ() {
    processMessageReceiver.shutdown();
    this.processMessageSender.shutdown();

    // disconnect the FilterMessageSender object from JMS
    // and perform and shutdown logic
    filterMessageSender.shutdown();

    this.primaryRequestSender.sendProcessDisconnectionRequest(configurationController.getProcessConfiguration(), configurationController.getStartUp());
    if (secondaryRequestSender != null) {
      secondaryRequestSender.sendProcessDisconnectionRequest(configurationController.getProcessConfiguration(), configurationController.getStartUp());
    }

    log.info("terminateDAQ - Process terminated gently");
    System.exit(0);
  }

  /**
   * This method does all the daq's configuration job, basing on a DOM
   * configuration XML document given as an argument.
   */
  public void configure() {
    log.debug("configure - entering configure()..");
    configurationController.initProcess();

    log.debug("configure - connecting to ProcessMessageSender");
    // create and initialize ProccessMessageSender
    // processMessageSender = new ProcessMessageSender(processConfiguration,
    // jndiMonitor);
    // try to connect establish ProcessMessageSender's connection
    processMessageSender.init();
    processMessageSender.connect();

    log.debug("configure - connecting to FilterMessageSender");
    // create and initialize the FilterMessageSender

    // filterMessageSender = new FilterMessageSender(processConfiguration,
    // jndiMonitor);
    // connect the FilterMessageSender to JMS, using a separate thread
    // (to make sure DAQ starts, even if filter JMS broker is not reachable)
    FilterConnectorThread filterConnectorThread = new FilterConnectorThread(filterMessageSender);
    filterConnectorThread.start();

    // Start "ticking" of the ProcessMessageSender's AliveTimer. As soon as the
    // first AliveTag arrives to TIM server, the DAQ will be considered as
    // operational.
    log.debug("configure - Starting DAQ alive timer.");
    processMessageSender.startAliveTimer();

    ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();

    AtomicInteger eqUnitsConnectedProperly = new AtomicInteger(0);
    // for each equipment unit defined in the ProcessConfiguration XML
    for (EquipmentConfiguration conf : processConfiguration.getEquipmentConfigurations().values()) {
      long equipmentId = conf.getId();
      String equipmentUnitName = conf.getName();

      Thread equipmentThread = new Thread(() -> configureEquipment(equipmentId, conf, eqUnitsConnectedProperly), equipmentUnitName);
      equipmentThread.start();

      try {
        equipmentThread.join();
      } catch (InterruptedException e) {
        log.error("There was a problem while running a thread", e);
      }
    } // for

    // try to establish ProcessMessageReceiver's JMS topic connection
    processMessageReceiver.init();
    processMessageReceiver.connect();

    log.info("configure - Number of equipment units configured properly : {} out of {}", eqUnitsConnectedProperly, processConfiguration.getEquipmentConfigurations().size());
    log.info("configure - DAQ initialized and running.");
  }

  /**
   * Configures and creates new {@link EquipmentMessageHandler} for an equipment unit
   *
   * @param equipmentId id of equipment unit
   * @param conf equipment configuration
   * @param eqUnitsConnectedProperly keeps in log information how many equipment units have been configured properly
   */
  private void configureEquipment(long equipmentId, EquipmentConfiguration conf, AtomicInteger eqUnitsConnectedProperly) {
    EquipmentMessageHandler equnit = null;

    boolean dynamicTimeDeadbandEnabled = properties.getFilter().getDynamicDeadband().isEnabled();

    log.info("configure - Dynamic timedeadband enabled for equipment id: {} enabled {}", equipmentId, dynamicTimeDeadbandEnabled);
    conf.setDynamicTimeDeadbandEnabled(dynamicTimeDeadbandEnabled);

    EquipmentMessageSender equipmentMessageSender = (EquipmentMessageSender) applicationContext.getBean(EQUIPMENT_MESSAGE_SENDER);
    equipmentMessageSender.init(conf);

    configurationController.addCoreDataTagChanger(equipmentId, equipmentMessageSender);
    try {
      validateDataTags(conf, equipmentMessageSender);
      validateCommandTags(conf, equipmentMessageSender);
      equnit = EquipmentMessageHandler.createEquipmentMessageHandler(conf.getHandlerClassName(),
              new EquipmentCommandHandler(equipmentId,
              requestController), new EquipmentConfigurationHandler(equipmentId, configurationController),
              equipmentMessageSender, applicationContext);

    } catch (InstantiationException e) {
      String msg = "Error while instantiating " + conf.getHandlerClassName();
      equipmentMessageSender.confirmEquipmentStateIncorrect(msg + ": " + e.getMessage());
      log.error(msg, e);
    } catch (IllegalAccessException e) {
      String msg = "Access error while calling constructor of " + conf.getHandlerClassName();
      equipmentMessageSender.confirmEquipmentStateIncorrect("Error in code: " + msg);
      log.error(msg, e);
    } catch (ClassNotFoundException e) {
      String msg = "Handler class not found: " + conf.getHandlerClassName();
      equipmentMessageSender.confirmEquipmentStateIncorrect("Error during configuration: " + msg);
      log.error(msg, e);
    }
    if (equnit != null) {
      if (registerNewEquipmentUnit(equnit)) {
        eqUnitsConnectedProperly.incrementAndGet();
      }
    }
  }

  private boolean registerNewEquipmentUnit(final EquipmentMessageHandler eqHandler) {
    this.eqLookupTable.put(eqHandler.getEquipmentConfiguration().getId(), eqHandler);
    log.info("registerNewEquipmentUnit - Number of supervised equipment units : {}", eqLookupTable.size());
    return startEquipmentMessageHandler(eqHandler);
  }

  private boolean unregisterEquipmentUnit(final Long eqId) {
    boolean stopStatus = stopEquipmentMessageHandler(eqId);
    this.eqLookupTable.remove(eqId);

    // remove equipment configuratio from the process configuration object
    configurationController.getProcessConfiguration().removeEquipmentConfiguration(eqId);

    log.info("unregisterEquipmentUnit - Number of supervised equipment units : {}", eqLookupTable.size());
    return stopStatus;
  }

  /**
   * This will validate the data tags of this configuration and invalidate them
   * via the equipment message sender if necessary.
   *
   * @param conf                   The configuration to use.
   * @param equipmentMessageSender The sender to use.
   */
  private void validateDataTags(final EquipmentConfiguration conf, final EquipmentMessageSender equipmentMessageSender) {
    for (SourceDataTag sourceDataTag : conf.getDataTags().values()) {
      try {
        log.debug("Validating data tag #{}", sourceDataTag.getId());
        sourceDataTag.validate();
      } catch (ConfigurationException e) {
        log.error("Error validating configuration for DataTag {}", sourceDataTag.getId(), e);
        SourceDataTagQuality quality = new SourceDataTagQuality(SourceDataTagQualityCode.INCORRECT_NATIVE_ADDRESS, e.getMessage());
        equipmentMessageSender.update(sourceDataTag.getId(), quality);
      }
    }
  }

  /**
   * This will validate the command tags of this configuration and invalidate
   * them via the equipment message sender if necessary.
   *
   * @param equipmentConfiguration The configuration to use.
   * @param equipmentMessageSender The sender to use. TODO Invalidation of
   *                               command tags not possible. Find a solution.
   */
  private void validateCommandTags(final EquipmentConfiguration equipmentConfiguration, final EquipmentMessageSender equipmentMessageSender) {
    Iterator<SourceCommandTag> commandTagIterator = equipmentConfiguration.getCommandTags().values().iterator();
    while (commandTagIterator.hasNext()) {
      SourceCommandTag sourceCommandTag = commandTagIterator.next();
      try {
        log.debug("Validating command tag #{}", sourceCommandTag.getId());
        sourceCommandTag.validate();
      } catch (ConfigurationException e) {
        log.error("Error validating configuration for CommandTag {}", sourceCommandTag.getId(), e);
        commandTagIterator.remove();
      }
    }
  }

  /**
   * Starts the equipment message handler (connecting to data source and
   * refreshing data tags).
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
      log.error("startEquipmentMessageHandler - Could not connect EquipmentUnit to its data source. EquipmentMessageHandler name: {} id: {}",
              equipmentConfiguration.getName(), equipmentConfiguration.getId());
      String errMsg = "EqIOException : code = " + ex.getErrorCode() + " message = " + ex.getErrorDescription();
      log.error(errMsg);
      equipmentMessageHandler.getEquipmentMessageSender().confirmEquipmentStateIncorrect(errMsg);
      success = false;
    } catch (Exception ex) {
      log.error("startEquipmentMessageHandler - Could not connect EquipmentUnit to its data source. EquipmentMessageHandler name: {} id: {}",
              equipmentMessageHandler.getEquipmentConfiguration().getName(), equipmentMessageHandler.getEquipmentConfiguration().getId());
      String errMsg = "Unexpected exception caught whilst connecting to equipment: ";
      log.error(errMsg, ex);
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
        log.warn("stopEquipmentMessageHandler - Could not discconnect EquipmentUnit from its data source. EquipmentMessageHandler name : {} id : {}",
                conf.getName(), eqId);
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
   * Implementation of a spring interface to get the application context.
   *
   * @param applicationContext The application context of this application.
   */
  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Sets the reference to the primary ProcessRequestSender: the primary sender
   * also requests the XML config at startup.
   *
   * @param primaryRequestSender the primaryRequestSender to set
   */
  public void setPrimaryRequestSender(final ProcessRequestSender primaryRequestSender) {
    this.primaryRequestSender = primaryRequestSender;
  }

  /**
   * Ref to the secondary request sender, which only sends out disconnect
   * notifications.
   *
   * @param secondaryRequestSender the secondaryRequestSender to set
   */
  public void setSecondaryRequestSender(final ProcessRequestSender secondaryRequestSender) {
    this.secondaryRequestSender = secondaryRequestSender;
  }

  /**
   * Updates the DAQ by injecting new Equipment Unit
   *
   * @param equipmentUnitAdd The newly injected equipment unit
   * @return A change report with information about the success of the update.
   */
  public ChangeReport onEquipmentUnitAdd(final EquipmentUnitAdd equipmentUnitAdd) {
    log.debug("onEquipmentUnitAdd - entering onEquipmentUnitAdd()..");

    ChangeReport changeReport = new ChangeReport(equipmentUnitAdd);
    changeReport.setState(CHANGE_STATE.SUCCESS);

    ProcessConfiguration processConfiguration = configurationController.getProcessConfiguration();

    // // check if equipment unit with same id is not already registered
    if (processConfiguration.getEquipmentConfiguration(equipmentUnitAdd.getEquipmentId()) != null) {
      changeReport.appendError("onEquipmentUnitAdd - Equipment unit id: " + equipmentUnitAdd.getEquipmentId() + " is already registered");
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

    boolean dynamicTimeDeadbandEnabled = properties.getFilter().getDynamicDeadband().isEnabled();
    conf.setDynamicTimeDeadbandEnabled(dynamicTimeDeadbandEnabled);

    log.info("onEquipmentUnitAdd - Dynamic timedeadband enabled for equipment id: {} enabled: {}", conf.getId(), dynamicTimeDeadbandEnabled);

    EquipmentMessageSender equipmentMessageSender = (EquipmentMessageSender) applicationContext.getBean(EQUIPMENT_MESSAGE_SENDER);

    equipmentMessageSender.init(conf);

    configurationController.addCoreDataTagChanger(conf.getId(), equipmentMessageSender);

    try {
      validateDataTags(conf, equipmentMessageSender);
      validateCommandTags(conf, equipmentMessageSender);
      equnit = EquipmentMessageHandler.createEquipmentMessageHandler(conf.getHandlerClassName(), new EquipmentCommandHandler(conf.getId(), requestController)
          , new EquipmentConfigurationHandler(conf.getId(), configurationController), equipmentMessageSender, applicationContext);

      // put the equipment configuration into the process configuration's map
      processConfiguration.addEquipmentConfiguration(conf);
    } catch (InstantiationException e) {
      String msg = "Error while instantiating " + conf.getHandlerClassName();
      equipmentMessageSender.confirmEquipmentStateIncorrect(msg + ": " + e.getMessage());
      log.error(msg, e);
    } catch (IllegalAccessException e) {
      String msg = "Access error while calling constructor of " + conf.getHandlerClassName();
      equipmentMessageSender.confirmEquipmentStateIncorrect("Error in code: " + msg);
      log.error(msg, e);
    } catch (ClassNotFoundException e) {
      String msg = "Handler class not found: " + conf.getHandlerClassName();
      equipmentMessageSender.confirmEquipmentStateIncorrect("Error during configuration: " + msg);
      log.error(msg, e);
    }

    if (equnit != null) {
      if (!registerNewEquipmentUnit(equnit)) {
        changeReport.setState(CHANGE_STATE.REBOOT);
        changeReport.appendWarn("problem detected while registering new equipment. You need to restart the DAQ");
      }
    }

    return changeReport;
  }

  /**
   * Updates the DAQ by removing a whole EquipmentUnit
   *
   * @param equipmentUnitRemove The equipment unit to be removed
   * @return A change report with information about the success of the update.
   */
  public ChangeReport onEquipmentUnitRemove(final EquipmentUnitRemove equipmentUnitRemove) {
    log.debug("onEquipmentUnitRemove - entering onEquipmentUnitRemove()..");

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
      changeReport.appendWarn("problem detected while unregistering equipment id: " + equipmentUnitRemove.getEquipmentId() + ". You need to restart the DAQ");
    }

    return changeReport;
  }
}
