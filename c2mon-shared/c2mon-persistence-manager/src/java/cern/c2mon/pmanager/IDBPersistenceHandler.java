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
package cern.c2mon.pmanager;

import java.sql.SQLException;
import java.util.List;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;

/**
 * Interface exposing the functionality that any class willing to store incoming data to 
 * the server to TIMDB should implement
 * @author mruizgar
 *
 */
public interface IDBPersistenceHandler {
    
    
    /**
     * Gets a string identifying the user account and database with which this class operates
     * @return String identifying the database account used by this class
     */
    String getDBInfo();
    
    /** Stores an IFallback object into a DB table 
     *  @param object IFallback object containing the data to be committed to the DB
     *  @throws SQLException An exception is thrown in case the object cannot be committed to the DB
     */    
     void storeData(IFallback object) throws SQLException;

     /** Stores a list of incoming objects into a db table
     *  @param data List of IFallback objects containing the data to be committed to the DB
     *  @throws IDBPersistenceException An exception is thrown in case the objects cannot be committed to the DB
     */
    void storeData(List data) throws IDBPersistenceException;

        
}


