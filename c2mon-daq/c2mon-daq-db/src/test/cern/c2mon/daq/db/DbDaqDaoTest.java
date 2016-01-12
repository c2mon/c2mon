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
package cern.c2mon.daq.db;

import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

/**
 * A test class for performing test operations on the database.
 * 
 * @author Aleksandra Wardzinska
 * */
public class DbDaqDaoTest extends SqlSessionDaoSupport {

    /**
     * Signals an alert for the given data tag name
     * @param dataTagId the id of the datatag
     * */
    public void signalAlert(final long dataTagId) {
        this.getSqlSession().update("testUpdateDataTag", dataTagId);
    }
    
    /**
     * Gets all names of the datatags present in the database
     * @return a list of datatags names
     * */
    @SuppressWarnings("unchecked")
    public List<String> getAllDataTagsNames() {
        return this.getSqlSession().selectList("getAllDataTagsNames"); 
    }
    
    /**
     * Gets all ids of the datatags present in the database
     * @return a list of datatags ids
     * */
    @SuppressWarnings("unchecked")
    public List<Long> getAllDataTagsIds() {
        return this.getSqlSession().selectList("getAllDataTagsIds");
    }
    
    /**
     * Sets the dataSource parameters
     * @param dbUrl     url to the database
     * @param dbUsername    database username
     * @param dbPassword    database password 
     * */
    public void setDataSourceParams(final String dbUrl, final String dbUsername, final String dbPassword) {
        BasicDataSource ds = (BasicDataSource) this.getSqlSession().getConfiguration().getEnvironment().getDataSource();
        ds.setUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
    }
}
