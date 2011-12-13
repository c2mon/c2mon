/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.jms.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import cern.c2mon.client.jms.JmsProxy;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.client.command.CommandExecuteRequest;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandTagHandle;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * Implementation of the RequestHandler bean.
 * 
 * @see cern.c2mon.client.jms.RequestHandler
 * @author Mark Brightwell
 * 
 */
public class RequestHandlerImpl implements RequestHandler {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(RequestHandlerImpl.class);

  /**
   * The maximum number of tags in a single request. Each request runs in its
   * own thread on the server and is sent in a single JMS message.
   */
  private static final int MAX_REQUEST_SIZE = 100;

  /**
   * Core number of threads in executor.
   */
  private static final int CORE_POOL_SIZE = 40;

  /**
   * Max number of exector threads.
   */
  private static final int MAX_POOL_SIZE = 100;

  /**
   * Thread idle timeout in executor (in seconds), including core threads.
   */
  private static final long KEEP_ALIVE_TIME = 60;

  /**
   * Thread pool queue size.
   */
  private static final int QUEUE_SIZE = 10;

  /**
   * Ref to JmsProxy bean.
   */
  private JmsProxy jmsProxy;

  /**
   * Name of request queue.
   */
  private String requestQueue;
  
  /**
   * Default request timeout for requests. NullPointerException is thrown if
   * timeout occurs.
   */
  private int requestTimeout;
  
  /**
   * Executor for submitting requests to the server.
   */
  private ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
      new ArrayBlockingQueue<Runnable>(QUEUE_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());

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
    return jmsProxy.sendRequest(clientRequest, requestQueue, requestTimeout);
  }

  @Override
  public Collection<TagUpdate> requestTags(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTags(..) method called with null parameter.");
    }
    return executeRequest(tagIds, TagUpdate.class);
  }

  @Override
  public Collection<AlarmValue> requestAlarms(final Collection<Long> alarmIds) throws JMSException {
    if (alarmIds == null) {
      throw new NullPointerException("requestAlarms(..) method called with null parameter.");
    }
    return executeRequest(alarmIds, AlarmValue.class);
  }
  
  @Override
  public Collection<CommandTagHandle> requestCommandTagHandles(final Collection<Long> commandIds) {
    if (commandIds == null) {
      throw new NullPointerException("requestTags(..) method called with null parameter.");
    }
    return executeRequest(commandIds, CommandTagHandle.class);
  }
  
  @Override
  public ConfigurationReport applyConfiguration(final Long configurationId) {
    ArrayList<Long> ids = new ArrayList<Long>();
    ids.add(configurationId);

    Collection<ConfigurationReport> report = executeRequest(ids, ConfigurationReport.class);

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

    return report.iterator().next();
  }

  @Override
  public Collection<TagConfig> requestTagConfigurations(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTagConfigurations(..) method called with null parameter.");
    }
    return executeRequest(tagIds, TagConfig.class);
  }

  @Override
  public Collection<TagValueUpdate> requestTagValues(final Collection<Long> tagIds) throws JMSException {
    if (tagIds == null) {
      throw new NullPointerException("requestTagValues(..) method called with null parameter.");
    }
    return executeRequest(tagIds, TagValueUpdate.class);
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
  private <T extends ClientRequestResult> Collection<T> executeRequest(final Collection<Long> ids, final Class<T> clazz) {
    LOGGER.debug("Initiating client request.");
    ClientRequestImpl<T> clientRequest = new ClientRequestImpl<T>(clazz);
    Iterator<Long> it = ids.iterator();
    Collection<Future<Collection<T>>> results = new ArrayList<Future<Collection<T>>>();
    int counter = 0;
    while (it.hasNext()) {
      while (it.hasNext() && counter < MAX_REQUEST_SIZE) {
        clientRequest.addTagId(it.next());
        counter++;
      }
      RequestValuesTask<T> task = new RequestValuesTask<T>(clientRequest);
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
  @Required
  public void setRequestQueue(final String requestQueue) {
    this.requestQueue = requestQueue;
  }

  /**
   * Setter method.
   * 
   * @param requestTimeout
   *          the requestTimeout to set
   */
  @Required
  public void setRequestTimeout(final int requestTimeout) {
    this.requestTimeout = requestTimeout;
  }

  @Override
  public String getProcessXml(final String processName) throws JMSException {
    ClientRequestImpl<ProcessXmlResponse> xmlRequest = new ClientRequestImpl<ProcessXmlResponse>(ProcessXmlResponse.class);
    xmlRequest.setRequestParameter(processName);
    // response should have a unique element in
    ProcessXmlResponse response = jmsProxy.sendRequest(xmlRequest, requestQueue, requestTimeout).iterator().next();
    if (response.getProcessXML() != null) {
      return response.getProcessXML();
    } else {
      throw new RuntimeException(response.getErrorMessage());
    }
  }
  
  @Override
  public Collection<ProcessNameResponse> getProcessNames() throws JMSException {
    
    ClientRequestImpl<ProcessNameResponse> namesRequest = new ClientRequestImpl<ProcessNameResponse>(ProcessNameResponse.class);

    return jmsProxy.sendRequest(namesRequest, requestQueue, requestTimeout);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> CommandReport executeCommand(CommandExecuteRequest<T> commandExecuteRequest) throws JMSException {
    
    ClientRequestImpl clientRequest = new ClientRequestImpl<CommandReport>(CommandReport.class);
    clientRequest.setObjectParameter(commandExecuteRequest);
    
    Collection<CommandReport> c = jmsProxy.sendRequest(clientRequest, requestQueue, commandExecuteRequest.getTimeout());
    CommandReport report = c.iterator().next();
    
    return report;
  }

  /**
   * This task calls the JmsProxy with the passed request and returns the
   * requested collection.
   * 
   * @author Mark Brightwell
   * 
   */
  private class RequestValuesTask<T extends ClientRequestResult> implements Callable<Collection<T>> {

    private ClientRequestImpl<T> clientRequest;

    public RequestValuesTask(ClientRequestImpl<T> clientRequest) {
      this.clientRequest = clientRequest;
    }

    @Override
    public Collection<T> call() throws Exception {
      return jmsProxy.sendRequest(clientRequest, requestQueue, requestTimeout);
    }
  }

}
