/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.shared.Status;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.dmn2.db.DiamonDbGateway;
import cern.dmn2.db.MetricData;
import freemarker.template.TemplateException;

/**
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class ClientSubscription implements DataTagUpdateListener {

    final Subscription subscription;
    final Subscriber owner;
    
    HashSet<Long> myChildrenTagIds = new HashSet<Long>();
    HashMap<Long, Tag> tree = new HashMap<Long, Tag>();
    
    private Logger logger = Logger.getLogger(ClientSubscription.class);
    
    private TagCache cache = null;
    
    
    
    public ClientSubscription(Subscription subscription, Subscriber subscriber) {
        this.subscription = subscription;
        this.owner = subscriber;
        resolveTags();
    }
    
    
    @Autowired
    public void setTagCache(TagCache cache) {
        this.cache = cache;
    }
    
    public void resolveTags() {
        HashSet<Long> toSubscribe = new HashSet<Long>();
        
        /*
         * iterate over the RULE tags ids from the user. 
         */
        
            List<MetricData> list = DiamonDbGateway.getDbService().getEntityMetrics(subscription.getTagId());
            Tag parent = new Tag(subscription.getTagId(), true);
            
            /*
             * for each metric ...
             */
            for (MetricData md : list) {
                if (md.hasLimit()) {
                    /*
                     * of course it has, otherwise we couldn't subscribe to it, right?
                     * it doesn't make sense to subscribe to a metric itself as it does not have a threshold.
                     */
                    Tag childRule = tree.get(md.getLimitId());
                    
                    if (childRule == null) {
                        /*
                         * this rule was not created (yet). Do this on the fly..
                         */
                        childRule = new Tag(md.getId(), true);
                        
                        
                    } else if (childRule.getId() == parent.getId()) {
                        /*
                         * if the 
                         */
                        childRule = parent;
                    }
                    parent.addChildTag(childRule);
                    parent.addChildTag(new Tag(md.getId(), false));
                    
                    /*
                     * put both into our little personal fast-look-up list.
                     */
                    toSubscribe.add(md.getId());
                    toSubscribe.add(md.getLimitId());
                    tree.put(md.getId(), childRule);
                }
            }
        myChildrenTagIds = toSubscribe;
    }
    
    
    public HashSet<Long> getAllClientRuleTags() {
        return myChildrenTagIds;
    }

    public boolean alreadyNotified(Long tagId, Status status) {
        return false;
    }


    public boolean hasValueChanged(ClientDataTagValue before, ClientDataTagValue after) {
        boolean result = false;
        if (before.getTypeNumeric().equals(TypeNumeric.TYPE_DOUBLE)) {
            result = (Double) before.getValue() != (Double) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_INTEGER)) {
            result = ((Integer) before.getValue()) != (Integer) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_STRING)) {
            result = ((String) before.getValue()) != (String) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_FLOAT)) {
            result = ((Float) before.getValue()) != (Float) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_LONG)) {
            result = ((Long) before.getValue()) != (Long) after.getValue();
        } else {
            result = false;
        }
        return result;
    }


    @Override
    public void onUpdate(ClientDataTagValue update) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Update incoming : " + update.getName() + ", " + update.getId() + ", isRule=" + update.isRuleResult() + ", value= " + update.getValue());
            }
            
            if (!update.getDataTagQuality().isExistingTag()) {
                logger.warn("TagID=" + update.getId() + ": Does not known by the C2Mon server!");
                myChildrenTagIds.remove(update.getId());
                tree.remove(update.getId());
                return;
            }
            
            if (!update.getDataTagQuality().isValid()) {
                logger.warn("TagID=" + update.getId() + ": is not valid ! Skipping notification");
                return;
            }
            
            
            
            Tag fromCache = cache.get(update.getId());
            if (fromCache == null) {
                logger.warn("I have recevied an update " + update.getId() + " I am not (yet) aware of. Discarding...");
                //TODO UNSUBSCRIBE ?
                return;
            }
            
            /*
             * Metric and Rule updates from here on.
             * They are also known to us. 
             */
            
            /*
             * Metric processing
             */
            if (!update.isRuleResult()) {
                if (fromCache.getLatestUpdate() != null
                        // no last update available. Sure, why not if we just started up.
                        // but if we have one we should compare the values, if they have changed 
                        // and in case yes (and the parent rule is in ERROR) we send a re-notification.
                        
                    && hasValueChanged(fromCache.getLatestUpdate(), update)) {
                    
                    HashMap<Long, Tag> list = fromCache.getParents();
                    for (Tag parent : list.values()) {
                        if (parent.getLatestStatus().worserThan(Status.OK)) {
                        logger.info("TagID=" + update.getId() + ": Metric value has changed. Parent is in " + parent.getLatestStatus() + ". Notification is required to be send.");
                        // sendReportOnValueChange(fromCache.getLatestUpdate());
                        // TODO notification in case parentRule==Error and the metric value has changed. E.g. even higher CPU load.
                        }
                    }
                }
                
                cache.updateTag(update);
                return;
            }
            /*
             * Rule processing. Can only be 0,1,2
             */
            
            // save the previous state 
            ClientDataTagValue previous = fromCache.getLatestUpdate();
            // update the cache with latest rule result
            fromCache = cache.updateTag(update);
            
            if (previous == null) {
                // previous state not known.
                previous = fromCache.getLatestUpdate();
                logger.debug(
                        "TagID="  + update.getId() 
                        + ": Previous state was not available. New state is " 
                        + fromCache.getLatestStatus().toString());
                
                if (fromCache.getLatestStatus().worserThan(Status.OK)) {
//                    sendReportOnStatusChange(fromCache);
                }
            } else {
                if (!fromCache.hasStatusChanged()) {
                    logger.debug("TagID="  + update.getId() + ": Nothing to report ");
                    return;
                } else {
//                    sendReportOnStatusChange(fromCache);
                }
            }
        } catch (Exception e) {
            logger.error("Problems processing tag " + update.getId() + ": " + e.getMessage(), e);
        }
    }
    
    
    
    
    
    public void sendReportOnStatusChange(String mailSubject, String mailBody, Tag update) throws IOException, TemplateException {
        
        /*
         * if we are here, we have an entity which got an update
         */
//        int status = 0;
//        // the new status
//        if (update.getLatestUpdate().getTypeNumeric() == TypeNumeric.TYPE_INTEGER) {
//            status  = ((Integer) update.getLatestUpdate().getValue()).intValue();
//        } else {
//           throw new IllegalArgumentException("The passed status is not a valid one : " + status);
//        }
//        
//        
//        ArrayList<ClientDataTagValue> cdtvList = new ArrayList<ClientDataTagValue>();
//        for (Tag t : getInterestingTags(update)) {
//            ClientDataTagValue latest = t.getLatestUpdate(); 
//            if (t.getLatestUpdate() == null) {
//                // no latest value found - maybe we are not subscribed to this tag 
//                // or we haven't received an update for this (yet)
//                logger.error("TagID=" + update.getId() + ": No current value found for Tag=" + t.getId() + "!");
//                ClientDataTagValue fake = new ClientDataTagImpl(t.getId());
//                latest = fake;
//                //latest = getLatestUpdateForTag(t.getId());
//                //cache.get(t.getId()).update(latest);
//            }
//            //if (hasValueChanged(fromCache.getLatestUpdate(), update)
//            cdtvList.add(latest);
//        }
//        if (cdtvList.size() > 0) {
//            logger.info("TagID=" + update.getId() + ": Found " + cdtvList.size() + " children");
//        } else {
//            logger.warn("TagID=" + update.getId() + ": No interesting metrics found. Maybe race condition where the parent was faster than the child update? No notification will be send.");
//            //tagManager.getDataTags(update.getAllMetrics());
//            // TODO : should we try to get the latest update here from the server  and then process ?
//            //latest = getLatestUpdateForTag(t.getId());
//            //cache.get(t.getId()).update(latest);
//            return;
//        }
//        
//        String text = "";
//        text = textCreator.getTextForUpdate(update.getLatestUpdate());
//        text += "\n " + textCreator.getFreeTextMapForChildren(cdtvList);
//
//        if (logger.isTraceEnabled()) {
//            logger.trace("TagID=" + update.getId() + ": Created text : \n" + text.toString());
//        }
//
//        StringBuilder subject = new StringBuilder();
//        subject.append(update.getLatestUpdate().getName()).append(" has changed status to ").append(Status.fromInt(status));
//        
//        
//        if (subscription.isEnabled() && subscription.isInterestedInLevel(status)) {
//            
//            if (subscription.getLastNotifiedStatus() == status) {
//                /*
//                 * already notified on this status.
//                 */
//                logger.info("TagID=" + update.getId() + ": User '" + owner.getUserName() + "' was already notified on this rule with status " + status + " at " + subscription.getLastNotification() + ". Skipping.");
//            } else {
//                /*
//                 * store the latest update on this and send the notification
//                 */
//                subscription.setLastNotifiedStatus(status);
//                
//                if (subscription.isMailNotification()) {
//                    notifyMail(mailSubject, mailBody);
//                }
//                if (subscription.isSmsNotification()) {
//                    notifySms(subject.toString());
//                }
//            }
//        }
    }
    
    
    public void notifySms(String smsText) {
        if (logger.isDebugEnabled()) {
            logger.debug("Notifying " + owner.getSms());
        }
        try {
            //mailer.sendEmail(subscriber.getSms(), null, smsText);
        } catch (Exception e) {
            logger.error("Error when sending notification sms to " + owner.getSms() + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void notifyMail(String subject, String text) {
        if (logger.isDebugEnabled()) {
            logger.debug("Notifying " + owner.getEmail());
        }
        try {
            //mailer.sendEmail(subscriber.getEmail(), subject, text);
        } catch (Exception e) {
            logger.error("Error when sending notification mail to " + owner.getEmail() + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
        
        
}
    
