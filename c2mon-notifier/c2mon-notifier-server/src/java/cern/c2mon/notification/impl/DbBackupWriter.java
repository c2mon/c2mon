/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.UserNotFoundException;
import cern.dmn2.core.Status;

/**
 * An implementation of the {@link BackupWriter} interface which writes data 
 * to a (remote/local) database.
 * 
 * @author felixehm
 */
public class DbBackupWriter implements BackupWriter {
    
    final JdbcTemplate jdbcTemplate;
    
    Logger logger = LoggerFactory.getLogger(DbBackupWriter.class);
    
    private long lastStoreTime = 0;
    private long lastLoadTime = 0;
    
    private long lastFullStorageTime = 0;
    
    /**
     * @return Returns the lastStoreTime.
     */
    @Override
    @ManagedAttribute
    public long getLastStoreTime() {
        return lastStoreTime;
    }

    /**
     * @return Returns the lastLoadTime.
     */
    @Override
    @ManagedAttribute
    public long getLastLoadTime() {
        return lastLoadTime;
    }

       /** Constructor.
     * 
     * @param dataSource the DataSource to use for the database operations.
     */
    public DbBackupWriter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Creates the schema in the database.
     */
    public void createSchema() {
        logger.debug("Creating schema...");
        
        /*
         * for sqlite we need to make sure this is switched on. It enables the foreign key relationships.
         */
        jdbcTemplate.execute("PRAGMA foreign_keys = ON");
        
        jdbcTemplate.execute(
                "CREATE TABLE DMN_NOTIFY_SUBSCRIBERS (" 
                + " userid VARCHAR(16) NOT NULL," 
                + " email VARCHAR(128) NOT NULL," 
                + " sms VARCHAR(64), " 
                + " reportinterval INTEGER DEFAULT 0, " 
                + "CONSTRAINT DMN_NOTIFY_SUBSCRIBERS_PK PRIMARY KEY (userid)" 
                + ")");
        
        jdbcTemplate.execute(
                "CREATE TABLE DMN_NOTIFY_SUBSCRIPTIONS (" 
                + " userid VARCHAR(16) NOT NULL,"
                + " tagid INTEGER NOT NULL, "
                + " enabled INTEGER DEFAULT 1, " 
                + " notifylevel VARCHAR(12) NOT NULL, "
                + " lastnotifiedstate INTEGER NOT NULL, "
                + " lastnotifiedts TIMESTAMP , "
                + " resolvedtags VARCHAR(2048) , "
                + " onvaluechange INTEGER DEFAULT 0 , "
                + " sms_enabled INTEGER DEFAULT 0 , "
                + "CONSTRAINT DMN_NOTIFY_SUBSCRIPTIONS PRIMARY KEY (userid, tagid), "
                + "CONSTRAINT DMN_NOTIFY_LEVEL_CHECK CHECK (notifylevel in ('ok','warning','error')),"
                + "FOREIGN KEY (userid) REFERENCES DMN_NOTIFY_SUBSCRIBERS(userid) ON DELETE CASCADE" 
                + ")");
    }
    
    @Override
    public void addSubscriber(Subscriber s) {
        logger.trace("entering addSubscriber()");
        
        logger.debug("Adding Subscriber Name={}, TagIds={}", s.getUserName(), s.getSubscribedTagIds());
        
        jdbcTemplate.update(
                "INSERT INTO DMN_NOTIFY_SUBSCRIBERS (userid, email, sms, reportinterval) VALUES (?,?,?,?)", 
                new Object [] {s.getUserName(), s.getEmail(), s.getSms(), s.getReportInterval()}, 
                new int [] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER});
        
        List<Object[]> toAdd = new ArrayList<Object[]>();
        for (Subscription sup : s.getSubscriptions().values()) {
            logger.trace("Adding Subscription to batch: {}", sup);
            toAdd.add(new Object [] {sup.getSubscriberId(), sup.isEnabled(), sup.getNotificationLevel().toString(), sup.getTagId(), sup.getLastNotifiedStatus().toInt(), sup.getLastNotification(), getResolvedTagsAsString(sup), sup.isNotifyOnMetricChange(), sup.isSmsNotification()});
        }
        
