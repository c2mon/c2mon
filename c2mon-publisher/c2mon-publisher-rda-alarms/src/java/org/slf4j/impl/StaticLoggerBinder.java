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
 * Why do we need this? SLF4J takes the first "binding" it finds in the classpath, and uses
 * the logging implementation referenced by this binding. Unfortunately, c2mon-client comes
 * for now with the active-mq-5.5.1-fuse, which itself contains an old and crappy binding +
 * implementation of the logging API. Result: as soon as someone tries a log.xxx("{}",x);
 * the app crashes.
 * 
 * To avoid this situation:
 * - this class allows to point to any implementation (using the right "factory"
 * - as it is part of our code, it will most of the time be placed at the beginning of the class path
 * - in certain situations (i.e. in presence of a crappy implementation with the same name), one
 *   must also place the right implementation at the beggining of the classpath.
 * 
 * @author mbuttner
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static String REQUESTED_API_VERSION = "1.7";

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