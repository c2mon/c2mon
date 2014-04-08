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

package cern.c2mon.statistics.consumer;

import java.io.FileInputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 * Wrapper class for the SqlMapClient (ibatis)
 * 
 * @author Mark Brightwell
 * 
 */
public final class SqlMapper {

    /**
     * The number of inserts per batch.
     */
    private static final int INSERTS_PER_BATCH = 500;
    
    /**
     * The general logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SqlMapper.class);

    /**
     * The tag logger.
     */
    private static final Logger TAGLOGGER = Logger.getLogger("SourceDataTagLogger");

    /**
     * The SqlMapClient that this class is wrapping.
     */
    private static final SqlMapClient SQLMAP;
    
    

    static {
        try {
            //get c2mon.properties file containing DB access details
            String timPropertiesLocation;
            if (System.getProperty("c2mon.properties") == null) {
                timPropertiesLocation = System.getProperty("consumer.home") + "/conf/c2mon-consumer.properties"; 
            }
            else {
                timPropertiesLocation = System.getProperty("c2mon.properties");
            }
            
            // the input stream for properties file
            FileInputStream timPropertiesFile = null;

            // create the properties file
            LOGGER.debug("configuring kernel...");
            try {
                timPropertiesFile = new FileInputStream(timPropertiesLocation);
            } catch (java.io.FileNotFoundException ex) {
                LOGGER.error("FileNotFoundException caught : " + ex.getMessage());
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }

            Properties properties = new Properties();

            //load the kernel properties
            try {
                properties.load(timPropertiesFile);
            } catch (java.io.IOException ex) {
                LOGGER.fatal("IOException caught : " + ex.getMessage());
                ex.printStackTrace();
            }
            
            //construct the SqlMap from the XML file, using the tim.properties DB settings
            String resource = "cern/c2mon/statistics/consumer/sqlmap/FilterSqlMapConfig.xml";
            Reader reader = Resources.getResourceAsReader(resource);
            SQLMAP = SqlMapClientBuilder.buildSqlMapClient(reader, properties);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing SqlConfig class. Cause: " + e);
        }
    }
    
    /**
     * Private constructor to override public one.
     */
    private SqlMapper() {     
    }

    /**
     * Getter method returning the wrapped SqlMapClient.
     * @return the SqlMapClient
     */
    public static SqlMapClient getSqlMapInstance() {
        return SQLMAP;
    }

    /**
     * Writes the list of updates to the database.
     * 
     * @param dataValues a list of FilterPersistenceObject's
     * @throws IDBPersistenceException if error in writing list of objects to database
     */
    public static void writeToDatabase(final List<FilterPersistenceObject> dataValues) throws IDBPersistenceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("entering writeToDataBase()");
        }
        Iterator<FilterPersistenceObject> it = dataValues.iterator();
        LOGGER.debug("got iterator");
        // iterate through all objects in the Collection
        try {
            try {
                LOGGER.debug("starting transaction");
                SQLMAP.startTransaction();
                while (it.hasNext()) {
                    LOGGER.debug("starting batch");
                    SQLMAP.startBatch();
                    int counter = 0;
                    // insert the tags into the database in max batches of INSERTS_PER_BATCH
                    while (it.hasNext() && counter < INSERTS_PER_BATCH) {                        
                        FilteredDataTagValue filteredDataTagValue = ((FilterPersistenceObject) it.next()).getFilteredDataTagValue();
                        TAGLOGGER.info(filteredDataTagValue);
                        SQLMAP.insert("insertTagValue", filteredDataTagValue);
                        counter++;
                    }
                    LOGGER.debug("commiting transaction");
                    SQLMAP.commitTransaction();
                }
            } finally {
                //always end the transaction
                SQLMAP.endTransaction();
            }
        } catch (SQLException e) {
            LOGGER.error("SQLException caught in writing tag values to DB: " + e);
            throw new IDBPersistenceException("Exception caught in writing data tags to DB.");
            //send email notification
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("leaving writeToDataBase()");
        }
    }

    /**
     * A method for testing the database insertion.
     * @param fdt the FilteredDataTagValue to write to database 
     */
    public static void testInsert(final FilteredDataTagValue fdt) {
        try {
            SQLMAP.startTransaction();
            SQLMAP.insert("insertTagValue", fdt);
            SQLMAP.commitTransaction();
        } catch (SQLException e) {
            LOGGER.error("SQLException caught in writing tag values to DB: " + e);
        }

    }

}
