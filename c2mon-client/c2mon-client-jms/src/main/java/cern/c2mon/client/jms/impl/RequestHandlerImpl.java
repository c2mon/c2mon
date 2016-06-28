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
package cern.c2mon.client.jms.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.listener.ClientRequestReportListener;
import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
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
 * @see cern.c2mon.client.jms.RequestHandler
 * @author Mark Brightwell
 *
 */
@Service("coreRequestHandler")
public class RequestHandlerImpl implements RequestHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandlerImpl.class);

  /**
   * The maximum number of tags in a single request. Each request runs in its
   * own thread on the server and is sent in a single JMS message.
   */
  @Value("${c2mon.client.request.size}")
  private int maxRequestSize = 500;

  /**
   * Core/max number of threads in executor.
   */
  @Value("${c2mon.client.request.threads.max}")
  private int corePoolSize = 5;

  /**
   * Thread idle timeout in executor (in seconds), including core threads.
   */
  private static final long KEEP_ALIVE_TIME = 60;

  /**
   * Ref to JmsProxy bean.
   */
  protected JmsProxy jmsProxy;

  /**
   * Name of main client request queue.
   */
  @Value("${c2mon.client.jms.request.queue}")
  protected String defaultRequestQueue;

  @Value("${c2mon.client.jms.admin.queue}")
  private String adminRequestQueue;

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
  public RequestHandlerImpl(final JmsProxy jmsProxy) {
    super();
    this.jmsProxy = jmsProxy;
    executor.allowCoreThreadTimeOut(true);
  }


  @Override
  public Collection<SupervisionEvent> getCurrentSupervisionStatus() throws JMSException {
    ClientRequestImpl<SupervisionEvent> clientRequest = new ClientRequestImpl<SupervisionEvent>(SupervisionEvent.class);
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

    ClientRequestImpl<AlarmValue> activeAlarmsRequest = new ClientRequestImpl<AlarmValue>(
        ClientRequest.ResultType.TRANSFER_ACTIVE_ALARM_LIST,
          ClientRequest.RequestType.ACTIVE_ALARMS_REQUEST,
          60000); // == timeout

    Collection<AlarmValue> c = jmsProxy.sendRequest(activeAlarmsRequest, defaultRequestQueue,
        activeAlarmsRequest.getTimeout());

    return c;
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
    ArrayList<Long> ids = new ArrayList<Long>();
    ids.add(configurationId);

    Collection<ConfigurationReport> report = executeRequest(ids, ConfigurationReport.class, reportListener, adminRequestQueue);

    if (report.isEmpty()) {
      final String errorMsg = "applyConfiguration returned an empty Collection";
      LOGGER.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }
    if (report.size() > 1) {
      final String errorMsg = "applyConfiguration returned a Collection with more than 1 result";
      LOGGER.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }
    final ConfigurationReport receivedReport = report.iterator().next();
    if (receivedReport != null)
      LOGGER.trace("applyConfiguration(): Received Configuration report Report="
          + receivedReport.toXML());

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

    LOGGER.debug("Initiating client request.");
    ClientRequestImpl<T> clientRequest = new ClientRequestImpl<T>(clazz);
    Iterator<Long> it = ids.iterator();
    Collection<Future<Collection<T>>> results = new ArrayList<Future<Collection<T>>>();
    int counter = 0;
    while (it.hasNext()) {
      while (it.hasNext() && counter < maxRequestSize) {
        clientRequest.addTagId(it.next());
        counter++;
      }
      RequestValuesTask<T> task = new RequestValuesTask<T>(clientRequest, reportListener, requestQueue);
      results.add(executor.submit(task));
      clientRequest = new ClientRequestImpl<T>(clazz);
      counter = 0;
    }
    Collection<T> finalCollection = new ArrayList<T>();
    for (Future<Collection<T>> result : results) {
      try {
        finalCollection.addAll(result.get());
      } catch (InterruptedException e) {
        LOGGER.error("InterruptedException caught while executing RequestValuesTask.", e);
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        LOGGER.error("ExecutionException caught while executing RequestValuesTask.", e);
        throw new RuntimeException(e);
      }
    }
    LOGGER.debug("Client request completed.");
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

    LOGGER.debug("Initiating client request.");
    ClientRequestImpl<T> clientRequest = new ClientRequestImpl<T>(clazz);
    Iterator<String> it = regexList.iterator();
    Collection<Future<Collection<T>>> results = new ArrayList<Future<Collection<T>>>();
    int counter = 0;
    while (it.hasNext()) {
      while (it.hasNext() && counter < maxRequestSize) {
        clientRequest.addRegex(it.next());
        counter++;
      }
      RequestValuesTask<T> task = new RequestValuesTask<T>(clientRequest, reportListener, requestQueue);
      results.add(executor.submit(task));
      clientRequest = new ClientRequestImpl<T>(clazz);
      counter = 0;
    }
    Collection<T> finalCollection = new ArrayList<T>();
    for (Future<Collection<T>> result : results) {
      try {
        finalCollection.addAll(result.get());
      } catch (InterruptedException e) {
        LOGGER.error("InterruptedException caught while executing RequestValuesTask.", e);
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        LOGGER.error("ExecutionException caught while executing RequestValuesTask.", e);
        throw new RuntimeException(e);
      }
    }
    LOGGER.debug("Client request completed.");
    return finalCollection;
  }

  /**
   * Setter method.
   *
   * @param requestQueue
   *          the requestQueue to set
   */
  public void setRequestQueue(final String requestQueue) {
    this.defaultRequestQueue = requestQueue;
  }

  @Override
  public String getProcessXml(final String processName) throws JMSException {
    ClientRequestImpl<ProcessXmlResponse> xmlRequest = new ClientRequestImpl<ProcessXmlResponse>(ProcessXmlResponse.class);
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

    ClientRequestImpl<ProcessNameResponse> namesRequest = new ClientRequestImpl<ProcessNameResponse>(ProcessNameResponse.class);

    return jmsProxy.sendRequest(namesRequest, defaultRequestQueue, namesRequest.getTimeout());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> CommandReport executeCommand(final CommandExecuteRequest<T> commandExecuteRequest) throws JMSException {

    ClientRequestImpl clientRequest = new ClientRequestImpl<CommandReport>(CommandReport.class);
    clientRequest.setObjectParameter(commandExecuteRequest);

    Collection<CommandReport> c = jmsProxy.sendRequest(clientRequest, defaultRequestQueue, commandExecuteRequest.getTimeout());
    CommandReport report = c.iterator().next();

    return report;
  }

  @Override
  public Collection<ConfigurationReportHeader> getConfigurationReports() throws JMSException {

    ClientRequestImpl<ConfigurationReportHeader> clientRequest = new ClientRequestImpl<>(ResultType.TRANSFER_CONFIGURATION_REPORT_HEADER,
        RequestType.RETRIEVE_CONFIGURATION_REQUEST, 30000);
    Collection<ConfigurationReportHeader> reports = jmsProxy.sendRequest(clientRequest, defaultRequestQueue, clientRequest.getTimeout());

    if (reports.isEmpty()) {
      LOGGER.warn("getConfigurationReports() returned an empty collection");
    }

    LOGGER.trace("getConfigurationReports(): Received " + reports.size() + " configuration report headers");
    return reports;
  }

  @Override
  public Collection<ConfigurationReport> getConfigurationReports(Long id) throws JMSException {

    ClientRequestImpl<ConfigurationReport> clientRequest = new ClientRequestImpl<>(ResultType.TRANSFER_CONFIGURATION_REPORT,
        RequestType.RETRIEVE_CONFIGURATION_REQUEST, 10000);
    clientRequest.setRequestParameter(String.valueOf(id));
    Collection<ConfigurationReport> reports = jmsProxy.sendRequest(clientRequest, defaultRequestQueue, clientRequest.getTimeout());

    if (reports.isEmpty()) {
      LOGGER.warn("getConfigurationReports() returned an empty collection");
    }

    LOGGER.trace("getConfigurationReports(): Received " + reports.size() + " reports for configuration " + id);
    return reports;
  }

  @Override
  public TagStatisticsResponse requestTagStatistics() throws JMSException {
    ClientRequestImpl<TagStatisticsResponse> clientRequest = new ClientRequestImpl<>(TagStatisticsResponse.class);
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
