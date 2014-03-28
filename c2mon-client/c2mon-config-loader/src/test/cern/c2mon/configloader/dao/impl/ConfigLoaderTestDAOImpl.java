/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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