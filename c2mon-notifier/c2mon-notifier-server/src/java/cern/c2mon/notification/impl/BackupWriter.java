/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.notification.impl;

import java.util.concurrent.ConcurrentHashMap;

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
    public ConcurrentHashMap<String, Subscriber> load();
    
    /**
     * 
     * @param toStore a HashMap containing all {@link Subscriber}s and their 
     *          {@link Subscription}s with the {@link Subscriber#getUserName()} as key
     */
    public void store(ConcurrentHashMap<String, Subscriber> toStore);
    
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
