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

import cern.c2mon.notification.shared.Subscriber;

public class Example {

    /** The path to the core Spring XML */
    private static final String APPLICATION_SPRING_XML_PATH = "cern/c2mon/client/notification/config/client.xml";
    
    public static void main(String [] args) {
        
        System.setProperty("log4j.configuration", "cern/c2mon/client/notification/config/log4j.properties");
        
        final ClassPathXmlApplicationContext xmlContext = 
            new ClassPathXmlApplicationContext(APPLICATION_SPRING_XML_PATH);
        
        
        NotificationService service = xmlContext.getBean(NotificationServiceImpl.class);
        try {
            Subscriber s = service.getSubscriber("felixehm");
            service.removeSubscription(s, 1069821L);
            
            s = service.getSubscriber("felixehm");
            if (s.getSubscriptions().get(1069821L) != null) {
                System.err.println("Still there!");
            }
            
        }catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
        xmlContext.close();
    }

}
