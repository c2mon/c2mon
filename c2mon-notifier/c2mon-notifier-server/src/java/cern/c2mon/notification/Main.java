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
package cern.c2mon.notification;


import org.apache.log4j.PropertyConfigurator;
import org.springframework.context.support.FileSystemXmlApplicationContext;


/**
 * @author felixehm
 */
public class Main {

    /** Starts the notifier spring context.
     *
     * You can override the default one (classpath) by specifying the JVM option 'server-context'.
     *
     * @param args cmd args.
     */
    public static void main(String[] args) {
        System.setProperty("log4j.configuration", System.getProperty("log4j.configuration", "file:log4j.properties"));

        FileSystemXmlApplicationContext context = null;
        try {

            PropertyConfigurator.configureAndWatch(System.getProperty("log4j.configuration"), 1000 * 60);

            context = new FileSystemXmlApplicationContext(System.getProperty("server-context",
                    "classpath:cern/c2mon/notification/context.xml"));
            // wait until we close, die or whatever
            while (context.isRunning()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

}
