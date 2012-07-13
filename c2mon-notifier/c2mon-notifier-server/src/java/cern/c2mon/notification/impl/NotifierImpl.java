package cern.c2mon.notification.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.annotation.ManagedOperation;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.notification.Mailer;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TagCacheUpdateListener;
import cern.c2mon.notification.TextCreator;
import cern.c2mon.notification.shared.Status;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.tim.shared.common.datatag.TagQualityStatus;
import freemarker.template.TemplateException;

/**
 * A Daemon which notifies clients about errors reported by the C2MON server.
 * 
 * 
 * 
 * @author felixehm
 * 
 */
public class NotifierImpl implements TagCacheUpdateListener {

	/**
	 * our Logger.
	 */
	private Logger logger = Logger.getLogger(NotifierImpl.class);

	/**
	 * our registry which keep information on who is registered to what.
	 */
	private SubscriptionRegistry registry = null;

	/**
	 * The {@link C2monTagManager} reference, so we can subscribe to new
	 * datatags or unsubscribe from existing.
	 */
	//private C2monTagManager tagManager;

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
	 * Constructor. starts also {@link C2monServiceGateway}
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
	 * 
	 * @param mailerService the {@link Mailer} service which is required for sending the Mails /SMS.
	 */
	@Required
	public void setMailer(Mailer mailerService) {
	    logger.debug("Setting mailer.");
		this.mailer = mailerService;
	}
	
	@Required
	public void setTextCreator(TextCreator textCreator) {
	    logger.debug("Setting text creator.");
	    this.textCreator = textCreator;
	}
	
	/**
	 * 
	 * @param cache the {@link TagCache} object to set.
	 */
	@Required
	public void setTagCache(TagCache cache) { 
	    if (cache == null) {
	        throw new IllegalArgumentException("passed object for TagCache is null!");
	    }
	    this.cache = cache;
	}
	
