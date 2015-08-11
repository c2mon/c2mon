/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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

    private PreparedStatement pstmt;
    private Statement getSourcesStmt;
    private static final Logger LOG = LoggerFactory.getLogger(DataProviderDB.class);

    //
    // --- CONSTRUCTION --------------------------------------------------------------------------
    //
    public DataProviderDB(DataSource ds) {
        String sql = "select source_id from alarm_definition where alarm_id=?";
        try {
            pstmt = ds.getConnection().prepareStatement(sql);
            getSourcesStmt = ds.getConnection().createStatement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }   
    }
    
    //
    // --- Overrides DataProviderInterface -------------------------------------------------------
    //
    @Override
    public String getSource(String alarmId) throws Exception {
        LOG.trace("Query source name for " + alarmId + " ...");
        String sourceId = "?";
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
        if (getSourcesStmt != null) {
            try {
                getSourcesStmt.close();
                LOG.info("Statement successfully closed.");
            } catch (Exception e) {
                LOG.warn("Failed to close preparedStatement", e);
            }            
        }
    }

    @Override
    public void initSourceMap(Map<String, String> smap) {
        // do not know how to this efficiently in DB, skip
    }

    @Override
    public Collection<String> getSourceNames() throws Exception {
        String sql = "select source_id from source_definition";
        ArrayList<String> sourceNames = new ArrayList<String>();
        try (ResultSet rs = getSourcesStmt.executeQuery(sql)) {
            while (rs.next()) {
                sourceNames.add(rs.getString(1));
            }
        }
        return sourceNames;
    }
    
}
