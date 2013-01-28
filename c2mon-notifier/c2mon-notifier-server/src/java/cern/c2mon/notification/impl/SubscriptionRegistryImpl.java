/**
 * 
 */
package cern.c2mon.notification.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.TagNotFoundException;
import cern.c2mon.notification.shared.UserNotFoundException;
import cern.dmn2.core.Status;
import cern.dmn2.db.DiamonDbGateway;
import cern.dmn2.db.PersonData;

/**
 * @author felixehm
 *
 */
public class SubscriptionRegistryImpl implements SubscriptionRegistry {
	
	/**
	 * our logger.
	 */
	private Logger logger = Logger.getLogger(SubscriptionRegistryImpl.class);

	/**
	 * our list of subscriptions organized by users. 
	 */
	ConcurrentHashMap<String, Subscriber> users = new ConcurrentHashMap<String, Subscriber>();
	
	/**
	 * a object to sync the backup writers and add/remove operations. 
	 */
	private Object accessLock = new Object();
	
	/**
	 * a list of long which contains all tagids for which subscriptions exists.
	 * Clearly for speed reasons introduced. 
	 * 
	 * Maybe obsoleted in case speed is enough.
	 */
	private HashSet<Long> tagIds = new HashSet<Long>();
	
	/**
	 * flag to indicated whether we auto-save every time a change to the registry happens.
	 */
	private long autoSaveLocal = 0;   // 30 seconds autosafe
	
	/**
	 * 
	 */
	private static long AUTO_SAVE_INTERVAL = 20000;
	
	/**
	 * our db writer.
	 */
	private DbBackupWriter dbWriter;
	
	/**
     * our local backup writer in case the db is malfunctioning.
     * You can set it with {@link #setBackUpWriter(BackupWriter)}.
     * 
     */
    private BackupWriter localBackupWriter;
    
    /**
     * a timestamp indicating the last modification to the registry. 
     */
    private long lastModificationTime = System.currentTimeMillis();
    
    /**
     * name of our local registry backup.
     * @see #localBackupWriter
     */
    private String localBackupFileName = "registry.backup";
	
    /**
     * our reference to the notifier who is responsible for the c2mon subscriptions.
     */
    @Autowired
    private TagCache tagCache;
    
    
	/**
	 * Empty constructor.
	 */
	public SubscriptionRegistryImpl() {
	    logger.info("SubscriptionRegistry created.");
	}
	
	/** Reload the config from the DB. 
	 * If this is not available it will use the {@link #backupWriter} to 
	 * read the latest snapshot on the local disk.
	 * 
	 */
	//@PostConstruct
    public void reloadConfig() {
	    
	    /*
	     * may have been set by externally
	     */
	    if (localBackupWriter == null) {
	        localBackupWriter = new FileBackUpWriter(getLocalBackupFileName());
	    }
	    
	    /*
	     *  load first from DB if possible.
	     */
        if (dbWriter != null) {
            logger.info("Attempting to load config from DB ...");
            try {
                dbWriter.isFine();  // may throw Exception 
                synchronized (users) {
                    users = dbWriter.load();
                    updateTagIdList();
                }
                localBackupWriter.store(users);
                logger.info("Loading data from DB was successful.");
            } catch (Exception ex) {
                logger.info("Could not load data from DB : " + ex.getMessage(), ex);
                users = localBackupWriter.load();
                updateTagIdList();
                setLastModificationTime(System.currentTimeMillis());
            }
        } else { 
    	    /*
    	     *  remote db didn't work or not configured. Reload locally.
    	     */
            logger.info("Loading data from local backup " + getLocalBackupFileName());
            users = localBackupWriter.load();
            updateTagIdList();
	        logger.info("Loading data from local backup was successful.");
	        /*
	         *  no db writer. We need a auto-saver
	         */
	        if (getAutoSaveInterval() == 0) {
	            setAutoSaveInterval(AUTO_SAVE_INTERVAL); // set auto-saving
	        }
        }
        if (logger.isTraceEnabled()) {
            logger.trace(this.toString());
        }
        
        logger.info("Registry load finished.");
	}	
	
	/**
	 * 
	 */
	public void start() {
	    /*
         *  check if autosaver is demanded and start it in case it is. 
         */
        if (getAutoSaveInterval() > 0) {
            logger.info("Creating default local backup file writer with fileName " + getLocalBackupFileName());
            
            startDatabaseBackupWriter(localBackupWriter);
            if (dbWriter != null) {
                startDatabaseBackupWriter(dbWriter);
            } 
        } else {
            // else : no local and no db backup. Everything is forgotten after restart.
            logger.info("No backup writer requested. All changes will be lost after restart!");
        }
	}
	
