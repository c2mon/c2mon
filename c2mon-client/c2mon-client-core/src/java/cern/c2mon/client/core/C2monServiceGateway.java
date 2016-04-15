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
package cern.c2mon.client.core;

import cern.c2mon.client.core.config.C2monAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import cern.c2mon.client.core.manager.CommandManager;
import cern.c2mon.client.core.manager.SupervisionManager;
import cern.c2mon.client.core.manager.TagManager;

/**
 * This class is the main facade for all applications using the
 * C2MON client API.
 * <p>
 * The C2MON service gateway provides access to the different
 * C2MON manager singleton instances. A client application should
 * only use functionality which are provided by these classes.
 * <p>
 *
 *
 * @author Matthias Braeger
 */
public class C2monServiceGateway {

  /** Class logger */
  private static final Logger LOG = LoggerFactory.getLogger(C2monServiceGateway.class);

  /** The SPRING application context, which can be used as parent context */
  private static ApplicationContext context = null;

  /**
   * The maximum amount of time in milliseconds which the C2MON ServiceGateway shall
   * wait before aborting waiting that the connection to the C2MON server is established.
   */
  private static final Long MAX_INITIALIZATION_TIME = 60000L;

  /** Static reference to the <code>C2monCommandManager</code> singleton instance */
  private static CommandManager commandManager = null;

  /** Static reference to the <code>C2monTagManager</code> singleton instance */
  private static TagManager tagManager = null;

  /** Static reference to the <code>TagService</code> singleton instance */
  private static TagService tagService = null;

  /** Static reference to the <code>ConfigurationService</code> singleton instance */
  private static ConfigurationService configurationService = null;

  /** Static reference to the <code>AlarmService</code> singleton instance */
  private static AlarmService alarmService = null;

  /** Static reference to the <code>StatisticsService</code> singleton instance */
  private static StatisticsService statisticsService = null;

  /** Static reference to the <code>C2monSupervisionManager</code> singleton instance */
  private static SupervisionManager supervisionManager = null;

  /**
   * Protected default constructor
   */
  protected C2monServiceGateway() {
    // Do nothing
  }

  /**
   * This method returns the SPRING application context of the {@linkplain C2monServiceGateway}
   * @return the application context of the {@linkplain C2monServiceGateway} which can be used
   *         as parent context for an API extension.
   */
  public static final ApplicationContext getApplicationContext() {
    return context;
  }


  /**
   * @deprecated Please use {@link #getCommandService()} instead
   * @return The C2MON command service, which shall be used
   *         for executing commands
   */
  @Deprecated
  public static C2monCommandManager getCommandManager() {
    return commandManager;
  }

  /**
   * @return The C2MON command service, which shall be used
   *         for executing commands
   */
  public static CommandService getCommandService() {
    return commandManager;
  }

  /**
   * @deprecated Use {@link #getTagService()} instead
   * @return The C2MON tag manager, which is managing
   *         the tag subscription and unsubscription.
   */
  @Deprecated
  public static C2monTagManager getTagManager() {
    return tagManager;
  }

  /**
   * @return The C2MON alarm service, which provides
   *         methods for alarm subscription and unsubscription.
   */
  public static AlarmService getAlarmService() {
    return alarmService;
  }

  /**
   * @return The C2MON statistics service, which allows
   *         to retrieve the statistics report.
   */
  public static StatisticsService getStatisticsService() {
    return statisticsService;
  }

  /**
   * @return The C2MON configuration service, which allows
   *         to manage the server configuration
   */
  public static ConfigurationService getConfigurationService() {
    return configurationService;
  }


  /**
   * @return The C2MON tag service, which provides
   *         methods for tag subscription and unsubscription.
   */
  public static TagService getTagService() {
    return tagService;
  }

  /**
   * @deprecated Please use {@link #getSupervisionService()} instead
   * @return the supervision manager
   */
  @Deprecated
  public static C2monSupervisionManager getSupervisionManager() {
    return supervisionManager;
  }

  /**
   * The supervision service allows registering listeners to get informed
   * about the connection state to the JMS brokers and the heart beat of
   * the C2MON server.
   * @return Instance of the {@link SupervisionService}
   */
  public static SupervisionService getSupervisionService() {
    return supervisionManager;
  }

  /**
   * Starts the C2MON core. Must be called at application start-up.
   * <p>
   * This method needs to be called before the C2monServiceGateway
   * can be used. It should be called synchronously by the main application
   * thread, and will return once the core is ready for use.
   * <p>
   * <b>Notice</b> that the method will return even if the core cannot connect to
   * JMS (reconnection attempts will be made until successful). You can check
   * the successful connection status with
   * {@link C2monSupervisionManager#isServerConnectionWorking()}. The advantage of
   * this behavior is that you can use the time in between to initialize your
   * application. However, if you don't want to check yourself that the connection
   * to the C2MON server is established you should maybe use
   * {@link #startC2monClientSynchronous()} instead.
   *
   */
  public static synchronized void startC2monClient() {
    if (context == null) {
      LOG.info("Starting C2MON client core.");

      context = new AnnotationConfigApplicationContext(C2monAutoConfiguration.class);
      initiateGatewayFields(context);

      ((AnnotationConfigApplicationContext) context).registerShutdownHook();
    }
    else {
      LOG.warn("startC2monClient() - C2MON client core already started.");
    }
  }

  /**
   * Starts the C2MON core. Must be called at application start-up.
   * <p>
   * This method needs to be called before the C2monServiceGateway
   * can be used. It should be called synchronously by the main application
   * thread, and will return once the core is ready for use.
   * <p>
   * <b>Notice</b> that the method won't return before the core has succesfully
   * established the connection to the C2MON server. However, if the C2MON
   * Client API hasn't managed to connect after 60 seconds this method will
   * return with a {@link RuntimeException}.
   *
   * @exception RuntimeException In case the connection to the C2MON server could not
   *            be established within 60 seconds. However, the C2MON Client API will
   *            continue trying to establish the connection, but by throwing this
   *            exception we want to avoid that the application is blocking too long
   *            on this call.
   * @see C2monSupervisionManager#isServerConnectionWorking()
   * @see #startC2monClient()
   */
  public static synchronized void startC2monClientSynchronous() throws RuntimeException {
    startC2monClient();

    LOG.info("Waiting for C2MON server connection (max " + MAX_INITIALIZATION_TIME / 1000  + " sec)...");

    Long startTime = System.currentTimeMillis();
    while (!supervisionManager.isServerConnectionWorking()) {
      try { Thread.sleep(200); } catch (InterruptedException ie) { /* Do nothing */ }
      if (System.currentTimeMillis() - startTime >= MAX_INITIALIZATION_TIME) {
        throw new RuntimeException(
            "Waited "
            + MAX_INITIALIZATION_TIME / 1000
            + " seconds and the connection to C2MON server could still not be established.");
      }
    }
    LOG.info("C2MON server connection established!");
  }

  /**
   * Initiate the static fields, retrieving it from the <code>context</code>
   *
   * @param context the application context
   */
  private static void initiateGatewayFields(final ApplicationContext context) {
    tagManager = context.getBean(TagManager.class);
    supervisionManager = context.getBean(SupervisionManager.class);
    commandManager = context.getBean(CommandManager.class);

    alarmService = context.getBean(AlarmService.class);
    configurationService = context.getBean(ConfigurationService.class);
    statisticsService = context.getBean(StatisticsService.class);
    tagService = context.getBean(TagService.class);
  }

  /**
   * Stop the C2MON client.
   */
  public static synchronized void stopC2monClient() {
    ((ConfigurableApplicationContext) context).close();
  }
}
