/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The data provider retrieving alarm definitions directly from the database. Temporary solution in case
 * the remote JMS dataprovider does not work. To be configured in the Spring context.
 * 
 * @author mbuttner
 */
public class DataProviderDB implements DataProviderInterface {

    private DataSource ds;
    private PreparedStatement pstmt;
    private static final Logger LOG = LoggerFactory.getLogger(DataProviderDB.class);

    //
    // --- CONSTRUCTION --------------------------------------------------------------------------
    //
    public DataProviderDB(DataSource ds) {
        LOG.info("Create data provider based on DB ..");
        String sql = "select source_id from alarm_definition where alarm_id=?";
        try {
            LOG.info("Prepare statement ...");
            pstmt = ds.getConnection().prepareStatement(sql);
            this.ds = ds;
        } catch (Exception e) {
            LOG.error("Failed to create data provider, HALT!", e);
            throw new RuntimeException(e);
        }   
        LOG.info("Ready.");
    }
    
    //
    // --- Overrides DataProviderInterface -------------------------------------------------------
    //
    @Override
    public String getSource(String alarmId) throws Exception {
        LOG.trace("Query source name for " + alarmId + " ...");
        String sourceId = null;
        pstmt.setString(1,  alarmId);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                sourceId = rs.getString(1);
            }
        } 
        LOG.debug("Source name for " + alarmId + " -> " + sourceId);
        return sourceId;
    }

    
    @Override
    public void close() {
        LOG.trace("Closing ... ");
        if (pstmt != null) {
            try {
                pstmt.close();
                LOG.info("Statement successfully closed.");
            } catch (Exception e) {
                LOG.warn("Failed to close preparedStatement", e);
            }            
        }
    }

    @Override
    public Collection<String> getSourceNames() throws Exception {
        ArrayList<String> sourceNames = new ArrayList<String>();
        String sql = "select source_id from alarm_sources_v";
        LOG.info("Retrieve the connection ...");
        Connection conn = ds.getConnection();
        LOG.trace("Prepare the statement for [{}] ... ", sql);
        PreparedStatement stmt = conn.prepareStatement(sql);
        LOG.trace("Execute ...");
        try ( ResultSet rs = stmt.executeQuery()) { 
            LOG.trace("Process result set ...");
            while (rs.next()) {
                String sourceName = rs.getString(1);
                LOG.debug("Adding {} ...", sourceName);
                sourceNames.add(sourceName);
            }
            LOG.trace("Closing ResultSet ...");
        }
        LOG.trace("Closing Statement ...");
        stmt.close();
        LOG.info("Returning {} records.", sourceNames.size());
        return sourceNames;
    }

    @Override
    public ConcurrentHashMap<String, String> initSourceMap(Set<String> alarmIds) throws Exception {
        ConcurrentHashMap<String,String> result = new ConcurrentHashMap<String,String>();
        for (String aid : alarmIds) {
            String sourceId = getSource(aid); 
            if (sourceId != null) {
                result.put(aid, sourceId);
            }
        }
        return result;
    }
    
}
