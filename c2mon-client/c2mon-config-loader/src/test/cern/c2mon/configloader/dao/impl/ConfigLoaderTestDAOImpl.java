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

package cern.c2mon.configloader.dao.impl;

import javax.annotation.PostConstruct;

import cern.c2mon.configloader.Configuration;
import cern.c2mon.configloader.dao.ConfigLoaderTestDAO;

/**
 * For test purposes only
 * 
 * @author wbuczak
 */
public class ConfigLoaderTestDAOImpl extends ConfigLoaderDaoImpl implements ConfigLoaderTestDAO {

    String query = "update %table% set applydate=? where configid=?";

    String insertQuery = "insert into %table%(configname, configdesc, author, createdate) values(?,?,?,?)";

    @PostConstruct
    void init() {
        query = query.replace("%table%", config.getDbConfigTableName());
        insertQuery = insertQuery.replace("%table%", config.getDbConfigTableName());
    }

    @Override
    public void setAppliedFlag(final long configId) {
        jdbcTemplate.update(query, new java.sql.Timestamp(System.currentTimeMillis()), configId);
    }

    @Override
    public void insert(Configuration conf) {

        jdbcTemplate.update(insertQuery, conf.getName(), conf.getDescription(), conf.getAuthor(),
                new java.sql.Timestamp(conf.getCreateTimestamp()));
    }
}
