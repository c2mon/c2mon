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

package cern.c2mon.publisher.mobicall;

import org.apache.log4j.PropertyConfigurator;

/**
 * Some static utility methods for the JUnit tests: start the publisher based on the
 * instances of mock classes, stop the publisher, configure log4j.
 * 
 * @author mbuttner
 */
public class TestUtil {

    protected static MobicallAlarmsPublisher publisher;
    protected static MobicallConfigLoaderMock loader;
    protected static C2monConnectionMock c2mon;
    protected static SenderMockImpl sender;
    
    // flag used to aoivd starting the publisher twice
    private static boolean running;

    /**
     * Prepare standard properties and setup logging
     */
    public static void init() {
        System.setProperty("c2mon.client.conf.url", "file:client.properties");
        System.setProperty("app.version", "0.1");
        System.setProperty("app.name", "test");

        String log4jConfigFile = System.getProperty("log4j.configuration", "log4j.properties");
        PropertyConfigurator.configure(log4jConfigFile);
    }

    /**
     * Start the publisher with intances of the mocks for all components.
     * @throws Exception
     */
    protected static void startTestPublisher() throws Exception {
        if (!running) {
            loader = new MobicallConfigLoaderMock();
            c2mon = new C2monConnectionMock();
            sender = new SenderMockImpl();
            publisher = new MobicallAlarmsPublisher(c2mon, loader, sender);
            publisher.connect();
        }
    }
    
    /**
     * Stop the publisher.
     */
    protected static void stopTestPublisher() {
        publisher.close();
        running = false;
    }
}
