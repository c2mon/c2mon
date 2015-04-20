/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */

// DMN-2128
package cern.c2mon.client.notification;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.dmn2.core.Status;

public class ExampleAddSMSonError {

    public static void main(String[] args) {

        System.setProperty("log4j.configuration", "cern/c2mon/client/notification/config/log4j.properties");
        System.setProperty("c2mon.client.conf.url", "http://bewww/~diamonop/c2mon/client/client.properties");

        long[] ruleTagId = { 1085509, 1085511 }; // List of rule tagids

        NotificationService service = NotificationGateway.getService();

        try {

            Subscriber subscriberInstance = service.getSubscriber("jpalluel"); // user
            for (long tagId : ruleTagId) {

                Subscription subscriptionInstance = new Subscription(subscriberInstance.getUserName(), tagId);
                subscriptionInstance.setSmsNotification(true);
                subscriptionInstance.setNotificationLevel(Status.ERROR);
                subscriberInstance.addSubscription(subscriptionInstance);
            }
            service.setSubscriber(subscriberInstance);

            /**
             * ! important otherwise, this spring context will not exit!
             */
            NotificationGateway.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

}
