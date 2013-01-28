/*
 * 
 * Copyright CERN 2013, All Rights Reserved.
 */
package cern.c2mon.notification;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;


/**
 * Describes the notification service and methods it has to provide.
 * 
 * @author felixehm
 */
public interface Notifier {

    /**
     * Sends a full report for the given Tag. Used for the initial notifications. ! We assume that the cache has already
     * been updated with the latest value !
     * 
     * @param update the Tag for this report.
     */
    void sendInitialReport(Tag update);

    /**
     * Sends a report whenever the passed rule update has changed. This can be by its the status (e.g. OK->WARN), or to
     * force checking if one of the child-rules change its state.
     * 
     * @param update a (rule) tag.
     */
    void sendReportOnRuleChange(Tag update);

    /**
     * Sends a notification if the passed Tag is a metric an it has changed it's values. Only Subscribers who have
     * enabled this type of notification will then receive a message.
     * 
     * @param update our Tag update to send the value change report.
     */
    void sendReportOnValueChange(Tag update);

    /**
     * Sends a message if the source went down (i.e. is not reachable.)
     * Subscribers receive only this message if they
     * have this notification type enabled.
     * 
     * @param update the {@link Tag}
     */
    void sendSourceAvailabilityReport(Tag update);

    /** Sends a reminder to the passed Subscriber for the associated Tag.
     * 
     * @param user the {@link Subscriber}
     */
    void sendReminder(Subscription user);
}
