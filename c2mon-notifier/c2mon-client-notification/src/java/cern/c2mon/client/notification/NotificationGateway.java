/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
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
    
    /** A JVM property to override the default spring context */ 
    private static String APPLICATION_SYS_PROP = "notification.props";
    
    private static NotificationService INSTANCE = null;
    
    
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
            ClassPathXmlApplicationContext xmlContext = null;
            
            if (System.getProperty(APPLICATION_SYS_PROP) != null) {
                xmlContext = new ClassPathXmlApplicationContext(System.getProperty(APPLICATION_SYS_PROP));
            } else {
                xmlContext = new ClassPathXmlApplicationContext(APPLICATION_SPRING_XML_PATH);
            }
            
            INSTANCE = xmlContext.getBean(NotificationServiceImpl.class);
        }
        return INSTANCE;
    }
    
}
