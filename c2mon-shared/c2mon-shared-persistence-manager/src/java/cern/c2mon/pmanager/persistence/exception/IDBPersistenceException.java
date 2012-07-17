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
package cern.c2mon.pmanager.persistence.exception;

/**
 * Exception sent 
 * @author mruizgar
 *
 */
public class IDBPersistenceException extends Exception {
    
    /**
     * Unique identifier of the class
     */
    private static final long serialVersionUID = 1L;
    
    /** Indicates the number of objects that have been stored before the exception took place*/
    private int committed;
    
    /**
     * @return the commited
     */
    public final int getCommited() {
        return committed;
    }

    /**
     * @param commited the commited to set
     */
    public final void setCommited(final int commited) {
        this.committed = commited;
    }

    /**
     * Constructor without parameters
     */
    public IDBPersistenceException() {
       super(); 
    }
    
    public IDBPersistenceException(String message, Throwable cause) {
      super(message, cause);      
    }

    public IDBPersistenceException(Throwable cause) {
      super(cause);      
    }

    /** Creates an exception with a detailed message 
     * @param msg The detailed message that the parent class will store
     * */
    public IDBPersistenceException(final String msg) {
        super(msg);
    }
    
    
    /** Creates an exception with a detailed message and it indicates the number of objects that had been
     * already committed
     * @param msg The detailed message that the parent class should store
     * @param committed The number of objects that had been already stored before the exception took place
     */
    public IDBPersistenceException(final String msg, final int committed) {
        super(msg);
        this.committed = committed;        
    }
    

}
