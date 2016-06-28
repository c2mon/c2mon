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
package cern.c2mon.pmanager;

import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

/**
 * Interface defining those methods that will allow an object to be stored or read back from one of the fallback files
 * @author mruizgar
 *
 */

public interface IFallback {
    
    /**
     * Converts the current object to a string representation
     * @return The string representation of the object
     */
     String toString();
    
    /**
     * Converts a string into an IFallback object
     * @param line Contains the information with wich the object is going to be populated
     * @return The created IFallback object
     * @throws DataFallbackException An exception is thrown if the line cannot be transformed into an IFallback object
     */
    IFallback getObject(String line) throws DataFallbackException;
    
    /** Returns the identifier of the object
     * 
     * @return A string that identifies the IFallback object 
     */
    String getId(); 
    

}