        jdbcTemplate.batchUpdate(
                "INSERT INTO DMN_NOTIFY_SUBSCRIPTIONS (userid, enabled, notifylevel, tagid, lastnotifiedstate, lastnotifiedts, resolvedtags, onvaluechange, sms_enabled) VALUES (?,?,?,?,?,?,?,?,?)", 
                toAdd,
                new int [] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER, Types.INTEGER});
    }
    
    
    public String getResolvedTagsAsString(Subscription sup) {
        StringBuilder resolvedTagsAndTheirStatus = new StringBuilder();
        Iterator<Long> tagsToJoin = sup.getResolvedSubTagIds().iterator();
        
        while (tagsToJoin.hasNext()) {
            Long l = tagsToJoin.next();
            Status subStatus = sup.getLastStatusForResolvedSubTag(l);
            resolvedTagsAndTheirStatus.append(l).append("=").append(subStatus.toString());
            if (tagsToJoin.hasNext()) {
                resolvedTagsAndTheirStatus.append(",");
            }
        }
        return resolvedTagsAndTheirStatus.toString();
    }
    
    
    public void updateSubscriber(Subscriber s) {
        logger.trace("entering updateSubscriber()");
        setSubscriber(s);
    }
    
    /**
     * 
     * @param sup the Subscription to add
     */
    @Override
    public void addSubscription(Subscription sup) {
       
        logger.trace("entering addSubscription() for User={}" + sup.getSubscriberId() + ", tagId= " + sup.getTagId());

        jdbcTemplate.update(
                "INSERT INTO DMN_NOTIFY_SUBSCRIPTIONS (userid, enabled, notifylevel, tagid, lastnotifiedstate, lastnotifiedts, resolvedtags, onvaluechange) VALUES (?,?,?,?,?,?,?,?,?)", 
                new Object [] {sup.getSubscriberId(), sup.isEnabled(), sup.getNotificationLevel().toString(), sup.getTagId(), sup.getLastNotifiedStatus().toInt(), sup.getLastNotification(), getResolvedTagsAsString(sup), sup.isNotifyOnMetricChange(), sup.isSmsNotification()}, 
                new int [] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER}, Types.INTEGER);
    }
    
    @Override
    public Subscriber getSubscriber(String id) throws UserNotFoundException {
        logger.trace("entering getSubscriber() for User {} ", id);
        
        Subscriber result = jdbcTemplate.query(
                "SELECT userid, email, sms, reportinterval FROM DMN_NOTIFY_SUBSCRIBERS where userid = ?", 
                new Object[] {id}, 
                new ResultSetExtractor<Subscriber>() {
                    @Override
                    public Subscriber extractData(ResultSet arg0) throws SQLException, DataAccessException {
                        if (arg0.next()) {
                            return getSubscriberFromRs(arg0);
                        } else {
                            return null;
                        }
                    }
                });
        
        if (result == null) {
            throw new UserNotFoundException("User " + id + " was not found in DB");
        }
        
        logger.debug("Getting subscriptions for Subscriber {} ", result.getUserName());
        

        List<Subscription> subscriptions = jdbcTemplate.query(
                "SELECT userid, enabled, notifylevel, tagid, lastnotifiedstate, lastnotifiedts, resolvedtags, onvaluechange, sms_enabled  FROM DMN_NOTIFY_SUBSCRIPTIONS where userid = ?", 
                new Object [] {id} ,
                new RowMapper<Subscription>() {
                    @Override
                    public Subscription mapRow(ResultSet arg0, int rowNumber) throws SQLException {
                        return getSubscriptionFromRs(arg0);
                    }
                });
        
        for (Subscription s : subscriptions) {
            result.addSubscription(s);
        }
        
        logger.debug("Returning Subscriber {}", result);
        
        
        return result;
        
    }
    
    @Override
    public void removeSubscriber(Subscriber s) {
        logger.trace("entering removeSubscriber() for User={}", s.getUserName());
        jdbcTemplate.update(
                "DELETE FROM DMN_NOTIFY_SUBSCRIBERS where userid = ?", 
                new Object[] {s.getUserName()});
        
        // not required as the db does this for us.
//        jdbcTemplate.update(
//                "DELETE FROM DMN_NOTIFY_SUBSCRIPTIONS where userid = ?", 
//                new Object[] {s.getUserName()});
        
    }
    
    @Override
    public void removeSubscription(Subscription sub) {

        logger.trace("entering removeSubscription() for User={}, tagId={} ", sub.getSubscriberId() , sub.getTagId());
        jdbcTemplate.update(
                "DELETE FROM DMN_NOTIFY_SUBSCRIPTIONS where userid = ? and tagid= ?", 
                new Object[] {sub.getSubscriberId(), sub.getTagId()});
    }
    
    /** Stores a {@link Subscriber} in the database.<br>
     * 
     * <b>Note: </b> This call is slow as it first removes all {@link Subscription} from the db and then calls {@link #addSubscriber(Subscriber)}.
     * 
     * @param s the {@link Subscriber} to update/insert
     */
    public void setSubscriber(Subscriber s) {
        logger.trace("entering setSubscriber() for User={}", s.getUserName());
        removeSubscriber(s);
        addSubscriber(s);
    }
    
    /**
     * stores all data to the db. 
     * <b>Note :</b>It does NOT compare the passed list with the db. 
     * It iterates over the {@link Subscriber} in the list and calls {@link #setSubscriber(Subscriber)}.
     * @see DbBackupWriter#setSubscriber(Subscriber)
     * @param toStore the HashMap of subscribers to store in the DB.
     */
    @Override
    public void store(final ConcurrentHashMap<String, Subscriber> toStore) {
        logger.trace("entering store()");
                
        TransactionTemplate tt = new TransactionTemplate(new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                
                logger.info("Storing subscriptions");
                if (logger.isTraceEnabled()) {
                    for (Subscriber s : toStore.values()) {
                        logger.trace("Storing :" + s.getUserName() + " " + s.getSubscriptions());
                    }
                }
                
                try {
                    long t1 = System.currentTimeMillis();
                    logger.debug("Delete subscribers, then re-insert.");
                    jdbcTemplate.execute("DELETE FROM DMN_NOTIFY_SUBSCRIBERS");
                    
                    
                    /*
                     * the following would be needed if the db does not support foreign key cascade. sqlite is ok when PRAGMA foreign_key=ON 
                     */
//                    int left = jdbcTemplate.query("SELECT count(*) as total FROM DMN_NOTIFY_SUBSCRIPTIONS", new ResultSetExtractor<Integer>() {
//                        public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
//                            if (rs.next()) {
//                                return rs.getInt("total");
//                            } else {
//                                return 0;
//                            }
//                        }
//                    });
//                    
//                    if (left > 0) {
//                        logger.error("Deleting subscriptions... DB was not able to do so.");
//                        jdbcTemplate.execute("DELETE FROM DMN_NOTIFY_SUBSCRIPTIONS");  
//                    }
          
                    // add all subscribers
                    List<Object[]> toAdd = new ArrayList<Object[]>();
                    List<Object[]> subs = new ArrayList<Object[]>();
                    for (Subscriber s : toStore.values()) {
                        toAdd.add(new Object [] {s.getUserName(), s.getEmail(), s.getSms(), s.getReportInterval()});
                        for (Subscription sub : s.getSubscriptions().values()) {
                            subs.add(new Object [] {sub.getSubscriberId(), sub.isEnabled(), sub.getNotificationLevel().toString(), sub.getTagId(), sub.getLastNotifiedStatus().toInt(), sub.getLastNotification(), getResolvedTagsAsString(sub), sub.isNotifyOnMetricChange(), sub.isSmsNotification()});
                        }
                    }
                    
                      // add all subscribers
                    int [] addedSubscribers = jdbcTemplate.batchUpdate(
                            "INSERT INTO DMN_NOTIFY_SUBSCRIBERS (userid, email, sms, reportinterval) VALUES (?,?,?,?)", 
                            toAdd, 
                            new int [] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER}); 
                    
                    // add all subscriptions
                    int [] addedSubscriptions = jdbcTemplate.batchUpdate(
                            "INSERT INTO DMN_NOTIFY_SUBSCRIPTIONS (userid, enabled, notifylevel, tagid, lastnotifiedstate, lastnotifiedts, resolvedtags, onvaluechange, sms_enabled) VALUES (?,?,?,?,?,?,?,?,?)", 
                            subs, 
                            new int [] {Types.VARCHAR, Types.INTEGER, Types.VARCHAR, Types.INTEGER, Types.INTEGER, Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER, Types.INTEGER});
                    
                    lastStoreTime = (System.currentTimeMillis() - t1);
                    logger.info("Stored {} Subscribers and {} Subscriptions in {}msec", 
                            addedSubscribers.length , addedSubscriptions.length,  getLastStoreTime());
                    
                    lastFullStorageTime = System.currentTimeMillis();
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error(ex.getMessage());
                    arg0.setRollbackOnly();
                }
            }
        });
    }
    
    
    /**
     * loads the registry from db.
     * 
     * @return the registry will all users and their subscriptions.
     */
    @Override
    public ConcurrentHashMap<String, Subscriber> load() {
        
        long t1 = System.currentTimeMillis(); 
        
        // get all subscribers
        List<Subscriber> subscribers = jdbcTemplate.query(
                "SELECT userid, email, sms, reportinterval FROM DMN_NOTIFY_SUBSCRIBERS", 
                new RowMapper<Subscriber>() {
                    @Override
                    public Subscriber mapRow(ResultSet arg0, int rowNumber) throws SQLException {
                        return getSubscriberFromRs(arg0);
                    }
                });
        // get all subscriptions
        List<Subscription> subscriptions = jdbcTemplate.query(
                "SELECT userid, enabled, notifylevel, tagid, lastnotifiedstate, lastnotifiedts, resolvedtags, onvaluechange, sms_enabled FROM DMN_NOTIFY_SUBSCRIPTIONS", 
                new RowMapper<Subscription>() {
                    @Override
                    public Subscription mapRow(ResultSet arg0, int rowNumber) throws SQLException {
                        return getSubscriptionFromRs(arg0);
                    }
                });
        
        ConcurrentHashMap<String, Subscriber> result = new ConcurrentHashMap<String, Subscriber>(subscribers.size());
        
        // add all subscribers to the resultlist ...
        for (Subscriber s : subscribers) {
            result.put(s.getUserName(), s);
        }
        
        // ... and map subscriptions to the users.
        for (Subscription s : subscriptions) {
            Subscriber owner = result.get(s.getSubscriberId());
            if (owner == null) {
                // db inconsistency : subscription without user
                // SHOULD NEVER HAPPEN as db constraints should make this sure.
                logger.error("Found Subscription without user : " + s);
                removeSubscription(s);
            } else {
                owner.addSubscription(s);
            }
        }
        
        lastLoadTime = (System.currentTimeMillis() - t1);
        logger.info("Loaded " + subscribers.size() + " Subscribers and " + subscriptions.size() + " Subscriptions in " + getLastLoadTime() + " millis");
        
        if (logger.isTraceEnabled()) {
            for (Entry<String, Subscriber> e : result.entrySet()) {
                logger.trace("Loaded :" + e.getValue().getUserName() + " " + e.getValue().getSubscriptions());
            }
        }
        
        return result;
    }
    
    
    public Subscriber getSubscriberFromRs(ResultSet rs) throws SQLException {
        Subscriber s = null;
        if (rs == null) {
            return s;
        }
        String userId = rs.getString("userid");
        String eMail = rs.getString("email");
        String sms = rs.getString("sms");
        int reportInterval = rs.getInt("reportinterval");
        s = new Subscriber(userId, eMail, sms, reportInterval);
        return s;
    }
    
    public Subscription getSubscriptionFromRs(ResultSet rs) throws SQLException {
        Subscription s = null;
        if (rs == null) {
            return s;
        }
        Long tagId = Long.valueOf(rs.getLong("tagid"));
        String userId = rs.getString("userid");
        int enabled = rs.getInt("enabled");
        int onvalueChange = rs.getInt("onvaluechange");
        int isSmsEnabled = rs.getInt("sms_enabled");
        String level = rs.getString("notifylevel");
        int lastStatus = rs.getInt("lastnotifiedstate");
        Timestamp ts = rs.getTimestamp("lastnotifiedts");
        String tmp = rs.getString("resolvedtags");
        
        s = new Subscription(userId, tagId, Status.fromString(level));
        s.setEnabled(enabled > 0 ? true : false);
        s.setLastNotifiedStatus(Status.fromInt(lastStatus));
        s.setLastNotification(ts);
        s.setNotifyOnMetricChange(onvalueChange > 0 ? true : false);
        s.setSmsNotification(isSmsEnabled > 0 ? true : false);
        
        if (tmp != null && tmp.length() > 10) {
            for (String t2 : tmp.split(",")) {
                String childTag = t2.split("=")[0];
                String savedStatus = t2.split("=")[1];
                Long id = Long.valueOf(childTag);
                s.addResolvedSubTag(id);
                s.setLastStatusForResolvedTSubTag(id, Status.fromString(savedStatus));
            }
        }
        
        return s;
    }

    @Override
    @ManagedAttribute
    public boolean isFine() {
        try {
            // test get and close a connection.
            return !jdbcTemplate.getDataSource().getConnection().isClosed();
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    @ManagedAttribute
    public String getUniqueName() {
        return DbBackupWriter.class.toString();
    }

    @Override
    @ManagedAttribute
    public long getLastFullWrite() {
        return lastFullStorageTime;
    }
    
    
   
}
