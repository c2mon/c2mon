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
package cern.c2mon.daq.opcua.connection.common;

import cern.c2mon.shared.common.datatag.ISourceDataTag;

/**
 * Generic listener for an OPCEndpoint.
 * 
 * @author Andreas Lang
 *
 */
public interface IOPCEndpointListener {
    
    /**
     * Called when a value of a tag changes.
     * 
     * @param dataTag The tag whose value changed.
     * @param timestamp The timestamp when the value was updated.
     * @param value The new value.
     */
    void onNewTagValue(final ISourceDataTag dataTag, long timestamp,
            final Object value);
    
    /**
     * Called in case an invalid tag causes an exception.
     * 
     * @param dataTag The tag which caused the exception.
     * @param cause The cause of the exception.
     */
    void onTagInvalidException(
            final ISourceDataTag dataTag, final Throwable cause);
    
    /**
     * Called in case a subscription fails.
     * 
     * @param cause The cause of the subscription failure.
     */
    void onSubscriptionException(final Throwable cause);
    
}