	/** Enables or disables writing regular local backups (independent from the {@link #dbWriter}). 
	 * 
	 * @param time the time in millis 
	 */
	@ManagedAttribute
	public void setAutoSaveInterval(long time) {
	    logger.info("Setting autoSaveInterval to " + time);
	    autoSaveLocal = time;
	}
	
	/**
	 * Stops all backup writers.
	 */
	private void stopWriters() {
	    // Should stop the backup writers
	    // TODO
	    autoSaveLocal = 0;
    }

    @ManagedAttribute
	public long getAutoSaveInterval() {
	    return autoSaveLocal;
	}
	
	/**
	 * @param writer an implementation of the {@link BackupWriter}.
	 */
	public void setDatabaseWriter(final DbBackupWriter writer) {
	    logger.info("Setting database writer.");
	    dbWriter = writer; 
	}
	
	/**
	 * Updates a subscriber. In case it does not exists it will be added.<br><br>
	 * <b>Note :</b> This call is rather slow for existing subscribers as it needs to compare the new 
	 * with old subscriptions in order to cancel or add tag subscriptions on the c2mon server.  
	 * 
	 * @param subscriber the {@link Subscriber} to set. 
	 * @throws TagNotFoundException in case one of the associated tags cannot be found.
	 */
    public void setSubscriber(Subscriber subscriber) throws TagNotFoundException {
	    checkNull(subscriber);

	    // a bit of a hack : we need to find the differences to cancel the c2mon subscriptions
	    if (logger.isTraceEnabled()) {
	        logger.trace("Entering setSubscriber() for user " + subscriber);
	    }
	    /*
	     * lets get the subscriber we have in the registry
	     */
	    Subscriber inReg = null;
	    
        inReg = getSubscriber(subscriber.getUserName());
	    inReg.setReportInterval(subscriber.getReportInterval());
	    
	    HashSet<Subscription> toStart = new HashSet<Subscription>();
	    /*
	     * iterate over the subscriptions from the NEW subscriber and ..
	     */
	    for (Entry<Long, Subscription> newSub : subscriber.getSubscriptions().entrySet()) {
	        if (inReg.getSubscription(newSub.getValue().getTagId()) == null) {
	            // .. add new subscriptions to the user
	            inReg.addSubscription(newSub.getValue());
	            toStart.add(newSub.getValue());
	        } 
	    }
	    if (tagCache != null) {
	        tagCache.startSubscription(toStart);
	    }
	    
	    /**
	     * iterate over subscriptions from the OLD subscriber and 
	     * remove (un-subscribe) them if they cannot be found in the NEW one
	     */
	    for (Subscription oldSub : inReg.getCopy().getSubscriptions().values()) {
	        if (subscriber.getSubscription(oldSub.getTagId()) == null) {
	            // remove Subscription
	            if (tagCache != null) {
	                tagCache.cancelSubscription(oldSub);
	            }
                inReg.removeSubscription(oldSub.getTagId());
	        } 
	    }
	    updateTagIdList();
	    
	    /**
	     * update all subscription objects with attributes from incoming subscriber
	     */
	    for (Subscription sameButPossiblyModified : subscriber.getSubscriptions().values()) {
	        Subscription existing = inReg.getSubscription(sameButPossiblyModified.getTagId()); 
	        existing.setEnabled(sameButPossiblyModified.isEnabled());
	        existing.setSmsNotification(sameButPossiblyModified.isSmsNotification());
	        existing.setMailNotification(sameButPossiblyModified.isMailNotification());
	        existing.setNotificationLevel(sameButPossiblyModified.getNotificationLevel());
	        existing.setNotifyOnMetricChange(sameButPossiblyModified.isNotifyOnMetricChange());
	    }
		
		setLastModificationTime(System.currentTimeMillis());

		/*
		 * we modify the passed object to and set it to the current object in our registry.
		 */
		subscriber = inReg;
		
		logger.trace("leaving setSubscriber()");
	}
	
	/**
	 * Iterates over ALL subscriptions and add the ids into an internal list. This operation is thread-safe.
	 * 
	 * @see SubscriptionRegistry#getAllRegisteredTagIds()  
	 */
	public void updateTagIdList() {
	    logger.trace("Entering updateTagIdList()");
		tagIds = new HashSet<Long>();
		for (Subscriber s : users.values()) {
			Iterator<Long> iter = s.getSubscribedTagIds().iterator();
			while (iter.hasNext()) {
				tagIds.add(iter.next());
			}
		}
		logger.trace("Leaving updateTagIdList()");
	}
	
