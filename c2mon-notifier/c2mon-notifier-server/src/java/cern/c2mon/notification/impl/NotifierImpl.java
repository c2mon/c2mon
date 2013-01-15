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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.notification.Mailer;
import cern.c2mon.notification.Notifier;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TagCacheUpdateListener;
import cern.c2mon.notification.TextCreator;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
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
    private Logger logger = Logger.getLogger(NotifierImpl.class);

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
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
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

        logger.info("Step 2 of 4: Intializing local TagCache.");
        registry.reloadConfig();

        registry.setTagCache(cache);

        logger.info("Step 3 of 4 : Starting local tag cache writer...");
        cache.startBackupWriter();
        cache.setNotifier(this);

        logger.info("Step 4 of 6 : Starting registry writer...");
        registry.start();

        /*
         * tell the cache where to find the registry in case the cache detects invalid subscriptions during the next
         * call.
         */
        //cache.registerListener(this);
        cache.setRegistry(registry);
        logger.info("Step 5 of 6: Starting subscriptions...");
        cache.startSubscription(registry.getRegisteredSubscriptions());

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
        service.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    checkCacheForChanges();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }, 2, 10, TimeUnit.SECONDS);

        logger.info("Notification service fully started.");
    }

    
    public void checkCacheForChanges() throws IOException, TemplateException {

        logger.info(updatedTags.size() + " tags have changed. Triggering notifications (if required)");
        HashSet<Tag> leftOver = new HashSet<Tag>();
        
        synchronized (updatedTags) {
            for (Tag changed : updatedTags) {
                if (registry.getAllRegisteredTagIds().contains(changed.getId())) {
                    sendReportOnRuleChange(changed);
                    changed.setToBeNotified(false);
                    for (Tag c : changed.getAllChildTagsRecursive()) {
                        c.setToBeNotified(false);
                    }
                } else {
                    leftOver.add(changed);
                }
            }

            // second iteration: now notify every
            logger.info(leftOver.size()
                    + " tags have changed and are indirectly subscribed to. Triggering notifications (if required)");
            for (Tag changed : leftOver) {
                if (changed.getToBeNotified().get() && !changed.isSourceDown()) {
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
            updatedTags.clear();
        }
    }


    public void onUpdate(Tag newElement) {
        newElement.setToBeNotified(true);
        updatedTags.add(newElement);
    }
    
    /**
     * Sends a message if the source went down (i.e. is not reachable.) Subscribers receive only this message if they
     * have this notification type enabled.
     * 
     * @param update the {@link Tag}
     * @throws Exception in case the text for the mail/sms cannot be rendered.
     */
    public void sendSourceAvailabilityReport(Tag update) {
        try {
            String text = textCreator.getTextForSourceDown(update);
            HashSet<Subscription> subscriptions = update.getSubscribers();
            logger.trace("Got " + subscriptions.size() + " which are interested in this tag.");
            for (Subscription s : subscriptions) {
                notifyMail(registry.getSubscriber(s.getSubscriberId()), update.getLatestUpdate().getDataTagQuality()
                        .getDescription(), text);
                s.setLastNotification(new Timestamp(System.currentTimeMillis()));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Sends a notification if the passed Tag is a metric an it has changed it's values. Only Subscribers who have
     * enabled this type of notification will then receive a message.
     * 
     * @param update our Tag update to send the value change report.
     */
    public void sendReportOnValueChange(Tag update) {

        HashMap<Long, Tag> list = update.getParents();
        boolean requiredtoSend = false;
        if (update.getLatestUpdate().getName().contains("PROC.MISSING.LIMIT")) {
            requiredtoSend = true;
        }
        
        for (Tag parent : list.values()) {
            if (parent.getLatestStatus().worserThan(Status.OK)) {
                logger.info("TagID=" + update.getId() + ": Metric value has changed. Parent is in "
                        + parent.getLatestStatus() + ". Notification to " + parent.getSubscribers().size()
                        + " subscribers is required to be send.");
                for (Subscription s : parent.getSubscribers()) {
                    
                    boolean toNotify = s.isEnabled() && (s.isNotifyOnMetricChange() || requiredtoSend) && s.isInterestedInLevel(parent.getLatestStatus());
                    
                    if (toNotify) {
                        Subscriber owner = registry.getSubscriber(s.getSubscriberId());
                        String body = textCreator.getTextForMetricUpdate(update, parent);
                        String subject = "Value changed for " + update.getLatestUpdate().getName();
                        if (s.isMailNotification()) {
                            notifyMail(owner, subject, body);
                        }
                        if (s.isSmsNotification()) {
                            notifySms(owner, subject);
                        }
                        s.setLastNotification(new Timestamp(System.currentTimeMillis()));
                    }
                }
            }
        }
    }

    /**
     * Sends a full report for the given Tag. Used for the initial notifications. ! We assume that the cache has already
     * been updated with the latest value !
     * 
     * @param update the Tag for this report.
     * @param s the {@link Subscription} for which we suppose to send the report.
     */
    public void sendInitialReport(Tag update) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entering sendFullReportOn");
        }

        for (Subscription s : update.getSubscribers()) {
            try {
                if (!s.getLastNotifiedStatus().equals(update.getLatestStatus())) {
                    HashSet<Tag> noGood = getProblemChildRules(update);
                    sendFullReportOn(update, s, noGood);
                    for (Tag t : noGood) {
                        s.setLastStatusForResolvedTSubTag(t.getId(), t.getLatestStatus());
                    }
                    s.setLastNotifiedStatus(update.getLatestStatus());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Leaving sendFullReportOn()");
        }
    }

    /**
     * @param update the {@link Tag} to send the notification for.
     * @param sub the subscription object.
     * @param interestingRuleTags a list of Tags which are RULES
     * @return a list of tags for which we have notified for.
     * @throws IOException
     * @throws TemplateException
     */
    private void sendFullReportOn(Tag update, Subscription sub, HashSet<Tag> interestingRuleTags) throws IOException,
            TemplateException {
        List<Tag> interestingTags = new ArrayList<Tag>();

        for (Tag t : interestingRuleTags) {
            interestingTags.addAll(t.getAllChildMetrics());
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("TagID " + update.getId() + " Sending full report");
            if (logger.isTraceEnabled()) {
                logger.trace(interestingTags);
            }
        }

        if (sub == null) {
            throw new IllegalStateException("Passed Argument of subscription object is null!");
        }

        String subject = textCreator.getMailSubjectForStateChange(update, interestingTags);
        String text = textCreator.getTextForRuleUpdate(update) + "\n "
                + textCreator.getFreeTextMapForChildren(interestingTags);
        if (sub.isEnabled() && sub.isInterestedInLevel(update.getLatestStatus())) {
            Subscriber user = registry.getSubscriber(sub.getSubscriberId());
            if (sub.isMailNotification()) {
                notifyMail(user, subject, text);
            }
            if (sub.isSmsNotification()) {
                notifySms(user, subject);
            }
        }
    }

    /**
     * Sends a message in case the passed Tag was updated. 
     * 
     * this method should only be used when the Tag REALLY updated.
     * 
     * @param update
     * @throws IOException
     * @throws TemplateException
     */
    public void sendReportOnRuleChange(Tag update) {

        logger.trace("Entering sendOnRuleChange()");

        if (update.isSourceDown()) {
            sendSourceAvailabilityReport(update);
            return;
        }

        if (update.getAllChildRules().size() == 0) {
            // R->M
            for (Subscription s : update.getSubscribers()) {
                try {
                    HashSet<Tag> interestingChildRules = new HashSet<Tag>();
                    interestingChildRules.add(update);
    
                    if (s.getTagId() != update.getId()
                            && !s.getLastStatusForResolvedSubTag(update.getId()).equals(update.getLatestStatus())) {
                        // an update of a rule which belongs to a higher rule
                        sendFullReportOn(update, s, interestingChildRules);
                        s.setLastStatusForResolvedTSubTag(update.getId(), update.getLatestStatus());
                    } else if (!s.getLastNotifiedStatus().equals(update.getLatestStatus())) {
                        // a direct rule-metric subscription
                        sendFullReportOn(update, s, interestingChildRules);
                        if (logger.isDebugEnabled()) {
                            logger.debug("TagID " + update.getId() + " Setting status " + update.getLatestStatus()
                                    + " for Subscriber " + s.getSubscriberId() + "[" + s.getTagId() + " " + s.getLastNotifiedStatus() + "]");
                        }
                        s.setLastNotifiedStatus(update.getLatestStatus());
                    } else {
                        logger.info("Update for TagID=" + update.getId() + " but no status change.");
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        } else {
            // R->R->M
            for (Subscription s : update.getSubscribers()) {
                HashSet<Tag> interestingChildRule = new HashSet<Tag>();
                if (s.getLastNotifiedStatus().equals(update.getLatestStatus())) {
                    // no change for the update (why do we receive it ?) 
                    // lets check if something has changed on the children side.
                    for (Tag cR : update.getAllChildRules()) {
                        if (!s.getLastStatusForResolvedSubTag(cR.getId()).equals(cR.getLatestStatus())) {
                            // add to interesting list.
                            interestingChildRule.add(cR);
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("ChildRule " + cR.getId() + " ["
                                        + s.getLastStatusForResolvedSubTag(cR.getId())
                                        + "] is not interesting for Subscription " + s.getSubscriberId() + " [" + s.getTagId() + " "
                                        + s.getNotificationLevel() + " " + s.getLastNotifiedStatus() + "]");
                            }
                        }
                    }
                } else {
                    interestingChildRule = getProblemChildRules(update);
                }

                // now real sending
                try {
                    if (interestingChildRule.size() > 0) {
                        sendFullReportOn(update, s, interestingChildRule);
                        for (Tag cR : interestingChildRule) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("TagID " + cR.getId() + " Setting status " + cR.getLatestStatus()
                                        + " for Subscriber " + s.getSubscriberId());
                            }
                            s.setLastStatusForResolvedTSubTag(cR.getId(), cR.getLatestStatus());
                        }
                    }
                    logger.debug("TagID " + update.getId() + " Setting status " + update.getLatestStatus()
                            + " for Subscriber " + s.getSubscriberId());
                    s.setLastNotifiedStatus(update.getLatestStatus());
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } // END FOR
        }

        logger.trace("Leaving sendOnRuleChange()");
    }

    // / --------------------------- END main Logic -------------------------------- \\\

    public void notifySms(Subscriber subscriber, String smsText) {
        if (logger.isDebugEnabled()) {
            logger.debug("Notifying via SMS to " + subscriber.getSms());
        }
        try {
            // mailer.sendEmail(subscriber.getSms(), null, smsText);
        } catch (Exception e) {
            logger.error("Error when sending notification sms to " + subscriber.getSms() + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void notifyMail(Subscriber subscriber, String subject, String text) {
        if (logger.isDebugEnabled()) {
            logger.debug("Notifying via Mail to " + subscriber.getEmail());
        }
        try {
            mailer.sendEmail(subscriber.getEmail(), subject, text);
        } catch (Exception e) {
            logger
                    .error("Error when sending notification mail to " + subscriber.getEmail() + " : " + e.getMessage(),
                            e);
        }
    }

    /**
     * Finds a list of changed children. This happens recursively for the passed argument
     * 
     * @param t the Tag for which to find all children tags (rules)
     * @return a list of tags which report themself as changed
     * @see Tag#hasStatusChanged()
     */
    public HashSet<Tag> getProblemChildRules(Tag t) {
        HashSet<Tag> result = new HashSet<Tag>();

        for (Tag c : t.getAllChildTagsRecursive()) {
            if (c.isRule() && c.getLatestStatus().worserThan(Status.OK)) {
                logger.debug("TagID=" + t.getId() + " : Adding interesting rule " + c.getId()
                        + " as it is worse than OK.");
                result.add(c);
            }
        }
        return result;
    }
}