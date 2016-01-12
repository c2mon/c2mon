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
package cern.c2mon.daq.opcua.connection.common.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Subscription Group which holds information and items for a subscription.
 * 
 * @author Andreas Lang
 *
 * @param <ID> The item defintion type which is hold by this group.
 */
public class SubscriptionGroup<ID extends ItemDefinition< ? > > {
    
    /**
     * Collection of item definitions.
     */
    private final Collection<ID> defintions = new ConcurrentLinkedQueue<ID>();
    
    /**
     * The time deadband used for this group.
     */
    private int timeDeadband;
    
    /**
     * The value deadband used for this group.
     */
    private float valueDeadband;

    /**
     * Creates a new Subscription group with the provided time and value
     * deadband.
     * 
     * @param timeDeadband The time deadband to use.
     * @param valueDeadband The value deadband to use.
     */
    public SubscriptionGroup(final int timeDeadband,
            final float valueDeadband) {
        this.timeDeadband = timeDeadband;
        this.valueDeadband = valueDeadband;
    }

    /**
     * Adds an item definition to the group.
     * 
     * @param itemDefintion The item definition to add.
     */
    public void addDefintion(final ID itemDefintion) {
        if (itemDefintion != null)
            defintions.add(itemDefintion);
    }
    
    /**
     * Removes an item definiton from the group.
     * 
     * @param itemDefintion The item definition to remove.
     */
    public void removeDefintion(final ID itemDefintion) {
        if (itemDefintion != null)
            defintions.remove(itemDefintion);
    }
    
    /**
     * Removes all item definitions in the group.
     */
    public void clearDefintions() {
        defintions.clear();
    }
    
    /**
     * Returns the current item definitions. This is a live list changes to
     * this list will be reflected in the group. The collection is thread safe.
     * 
     * @return Collection of definitions in this group.
     */
    public Collection<ID> getDefintions() {
        return defintions;
    }

    /**
     * @return the timeDeadband
     */
    public int getTimeDeadband() {
        return timeDeadband;
    }

    /**
     * @return the valueDeadband
     */
    public float getValueDeadband() {
        return valueDeadband;
    }
    
    /**
     * Checks if there are unsubscribed item definitions in this group.
     * 
     * @return True if there are unsubscribed item definitions else false.
     */
    public boolean hasUnsubscribedDefinitions() {
        boolean hasUnsubscribedDefinitions = false;
        for (ID definition : defintions) {
            if (!definition.isSubscribed()) {
                hasUnsubscribedDefinitions = true;
                break;
            }
        }
        return hasUnsubscribedDefinitions;
    }
    
    /**
     * Retrieves all unsubscribed item definitons.
     * 
     * @return The item definitions which are not subscribed.
     */
    public Collection<ID> getUnsubscribedDefinitions() {
        Collection<ID> unsubscribedDefinitions = new ArrayList<ID>();
        for (ID definition : defintions) {
            if (!definition.isSubscribed()) {
                unsubscribedDefinitions.add(definition);
            }
        }
        return unsubscribedDefinitions;
    }

    /**
     * Returns the size of this subscription group.
     * 
     * @return The item definitions in this group.
     */
    public int size() {
        return defintions.size();
    }
}
