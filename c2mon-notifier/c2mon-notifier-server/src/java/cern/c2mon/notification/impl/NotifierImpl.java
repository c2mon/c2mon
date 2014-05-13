package cern.c2mon.notification.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.notification.Mailer;
import cern.c2mon.notification.Notifier;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TagCacheUpdateListener;
import cern.c2mon.notification.TextCreator;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.TagNotFoundException;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.dmn2.core.Status;
import freemarker.template.TemplateException;

/**
 * A Daemon which notifies clients about errors reported by the C2MON server.
 * 
 * @author felixehm
 */
public class NotifierImpl implements Notifier, TagCacheUpdateListener {

    /**
     * our Logger.
     */
    private Logger logger = LoggerFactory.getLogger(NotifierImpl.class);

    /**
     * our registry which keep information on who is registered to what.
     */
    private SubscriptionRegistry registry = null;

    /**
     * The {@link C2monTagManager} reference, so we can subscribe to new datatags or unsubscribe from existing.
     */
    // private C2monTagManager tagManager;

    /**
     * our {@link Mailer} instance to send the notifications.
     */
    private Mailer mailer;
    /**
     * our {@link TextCreator} instance for rendering mail and sms message.
     */
    private TextCreator textCreator;

    /**
     * our private local cache of the tag updates..
     */
    private TagCache cache;

    /**
     * runs our worker which checks regularly if notifications should be send.
     */
    private ScheduledExecutorService updatedTagsCheckerService = Executors.newSingleThreadScheduledExecutor();
    
    /**
     * the list of tags which were updated between our checks.
     */
    private HashSet<Tag> updatedTags = new HashSet<Tag>();

    /**
     * Constructor. starts also {@link C2monServiceGateway}
     * 
     * @throws IOException in case the {@link TextCreator} cannot be initialized.
     */
    public NotifierImpl() throws IOException {
        textCreator = new TextCreator();
        logger.info("Notifier created.");
    }

    /**
     * @param registry the {@link SubscriptionRegistry}
     */
    @Required
    public void setSubscriptionRegistry(SubscriptionRegistry registry) {
        logger.debug("Setting subscription registry.");
        this.registry = registry;
        if (registry != null) {
            registry.setTagCache(cache);
        }
    }

    /**
     * @param mailerService the {@link Mailer} service which is required for sending the Mails /SMS.
     */
    @Required
    public void setMailer(Mailer mailerService) {
        logger.debug("Setting mailer.");
        this.mailer = mailerService;
    }

    /**
     * Sets the TextCreator to use for rendering the messages.
     * 
     * @param textCreator the {@link TextCreator}
     */
    @Required
    public void setTextCreator(TextCreator textCreator) {
        logger.debug("Setting text creator.");
        this.textCreator = textCreator;
    }

