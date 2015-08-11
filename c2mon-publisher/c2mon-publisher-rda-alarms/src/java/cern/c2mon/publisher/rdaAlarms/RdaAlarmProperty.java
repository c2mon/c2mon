/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.publisher.rdaAlarms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.cmw.data.Data;
import cern.cmw.data.DataFactory;
import cern.cmw.data.Entry;
import cern.cmw.rda3.common.data.AcquiredData;
import cern.cmw.rda3.common.exception.ServerException;
import cern.cmw.rda3.server.core.SetRequest;
import cern.cmw.rda3.server.subscription.SubscriptionSource;

/**
 * This class represents a RDA property. For each tag which is published via RDA an instance of this class is created.
 * It handles the registration of the listeners and is responsible of notifiying all subscribers about value updates.
 *
 * @author Mark Buttner
 */
public class RdaAlarmProperty {

    private static final Logger LOG = LoggerFactory.getLogger(RdaAlarmProperty.class);

    private Data currentValue = null;
    private SubscriptionSource subscriptionSource;
    private final String rdaPropertyName;

    //
    // --- CONSTRUCTION ---------------------------------------------------------------------------
    //
    RdaAlarmProperty(final String pRdaPropertyName) {
        this.rdaPropertyName = pRdaPropertyName;
    }

    //
    // --- PUBLIC METHODS --------------------------------------------------------------------------
    //
    public synchronized void setSubscriptionSource(SubscriptionSource subscription) {
        this.subscriptionSource = subscription;
    }

    public synchronized void removeSubscriptionSource() {
        this.subscriptionSource = null;
    }

    /**
     * Generates a new {@link Data} object from the received tag update and propagates it to all the listeners.
     * 
     * @param av -
     */
    public synchronized void onUpdate(AlarmValue av) {
        String tagName = av.getFaultFamily() + ":" + av.getFaultMember() + ":" + av.getFaultCode();
        Data newValue = DataFactory.createData();

        if (currentValue != null) {
            newValue = currentValue.clone();
        }

        if (newValue.exists(tagName)) {
            newValue.remove(tagName);
        }

        String status = "TERMINATE";
        if (av.isActive()) {
            status = "ACTIVE";
        }
        newValue.append(tagName, status);
        LOG.debug("Value update received for RDA property " + rdaPropertyName + " " + newValue.size());

        // check, because there might not be any RDA3 clients subscribed yet
        if (subscriptionSource != null) {
            Data filters = subscriptionSource.getContext().getFilters();
            subscriptionSource.notify(getValue(newValue, filters));
        }
        currentValue = newValue;
    }

    //
    // --- PACKAGE METHODS ------------------------------------------------------------------------
    //
    protected synchronized AcquiredData getValue(Data filters) {
        return getValue(currentValue, filters);
    }

    synchronized Data get() {
        return currentValue;
    }

    /**
     * @param request not used as the action is not implemented. 
     */
    synchronized void set(final SetRequest request) throws ServerException {
        throw new ServerException("SET is not supported by the server");
    }

    //
    // --- PRIVATE METHODS -------------------------------------------------------------------------
    //
    private AcquiredData getValue(Data value, Data filters) {
        if (filters != null && filters.getAllEntriesSize() > 0) {            
            Data filteredValue = DataFactory.createData();

            for (Entry e : filters.getEntries()) {
                filteredValue.append(e.getString(), value.getString(e.getString()));
            }
            return new AcquiredData(filteredValue);
        }
        return new AcquiredData(value);
    }

}
