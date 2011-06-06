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
package cern.c2mon.pmanager.persistence;

import cern.c2mon.pmanager.IFallback;

import java.util.List;

/**
 * Interface defining those methods that have to be implemented to be able to
 * log/commit data to the DB
 * 
 * @author mruizgar
 * 
 */
public interface IPersistenceManager {

    /**
     * It stores a list of IFallback objects into the database. It relies in the
     * fallback mechanism to store the data in case the DB connection is lost
     * 
     * @param data
     *            Set of IFallback objects which data has to be stored into the
     *            proper DB
     */
    void storeData(List data);

    /**
     * Stores an IFallback ojbect into the database. It relies in the fallback
     * mechanism to store the data in case the DB connection is lost
     * 
     * @param dataObject
     *            The object which information has to be stored in the proper DB
     */
    void storeData(IFallback dataObject);

    /**
     * It releases the memory that may be used by this interface implementations
     */
    void finalize();

}
