/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * Do not let sl4j decide about the binding as it might use an old one coming from active mq.
 * This class forces log4j to be the implementation of the logging API
 * 
 * Note: to get the thing to fully work, the log4j binding jar must also be before the activeMQ 5.5.1
 * jar in classpath!
 * 
 * @author mbuttner
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static String REQUESTED_API_VERSION = "1.6";

    public static final StaticLoggerBinder getSingleton() {
      return SINGLETON;
    }

    private StaticLoggerBinder() {
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return new Log4jLoggerFactory();
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return "org.slf4j.impl.Log4jLoggerFactory";
    }
}