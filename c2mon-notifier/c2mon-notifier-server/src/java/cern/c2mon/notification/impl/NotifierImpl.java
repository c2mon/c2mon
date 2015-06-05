package cern.c2mon.notification.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                logger.warn("Tag {} not found subscription  user {} : {}", s.getTagId(), s.getSubscriberId(), 
                        ignore.getMessage());
            }
        }
        registry.updateLastModificationTime();

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
                logger.info(
                        "{} Tags have changed and are indirectly subscribed to. Triggering notifications (if required)",
                        leftOver.size());
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
     * 
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

            for (Subscription s : subscriptions) {
                logger.debug("{} Sending TAG DOWN to Subscriber {} .", update.getId(), s.getSubscriberId());

                if (!s.getLastNotifiedStatus().equals(Status.UNREACHABLE)) {
                    Subscriber subscriber = registry.getSubscriber(s.getSubscriberId());
                    notifyMail(subscriber, update.getLatestUpdate().getDataTagQuality().getDescription(), text);
                    s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                    s.setLastNotifiedStatus(Status.UNREACHABLE);

                    if (s.isSmsNotification()) {
                        notifySms(subscriber, update.getName() + " DOWN:"
                                + update.getLatestUpdate().getDataTagQuality().getDescription());
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot send TAG DOWN message: " + ex.getMessage(), ex);
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
                Subscriber subscriber = registry.getSubscriber(s.getSubscriberId());
                notifyMail(subscriber, update.getLatestUpdate().getDataTagQuality().getDescription(), text);
                s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                s.setLastNotifiedStatus(update.getLatestStatus());

                if (s.isSmsNotification()) {
                    notifySms(subscriber, update.getLatestUpdate().getName() + " recovered.");
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
        if (update.getLatestUpdate().getName().contains("PROC.MISSING")) {
            requiredtoSend = true;
            logger.debug("This notification is required to be send to all enabled recipients.");
        }

        for (Tag parent : list.values()) {
            if (parent.getLatestStatus().worserThan(Status.OK)) {

                logger.info(
                        "{} Metric value has changed. Parent is in {}. Notification to {} subscribers is required to be send.",
                        update.getId(), parent.getLatestStatus(), parent.getSubscribers().size());

                for (Subscription s : parent.getSubscribers()) {
                    if (!s.isEnabled()) {
                        logger.debug("{} Subscription {} is not enabled.", update.getId(), s.getSubscriberId());
                        continue;
                    }

                    if (!requiredtoSend) {
                        // let do some additional checks
                        if (s.isNotifyOnMetricChange()) {
                            logger.debug("{} '{}' wants notification for metric change.", update.getId(),
                                    s.getSubscriberId());
                            requiredtoSend = true;
                        }
                        if (!s.isInterestedInLevel(parent.getLatestStatus())) {
                            logger.debug("{} '{}' is not interested in this level.", update.getId(),
                                    s.getSubscriberId());
                            requiredtoSend = false;
                        }
                    } else {
                        // No log message for required notification
                    }

                    if (requiredtoSend) {
                        Subscriber owner = registry.getSubscriber(s.getSubscriberId());
                        String body = textCreator.getTextForMetricUpdate(update, parent);
                        String subject = "Value changed for " + update.getLatestUpdate().getName() + "to "
                                + update.getLatestUpdate().getValue();
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

        Set<Tag> noGood = getProblemChildRules(update);
        logger.debug("{} Sending initial report with {} problematic children ", update.getId(), noGood.size());
        

        if (checkSourceDownReport(update)) {
            return;
        }

        for (Subscription s : update.getSubscribers()) {
            try {
                if (!s.getLastNotifiedStatus().equals(update.getLatestStatus())) {

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
    private void sendFullReportOn(Tag update, Subscription sub, Set<Tag> interestingRuleTags) throws IOException,
            TemplateException {
        logger.trace("Entering sendFullReportOn()");

        if (sub == null) {
            throw new IllegalStateException("Passed Argument of subscription object is null!");
        }

        logger.debug("{} Sending full report to {}", update.getId(), sub.getSubscriberId());
        logger.trace("{} List of interesting tags: {}", update.getId(), interestingRuleTags);

        String subject = textCreator.getMailSubjectForStateChange(update, interestingRuleTags);
        String text = textCreator.getReportForTag(update, interestingRuleTags, cache);

        if (sub.isEnabled()) {
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

        if (checkSourceDownReport(update)) {
            return;
        }

        /**
         * Tag has recovered:
         */
        // if (update.hasStatusRecovered()) {
        // sendTagRecoveredMessage(update);
        // return;
        // }

        /**
         * Check more detailed on the problem and collect info
         */
        Set<Tag> interestingChildRules = new HashSet<Tag>();
        
        logger.debug("{} has changed its state : {} -> {}. ", update.getId(), update.getPreviousStatus(),
                update.getLatestStatus());
        
        if (update.getAllChildRules().size() == 0) {

            // R->M update
            logger.debug("{} has no child rules: R->M ", update.getId());
            
            for (Subscription s : update.getSubscribers()) {
                Status oldStatus = s.getLastStatusForResolvedSubTag(update.getId());

                logger.trace("{} Checking if subscriber '{}' is interested in this update...", update.getId(),
                        s.getSubscriberId());
                try {
                    // we add ourself, as we need to report on the metrics
                    interestingChildRules.add(update);

                    if (s.getTagId().longValue() != update.getId().longValue()) {

                        if (oldStatus != null && !oldStatus.equals(update.getLatestStatus())
                                && !s.isInterestedInLevel(oldStatus)) {
                            // recovery of a child rule. e.g. WARN->OK
                            logger.debug("{} subscriber '{}' not interested in this update", update.getId(),
                                    s.getSubscriberId());
                            continue;
                        }

                        // an update of a rule which belongs to a higher rule
                        sendFullReportOn(update, s, interestingChildRules);
                        s.setLastStatusForResolvedTSubTag(update.getId(), update.getLatestStatus());
                        s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                        logger.debug("{} Setting new notified state '{}' for Subscriber '{}'", update.getId(),
                                update.getLatestStatus(), s.getSubscriberId());
                    } else if (!s.getLastNotifiedStatus().equals(update.getLatestStatus())) {
                        // a direct rule-metric subscription
                        sendFullReportOn(update, s, interestingChildRules);
                        logger.debug(
                                "{} Setting new notified state '{}' for Childrule {} (state before: '{}') for Subscriber '{}'.",
                                update.getId(), update.getLatestStatus(), s.getTagId(), s.getLastNotifiedStatus(),
                                s.getSubscriberId());
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

            if (update.hasStatusChanged()) {
                // no status change, but we got an update:
                // 1. check if the quality has changed ? -> yes, send update
            } else {
                // status has changed:
                // full send
            }
            
            
            
            logger.debug("{} has child rules: R->R1->M1, R->R2->M2, ...", update.getId());
            logger.trace("{} children rules: ", update.getAllChildRules());

            // all metrics of this rule R->M1, R->M2,.. (if any)
            List<Tag> childRecursiveRules = new ArrayList<Tag>();
            for (Tag c : update.getAllChildTagsRecursive()) {
                if (c.isRule()) {
                    childRecursiveRules.add(c);
                }
            }

            // R->R->M
            for (Subscription s : update.getSubscribers()) {

                // 1. mode changed
                // 2. quality changed
                // 3. status changed

                try {

                    // let check the children if they have changed since last check...
                    for (Tag child : childRecursiveRules) {
                        if (!s.getLastStatusForResolvedSubTag(child.getId()).equals(child.getLatestStatus())) {
                            // add to interesting list.
                            logger.debug("{} Adding '{}' [{}] as interesting child.", update.getId(), child.getId(),
                                    child.getLatestStatus());
                            interestingChildRules.add(child);
                        }
                        //
                    }

                    s.setLastNotifiedStatus(update.getLatestStatus());

                    // if (update.getLatestStatus().worserThan(s.getNotificationLevel()) &&
                    // !s.getNotificationLevel().equals(update.getPreviousStatus())) {
                    // continue;
                    // }

                    // update for same status
                    if (s.getNotificationLevel().equals(update.getLatestStatus())) {
                        ///
                    } else if (update.getLatestStatus().betterThan(s.getNotificationLevel())) {
                        // update better than our subscription level
                        if (!s.getNotificationLevel().betterThan(update.getPreviousStatus()) && !s.getNotificationLevel().equals(update.getPreviousStatus())) {
                            // check if the previous state was interesting to us. Only then we send notifications
                            continue;
                        } 
                    } 
                    //
                    // if (!s.isInterestedInLevel(update.getLatestStatus(), update.getPreviousStatus())) {
                    // continue;
                    // }

                    // abort, if no children have changed and no direct metrics have changed.
                    // if (interestingChildRules.size() == 0 && changedDirectChildMetrics.size() == 0) {
                    // logger.debug("{} No interesting child for {}.", update.getId(), s.getSubscriberId());
                    // // set the last notification timestamp and status here ?
                    // // why should we receive an update without changes ?
                    // continue;
                    // }

                    if (true) {
                        sendFullReportOn(update, s, interestingChildRules);
                        s.setLastNotification(new Timestamp(System.currentTimeMillis()));

                        for (Tag cR : interestingChildRules) {
                            logger.debug("Setting status for resolved subtag '{}' to [{}] for Subscriber '{}'",
                                    update.getId(), cR.getId(), cR.getLatestStatus(), s.getSubscriberId());
                            s.setLastStatusForResolvedTSubTag(cR.getId(), cR.getLatestStatus());
                        }
                        if (update.getId().longValue() != s.getTagId().longValue()) {
                            // a sub rule update
                            logger.debug("{} Setting status for resolved subtag '{}' to [{}] for Subscriber '{}'",
                                    update.getId(), update.getId(), update.getLatestStatus(), s.getSubscriberId());
                            s.setLastStatusForResolvedTSubTag(update.getId(), update.getLatestStatus());
                        } else {
                            logger.debug("{} Setting last notified status to [{}] for Subscriber '{}'", update.getId(),
                                    update.getLatestStatus(), s.getSubscriberId());
                        }
                    }

                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }

            }
        }

        logger.trace("Leaving sendReportOnRuleChange()");
    }

    boolean checkSourceDownReport(Tag update) {
        DataTagQuality quality = update.getLatestUpdate().getDataTagQuality();

        //
        // quality check first
        // Should we announce this anyhow ?
        //
        if (!quality.isValid()) {
            if (quality.isInvalidStatusSet(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED)
                    || quality.isInvalidStatusSet(TagQualityStatus.JMS_CONNECTION_DOWN)
                    || quality.isInvalidStatusSet(TagQualityStatus.PROCESS_DOWN)) {
                logger.trace("{} Quality is {} . No announcement with these states: {}", update.getId(),
                        quality.getInvalidQualityStates());
                return true;
            } else if (quality.isInvalidStatusSet(TagQualityStatus.SUBEQUIPMENT_DOWN)
                    || quality.isInvalidStatusSet(TagQualityStatus.EQUIPMENT_DOWN)
                    || quality.isInvalidStatusSet(TagQualityStatus.INACCESSIBLE)) {
                // tell user that the source is down
                sendSourceAvailabilityReport(update);
                return true;
            } else {
                // tag is invalid, but we announce this reason to the user in the following ...
                logger.trace("{} quality is {}. User notification required.", update.getId(),
                        quality.getInvalidQualityStates());
            }
        }
        return false;
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
            logger.error("Error when sending notification sms to " + smsAddress, e);
        }
    }

    /**
     * @param subscriber The Subscriber to send the passed text
     * @param subject The subject of this message
     * @param text The body
     */
    void notifyMail(Subscriber subscriber, String subject, String text) {

        logger.debug("Notifying via Mail to {} ", subscriber.getEmail());
        
        logger.trace("Mail Text {} ", text);

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
    public Set<Tag> getProblemChildRules(Tag t) {
        HashSet<Tag> result = new HashSet<Tag>();

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
     * 
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