	/**
	 * Starts the subscription to the c2mon system. Requires the {@link #registry} to be initialised with {@link #setSubscriptioRegistry(SubscriptionRegistry)}.
	 * @see NotifierImpl#setSubscriptioRegistry(SubscriptionRegistry)
	 */
	@PostConstruct
	public void start() {
	    logger.info("Starting....");
	    
		if (registry == null) {
			throw new IllegalStateException("The SubscriptionRegistry was not set. Please call setSubscriptionRegistry(..)");
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
		//reloadConfig();

		registry.setTagCache(cache);
           
		logger.info("Step 3 of 4 : Starting local tag cache writer...");
        cache.startBackupWriter();
        
        logger.info("Step 4 of 6 : Starting registry writer...");
        registry.start();
        
        /*
         * tell the cache where to find the registry in case the cache detects invalid subscriptions during the next call.
         */
        cache.registerListener(this);
        cache.setRegistry(registry);
        logger.info("Step 5 of 6 : Starting subscriptions...");
        cache.startSubscription(registry.getRegisteredSubscriptions());
        
        
        logger.info("Notification service fully started.");
    }
	
	@ManagedOperation(description="Triggers a reload of all subscriptions and their sub-tag ids.")
	public void reloadConfig() {
	    /*
         * get the parent tag id's from the user subscriptions
         */
	    registry.reloadConfig();
	    HashSet<Long> registeredSubscriptionIds = registry.getAllRegisteredTagIds();
	    
	    /*
         * resolve the ids to their Tag object add them to the cache.
         * Here, also all children of this id are linked and resolved.
         */
	    cache.resolveSubTags(registeredSubscriptionIds);
	    
        /*
         * reloads tag cache from disk.
         */
	    try {
	        //cache.initStatusFromPersistence();
	    } catch (Exception e) {
            // IGNORE
        }
        

        
        logger.info("Finished updating cache from server.");
	}
	

	@Override
    public void onUpdate(Tag incoming) {
	     ClientDataTagValue update = incoming.getLatestUpdate();
	     
	     logger.debug("TagID=" + update.getId() + ": Processing update. ");
	     
	     try {
            /*
             * Metric processing
             */
    		if (!incoming.isRule()) {
    		    if (incoming.getLatestUpdate() != null
    		            // no last update available. Sure, why not if we just started up.
    		            // But if we have one, we should compare the values if they have changed 
    		            // and in case yes (and the parent rule is in ERROR) we send a re-notification.
    		         && incoming.hasValueChanged()) {
      		        sendReportOnValueChange(incoming);
    		    }
    		    logger.trace("TagID=" + update.getId() + ": updating value as it has changed");
    		    return;
    		}

    		/*
             * Rule processing. Can only be 0,1,2 OR down/up
             */
    		for (Tag parent : incoming.getParents().values()) {
    		    if (!parent.getLatestStatus().betterThan(incoming.getLatestStatus())) {
    		        // child was faster than parent. No notification
    		        logger.debug("TagID=" + update.getId() + ":Child was faster than parent. No notification send.");
    		        return;
    		    }
    		}
    		
    		
    		if (incoming.isSourceDown() && !incoming.wasSourceDown()) {
    		    logger.info("TagID=" + update.getId() + ": Sending source 'is not accessible' notification");    
    		    sendSourceAvailabilityMessage(incoming);
            } else if (incoming.wasSourceDown()) {
                logger.info("TagID=" + update.getId() + ": Sending source 'is accessible' notification.");
                sendSourceAvailabilityMessage(incoming);
            } else {
                if (incoming.hasStatusChanged()) {
                    sendReportOnStatusChange(incoming);
                } else {
                   logger.info("TagID=" + update.getId() + ": Status has not changed. No notifications are sent."); 
                }
            }
		} catch (Exception e) {
            logger.error("Problems processing tag " + update.getId() + ": " + e.getMessage(), e);
        }
	}
		
		
	public void sendSourceAvailabilityMessage(Tag update) throws IOException, TemplateException {
	    String text = textCreator.getTextForSourceDown(update);
	    HashSet<Subscription> subscribers = update.getSubscribers();
        logger.trace("Got " + subscribers.size() + " which are interested in this tag.");
        for (Subscription s : subscribers) {
            notifyMail(registry.getSubscriber(s.getSubscriberId()), update.getLatestUpdate().getDataTagQuality().getDescription() , text);
        }
	}
	
	public void sendReportOnValueChange(Tag update) {
	    
	    HashMap<Long, Tag> list = update.getParents();
	    for (Tag parent : list.values()) {
            if (parent.getLatestStatus().worserThan(Status.OK)) {
                logger.info("TagID=" + update.getId() + ": Metric value has changed. Parent is in " + parent.getLatestStatus() + ". Notification is required to be send.");
                for (Subscription s : update.getSubscribers()) {
                    if (s.isEnabled() && s.isNotifyOnMetricChange() && s.isInterestedInLevel(parent.getLatestStatusInt())) {
                        Subscriber owner = registry.getSubscriber(s.getSubscriberId());
                        String body = textCreator.getTextForMetricUpdate(update);
                        String subject = "Value changed for " + update.getLatestUpdate().getValueDescription() + " to " + update.getLatestUpdate().getValue();
                        if (s.isMailNotification()) {
                            notifyMail(owner, subject, body);
                        } 
                        if (s.isSmsNotification()) {
                            notifySms(owner, subject);
                        }
                    }
                }
            }
        }
	}
	 
	
	public void sendReportOnStatusChange(Tag update) throws IOException, TemplateException {
		    
	    /*
         * if we are here, we have an entity which got an update
         */
	    int status  = update.getLatestStatusInt();
        
        ArrayList<ClientDataTagValue> cdtvList = new ArrayList<ClientDataTagValue>();
        List<Tag> interestingTags = getInterestingTags(update);
        
        for (Tag t : interestingTags) {
            ClientDataTagValue latest = t.getLatestUpdate(); 
            if (latest == null) {
                
                // no latest value found - maybe we are not subscribed to this tag 
                // or we haven't received an update for this (yet)
                logger.warn("TagID=" + update.getId() + ": No current value found for Tag=" + t.getId() + "!");
                HashSet<Long> toGet = new HashSet<Long>();
                toGet.add(t.getId());
                Collection<ClientDataTagValue> oneList = C2monServiceGateway.getTagManager().getDataTags(toGet);
                latest = oneList.iterator().next();
                
                //ClientDataTagValue fake = new ClientDataTagImpl(t.getId());
                //latest = fake;
            }
            cdtvList.add(latest);
        }
        if (cdtvList.size() > 0) {
            logger.info("TagID=" + update.getId() + ": Found " + cdtvList.size() + " children");
        } else {
            logger.warn("TagID=" + update.getId() + ": No interesting metrics found. Maybe race condition where the parent was faster than the child update? No notification will be send.");
            //tagManager.getDataTags(update.getAllMetrics());
            // TODO : should we try to get the latest update here from the server  and then process ?
            //latest = getLatestUpdateForTag(t.getId());
            //cache.get(t.getId()).update(latest);
            return;
        }
        
		String text = textCreator.getTextForRuleUpdate(update.getLatestUpdate());
        text += "\n " + textCreator.getFreeTextMapForChildren(cdtvList);

		if (logger.isTraceEnabled()) {
			logger.trace("TagID=" + update.getId() + ": Created text : \n" + text.toString());
		}

		StringBuilder subject = new StringBuilder();
		if (cdtvList.size() == 1) {
		    ClientDataTagValue single = cdtvList.iterator().next();
		    subject.append(TextCreator.statusToTransitionText(single)).append(" ").append(single.getDescription());
		} else {
		    subject.append(update.getLatestUpdate().getName()).append(" has changed status to ").append(Status.fromInt(status));
		}
    	
    	/*
    	 * iterate over Subscribers and their Subscriptions
    	 */
		HashSet<Subscription> subscribers = update.getSubscribers();
		logger.trace("Got " + subscribers.size() + " which are interested in this tag.");
		Status stat = Status.fromInt(status);
		if (stat.equals(Status.UNKNOWN)) {
            logger.warn("TagID=" + update.getId() + ": could not resolve status '" + status + "' (int value)");
            return;
        }
		
	    for (Subscription sub : subscribers) {
			Subscriber s = registry.getSubscriber(sub.getSubscriberId());

			for (Tag t : interestingTags) {
    			if (sub.getLastStatusForResolvedSubTag(t.getId()).equals(stat)) {
    			    /*
    			     * already notified on this status.
    			     */
    			    logger.info("TagID=" + update.getId() + ": User '" + s.getUserName() + "' was already notified on this rule with status " + status + " at " + sub.getLastNotification() + ". Skipping.");
    			    continue;
    			}
    			if (!sub.isEnabled()) {
    			    logger.trace("TagID=" + update.getId() + ": Subscription is not enabled for user '" + s.getUserName());
    			    continue;
    			}
    			
    			if (!sub.isInterestedInLevel(status)) {
    			    logger.trace("TagID=" + update.getId() + ": Subscription for user " + s.getUserName() + " is not interested for level '" + status);
                    continue;
    			}
    			
    		    /*
    		     * store the latest update on this and send the notification
    		     */
    		    sub.setLastNotifiedStatus(status);
    		    sub.setLastStatusForResolvedTSubTag(t.getId(), stat);
    		    
    			if (sub.isMailNotification()) {
    				notifyMail(s, subject.toString(), text.toString());
    			}
    			if (sub.isSmsNotification()) {
    				notifySms(s, subject.toString());
    			}
			}
			
	    }
	}
	
	
	
	public void notifySms(Subscriber subscriber, String smsText) {
		if (logger.isDebugEnabled()) {
			logger.debug("Notifying via SMS to " + subscriber.getSms());
		}
		try {
			//mailer.sendEmail(subscriber.getSms(), null, smsText);
		} catch (Exception e) {
			logger.error("Error when sending notification sms to " + subscriber.getSms() + " : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void notifyMail(Subscriber subscriber, String subject, String text) {
		if (logger.isDebugEnabled()) {
			logger.debug("Notifying via SMS to " + subscriber.getEmail());
		}
		try {
			mailer.sendEmail(subscriber.getEmail(), subject, text);
		} catch (Exception e) {
			logger.error("Error when sending notification mail to " + subscriber.getEmail() + " : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public List<Tag> getInterestingTags(Tag tag) {
	    ArrayList<Tag> result = new ArrayList<Tag>();
        
        /*
         * we need to add ourself as we may have children
         */
        if (!tag.isRule()) {
            return result;
        }
        for (Tag c : tag.getChildTags()) {
            if (c.isRule()) {
                List<Tag> problemChilds = getInterestingTags(c);
                result.addAll(problemChilds);
            } else {
                if (tag.hasStatusChanged()) {
                    /*
                     * our own metric tag should be in the rule expression, right ?
                     * otherwise : && tag.getLatestUpdate().getRuleExpression().getInputTagIds().contains(c.getId())
                     */
                    result.add(c);
                } else {
                    logger.debug("Value has not changed for metric tag " + c.getId());
                }
            }
        }
        return result;
	}
	
}