/******************************************************************************
 * Copyright (C) 2010-2018 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.client.core.jms.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.*;

import javax.jms.JMSException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.JmsProxy;
import cern.c2mon.client.core.jms.RequestHandler;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.command.CommandExecuteRequest;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandTagHandle;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequest.RequestType;
import cern.c2mon.shared.client.request.ClientRequest.ResultType;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;

/**
 * Implementation of the RequestHandler bean.
 *
 * @see RequestHandler
 * @author Mark Brightwell
 *
 */
@Slf4j
@Service("coreRequestHandler")
public class RequestHandlerImpl implements RequestHandler {

  /**
   * The maximum number of tags in a single request. Each request runs in its
   * own thread on the server and is sent in a single JMS message.
   */
  @Setter
  private int maxRequestSize = 500;

  /**
   * Core/max number of threads in executor.
   */
  @Setter
  private int corePoolSize = 5;

  /**
   * Thread idle timeout in executor (in seconds), including core threads.
   */
  private static final long KEEP_ALIVE_TIME = 60;

  /**
   * Ref to JmsProxy bean.
   */
  protected final JmsProxy jmsProxy;

  /**
   * Name of main client request queue.
   */
  protected final String defaultRequestQueue;

  protected final String adminRequestQueue;
  
  /** Timeout of client request in milliseconds */
  private final int requestTimeout;

  /**
   * Executor for submitting requests to the server.
   */
  private ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, corePoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
      new LinkedBlockingDeque<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

  /**
   * Constructor.
   *
   * @param jmsProxy
   *          the proxy bean
   */
  @Autowired
  public RequestHandlerImpl(final JmsProxy jmsProxy, final C2monClientProperties properties) {
    this.jmsProxy = jmsProxy;
    this.defaultRequestQueue = properties.getJms().getRequestQueue();
    this.adminRequestQueue = properties.getJms().getAdminRequestQueue();
    this.maxRequestSize = properties.getMaxTagsPerRequest();
    this.corePoolSize = properties.getMaxRequestThreads();
    this.requestTimeout = properties.getJms().getRequestTimeout();
    log.warn("Request timeout SET TO {}", this.requestTimeout);
    executor.allowCoreThreadTimeOut(true);
  }


  @Override
  public Collection<SupervisionEvent> getCurrentSupervisionStatus() throws JMSException {
    ClientRequestImpl<SupervisionEvent> clientRequest = new ClientRequestImpl<>(SupervisionEvent.class, requestTimeout);
    return jmsProxy.sendRequest(clientRequest, defaultRequestQueue, clientRequest.getTimeout());
  }

