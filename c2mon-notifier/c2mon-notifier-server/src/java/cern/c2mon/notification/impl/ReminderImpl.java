/*
 * 
 * 
 * Copyright CERN 2013, All Rights Reserved.
 */
package cern.c2mon.notification.impl;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.notification.Notifier;
import cern.c2mon.notification.Reminder;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.shared.Subscription;
import cern.dmn2.core.Status;

/**
 * A module which sends reminder for problems.
 * 
 * @author felixehm
 */
public class ReminderImpl implements Reminder {

    private static Logger logger = LoggerFactory.getLogger(Reminder.class);

    /**
     * the time when we sent the reminders
     */
    private Timestamp lastReminderRound = null;

    private Notifier notifier = null;

    private SubscriptionRegistry registry = null;

    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> myChecker = null;

    /**
     * default time for reminders
     */
    private long DEFAULT_REMINDER_TIME = 2 * 60 * 60 * 1000;

    /**
     * the configured time for regular reminders
     */
    private long reminderTime = DEFAULT_REMINDER_TIME;

    @Override
    public Timestamp lastReminderRound() {
        return lastReminderRound;
    }

    /**
     * @param time the reminder time in hours
     * @param unit the {@link TimeUnit}
     */
    @Override
    public void setReminderTime(long time, TimeUnit unit) {
        reminderTime = unit.toMillis(time);
        logger.info("Setting new reminder time to {}{}", Long.valueOf(time), unit.toString());
    }

    /**
     * @param millis the reminder time in milliseconds.
     */
    public void setReminderTime(long millis) {
        setReminderTime(millis, TimeUnit.MINUTES);
    }

    /**
     * @return the reminder time in milliseconds
     */
    @Override
    public long getReminderTime() {
        return reminderTime;
    }

    @Override
    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void setRegistry(SubscriptionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void start() {

        
        if (getReminderTime() > 0) {
            logger.info("Starting Reminder Service with reminderTime={}msec" , Long.valueOf(getReminderTime()));
            myChecker = service.scheduleWithFixedDelay(getWorker(), getReminderTime(), getReminderTime(),
                    TimeUnit.MILLISECONDS);
        } else {
            logger.info("Reminder not started as reminderTime <= 0 ");
        }

        logger.trace("Leaving start();");
    }

    @Override
    public void stop() {
        if (myChecker != null) {
            logger.info("Stopping Reminder Service...");
            myChecker.cancel(true);
        }

        logger.trace("Leaving stop();");
    }

    /**
     * @return a {@link Runnable} which executes {@link #checkForReminder()}.
     */
    Runnable getWorker() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    checkForReminder();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        };
    }

    /**
     * Iterates over the subscriptions from the {@link SubscriptionRegistry} and checks if a reminder has to be send.
     * 
     * @see #setRegistry(SubscriptionRegistry)
     * @see #setTagCache(TagCache)
     * @see #setReminderTime(long, TimeUnit)
     */
    void checkForReminder() {
        logger.trace("Entering checkForReminder()");

        Timestamp maxTimeBeforeReminder = new Timestamp(System.currentTimeMillis() - getReminderTime());

        logger.debug("Using maxTimeBeforeReminder={}", maxTimeBeforeReminder);
        
        for (Subscription sup : registry.getRegisteredSubscriptions()) {
            Timestamp lastNotification = sup.getLastNotification();

            if (lastNotification.before(maxTimeBeforeReminder) && sup.isEnabled()
                    && sup.getLastNotifiedStatus().worserThan(Status.OK)) {
                logger.trace("Subscription Tag={}, User={}: LastNotifiedStatus={} had last notification at {}", 
                        sup.getTagId(), sup.getSubscriberId(), sup.getLastNotifiedStatus(), lastNotification);
                
                sendReminder(sup);
                Timestamp ts = new Timestamp(System.currentTimeMillis());

                logger.debug("Subscription Tag={}, User= {}: Setting lastReminderTime to {}.", sup.getTagId(), sup.getSubscriberId(), ts);
            } else {
                logger.debug("Subscription Tag={}, User= {}: No reminder required", sup.getTagId(), sup.getSubscriberId() );
            }
        }
        logger.trace("Leaving checkForReminder()");
    }

    void sendReminder(Subscription sub) {
        logger.debug("Subscription Tag={}, User= {}: Sending reminder...", sub.getTagId(), sub.getSubscriberId() );
        notifier.sendReminder(sub);
        logger.trace("Leaving sendReminder()");
    }

}
