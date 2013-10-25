package cern.c2mon.driver.db;

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
        return (List<String>) this.getSqlSession().selectList("getAllDataTagsNames"); 
    }
    
    /**
     * Gets all ids of the datatags present in the database
     * @return a list of datatags ids
     * */
    @SuppressWarnings("unchecked")
    public List<Long> getAllDataTagsIds() {
        return (List<Long>) this.getSqlSession().selectList("getAllDataTagsIds");
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