	/**
	 * @return an exact copy of the registry. All changes to the result will not affect the original registry.
	 */
	public ConcurrentHashMap<String, Subscriber> getCopy() {
	    ConcurrentHashMap<String, Subscriber> result = new ConcurrentHashMap<String, Subscriber>();
	    synchronized (accessLock) {
	        for (Entry<String, Subscriber> e : users.entrySet()) {
	            result.put(e.getKey(), e.getValue().getCopy());
	        }
	    }
	    return result;
	}

	/**
	 * Adds a subscriber to the system. 
	 * @param user the {@link Subscriber} to add
	 * @throws TagNotFoundException in case one of the tags the user wants to subscribe does not exist.
	 */
	public void addSubscriber(Subscriber user) throws TagNotFoundException {
	    checkNull(user);
	    
	    logger.trace("Entering addSubscriber()" + user);

	    users.put(user.getUserName(), user);
        updateTagIdList();

	    /*
	     * start the subscription
	     */
	    if (tagCache != null) {
	        tagCache.startSubscription(new HashSet<Subscription>(user.getSubscriptions().values()));
        }
	    
	    setLastModificationTime(System.currentTimeMillis());
	    if (logger.isTraceEnabled()) {
	        logger.trace("Leaving addSubscriber() for user " + user.getUserName());
	    }
	}
	
	@Override
	public void addSubscription(Subscription subscription) {
	    checkNull(subscription);
	    String userId = subscription.getSubscriberId();

	    if (logger.isTraceEnabled()) {
	        logger.trace("Adding subscription for TagID=" + subscription.getTagId() + " to user " + subscription.getSubscriberId());
	    }
	    
	    // thread-safe in Subscriber object
	    Subscriber owner = getSubscriber(userId);
	    logger.trace("Subscriber : " + owner);
	    if (owner.getSubscriptions().containsKey(subscription.getTagId())) {
	        // throw an exception if the user is already subscribed ?
	        //throw new IllegalStateException("User is already subscribed to " + subscription.getTagId());
	    }
	    owner.addSubscription(subscription);
	    
        /*
         * we could also call updateTagList() but as we only add one, it is much faster.
         */
		if (!tagIds.contains(subscription.getTagId())) {
		    tagIds.add(subscription.getTagId());
		}
		
		// by default we start we OK.
		subscription.setLastNotifiedStatus(Status.OK);
		
		/*
		 * start the subscription
		 */
	    if (tagCache != null) {
	        HashSet<Subscription> toStart = new HashSet<Subscription>();
	        toStart.add(subscription);
	        try {
	            tagCache.startSubscription(toStart);
	        } catch (TagNotFoundException ex) {
	            tagIds.remove(subscription.getTagId());
	            owner.removeSubscription(subscription.getTagId());
	            throw ex;
	        }
        }
	    setLastModificationTime(System.currentTimeMillis());
	}
	
	/**
	 * Removes a Subscription and cancels all related sub-tags using the C2Mon API. 
	 * @param subscriber the {@link Subscriber} to cancel. 
	 */
	public void removeSubscriber(Subscriber subscriber) {
	    for (Subscription s : subscriber.getSubscriptions().values()) {
	        removeSubscription(s);
	    }
	    
	    users.remove(subscriber.getUserName());
	    updateTagIdList();
	}
	
	@Override
	public List<Subscription> getSubscriptionsForUser(Subscriber user) {
	    checkNull(user);
		if (users.get(user.getUserName()) != null) {
			return new ArrayList<Subscription>(users.get(user.getUserName()).getSubscriptions().values());
		} else {
			return new ArrayList<Subscription>();
		}
	}
	
	@Override
	public Subscriber getSubscriber(String userName) throws UserNotFoundException {
	    checkNull(userName);
	    
		if (users.containsKey(userName)) {
		    logger.debug("Found user " + userName + " : " + users.get(userName));
			return users.get(userName);
		} else {
		    PersonData p = DiamonDbGateway.getDbService().getPersonData(userName.toUpperCase());
		    if (p == null) {
		        throw new UserNotFoundException("User " + userName + " is not registered.");
		    } else {
		        Subscriber s = new Subscriber(userName, p.getMail(), p.getMobile());
		        logger.trace("Creating new Subscriber " + s);
		        users.put(s.getUserName(), s);
		        return s;
		    }
		}
	}

	
	/** Removes a subscription from the registry and calls the {@link NotifierImpl#cancelSubscription(Subscription)}.
	 * 
	 * @param subscription the subscription to remove.
	 * @throws UserNotFoundException in case the owner of this subscription does not even exist.
	 */
	public void removeSubscription(Subscription subscription) throws UserNotFoundException {
	    checkNull(subscription);
	    if (logger.isTraceEnabled()) {
	        logger.trace("entering removeSubscription() : USER=" + subscription.getSubscriberId() + " TagId=" + subscription.getTagId());
	    }
	    
	    Subscriber subscriber = getSubscriber(subscription.getSubscriberId());
	    subscription = subscriber.getSubscription(subscription.getTagId());
	    
		subscriber.removeSubscription(subscription.getTagId());
		updateTagIdList();
		
		/*
		 * we need to tell the notifier to remove the subscription
		 */
		if (tagCache != null) {
		    tagCache.cancelSubscription(subscription);
		}
		
		setLastModificationTime(System.currentTimeMillis());
	}
	
