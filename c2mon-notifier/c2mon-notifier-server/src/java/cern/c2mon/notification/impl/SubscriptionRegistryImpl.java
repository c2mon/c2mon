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

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import cern.accsoft.commons.util.Trace;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.UserNotFoundException;

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
	protected HashMap<String, Subscriber> users = new HashMap<String, Subscriber>();
	
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
	protected HashSet<Long> tagIds = new HashSet<Long>();
	
	/**
	 * flag to indicated whether we auto-save every time a change to the registry happens.
	 */
	private long autoSaveLocal = 0;   // 30 seconds autosafe
	
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
    private NotifierImpl notifier;
    
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
	@PostConstruct
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
                users = dbWriter.load();
                updateTagIdList();
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
	            setAutoSaveInterval(20000); // 20 sec auto-saving
	        }
        }
        logger.info("Loaded " + users.size() + " subscribers from DB");
	}	
	
	
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
	public void setDatabaseWriter(DbBackupWriter writer) {
	    logger.info("Setting database writer.");
	    dbWriter = writer; 
	}
	
	/**
	 * Updates a subscriber. In case it does not exists it will be added.<br><br>
	 * <b>Note :</b> This call is rather slow for existing subscribers as it needs to compare the new 
	 * with old subscriptions in order to cancel or add tag subscriptions on the c2mon server.  
	 * 
	 * @param subscriber the {@link Subscriber} to set. 
	 */
    @SuppressWarnings("unchecked")
    public void setSubscriber(Subscriber subscriber) {
	    checkNull(subscriber);
	    
	    // a bit of a hack : we need to find the differences to cancel the c2mon subscriptions
	    HashSet<Long> before = null;
	    if (logger.isTraceEnabled()) {
	        logger.trace("Entering setSubscriber() for user " + subscriber);
	    }
	    
		synchronized (accessLock) {
		    /*
		     * get the internal tag list and make a copy.
		     */
		    before = (HashSet<Long>) getAllRegisteredTagIds().clone();
		    
		    /*
		     * make the changes to the reg and ...
		     */
			users.put(subscriber.getUserName(), subscriber);
			/*
			 *  ... update the internal tag list 
			 */
			updateTagIdList();
		}	
		
		/*
		 * find the differences between new and old list 
		 * and tell the notifier to cancel the tags which have been been found. 
		 */
		Iterator<Long> i = before.iterator();
		
		HashSet<Long> toRemove = new HashSet<Long>();
		HashSet<Long> now = getAllRegisteredTagIds();
		while (i.hasNext()) {
		    Long tagId = i.next();
		    if (!now.contains(tagId)) {
		        toRemove.add(tagId);
		    }
		}
		if (notifier != null && toRemove.size() > 0) {
		    logger.debug("Cancelling the following tagid subscriptions : " + toRemove.toString());
		    notifier.cancelSubscription(toRemove);
		}
		setLastModificationTime(System.currentTimeMillis());
		
		logger.trace("leaving setSubscriber()");
	}
	
	/**
	 * updates the internal tag list which. 
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
	public HashMap<String, Subscriber> getCopy() {
	    HashMap<String, Subscriber> result = new HashMap<String, Subscriber>();
	    synchronized (accessLock) {
	        for (Entry<String, Subscriber> e : users.entrySet()) {
	            result.put(e.getKey(), e.getValue().getCopy());
	        }
	    }
	    return result;
	}
		
	public void addSubscriber(Subscriber user) {
	    checkNull(user);
	    
	    logger.trace("Entering addSubscriber()" + user);
	    
	    if (notifier != null) {
	        notifier.startSubscription(new HashSet<Long>(user.getSubscribedTagIds()));
	    }
	    
	    synchronized (accessLock) {
            users.put(user.getUserName(), user);
            updateTagIdList();
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
	    getSubscriber(userId);

	    HashSet<Long> toCheck = new HashSet<Long>();
	    toCheck.add(subscription.getTagId());
	    
	    if (notifier != null) {
	        notifier.startSubscription(toCheck);
	    }
	    
		synchronized (users) {
		    users.get(userId).addSubscription(subscription);
        }
		if (!tagIds.contains(subscription.getTagId())) {
    		synchronized (tagIds) {
    		    tagIds.add(subscription.getTagId());
            }
		}
		setLastModificationTime(System.currentTimeMillis());
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
			return users.get(userName);
		} else {
			throw new UserNotFoundException("User " + userName + " is not registered for notification");
		}
	}

	
	/**
	 * 
	 * @param subscription the subscription to remove.
	 * @throws UserNotFoundException in case the owner of this subscription does not even exist.
	 */
	public void removeSubscription(Subscription subscription) throws UserNotFoundException {
	    checkNull(subscription);
	    if (logger.isTraceEnabled()) {
	        logger.trace("entering removeSubscription() : USER=" + subscription.getSubscriberId() + " TagId=" + subscription.getTagId());
	    }
	    
		getSubscriber(subscription.getSubscriberId()).removeSubscription(subscription);
		updateTagIdList();
		
		/*
		 * we need to tell the notifier to remove the subscription
		 */
		if (notifier != null) {
		    if (!getAllRegisteredTagIds().contains(subscription.getTagId())) {
		        logger.info("Removing subscription for ID=" + subscription.getTagId());
		        // only remove c2mon subscription if there is no one left who is interested. 
		        HashSet<Long> toRemove = new HashSet<Long>();
		        toRemove.add(subscription.getTagId());
		        notifier.cancelSubscription(toRemove);
		    }
		}
		
		setLastModificationTime(System.currentTimeMillis());
	}
	
	/**
	 * 
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
	 * 
	 */
	public List<Subscriber> getRegisteredUsers() {
		return new ArrayList<Subscriber>(users.values());
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
                while (getAutoSaveInterval() > 0) {
                    try {
                        Thread.sleep(getAutoSaveInterval());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    try {
                        if (getLastModificationTime() > writer.getLastFullWrite()) {
                            logger.debug("Storing Registry into DB");
                            writer.store(getCopy());
                        } else {
                            logger.debug("No storage as no modification took place.");
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
     * @return Returns the lastModificationTime.
     */
    public long getLastModificationTime() {
        return lastModificationTime;
    }
    
}