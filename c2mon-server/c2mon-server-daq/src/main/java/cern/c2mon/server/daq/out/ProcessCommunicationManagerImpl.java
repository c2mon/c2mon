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
package cern.c2mon.server.daq.out;

import cern.c2mon.cache.actions.process.ProcessService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.daq.config.DaqProperties;
import cern.c2mon.shared.client.command.CommandExecutionStatus;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandReportImpl;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ChangeRequest;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;
import cern.c2mon.shared.daq.exception.ProcessRequestException;
import cern.c2mon.shared.daq.serialization.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.stereotype.Service;

import javax.jms.ConnectionFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;


/**
 * Implementation of the ProcessCommunicationManager using ActiveMQ middleware and Spring.
 */
@Service
public class ProcessCommunicationManagerImpl implements ProcessCommunicationManager, SmartLifecycle {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCommunicationManagerImpl.class);

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /**
   * Reference to the equipment cache.
   */
  private C2monCache<Equipment> equipmentCache;

  /**
   * Reference to process cache.
   */
  private C2monCache<Process> processCache;

  /**
   * Reference to process facade.
   */
  private ProcessService processService;

  /**
   * Reference to the bean managing the JMS
   * sending.
   */
  private JmsProcessOut jmsProcessOut;

  /**
   * Pooled JMS connection factory.
   */
  private ConnectionFactory processOutConnectionFactory;

  private DaqProperties properties;

  /**
   * Autowired constructor.
   *
   * @param equipmentCache
   * @param processService
   * @param jmsProcessOut
   */
  @Autowired
  public ProcessCommunicationManagerImpl(C2monCache<Equipment> equipmentCache,
                                         ProcessService processService,
                                         JmsProcessOut jmsProcessOut,
                                         @Qualifier("processOutConnectionFactory") ConnectionFactory connectionFactory,
                                         DaqProperties properties) {
    super();
    this.equipmentCache = equipmentCache;
    this.processCache = processService.getCache();
    this.processService = processService;
    this.jmsProcessOut = jmsProcessOut;
    this.processOutConnectionFactory = connectionFactory;
    this.properties = properties;
  }

  @Override
  public SourceDataTagValueResponse requestDataTagValues(final SourceDataTagValueRequest pRequest) throws ProcessRequestException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Requesting datatag values for " + pRequest.getType());
    }

    SourceDataTagValueResponse result = null;

    if (pRequest == null) {
      String errorMessage = "requestDataTagValues() : called with null parameter.";
      throw new ProcessRequestException(errorMessage);
    } else {
      try {
        //first retrieve process id of the request
        Long processId = null;
        if (pRequest.getType().equals(SourceDataTagValueRequest.DataTagRequestType.PROCESS)) {
          processId = pRequest.getId();
          LOGGER.debug("requestDataTagValues() for PROCESS " + processId);
        } else if (pRequest.getType().equals(SourceDataTagValueRequest.DataTagRequestType.EQUIPMENT)) {
          Long equipmentId = pRequest.getId();
          LOGGER.debug("requestDataTagValues() for EQUIPMENT " + equipmentId);

          try {
            Equipment equipment = equipmentCache.get(equipmentId);
            processId = equipment.getProcessId();
          } catch (CacheElementNotFoundException cacheEx) {
            String errorMessage = "Unable to treat data tag request.";
            throw new ProcessRequestException(errorMessage, cacheEx);
          }

        } else if (pRequest.getType().equals(SourceDataTagValueRequest.DataTagRequestType.DATATAG)) {
          LOGGER.debug("requestDataTagValues() for DATATAG " + pRequest.getId());
          String errorMessage = "requestDataTagValues() : request for individual tags currently not supported.";
          throw new ProcessRequestException(errorMessage);
        } else {
          String errorMessage = "SourceDataTagValueRequest type not recognized - unable to process it.";
          LOGGER.error(errorMessage);
          throw new ProcessRequestException(errorMessage);
        }

        //if managed to set the processId

        try {
          Process process = processCache.get(processId);
          if (processService.isRunning(process.getId())) {
            LOGGER.debug("requestDataTagValues() : associated process is running.");
            String reply = jmsProcessOut.sendTextMessage(MessageConverter.requestToJson(pRequest), getJmsDaqCommandQueue(process), 10000);

            if (reply != null) {
              LOGGER.debug("requestDataTagValues() : reply received: " + reply);
              result = MessageConverter.responseFromJson(reply, SourceDataTagValueResponse.class);
            } else {
              String errorMessage = "No response received for a SourceDataTagValueRequest (request timeout?)";
              throw new ProcessRequestException(errorMessage);
            }
          } else {
            String errorMessage = "requestDataTagValues() : Process " + processId + " is not running.";
            throw new ProcessRequestException(errorMessage);
          }
        } catch (CacheElementNotFoundException cacheEx) {
          String errorMessage = "Unable to process data tag request.";
          throw new ProcessRequestException(errorMessage, cacheEx);
        }

      } catch (Exception e) {
        String errorMessage = "requestDataTagValues() : Exception caught";
        LOGGER.error(errorMessage, e);
        throw new RuntimeException(errorMessage, e);
      }
    }
    return result;
  }

  @Override
  public <T> CommandReport executeCommand(final CommandTag<T> commandTag, final T value) {
    // Before attempting anything else, make sure none of the parameters is null
    if (commandTag == null) {
      LOGGER.warn("executeCommand() : called with null CommandTagHandler parameter.");
      throw new NullPointerException("executeCommand(..) method called with a null CommandTagHandle.");
    }
    if (value == null) {
      LOGGER.warn("executeCommand() : called with null value parameter.");
      throw new NullPointerException("executeCommand(..) method called with a null value parameter.");
    }
    LOGGER.debug("executeCommand() : called for command id " + commandTag.getId());

    CommandReport result = null;

    try {
      Process process = processCache.get(commandTag.getProcessId());
      if (processService.isRunning(process.getId())) {
        LOGGER.debug("executeCommand() : associated process is running.");

        // treat command
        SourceCommandTagValue val = new SourceCommandTagValue(commandTag.getId(), commandTag.getName(), commandTag.getEquipmentId(), commandTag.getMode(),
            value, commandTag.getDataType());
        String reply = jmsProcessOut.sendTextMessage(MessageConverter.requestToJson(val), getJmsDaqCommandQueue(process), commandTag.getExecTimeout());
        LOGGER.debug("executeCommand() : reply received: " + reply);
        if (reply != null) {
          try {
            SourceCommandTagReport report = MessageConverter.responseFromJson(reply, SourceCommandTagReport.class);
            switch (report.getStatus()) {
              case STATUS_OK:
                CommandReportImpl commandReport = new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_OK, report.getFullDescription());
                commandReport.setReturnValue(report.getReturnValue());
                return commandReport;

              case STATUS_TEST_OK:
                return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_NOT_EXECUTED, report.getFullDescription());

              default:
                return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_EXECUTION_FAILED, report.getFullDescription());
            }
          } catch (RuntimeException e) {
            return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_SERVER_ERROR,
                "Reply received from DAQ could not serialized");
          }
        } else {
          return new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_TIMED_OUT);
        }
      } else {
        LOGGER.warn("executeCommand() : Process is not running.");
        result = new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_PROCESS_DOWN, "The associated DAQ process is not running.");
      }
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("executeCommand() : Process (id=" + commandTag.getProcessId() + ") related to command tag (id="
          + commandTag.getId() + ") not found in cache.", cacheEx);
      result = new CommandReportImpl(commandTag.getId(), CommandExecutionStatus.STATUS_SERVER_ERROR, "Process related to command not found in cache.");
    } catch (Exception e) {
      LOGGER.error("executeCommand() : Exception", e);
    }
    return result;
  }

  @Override
  public ConfigurationChangeEventReport sendConfiguration(final Long processId, final List<Change> changeList)
      throws ParserConfigurationException, IllegalAccessException, InstantiationException,
      TransformerException, NoSuchFieldException, NoSimpleValueParseException {

    ChangeRequest request = new ChangeRequest(changeList);
    String configString = MessageConverter.requestToJson(request);
    int configurationTimeout = properties.getJms().getConfigurationTimeout();
    String reply = jmsProcessOut.sendTextMessage(configString, getJmsDaqCommandQueue(processCache.get(processId)), configurationTimeout);

    if (reply != null) {

      ConfigurationChangeEventReport report = MessageConverter.responseFromJson(reply, ConfigurationChangeEventReport.class);
      return report;
    } else {
      LOGGER.warn("Null reconfiguration reply received from DAQ - probably due to timeout, current set at " + configurationTimeout);
      //TODO create new default failure report for all changes in changeList... and return (changes only applied on server then)
      throw new RuntimeException("Null reconfiguration reply received from DAQ - probably due to timeout, current set at " + configurationTimeout);
    }
  }

  private String getJmsDaqCommandQueue(Process process) {
    String jmsDaqQueueTrunk = properties.getJms().getQueuePrefix();
    return jmsDaqQueueTrunk + ".command." + process.getCurrentHost() + "." + process.getName() + "." + process.getProcessPIK();
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable callback) {
    stop();
    callback.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    running = true;
  }

  @Override
  public void stop() {
    if (running) {
      running = false;
      try {
        LOGGER.debug("Shutting down ProcessCommunicationManager bean and associated JMS connections.");
        if (processOutConnectionFactory instanceof SingleConnectionFactory) {
          ((SingleConnectionFactory) processOutConnectionFactory).destroy();
        }
      } catch (Exception e) {
        LOGGER.error("Exception caught when trying to shutdown the server->DAQ JMS connections:", e);
        e.printStackTrace(System.err);
      }
    }
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_INTERMEDIATE;
  }

}