    /**
     * Sets the cache which provides access to the latest {@link Tag} object.
     * 
     * @param cache the {@link TagCache} object to set.
     * @see TagCache
     */
    @Required
    public void setTagCache(TagCache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("passed object for TagCache is null!");
        }
        this.cache = cache;
        cache.registerListener(this);
    }

    /**
     * Starts the notification service. The C2Mon service will be contacted, the {@link SubscriptionRegistry} loaded and
     * the {@link TagCache} started.
     * 
     * @see NotifierImpl#setSubscriptioRegistry(SubscriptionRegistry)
     */
    @PostConstruct
    public void start() {
        logger.info("Starting....");

        if (registry == null) {
            throw new IllegalStateException(
                    "The SubscriptionRegistry was not set. Please call setSubscriptionRegistry(..)");
        }
        if (mailer == null) {
            throw new IllegalStateException("The Mailer was not set. Please call setMailer(..)");
        }
        if (textCreator == null) {
            throw new IllegalStateException("The TextCreator was not set. Please call setTextCreator(..)");
        }
        if (cache == null) {
            throw new IllegalStateException("The TagCache was not set. Please init the cache.");
        }

        logger.info("Step 1 of 4: Starting C2Mon Service.");

        C2monServiceGateway.startC2monClient();

        while (!C2monServiceGateway.getSupervisionManager().isServerConnectionWorking()) {
            logger.info("Waiting for C2Mon Service to initialize..");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException("Error while waiting for C2Mon serivce to start up.");
            }
        }
        
        logger.info("Step 2 of 4: Intializing subscription registry .");
        registry.reloadConfig();
        registry.setTagCache(cache);

        cache.setNotifier(this);

        logger.info("Step 4 of 6 : Starting registry writer...");
        registry.start();

        /*
         * tell the cache where to find the registry in case the cache detects invalid subscriptions during the next
         * call.
         */
        cache.setRegistry(registry);
        logger.info("Step 5 of 6: Starting subscriptions...");
        for (Subscription s : registry.getRegisteredSubscriptions()) {
            try {
                cache.startSubscription(s);
             } catch (TagNotFoundException ignore) {
                 logger.warn("Cannot start subscription for user %s with tagid %s: %s", s.getSubscriberId(), s.getTagId(), ignore.getMessage()); 
             }
        }
        

        /**
         * wait for all updates
         */
        while (cache.getUnitializedTags().size() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized (updatedTags) {
            updatedTags.clear();
        }

        logger.info("Step 6 of 6 : Starting the cache checker...");
        // start the cache checker
        startUpdateChecker();

        logger.info("Notification service fully started.");
    }

    
    public void checkCacheForChanges() {

        HashSet<Tag> leftOver = new HashSet<Tag>();
        
        synchronized (updatedTags) {
            for (Tag changed : updatedTags) {

                if (registry.getAllRegisteredTagIds().contains(changed.getId())) {
                    sendReportOnRuleChange(changed);
                    for (Tag c : changed.getAllChildTagsRecursive()) {
                        c.setToBeNotified(false);
                    }
                } else {
                    leftOver.add(changed);
                }
            }

            // second iteration: now notify every
            if (!leftOver.isEmpty()) {
                logger.info("{} Tags have changed and are indirectly subscribed to. Triggering notifications (if required)", leftOver.size());
                for (Tag changed : leftOver) {
                    if (changed.getToBeNotified()) {
                        if (changed.isRule()) {
                            sendReportOnRuleChange(changed);
                        } else {
                            sendReportOnValueChange(changed);
                        }
                        changed.setToBeNotified(false);
                    } else {
                        // not a valid item. IGNORE as we only announce down's for direct tag subscriptions
                    }
                }
            }
            
            updatedTags.clear();
        }
    }


    /**
     * Marks the passed Tag as to be notified in the next round.
     * @param newElement a {@link Tag} which was decided to send notifications for.
     */
    @Override
    public void onUpdate(Tag newElement) {
        newElement.setToBeNotified(true);
        synchronized (updatedTags) {
             updatedTags.add(newElement); 
        }
                
    }
    
    @Override
    public void sendSourceAvailabilityReport(Tag update) {
        logger.trace("{} Entering sendSourceAvailabilityReport()", update.getId());
        try {
            String text = textCreator.getTextForSourceDown(update);
            HashSet<Subscription> subscriptions = update.getSubscribers();
            
            logger.debug("{} Sending TAG DOWN to {} Subscribers .", update.getId(), subscriptions.size());
            
            for (Subscription s : subscriptions) {
                Subscriber subscriber  = registry.getSubscriber(s.getSubscriberId());
                notifyMail(subscriber, update.getLatestUpdate().getDataTagQuality()
                        .getDescription(), text);
                s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                s.setLastNotifiedStatus(Status.UNREACHABLE);
                
                if (s.isSmsNotification()) {
                    notifySms(subscriber, update.getLatestUpdate().getDataTagQuality().getDescription());
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        logger.trace("{} Leaving sendSourceAvailabilityReport()", update.getId());
    }

    public void sendTagRecoveredMessage(Tag update) {
        logger.trace("{} Entering sendRecoveryReport()", update.getId());
        
        try {
            String text = textCreator.getTextForSourceDown(update);
            HashSet<Subscription> subscriptions = update.getSubscribers();
            
            logger.debug("{} Sending TAG RECOVERED to {} Subscribers .", update.getId(), subscriptions.size());
            for (Subscription s : subscriptions) {
                Subscriber subscriber  = registry.getSubscriber(s.getSubscriberId());
                notifyMail(subscriber, update.getLatestUpdate().getDataTagQuality()
                        .getDescription(), text);
                s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                s.setLastNotifiedStatus(update.getLatestStatus());
                
                if (s.isSmsNotification()) {
                    notifySms(subscriber, update.getLatestUpdate().getName() + " is down.");
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot send RECOVERY message: " + ex.getMessage(), ex);
        }
        
        logger.trace("{} Leaving sendSourceAvailabilityReport()", update.getId());
    }
    
    
    @Override
    public void sendReportOnValueChange(Tag update) {

        logger.trace("Entering sendReportOnValueChange()");
        
        HashMap<Long, Tag> list = update.getParents();
        boolean requiredtoSend = false;
        if (update.getLatestUpdate().getName().contains("PROC.MISSING.LIMIT")) {
            requiredtoSend = true;
            logger.debug("This notification is required to be send to all enabled recipients.");
        }
        
        for (Tag parent : list.values()) {
            if (parent.getLatestStatus().worserThan(Status.OK)) {

                logger.info("{} Metric value has changed. Parent is in {}. Notification to {} subscribers is required to be send.", 
                        update.getId(), parent.getLatestStatus(), parent.getSubscribers().size());

                for (Subscription s : parent.getSubscribers()) {
                    if (!s.isEnabled()) {
                        logger.debug("{} Subscription {} is not enabled.", update.getId(), s.getSubscriberId());
                        continue;
                    }
                    
                    if (!requiredtoSend) {
                        // let do some additional checks
                        if (s.isNotifyOnMetricChange()) {
                            logger.debug("{} '{}' wants notification for metric change.", update.getId(), s.getSubscriberId());
                            requiredtoSend = true;
                        } 
                        if (!s.isInterestedInLevel(parent.getLatestStatus())) {
                            requiredtoSend = false;
                            logger.debug("{} '{}' is not interested in this level.", update.getId(), s.getSubscriberId());
                        }
                    } else {
                        // No log message for required notification 
                    }
                    
                    if (requiredtoSend) {
                        Subscriber owner = registry.getSubscriber(s.getSubscriberId());
                        String body = textCreator.getTextForMetricUpdate(update, parent);
                        String subject = "Value changed for " + update.getLatestUpdate().getName() + "to " + update.getLatestUpdate().getValue();
                        if (s.isMailNotification()) {
                            notifyMail(owner, subject, body);
                        }
                        if (s.isSmsNotification()) {
                            notifySms(owner, textCreator.getSmsTextForValueChange(update));
                        }
                        s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                    } else {
                        logger.debug("{}  No need to send notification..", update.getId());
                    }
                }
            }
        }
        logger.trace("Leaving sendReportOnValueChange()");
    }

    @Override
    public void sendInitialReport(Tag update) {
        logger.trace("Entering sendInitialReport()");

        logger.debug("{} Sending initial report to {} Subscribers ", update.getId(), update.getSubscribers().size());
        
        for (Subscription s : update.getSubscribers()) {
            try {
                if (!s.getLastNotifiedStatus().equals(update.getLatestStatus())) {
                    List<Tag> noGood = getProblemChildRules(update);
                    sendFullReportOn(update, s, noGood);
                    for (Tag t : noGood) {
                        s.setLastStatusForResolvedTSubTag(t.getId(), t.getLatestStatus());
                    }
                    s.setLastNotifiedStatus(update.getLatestStatus());
                }
            } catch (Exception e) {
                logger.error("Cannot send initial report", e);
            }
        }

        logger.trace("Leaving sendInitialReport()");
        
    }

    /**
     * @param update the {@link Tag} to send the notification for.
     * @param sub the subscription object.
     * @param interestingRuleTags a list of Tags which are RULES
     * @throws IOException
     * @throws TemplateException
     */
    private void sendFullReportOn(Tag update, Subscription sub, List<Tag> interestingRuleTags) throws IOException,
            TemplateException {
        logger.trace("Entering sendFullReportOn()");
        
        if (sub == null) {
            throw new IllegalStateException("Passed Argument of subscription object is null!");
        }
        
        logger.debug("{} Sending full report to {}",update.getId(), sub.getSubscriberId());
        logger.trace("{} List of interesting tags: {}", update.getId(), interestingRuleTags);

        String subject = textCreator.getMailSubjectForStateChange(update, interestingRuleTags);
        String text = textCreator.getReportForTag(update, interestingRuleTags, cache);
        String smsText = textCreator.getSmsTextForRuleChange(update, interestingRuleTags);
        
        if (sub.isEnabled() && sub.isInterestedInLevel(update.getLatestStatus())) {
            Subscriber user = registry.getSubscriber(sub.getSubscriberId());
            if (sub.isMailNotification()) {
                notifyMail(user, subject, text);
            }
            if (sub.isSmsNotification()) {
                notifySms(user, subject);
            }
        }
        logger.trace("Leaving sendFullReportOn()");
    }

    @Override
    public void sendReportOnRuleChange(Tag update) {

        logger.trace("{} Entering sendReportOnRuleChange()", update.getId());

        DataTagQuality quality = update.getLatestUpdate().getDataTagQuality();
        
        //
        // quality check first
        // Should we announce this anyhow ?
        //
        if (!quality.isValid()) {
            if (quality.isInvalidStatusSet(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED)
              || quality.isInvalidStatusSet(TagQualityStatus.JMS_CONNECTION_DOWN)
              || quality.isInvalidStatusSet(TagQualityStatus.PROCESS_DOWN)) {
              logger.trace("{} Quality is {} . No announcement with these states: {}", 
                      update.getId(), quality.getInvalidQualityStates());
                return;
            }
            else if (
                  quality.isInvalidStatusSet(TagQualityStatus.SUBEQUIPMENT_DOWN)
               || quality.isInvalidStatusSet(TagQualityStatus.EQUIPMENT_DOWN)
               || quality.isInvalidStatusSet(TagQualityStatus.INACCESSIBLE)) {
                // tell user that the source is down
                sendSourceAvailabilityReport(update);
                return;
            } else {
                // tag is invalid, but we announce this reason to the user in the following ...
                logger.trace("{} quality is {}. User notification required.", 
                        update.getId(), quality.getInvalidQualityStates());
            }
        }  
        
        /**
         * Tag has recovered:
         */
//        if (update.hasStatusRecovered()) {
//            sendTagRecoveredMessage(update);
//            return;
//        }

        /**
         * Check more detailed on the problem and collect info 
         */
        List<Tag> interestingChildRules = new ArrayList<Tag>();
        if (update.getAllChildRules().size() == 0) {
            
            // R->M update
            
            logger.debug("{} no child rule changed but received an update. R->M.", update.getId());
            for (Subscription s : update.getSubscribers()) {
                logger.trace("{} Subscriber '{}' is interested in this update.", update.getId(), s.getSubscriberId());
                try {
                    // we add ourselfs, as we need to report on the metrics
                    interestingChildRules.add(update);
                    
                    if (s.getTagId().longValue() != update.getId().longValue()) {
                        // an update of a rule which belongs to a higher rule
                        sendFullReportOn(update, s, interestingChildRules);
                        s.setLastStatusForResolvedTSubTag(update.getId(), update.getLatestStatus());
                        s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                        logger.debug("{} Setting new notified state '{}' for Subscriber '{}'", 
                                update.getId(), update.getLatestStatus(), s.getSubscriberId());
                    } else if (!s.getLastNotifiedStatus().equals(update.getLatestStatus())) {
                        // a direct rule-metric subscription
                        sendFullReportOn(update, s, interestingChildRules);
                        logger.debug("{} Setting new notified state '{}' for Childrule {} (state before: '{}') for Subscriber '{}'.", 
                                update.getId(), update.getLatestStatus(), s.getTagId(), s.getLastNotifiedStatus(), s.getSubscriberId());
                        s.setLastNotifiedStatus(update.getLatestStatus());
                        s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                    } else {
                        logger.info("{} no status change for Subscription '{}'.", update.getId(), s.getSubscriberId());
                    }
                } catch (Exception ex) {
                    logger.error("Cannot send report: " + ex.getMessage(), ex);
                }
            }
        } else {
            logger.debug("{} has child rules: R->R->M...", update.getId());
            logger.trace("{} children rules:", update.getAllChildRules());
            // R->R->M
            for (Subscription s : update.getSubscribers()) {
                if (s.getLastNotifiedStatus().equals(update.getLatestStatus())) {
                    logger.debug("{} received an update for it, but no status change. Checking children..", update.getId());
                    // no change for the update (why do we receive it ?) 
                    // lets check if something has changed on the children side.
                    for (Tag cR : update.getAllChildRules()) {
                        if (!s.getLastStatusForResolvedSubTag(cR.getId()).equals(cR.getLatestStatus())) {
                            // add to interesting list.
                            interestingChildRules.add(cR);
                        } else {
                            logger.debug("{} Childrule {} [{}] in Subscription {} was already notified ",
                                    update.getId(), cR.getId(), 
                                    s.getLastStatusForResolvedSubTag(cR.getId()), s.getSubscriberId());
                        }
                    }
                } else {
                    boolean metricsChanged = false;
                    for (Tag child : update.getAllChildTagsRecursive()) {
                        if (child.getToBeNotified()) {
                            // we received an update for this tag in the same iteration
                            if (child.isRule() && child.hasStatusChanged()) {
                                interestingChildRules.add(child);
                            } else if (!child.isRule()){
                                metricsChanged = true;
                            }
                        }
                    }
                    if (metricsChanged) {
                        interestingChildRules.add(update);
                    }
                    logger.debug("{} has changed its state. Found {} children which are interesting", update.getId(), interestingChildRules.size());
                }

                // now real sending
                try {
                    if (interestingChildRules.size() > 0) {
                        sendFullReportOn(update, s, interestingChildRules);
                        for (Tag cR : interestingChildRules) {
                            logger.debug("{} Setting status '{}' for resolved child {} for Subscriber '{}' ",
                                    update.getId(),
                                    cR.getLatestStatus(),
                                    cR.getId(),
                                    s.getSubscriberId()
                                    );
                            if (cR.getId().longValue() != s.getTagId().longValue()) {
                                s.setLastStatusForResolvedTSubTag(cR.getId(), cR.getLatestStatus());
                            }
                        }
                    } 
                    logger.debug("{} Setting status '{}' for Subscriber '{}' ", 
                            update.getId(), update.getLatestStatus(), s.getSubscriberId());
                    s.setLastNotifiedStatus(update.getLatestStatus());
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } // END FOR
        }

        logger.trace("Leaving sendReportOnRuleChange()");
    }

    
    
    
    // / --------------------------- END main Logic -------------------------------- \\\
    
    
    /**
     * @param subscriber The Subscriber to send the passed text
     * @param smsText The text to send
     */
    void notifySms(Subscriber subscriber, String smsText) {
        
        String smsAddress = subscriber.getSms() + "@mail2sms.cern.ch";
        logger.debug("Notifying via SMS to {}", smsAddress);
        
        try {
            mailer.sendEmail(smsAddress, "", smsText);
        } catch (Exception e) {
            logger.error("Error when sending notification sms to " + smsAddress , e);
        }
    }
    
    /**
     * 
     * @param subscriber The Subscriber to send the passed text
     * @param subject The subject of this message
     * @param text The body 
     */
    void notifyMail(Subscriber subscriber, String subject, String text) {
        
        logger.debug("Notifying via Mail to {} ", subscriber.getEmail());
        
        try {
            mailer.sendEmail(subscriber.getEmail(), subject, text);
        } catch (Exception e) {
            logger.error("Error when sending notification mail to " + subscriber.getEmail(), e);
        }
    }

    /**
     * Finds a list of changed children. This happens recursively for the passed argument
     * 
     * @param t the Tag for which to find all children tags (rules)
     * @return a list of tags which report themselfs as changed
     * @see Tag#hasStatusChanged()
     */
    public List<Tag> getProblemChildRules(Tag t) {
        ArrayList<Tag> result = new ArrayList<Tag>();

        for (Tag c : t.getAllChildTagsRecursive()) {
            if (c.isRule() && c.getLatestStatus().worserThan(Status.OK)) {
                result.add(c);
            }
        }
        return result;
    }

    @Override
    public void sendReminder(Subscription sup) {
        logger.debug("Sending Reminder for {} ,User={}", sup.getTagId(), sup.getSubscriberId());
        
        Subscriber user = registry.getSubscriber(sup.getSubscriberId());
        Tag toRemindeFor = cache.get(sup.getTagId());
        
        try {
            String body = textCreator.getReportForTag(toRemindeFor, getProblemChildRules(toRemindeFor), cache);
            mailer.sendEmail(user.getEmail(), "REMINDER for " + toRemindeFor.getLatestUpdate().getName(), body);
        } catch (Exception e) {
            logger.error("Cannot send reminder", e);
        }
        logger.trace("Leaving sendReminder()");
    }
    
    /**
     * our c2mon server heartbeat listener.
     * @return a {@link HeartbeatListener}
     */
    HeartbeatListener getC2MonHeartbeatListener() {
        return new HeartbeatListener() {
            
            @Override
            public void onHeartbeatResumed(Heartbeat pHeartbeat) {
                logger.info("C2MON Server Heartbeat resumed.");
                stopUpdateChecker();
                
            }
            
            @Override
            public void onHeartbeatReceived(Heartbeat pHeartbeat) {
                logger.trace("C2MON Server Heartbeat from {} received.", pHeartbeat.getHostName());
                startUpdateChecker();
                
            }
            
            @Override
            public void onHeartbeatExpired(Heartbeat pHeartbeat) {
                logger.warn("C2MON Server Heartbeat lost.");
                
            }
        };
    }
    
    void stopUpdateChecker() {
        updatedTagsCheckerService.shutdownNow();
    }
    
    void startUpdateChecker() {
        
        updatedTagsCheckerService.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    checkCacheForChanges();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            
        }, 2, 10, TimeUnit.SECONDS);
    }
    
}