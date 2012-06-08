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

import java.util.HashMap;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.UserNotFoundException;

/**
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public interface BackupWriter {

    /**
     * 
     * @return a HashMap containing all {@link Subscriber}s and their 
     *          {@link Subscription}s with the {@link Subscriber#getUserName()} as key
     */
    public HashMap<String, Subscriber> load();
    
    /**
     * 
     * @param toStore a HashMap containing all {@link Subscriber}s and their 
     *          {@link Subscription}s with the {@link Subscriber#getUserName()} as key
     */
    public void store(HashMap<String, Subscriber> toStore);
    
    /**
     * @return <code>TRUE</code> in case the BackupWriter is ok or <code>FALSE</code> in case not.
     */
    public boolean isFine();
    
    /**
     * @return [msec] how long the full load took. 
     */
    public long getLastLoadTime();
    
    /**
     * @return [msec] how long the full storage took. 
     */
    public long getLastStoreTime();
    
    /**
     * 
     * @return timestamp when the last full storage took place.
     */
    public long getLastFullWrite();
    
    public Subscriber getSubscriber(String id) throws UserNotFoundException;
    
    public void addSubscriber(Subscriber s);
    
    public void addSubscription(Subscription s);
    
    public void removeSubscription(Subscription s);
    
    public void removeSubscriber(Subscriber s);
    
    /**
     * 
     * @return a unique name for the implementation of the BackupWriter.
     */
    public String getUniqueName();
    
    
}