  @Override
  public Collection<TagUpdate> requestTags(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTags(..) method called with null parameter.");
    }
    return executeRequest(tagIds, TagUpdate.class, null, defaultRequestQueue);
  }

  @Override
  public Collection<TagUpdate> requestTagsByRegex(final Collection<String> regexList) throws JMSException {
    if (regexList == null) {
      throw new NullPointerException("requestTags(..) method called with null parameter.");
    }
    return executeNameRequest(regexList, TagUpdate.class, null, defaultRequestQueue);
  }

  @Override
  public Collection<AlarmValue> requestAlarms(final Collection<Long> alarmIds) throws JMSException {
    if (alarmIds == null) {
      throw new NullPointerException("requestAlarms(..) method called with null parameter.");
    }
    return executeRequest(alarmIds, AlarmValue.class, null, defaultRequestQueue);
  }

  @Override
  public Collection<AlarmValue> requestAllActiveAlarms() throws JMSException {

    ClientRequestImpl<AlarmValue> activeAlarmsRequest = new ClientRequestImpl<>(
        ClientRequest.ResultType.TRANSFER_ACTIVE_ALARM_LIST,
          ClientRequest.RequestType.ACTIVE_ALARMS_REQUEST,
          60_000);

    return jmsProxy.sendRequest(activeAlarmsRequest, defaultRequestQueue, activeAlarmsRequest.getTimeout());
  }

  @Override
  public Collection<CommandTagHandle> requestCommandTagHandles(final Collection<Long> commandIds) {
    if (commandIds == null) {
      throw new NullPointerException("requestTags(..) method called with null parameter.");
    }
    return executeRequest(commandIds, CommandTagHandle.class, null, defaultRequestQueue);
  }

  @Override
  public ConfigurationReport applyConfiguration(final Long configurationId) {

    return applyConfiguration(configurationId, null);
  }

  @Override
  public ConfigurationReport applyConfiguration(final Long configurationId, final ClientRequestReportListener reportListener) {
    ArrayList<Long> ids = new ArrayList<>();
    ids.add(configurationId);

    Collection<ConfigurationReport> report = executeRequest(ids, ConfigurationReport.class, reportListener, adminRequestQueue);

    if (report.isEmpty()) {
      final String errorMsg = "applyConfiguration returned an empty Collection";
      log.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }
    if (report.size() > 1) {
      final String errorMsg = "applyConfiguration returned a Collection with more than 1 result";
      log.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }
    final ConfigurationReport receivedReport = report.iterator().next();
    if (receivedReport != null)
      log.trace("Received configuration report: {}", receivedReport.toXML());

    return receivedReport;
  }

  @Override
  public Collection<TagConfig> requestTagConfigurations(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTagConfigurations(..) method called with null parameter.");
    }
    return executeRequest(tagIds, TagConfig.class, null, defaultRequestQueue);
  }

  @Override
  public Collection<TagValueUpdate> requestTagValues(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTagValues(..) method called with null parameter.");
    }
    return executeRequest(tagIds, TagValueUpdate.class, null, defaultRequestQueue);
  }

  /**
   * Splits and executes a id-base request, splitting the collection into
   * smaller requests.
   *
   * @param <T>
   *          type of request result
   * @param ids
   *          collection of ids to request
   * @param clazz
   *          type of request result
   * @return the result of the request
   */
  private <T extends ClientRequestResult> Collection<T> executeRequest(
      final Collection<Long> ids, final Class<T> clazz, final ClientRequestReportListener reportListener, final String requestQueue) {

    log.debug("Initiating client request");
    ClientRequestImpl<T> clientRequest = new ClientRequestImpl<>(clazz, requestTimeout);
    Iterator<Long> it = ids.iterator();
    Collection<Future<Collection<T>>> results = new ArrayList<>();
    int counter = 0;
    while (it.hasNext()) {
      while (it.hasNext() && counter < maxRequestSize) {
        clientRequest.addTagId(it.next());
        counter++;
      }
      RequestValuesTask<T> task = new RequestValuesTask<>(clientRequest, reportListener, requestQueue);
      results.add(executor.submit(task));
      clientRequest = new ClientRequestImpl<>(clazz, requestTimeout);
      counter = 0;
    }
    Collection<T> finalCollection = new ArrayList<>();
    for (Future<Collection<T>> result : results) {
      try {
        finalCollection.addAll(result.get());
      } catch (InterruptedException e) {
        log.error("InterruptedException caught while executing RequestValuesTask", e);
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        log.error("ExecutionException caught while executing RequestValuesTask", e);
        throw new RuntimeException(e);
      }
    }
    log.debug("Client request completed");
    return finalCollection;
  }

  /**
   * Splits and executes a id-base request, splitting the collection into
   * smaller requests.
   *
   * @param <T>
   *          type of request result
   * @param regexList
   *          collection of names or regular expression to request
   * @param clazz
   *          type of request result
   * @return the result of the request
   */
  private <T extends ClientRequestResult> Collection<T> executeNameRequest(
      final Collection<String> regexList, final Class<T> clazz, final ClientRequestReportListener reportListener, final String requestQueue) {

    log.debug("Initiating client request");
    ClientRequestImpl<T> clientRequest = new ClientRequestImpl<>(clazz, requestTimeout);
    Iterator<String> it = regexList.iterator();
    Collection<Future<Collection<T>>> results = new ArrayList<>();
    int counter = 0;
    while (it.hasNext()) {
      while (it.hasNext() && counter < maxRequestSize) {
        clientRequest.addRegex(it.next());
        counter++;
      }
      RequestValuesTask<T> task = new RequestValuesTask<>(clientRequest, reportListener, requestQueue);
      results.add(executor.submit(task));
      clientRequest = new ClientRequestImpl<>(clazz, requestTimeout);
      counter = 0;
    }
    Collection<T> finalCollection = new ArrayList<>();
    for (Future<Collection<T>> result : results) {
      try {
        finalCollection.addAll(result.get());
      } catch (InterruptedException e) {
        log.error("InterruptedException caught while executing RequestValuesTask", e);
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        log.error("ExecutionException caught while executing RequestValuesTask", e);
        throw new RuntimeException(e);
      }
    }
    log.debug("Client request completed.");
    return finalCollection;
  }

  @Override
  public String getProcessXml(final String processName) throws JMSException {
    ClientRequestImpl<ProcessXmlResponse> xmlRequest = new ClientRequestImpl<>(ProcessXmlResponse.class, requestTimeout);
    xmlRequest.setRequestParameter(processName);
    // response should have a unique element in
    ProcessXmlResponse response = jmsProxy.sendRequest(xmlRequest, defaultRequestQueue, xmlRequest.getTimeout()).iterator().next();
    if (response.getProcessXML() != null) {
      return response.getProcessXML();
    } else {
      throw new RuntimeException(response.getErrorMessage());
    }
  }

  @Override
  public Collection<ProcessNameResponse> getProcessNames() throws JMSException {

    ClientRequestImpl<ProcessNameResponse> namesRequest = new ClientRequestImpl<>(ProcessNameResponse.class, requestTimeout);

    return jmsProxy.sendRequest(namesRequest, defaultRequestQueue, namesRequest.getTimeout());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> CommandReport executeCommand(final CommandExecuteRequest<T> commandExecuteRequest) throws JMSException {

    ClientRequestImpl clientRequest = new ClientRequestImpl<>(CommandReport.class, requestTimeout);
    clientRequest.setObjectParameter(commandExecuteRequest);

    Collection<CommandReport> c = jmsProxy.sendRequest(clientRequest, defaultRequestQueue, commandExecuteRequest.getTimeout());
    CommandReport report = c.iterator().next();

    return report;
  }

  @Override
  public Collection<ConfigurationReportHeader> getConfigurationReports() throws JMSException {

    ClientRequestImpl<ConfigurationReportHeader> clientRequest = new ClientRequestImpl<>(ResultType.TRANSFER_CONFIGURATION_REPORT_HEADER,
        RequestType.RETRIEVE_CONFIGURATION_REQUEST, 30_000);
    Collection<ConfigurationReportHeader> reports = jmsProxy.sendRequest(clientRequest, defaultRequestQueue, clientRequest.getTimeout());

    if (reports.isEmpty()) {
      log.warn("getConfigurationReports() returned an empty collection");
    }

    log.trace("getConfigurationReports(): Received {} configuration report headers", reports.size());
    return reports;
  }

  @Override
  public Collection<ConfigurationReport> getConfigurationReports(Long id) throws JMSException {

    ClientRequestImpl<ConfigurationReport> clientRequest = new ClientRequestImpl<>(ResultType.TRANSFER_CONFIGURATION_REPORT,
        RequestType.RETRIEVE_CONFIGURATION_REQUEST, 10_000);
    clientRequest.setRequestParameter(String.valueOf(id));
    Collection<ConfigurationReport> reports = jmsProxy.sendRequest(clientRequest, defaultRequestQueue, clientRequest.getTimeout());

    if (reports.isEmpty()) {
      log.warn("getConfigurationReports() returned an empty collection");
    }

    log.trace("getConfigurationReports(): Received {} reports for configuration {}", reports.size(), id);
    return reports;
  }

  @Override
  public TagStatisticsResponse requestTagStatistics() throws JMSException {
    ClientRequestImpl<TagStatisticsResponse> clientRequest = new ClientRequestImpl<>(TagStatisticsResponse.class, requestTimeout);
    Collection<TagStatisticsResponse> response = jmsProxy.sendRequest(clientRequest, defaultRequestQueue, clientRequest.getTimeout());
    return response.iterator().next();
  }

  /**
   * This task calls the JmsProxy with the passed request and returns the
   * requested collection.
   *
   * @author Mark Brightwell
   *
   */
  private class RequestValuesTask<T extends ClientRequestResult> implements Callable<Collection<T>> {

    /** The request. */
    private ClientRequestImpl<T> clientRequest;

    /** Receives updates for the progress of the request. */
    private ClientRequestReportListener reportListener;

    /** The queue to send the request to */
    private String requestQueue;

    /**
     * @param clientRequest The request.
     * @param reportListener Receives updates for the progress of the request.
     */
    public RequestValuesTask(final ClientRequestImpl<T> clientRequest,
                             final ClientRequestReportListener reportListener,
                             final String requestQueue) {
      this.clientRequest = clientRequest;
      this.reportListener = reportListener;
      this.requestQueue = requestQueue;
    }

    /**
     * @param clientRequest The request.
     */
    public RequestValuesTask(final ClientRequestImpl<T> clientRequest) {
      this.clientRequest = clientRequest;
      this.reportListener = null; // can be null in case we don't care about the progress of the request.
    }

    @Override
    public Collection<T> call() throws Exception {
      return jmsProxy.sendRequest(clientRequest, requestQueue, clientRequest.getTimeout(), reportListener);
    }
  }

}