	/**
	 * @return HashMap<Subscriber, Subscription> A list of Subscribers and their subscriptions.
	 * @param tagId the id of the tag
	 */
	public HashMap<Subscriber, Subscription> getSubscriptionsForTagId(Long tagId) {
		HashMap<Subscriber, Subscription> result = new HashMap<Subscriber, Subscription>();
		for (Subscriber s : users.values()) {
			Subscription toAdd = s.getSubscriptions().get(tagId);
			if (toAdd != null) {
				result.put(s, toAdd);
			}
		}
		return result;
	}
	
	/**
	 * @return a list of all current registered subscribers.
	 */
	public List<Subscriber> getRegisteredUsers() {
		return new ArrayList<Subscriber>(users.values());
	}
	
	/**
	 * @return a list of current registered subscriptions
	 */
	public HashSet<Subscription> getRegisteredSubscriptions() {
	    HashSet<Subscription> result = new HashSet<Subscription>();
	    for (Subscriber s : users.values()) {
	        for (Subscription sup : s.getSubscriptions().values()) {
	            result.add(sup);
	        }
	    }
	    return result;
	}
	
	/**
	 * @return a string representation of the registry.
	 */
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("Registered Users:\n");
		for (Subscriber user : users.values()) {
			ret.append(user.toString());
		}
		return ret.toString();
	}

	@Override
	public HashSet<Long> getAllRegisteredTagIds() {
		return this.tagIds;
	}

	/**
	 * 
	 * @param backupWriter the BackupWriter to use when the {@link #dbWriter} is not working.
	 */
    public void setBackUpWriter(BackupWriter backupWriter) {
        this.localBackupWriter = backupWriter;
    }

    /**
     * @return Returns the localBackupFileName.
     */
    public String getLocalBackupFileName() {
        return localBackupFileName;
    }

    /**
     * @param localBackupFileName The localBackupFileName to set.
     */
    public void setLocalBackupFileName(String localBackupFileName) {
        this.localBackupFileName = localBackupFileName;
    }
    
    /**
     * 
     * @param arg the object to check for.
     */
    private void checkNull(Object arg) {
        if (arg == null) {
            throw new IllegalArgumentException("Passed argument is null! : ");
        }
    }
    /**
     * Starts a {@link BackupWriter} instance in a thread.
     *  
     * @param writer the {@link BackupWriter} to start.
     */
    private void startDatabaseBackupWriter(final BackupWriter writer) {
        new Thread(new Runnable() {
            public void run() {
                logger.trace("Starting DB BackupWriter.");
                
                while (getAutoSaveInterval() > 0) {
                    try {
                        Thread.sleep(getAutoSaveInterval());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        logger.error("Stopping DB BackupWriter as it was interruped: " + e.getMessage());
                        break;
                    }
                    try {
                        logger.trace("Checking if we need to write registry to DB ...");
                        if (getLastModificationTime() > writer.getLastFullWrite()) {
                            logger.debug("Storing Registry into DB");
                            writer.store(getCopy());
                            logger.trace("Entries stored in DB.");
                        } else {
                            logger.trace("No storage as no modification took place.");
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }, writer.getUniqueName()).start();
     }

    /**
     * @param lastModificationTime The lastModificationTime to set.
     */
    public void setLastModificationTime(long lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    /**
     * calls {@link #setLastModificationTime(long)} with the current Time.
     */
    public void updateLastModificationTime() {
        setLastModificationTime(System.currentTimeMillis());
    }
    
    /**
     * @return Returns the lastModificationTime.
     */
    public long getLastModificationTime() {
        return lastModificationTime;
    }

    /**
     * @return Returns the notifier.
     */
    public TagCache getTagCache() {
        return tagCache;
    }

    /**
     * @param tagCache The {@link TagCache} to set.
     */
    public void setTagCache(TagCache tagCache) {
        this.tagCache = tagCache;
    }
    
}