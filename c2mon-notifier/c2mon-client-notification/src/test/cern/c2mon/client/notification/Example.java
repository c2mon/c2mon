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

import cern.c2mon.notification.shared.Subscriber;

public class Example {
    
    public static void main(String [] args) {
        
        System.setProperty("log4j.configuration", "cern/c2mon/client/notification/config/log4j.properties");
        
        
        NotificationService service = NotificationGateway.getService();
        try {
            Subscriber s = service.getSubscriber("felixehm");


            service.subscribe(s.getUserName(), 2138729L);
            
            /**
             * ! important otherwise, this spring context will not exit! 
             */
            NotificationGateway.close();
            
        }catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        
    }

}
