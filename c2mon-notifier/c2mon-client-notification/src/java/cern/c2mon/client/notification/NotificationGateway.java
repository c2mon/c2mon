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
package cern.c2mon.client.notification;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/** A Gateway which returns a singleton implementation of the {@link NotificationService}. <br>
 * 
 * Check the {@link #getService()} for more details.
 * 
 * @author felixehm
 */
public class NotificationGateway {
    
    /** The path to the core Spring XML */
    private static final String APPLICATION_SPRING_XML_PATH = "cern/c2mon/client/notification/config/client.xml";
    
    private static NotificationService INSTANCE = null;
    
    private static ClassPathXmlApplicationContext xmlContext = null;
    
    
    /**
     * singleton class
     */
    private NotificationGateway () {
        
    }
    
    
    /** Creates a NotificationService from the spring context.<br><br> 
     * 
     * You can override the default context location by specifying <br>
     * the Java VM Option:<br><b><code>-Dnotification.props=&lt;location&gt;</code></b> 
     * 
     * @return a implementation of the NotificationService.
     */
    public static NotificationService getService() {
        if (INSTANCE == null) {
                xmlContext = new ClassPathXmlApplicationContext(APPLICATION_SPRING_XML_PATH);
            INSTANCE = xmlContext.getBean(NotificationServiceImpl.class);
        }
        return INSTANCE;
    }
    
    /**
     * Closes all resources.
     */
    public static void close() {
        if (xmlContext != null) {
            try {
                xmlContext.close();
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
}
