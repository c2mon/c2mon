/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.pmanager.mock;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

/**
 * This class is a fake implementation of the IFallback interface. It is only used for testing, without having
 * to reference any of the real implementations which are stored in different projects.
 * @author mruizgar
 *
 */
public class FallbackImpl implements IFallback {
    
   
    /** String constant identifying one FallbackImpl object*/     
    private static final String STR_LINE = "99999\tUNKNOWN\t0\t2009-01-29\t11:56:50.358\ttrue\tBoolean\tmruizgar\tpcst999\t2009-01-30\t15:37:41.1042009\t0\tOk"; 
    
    /** Contains the string representation of a FallbackImpl object */
    private String objectData;
    
    /** This will be used to simulate a syntax error in the fallback file*/
    public static final String ERROR = "Error"; 
   
  
    /**
     * Returns the id representing this object
     * @return String The object's id
     */
    public final String getId() {
        return "99999";
    }
    
    
    /**
     * @param objectData the objectData to set
     */
    public final void setObjectData(final String objectData) {
        this.objectData = objectData;
    }

    /**
     * Constructs a FallbackImpl object from the string received as a parameter
     * @param line The string representing the fallbackImpl object
     * @return A FallbackImpl object 
     * @throws DataFallbackException An exception is thrown in case that something happens during the string
     * transformation to the object
     */
    public final IFallback getObject(final String line) throws DataFallbackException {
        FallbackImpl fImpl = new FallbackImpl();
        fImpl.setObjectData(line);
        return fImpl;
        
    }
    
    /**
     * Converts a FallbackImpl object into a string representation
     * @return The object's string representation
     */
    public final String toString() {       
        if (this.objectData == null) {
            this.setObjectData(STR_LINE);
        }
        return this.objectData;
                
    }

}
