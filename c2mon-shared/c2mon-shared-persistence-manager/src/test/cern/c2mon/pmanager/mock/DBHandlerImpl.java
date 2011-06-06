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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
/**
 * Implements the IDBPersistenceHandler interface for testing purposes.
 * Instead of writing to a DB it just stores the data in an ArrayList
 * @author mruizgar
 *
 */
public class DBHandlerImpl implements IDBPersistenceHandler {

    /** Container for the objects that have to be stored */
    private ArrayList storage = new ArrayList();
    
    /** Gets a string identifying the DB user account
     *  @return String The strind identifying the user account 
     */    
    public final String getDBInfo() {
        return "timdb@test";
    }
    
    /** Stores an IFallback object into a DB table 
     *  @param object IFallback object containing the data to be committed to the DB
     *  @throws SQLException An exception is thrown in case the object cannot be committed to the DB
     */
    
     public final void storeData(final IFallback object) throws SQLException {
         if (object instanceof FallbackImpl) {
             if (!((FallbackImpl) object).toString().equals(FallbackImpl.ERROR)) {
                 storage.add(object);
             } else {
                 throw new SQLException("Connection to the DB has been lost");
             }
         }
     }

    /** Stores a list of objects read back from the fallback mechanism into a db table
     *  @param data List of IFallback objects read back from a fallback file and containing the
     *  data to be committed to the DB
     *  @throws SQLException An exception is thrown in case the objects cannot be committed to the DB 
     */
     public final void storeDataBack(final List data) throws SQLException {
         for (int i = 0; i < data.size(); i++) {
             if (data.get(i) instanceof FallbackImpl) {
                 if (!((FallbackImpl) data.get(i)).toString().equals(FallbackImpl.ERROR)) {
                     storage.add(storage); 
                 } else {
                     throw new SQLException("Connection to the DB has been lost");
                 }
         }
     }
         }
     

    /** Stores a list of incoming objects into a db table
     *  @param data List of IFallback objects containing the data to be committed to the DB
     *  @throws IDBPersistenceException An exception is thrown in case the objects cannot be committed to the DB
     *    
     */
    public final void storeData(final List data) throws IDBPersistenceException {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) instanceof FallbackImpl) {
                if (!((FallbackImpl) data.get(i)).toString().equals(FallbackImpl.ERROR)) {
                    storage.add(data.get(i)); 
                } else {                    
                    throw new IDBPersistenceException("Connection to the DB has been lost", 0);
                }
        } 
    }
    }

  
}
