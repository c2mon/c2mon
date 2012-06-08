package cern.c2mon.notification.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jmx.export.annotation.ManagedOperation;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.manager.TagManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.notification.Mailer;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TextCreator;
import cern.c2mon.notification.shared.ServiceException;
import cern.c2mon.notification.shared.Status;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import freemarker.template.TemplateException;

/**
 * A Daemon which notifies clients about errors reported by the C2MON server.
 * 
 * 
 * 
 * @author felixehm
 * 
 */
public class NotifierImpl implements DataTagUpdateListener {

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
	private C2monTagManager tagManager;

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
	@Autowired
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
		
		reloadConfig();

		/**
         * immediate notification after startup
         */
        for (Long id : registry.getAllRegisteredTagIds()) {
            try {
                Tag tag = cache.get(id);
                if (tag.hasStatusChanged()) {
                    logger.debug("Sending initial report for Tag " + id);
                    sendReportOnStatusChange(tag);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
           
        // real subscription here :
        
		
		logger.info("Step 3 of 4 : Starting local tag cache writer.");
        cache.start();
        
        logger.info("Step 4 of 4 : Starting subscription.");
        C2monServiceGateway.getTagManager().subscribeDataTags(cache.getTagIds(), this);
        
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
         * resolve the tag ids to their sub-tag ids.
         */
	    cache.initAndResolve(registeredSubscriptionIds);
        /*
         * reloads tag cache from disk.
         */
	    try {
	        cache.initStatusFromPersistence();
	    } catch (Exception e) {
            // IGNORE
        }
        
        long t1 = System.currentTimeMillis();
        logger.info("Reading latest values from server " + cache.getTagIds().size() + " registered data tags ...");
        
        Collection<ClientDataTagValue> updatesFromServer = C2monServiceGateway.getTagManager().getDataTags(cache.getTagIds());
        logger.info("Got " + updatesFromServer.size() + " updates from server within " + (System.currentTimeMillis() - t1) + "msec");
        
        for (ClientDataTagValue cdtv : updatesFromServer) {
            logger.trace("Updating initial value from server : " + cdtv.getId() + " with value " + cdtv.getValue() + ", isValid=" + cdtv.isValid());
            cache.updateTag(cdtv);
        }
        
        try {
            FileWriter f = new FileWriter(new File("test.cache"));
            f.append(cache.toString());
            f.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        logger.info("Finished updating cache from server.");
	}
	
	/** Resolves all sub-metrics from each element in <code>s</code> and using {@link TagCache#metricTagResolver(Tag, HashMap)} 
	 *   and calls {@link TagManager#subscribeDataTags(java.util.Set, DataTagUpdateListener)}
	 * 
	 * @param s the list of tags to subscribe to.
	 */
	public void startSubscription(HashSet<Long> s) {
	    Tag resolved = null;
	    try {
	        HashSet<Long> toSubscribeTo = new HashSet<Long>();
	        for (Long i : s) {
	            // add to TagCache
	            resolved = cache.metricTagResolver(new Tag(i, true), null);
	            
	            for (Tag child : resolved.getChildTags()) {
	                toSubscribeTo.add(child.getId());
	            }
	        }
	        
	        tagManager.subscribeDataTags(toSubscribeTo, this);
	        
	    } catch (Exception ex) {
	        throw new ServiceException("Cannot subscribe to datatag : " + ex.getMessage());
	    }
	}
	
	/** Resolves all related sub-tags for each element and  
	 * 
	 * @param tagIDs
	 * @throws ServiceException
	 */
	public void cancelSubscription(HashSet<Long> tagIDs) throws ServiceException {
        try {
            /*
             * our list which will contain all tags which are related to each element in the tagIds
             */
            HashMap<Long, Tag> toRemove = new HashMap<Long, Tag>();
            
            /*
             * let's resolve them
             */
            for (Long i : tagIDs) {
                Tag toResolve = new Tag(i, false);
                cache.metricTagResolver(toResolve, toRemove);
            }
            /*
             * got everything, let's cancel the the subscription.
             */
            tagManager.unsubscribeDataTags(toRemove.keySet(), this);

        } catch (Exception ex) {
            throw new ServiceException(ex.getMessage());
        }
        
        for (Long i : tagIDs) {
            cache.removeTag(i);
        }
	}
	
	
	
	
	/**
	 * processes and incoming update from the server.
	 * 
	 * @param update
	 *            The update from the C2MON server.
	 */
	public void onUpdate(ClientDataTagValue update) {
	 	
		try {
    		if (logger.isDebugEnabled()) {
    		    logger.debug("Update incoming : " + update.getName() + ", " + update.getId() + ", isRule=" + update.isRuleResult() + ", value= " + update.getValue());
    		}
    		
    		if (!update.getDataTagQuality().isExistingTag()) {
    		    logger.warn("TagID=" + update.getId() + ": Does not known by the C2Mon server!");
    		    for (Subscription s : registry.getSubscriptionsForTagId(update.getId()).values()) {
                    registry.removeSubscription(s);
                    cache.removeTag(update.getId());
                }
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
    		            // But if we have one, we should compare the values if they have changed 
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
    		        sendReportOnStatusChange(fromCache);
    		    }
    		} else {
    		    if (!fromCache.hasStatusChanged()) {
    		        logger.debug("TagID="  + update.getId() + ": Nothing to report ");
    		        return;
    		    } else {
    		        sendReportOnStatusChange(fromCache);
    		    }
		    }
    		
    		
    		
		} catch (Exception e) {
            logger.error("Problems processing tag " + update.getId() + ": " + e.getMessage(), e);
        }
	}
		
		
	public void sendReportOnStatusChange(Tag update) throws IOException, TemplateException {
		    
	    /*
         * if we are here, we have an entity which got an update
         */
        int status = 0;
        TypeNumeric type = update.getLatestUpdate().getTypeNumeric();
        // the new status
        if (type == TypeNumeric.TYPE_INTEGER || type == TypeNumeric.TYPE_DOUBLE) {
            status  = ((Double) update.getLatestUpdate().getValue()).intValue();
        } else {
            /*
             * do not run into danger of processing something we can't handle. 
             */
            logger.warn(
                    "TagID=" + update.getId() 
                    + ": status is not of Type " 
                    + cern.c2mon.client.common.tag.TypeNumeric.TYPE_INTEGER 
                    + " or " 
                    + cern.c2mon.client.common.tag.TypeNumeric.TYPE_DOUBLE 
                    + "  but of " + type);
            return;
        }
        
        
        HashMap<Subscriber, Subscription> subscribers = registry.getSubscriptionsForTagId(update.getId());
        // somebody interested ? 
        if (subscribers.isEmpty()) {
            logger.debug("TagID=" + update.getId() + ": Nobody is interested");
            return;
        }
        
        ArrayList<ClientDataTagValue> cdtvList = new ArrayList<ClientDataTagValue>();
        for (Tag t : getInterestingTags(update)) {
            ClientDataTagValue latest = t.getLatestUpdate(); 
            if (t.getLatestUpdate() == null) {
                // no latest value found - maybe we are not subscribed to this tag 
                // or we haven't received an update for this (yet)
                logger.error("TagID=" + update.getId() + ": No current value found for Tag=" + t.getId() + "!");
                ClientDataTagValue fake = new ClientDataTagImpl(t.getId());
                latest = fake;
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
        
		String text = "";
        text = textCreator.getTextForUpdate(update.getLatestUpdate());
        text += "\n " + textCreator.getFreeTextMapForChildren(cdtvList);

		if (logger.isTraceEnabled()) {
			logger.trace("TagID=" + update.getId() + ": Created text : \n" + text.toString());
		}

		StringBuilder subject = new StringBuilder();
		subject.append(update.getLatestUpdate().getName()).append(" has changed status to ").append(Status.fromInt(status));
    		
    	/*
    	 * iterate over Subscribers and their Subscriptions
    	 */
	    for (Entry<Subscriber, Subscription> entry : subscribers.entrySet()) {
    		
			Subscriber s = entry.getKey();
			Subscription sub = entry.getValue();
			
			if (sub.getLastNotifiedStatus() == status) {
			    /*
			     * already notified on this status.
			     */
			    logger.info("TagID=" + update.getId() + ": User '" + s.getUserName() + "' was already notified on this rule with status " + status + " at " + sub.getLastNotification() + ". Skipping.");
			    continue;
			}
			
			if (sub.isEnabled() && sub.isInterestedInLevel(status)) {
			    /*
			     * store the latest update on this and send the notification
			     */
			    sub.setLastNotifiedStatus(status);
			    
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
			logger.debug("Notifying " + subscriber.getSms());
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
			logger.debug("Notifying " + subscriber.getEmail());
		}
		try {
			//mailer.sendEmail(subscriber.getEmail(), subject, text);
		} catch (Exception e) {
			logger.error("Error when sending notification mail to " + subscriber.getEmail() + " : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	public ClientDataTagValue getLatestUpdateForTag(Long id) {
	    HashSet<Long> toGet = new HashSet<Long>();
	    toGet.add(id);
	    return tagManager.getDataTags(toGet).iterator().next();
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