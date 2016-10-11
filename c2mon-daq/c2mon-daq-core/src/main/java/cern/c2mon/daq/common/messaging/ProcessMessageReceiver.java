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

package cern.c2mon.daq.common.messaging;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.common.messaging.impl.RequestController;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.*;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;
import cern.c2mon.shared.daq.messaging.DAQResponse;

/**
 * The ProcessMessageReceiver is responsible for listening to incoming messages
 * from the server, initiating the required action and sending a response to the
 * server. Common behavior is implemented in this abstract class. Different
 * implementations can be provided for different JMS connections. Only one can
 * be used at a time and it is specified in the Spring XML configuration file
 * daq-core-service.xml.
 *
 * @author mbrightw
 */
@Slf4j
public abstract class ProcessMessageReceiver {

  /**
   * Reconfiguration logger to log the reconfiguration separately.
   */
  private static final Logger RECONF_LOGGER = LoggerFactory.getLogger("ReconfigurationLogger");

  /**
   * Lock used across the DAQ to prevent multiple requests from being handled at
   * once (in particular when receiving commands through both Sonic and
   * ActiveMQ).
   */
  private static ReentrantReadWriteLock requestLock = new ReentrantReadWriteLock();

  /**
   * Reference to the DriverKernel
   */
  @Setter
  private DriverKernel kernel;

  /**
   * The RequestController delivers the request from the server to the right
   * parts of the core and the equipment.
   */
  @Setter
  private RequestController requestController;

  /**
   * Sends a {@link  DAQResponse} to the server.
   *
   * @param response The response with the report for the server.
   * @param destination The JMS destination.
   * @param session The JMS session..
   */
  public abstract void sendDAQResponse(final DAQResponse response, final Destination destination, final Session session)
      throws JMSException;

  /**
   * Stop listening for requests from the server.
   * <p>
   * Also gently close any connection this object has to the message brokers
   * (except shared connections in the case of the ActiveMQ implementation,
   * where the shared connections are closed by the JmsSender implementation).
   */
  public abstract void disconnect();

  /**
   * Perform final shutdown of this message receiver (no prior disconnect call
   * is needed).
   */
  public abstract void shutdown();

  /**
   * Connect to the message broker and start listening for requests from the
   * server.
   */
  public abstract void connect();

  /**
   * This method is called each time ProcessMessageReceiver receives
   * MSG_RECONFIGURE_PROCESS message
   *
   * @param request         request with all information for the changes
   */
  public final ConfigurationChangeEventReport onReconfigureProcess(ChangeRequest request) {
    requestLock.writeLock().lock();
    log.debug("entering onReconfigureProcess()..");
    ConfigurationChangeEventReport configurationReport = new ConfigurationChangeEventReport();
    List<Change> changes = request.getChangeList();
    log.debug("got " + changes.size() + " reconfiguration events");

    if (changes != null) {
      for (Change change : changes) {
        ChangeReport changeReport = null;

        if (change instanceof EquipmentUnitAdd) {
          changeReport = kernel.onEquipmentUnitAdd((EquipmentUnitAdd) change);
        } else if (change instanceof EquipmentUnitRemove) {
          changeReport = kernel.onEquipmentUnitRemove((EquipmentUnitRemove) change);
        } else {
          log.debug("onReconfigureProcess - applyChange");
          changeReport = requestController.applyChange(change);
        }

        configurationReport.appendChangeReport(changeReport);
      }
    }

    RECONF_LOGGER.info(configurationReport.toString());

    requestLock.writeLock().unlock();
    return configurationReport;
  }

  /**
   * New implementation that does not assume we are using a unique session.
   *
   * @param sourceDataTagValueRequest     request object with all information for handling.
   * @throws JMSException May throw a JMS exception.
   */
  public DAQResponse onSourceDataTagValueUpdateRequest(SourceDataTagValueRequest sourceDataTagValueRequest) {
    requestLock.writeLock().lock();
    log.debug("entering onSourceDataTagValueUpdateRequest()... with session info");
    log.debug("received SourceDataTagValueRequest:\n" + sourceDataTagValueRequest);

    SourceDataTagValueResponse sourceDataTagValueResponse = requestController.onSourceDataTagValueUpdateRequest(sourceDataTagValueRequest);

    log.debug("leaving onSourceDataTagValueUpdateRequest()");
    requestLock.writeLock().unlock();
    return sourceDataTagValueResponse;
  }

  // TODO in shared change Topic to Destination

  /**
   * This method is called each time ProcessMessageReceiver receives
   * MSG_EXECUTE_COMMAND message
   *
   * @param sourceCommandTagValue         request object with all information for handling
   */
  public SourceCommandTagReport onExecuteCommand(SourceCommandTagValue sourceCommandTagValue) {
    requestLock.writeLock().lock();
    log.debug("entering onExecuteCommand()..");
    sourceCommandTagValue.log();

    log.debug("received CommandTag:\n" + sourceCommandTagValue);

    // this.kernel.executeCommand(sctv);
    SourceCommandTagReport sourceCommandTagReport = requestController.executeCommand(sourceCommandTagValue);

    if (sourceCommandTagReport != null) {
      sourceCommandTagReport.log();
    }

    log.debug("leaving onExecuteCommand()");
    requestLock.writeLock().unlock();
    return sourceCommandTagReport;
  }
}
