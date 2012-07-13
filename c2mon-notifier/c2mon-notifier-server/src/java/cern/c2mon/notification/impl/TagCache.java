/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.manager.TagManager;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TagCacheUpdateListener;
import cern.c2mon.notification.shared.ServiceException;
import cern.c2mon.notification.shared.Status;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.notification.shared.TagNotFoundException;
import cern.dmn2.db.DiamonDbGateway;
import cern.dmn2.db.MetricData;
import cern.dmn2.db.EntityData.Type;
import cern.tim.shared.common.datatag.TagQualityStatus;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * 
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class TagCache implements DataTagUpdateListener {

    private Logger logger = Logger.getLogger(TagCache.class);

    private ConcurrentHashMap<Long, Tag> cache = new ConcurrentHashMap<Long, Tag>();

    private String fileName = "tag.cache";
    
    private Thread thread;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private SubscriptionRegistry registry;
    
    private List<TagCacheUpdateListener> listeners = new ArrayList<TagCacheUpdateListener>();
    
    /**
     * Constructor
     */
    public TagCache() {
        
    }
    
    /** Updates an element in the underlying cache.
     * 
     * @param update the {@link ClientDataTagValue} 
     * @return the related {@link Tag} object or <code>NULL</code> If no corresponding {@link Tag} exists 
     */
//    public Tag updateTag(ClientDataTagValue update) {
//        Tag fromCache = get(update.getId());
//        if (fromCache == null) {
//            return null;
//        }
//        fromCache.update(update);
//        return fromCache;
//    }
    
    public void registerListener(TagCacheUpdateListener listener ) {
        listeners.add(listener);
    }
    
    /**
     * starts the backup writer which saves the cache every 30 sec to disk.
     */
    public void startBackupWriter() {
        scheduler.scheduleAtFixedRate(new CacheBackupThread(), 30, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 
     * @param id the tag id to resolve 
     * @return the full resolved {@link Tag} with all its children.
     * @throws TagNotFoundException in case the id is not registered in C2Mon
     */
    public Tag resolveSubTags(Long id) throws TagNotFoundException {
        Tag root = get(id);
        
        if (root == null) {
            logger.debug("Resolving sub-tags for " + id);
            root = metricTagResolver2(id, cache);
            if (root == null) {
                logger.fatal("Tag " + id + " cannot be resolved!");
            } else {
                cache.put(root.getId(), root);
            }
        }
        return root;
    }
    
    
    /** for each item in the passed list it resolves the sub tags and adds them all to it's internal list. 
     * 
     * @param registeredSubscriptionIds a list of tag ids (rules) which users are subscribed to. 
     * @return 
     */
    public void resolveSubTags(HashSet<Long> registeredSubscriptionIds) {
        logger.info("Resolving " + registeredSubscriptionIds.size() + " tags.");
        long t1 = System.currentTimeMillis();
        
        ExecutorService pool = Executors.newFixedThreadPool(4);
        final CountDownLatch latch = new CountDownLatch(registeredSubscriptionIds.size());
        for (final Long id : registeredSubscriptionIds) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        resolveSubTags(id);
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.warn(e.getMessage());
                    }
                }
                
            });
        }
        pool.shutdown();
        try {
            latch.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        logger.info("Loaded " + getSize() + " related tag id's in " + (System.currentTimeMillis() - t1) + " msec");
    }

    /**
     * @param tagID a {@link Tag} object. No information is used from this obejct 
     *              except the {@link Tag#getId()}. 
     * @param overallList if not null all Tag objects will be put into the passed object
     * @return The 
     */
    public Tag metricTagResolver(Long tagID, HashMap<Long, Tag> overallList) {
        
        if (get(tagID) != null) {
            logger.debug("Tag #" + tagID + "+ already in cache. returning current (local) object.");
            return get(tagID);
        }
        
        if (logger.isTraceEnabled()) {
            logger.trace("Tag # " + tagID + " : Resolving the subtags using C2MonServer..");
        }
        
        checkExists(tagID);
        
        List<MetricData> list = DiamonDbGateway.getDbService().getEntityMetrics(Type.SERVICE, tagID);
        
        Tag root = new Tag(tagID, true);

        for (MetricData md : list) {
            Tag childTag = null;
            
            if (md.hasLimit()) {
                Tag childRuleTag = new Tag(md.getLimitId(), true);
                childTag = new Tag(md.getId(), false);
                logger.trace("Adding Metric " + childTag.getId() + " to " + tagID);
                childRuleTag.addChildTag(childTag);
                
                logger.trace("Adding Rule " + md.getLimitId() + " to " + tagID);
                root.addChildTag(childRuleTag);

                if (overallList != null) {
                    overallList.put(childRuleTag.getId(), childRuleTag);
                    overallList.put(childTag.getId(), childTag);
                }
            } else {
                childTag = new Tag(md.getId(), false);
                logger.trace("Adding Metric " + childTag.getId() + " to " + tagID);
                root.addChildTag(childTag);
                //children.put(childTag.getId(), childTag);
                if (overallList != null) {
                    overallList.put(childTag.getId(), childTag);
                }
            }
        }
        return root;
    }

    /**
     * 
     * @param tagId the id of the tag to resolve
     * @param overallList all found tagids related to the passed id will be added into this list
     * @return a {@link Tag} with all its children attached
     * @throws TagNotFoundException in case the passed id does not exist on the server.
     */
    public Tag metricTagResolver2(Long tagId, ConcurrentHashMap<Long, Tag> overallList) throws TagNotFoundException {
        
        if (get(tagId) != null) {
            logger.debug("Tag #" + tagId + "+ already in cache. returning current (local) object.");
            return get(tagId);
        }
        
        if (logger.isTraceEnabled()) {
            logger.trace("Tag # " + tagId + " : Resolving the subtags using C2MonServer..");
        }
        
        ClientDataTagValue parent = checkExists(tagId);
        
        Tag root = null;
        
        if (parent.isRuleResult()) {
            root = new Tag(tagId, true);
            if (overallList != null) {
                for (Long l : parent.getRuleExpression().getInputTagIds()) {
                    Tag child = metricTagResolver2(l, overallList);
                    root.addChildTag(child);
                    child.addParentTag(root);
                    overallList.put(child.getId(), child);
                    logger.trace("got resolved child : " + child);
                }
                overallList.put(tagId, root);
            } else {
                for (Long l : parent.getRuleExpression().getInputTagIds()) {
                    Tag child = metricTagResolver2(l, overallList);
                    root.addChildTag(child);
                    child.addParentTag(root);
                    logger.trace("got resolved child : " + child);
                }
            }
        } else {
            root = new Tag(tagId, false);
        }
        //root.update(parent);
        return root;
    }
    
   /**
    * 
    * @param tagId the tag to check
    * @return a {@link ClientDataTagValue} from the server
    * @throws TagNotFoundException in case the Tag is not either : not defined on server <code>ClientDataTagValue.getDataTagQuality().isExistingTag()</code>
    */
   private ClientDataTagValue checkExists(Long tagId) throws TagNotFoundException {
       HashSet<Long> toResolve = new HashSet<Long>();
       toResolve.add(tagId);
       Collection<ClientDataTagValue> oneElementList = C2monServiceGateway.getTagManager().getDataTags(toResolve);
      
       if (oneElementList.size() == 0) {
           throw new TagNotFoundException("Tag " + tagId + " not found.");
       }
       ClientDataTagValue parent = oneElementList.iterator().next();
       if (!parent.getDataTagQuality().isExistingTag()) {
           throw new TagNotFoundException("Tag " + tagId + " is not defined in server!");
       }
       return parent;
   }
    
    
    /**
     * 
     * @param id the id of the {@link Tag}
     * @return a {@link Tag} associated with the id or <code>NULL</code> if it does not exist.
     */
    public Tag get(Long id) {
        return cache.get(id);
    }

    /**
     * 
     * @return a Set of ids which are registered in this object
     */
    public Set<Long> getTagIds() {
        return cache.keySet();
    }
    
    public HashMap<Long, Tag> getMetricTags() {
        HashMap<Long, Tag> result = new HashMap<Long, Tag>();
        for (Entry<Long, Tag> e : cache.entrySet()) {
            if (!e.getValue().isRule()) {
                result.put(e.getKey(), e.getValue());
            }
        }
        return result;
    }

    
    /**
     * Removes a subscription from all tags which are related. this method 
     * @param sub a {@link Subscription} object. It will be removed from all related sub-tags from the associated Tag.   
     */
    public void removeSubscription(Subscription sub) {
        Tag root = get(sub.getTagId());
        for (Tag child : root.getChildTags()) {
            child.removeSubscription(sub);
            if (child.getSubscribers().size() == 0) {
                cache.remove(child.getId());
            }
        }
    }
    
    
//    public void initStatusFromPersistence() {
//        logger.trace("entering initStatusFromPersistence()");
//        
//        long t1 = System.currentTimeMillis();
//        
//        File f = new File(fileName);
//        if (f.exists() && f.canRead()) {
//            try {
//                for (SimpleTagInformation si : getFromPersistence()) {
//                    Tag t = new Tag(si.tagID, si.isRule);
//                    t.setHistory(si.history);
//                    logger.debug("Updating Tag " + si.tagID + " to " + t.getLatestStatusInt());
//                }
//            } catch (Exception e) {
//                throw new IllegalStateException("Cannot load tag cache from disk (" + fileName + ". Reason : " + e.getMessage(), e);
//            }
//        } else {
//            /*
//             * we do nothing (no status from disk to recover);
//             */
//        }
//        logger.info("Updated status from local file persistence within " + (System.currentTimeMillis() - t1) + "msec");
//    }

    public ArrayList<SimpleTagInformation> getFromPersistence() throws Exception {
        logger.trace("entering getFromPersistence()");
        ArrayList<SimpleTagInformation> result = null;
        Gson gson = new Gson();
        StringBuilder contents = new StringBuilder();
        BufferedReader input = new BufferedReader(new FileReader(fileName));
        long t1 = System.currentTimeMillis();
        
        try {
            String line = null; // not declared within while loop
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } finally {
            input.close();
        }

        result = gson.fromJson(contents.toString(), new TypeToken<ArrayList<SimpleTagInformation>>() {}.getType());
        if (logger.isDebugEnabled()) {
            logger.debug("Read tag cache within " + (System.currentTimeMillis() - t1) + "msec");
        }
        return result;
    }

    /**
     * writes the tag, status and hasChanged attributes from {@link #cache} to disk. 
     */
    public void writeToPersistence() {
        ArrayList<SimpleTagInformation> toStore = new ArrayList<SimpleTagInformation>(cache.size());
        logger.trace("entering writeToPersistence()");
        long t1 = System.currentTimeMillis();
        
        
        for (Entry<Long, Tag> e : cache.entrySet()) {
            SimpleTagInformation si = new SimpleTagInformation();
            si.tagID = e.getKey();
            //si.status = e.getValue().getLatestStatusInt();
            //si.previousState = e.getValue().getPreviousStatusInt();
            si.history = e.getValue().getHistory();
            si.isRule = e.getValue().isRule();
            toStore.add(si);
        }
        Gson gson = new Gson();
        FileWriter fr = null;
        BufferedWriter output = null;
        try {
            fr = new FileWriter(fileName);
            output = new BufferedWriter(fr);
            output.write(gson.toJson(toStore));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (output != null) { try { output.close(); } catch (IOException e1) { e1.printStackTrace(); 
            }
            }
            if (fr != null)     { try { fr.close(); } catch (IOException e1) { e1.printStackTrace(); 
            }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Wrote tag cache within " + (System.currentTimeMillis() - t1) + "msec");
        }
    }
    
    
    private class SimpleTagInformation {
        private long tagID;
        private boolean isRule = false;
        private Status [] history = new Status [Tag.MAX_STATE_HISTORY_ENTRIES];
    }
    
    
    private class CacheBackupThread implements Runnable {

        @Override
        public void run() {
            try {
                writeToPersistence();
            } catch (Exception ex) {
               logger.error(ex.getMessage(), ex);
            }
        }
    }


    public long getSize() {
        return cache.size();
    }
    
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Entry<Long, Tag> e : cache.entrySet()) {
            b.append("ID " + e.getKey() + " -> " + e.getValue().toString() + "\n");
        }
        return b.toString();
    }

    
    private void checkUpdateOk(ClientDataTagValue tagUpdate) {
        if (logger.isDebugEnabled()) {
            logger.debug("Update incoming : TagID=" + tagUpdate.getId() + ", " + tagUpdate.getName() + ", isRule=" + tagUpdate.isRuleResult() + ", value= " + tagUpdate.getValue() + ",valid=" + tagUpdate.getDataTagQuality().isValid()+ ",isAccessible=" + tagUpdate.getDataTagQuality().isAccessible());
        }
        
        if (!tagUpdate.getDataTagQuality().isExistingTag()) {
            for (Subscription s : registry.getSubscriptionsForTagId(tagUpdate.getId()).values()) {
                registry.removeSubscription(s);
            }
            throw new IllegalStateException("TagID=" + tagUpdate.getId() + ": Is not known by the C2Mon server!");
        }
    }
    
    @Override
    public void onUpdate(ClientDataTagValue tagUpdate) {
        
        try {
            
            if (tagUpdate.getDataTagQuality().getInvalidQualityStates().containsKey(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED)) {
                logger.error("Server Heartbeat lost. Waiting for revival...");
                return;
            }
            
            checkUpdateOk(tagUpdate);
        
            Tag tag = cache.get(tagUpdate.getId());
            if (tag == null) {
                logger.warn("I have recevied an update " + tagUpdate.getId() + " I am not (yet) aware of. Discarding...");
                // TODO UNSUBSCRIBE from this item ?
                return;
            }
            
            tag.update(tagUpdate);
            for (TagCacheUpdateListener l : listeners) {
                l.onUpdate(tag);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex);
        }
    }
  
    
    
    /** Resolves all sub-metrics from each element in <code>s</code> and using {@link TagCache#metricTagResolver(Tag, HashMap)} 
     *   and calls {@link TagManager#subscribeDataTags(java.util.Set, DataTagUpdateListener)}
     * 
     * @param list the list of tags to subscribe to.
     * @throws TagNotFoundException in case one of the passed tagid cannot be subscribed to.
     */
    public void startSubscription(HashSet<Subscription> list) throws TagNotFoundException {
        try {
            HashSet<Long> toSubscribeTo = new HashSet<Long>();
            
            for (Subscription s : list) {
                logger.info("Starting subscription on Tag " + s.getTagId());
                // let the cache resolve the subtags and then we subscribe to them here afterwards.
                Tag root = resolveSubTags(s.getTagId());
                
                for (Tag child : root.getAllChildTagsRecursive()) {
                    toSubscribeTo.add(child.getId());
                    if (child.isRule()) {
                        // only RuleTags know about their subscription
                        child.addSubscription(s);
                    }
                    // .. and we need to know our resolved sub tags.
                    s.addResolvedSubTag(child.getId());
                }
                // we need to add ourself as well.
                toSubscribeTo.add(root.getId());
                root.addSubscription(s);
            }
            
            Collection<ClientDataTagValue> freshFromServer = C2monServiceGateway.getTagManager().getDataTags(toSubscribeTo);
            for (ClientDataTagValue c : freshFromServer) {
                checkUpdateOk(c);
                cache.get(c.getId()).update(c);                
            }
            for (Subscription s : list) {
                for (TagCacheUpdateListener l : listeners) {
                    l.onUpdate(cache.get(s.getTagId()));
                }
            }
            
            if (toSubscribeTo.size() > 0) {
                C2monServiceGateway.getTagManager().subscribeDataTags(toSubscribeTo, this);
            }
            
            
        } catch (TagNotFoundException ex) {
            throw ex;  
        } catch (Exception ex) {
            throw new ServiceException("Cannot subscribe to datatag : " + ex.getMessage());
        }
    }
    
    
    /** Triggers the {@link TagCache#removeTag(Long)} and calls {@link C2monServiceGateway#getTagManager()#cancelSubscription(Subscription)}. 
     * 
     * @see {@link SubscriptionRegistryImpl#setSubscriber(Subscriber)}
     * @see {@link SubscriptionRegistryImpl#removeSubscription(Subscription)} 
     * @param subscription
     * @throws ServiceException
     */
    public void cancelSubscription(Subscription subscription) throws ServiceException {
        
        // TODO : move into SubscriptionRegistry
        
        try {
            if (subscription.getResolvedSubTagIds().size() == 0) {
                throw new ServiceException("The given subscription didn't contain the resolved subtags !");
            }
            
            /*
             * remove the tags from the cache
             */
            HashSet<Long> toRemove = new HashSet<Long>(subscription.getResolvedSubTagIds());
            
            for (Long l : toRemove) {
                cache.get(l).removeSubscription(subscription);
            }
            cache.get(subscription.getTagId()).removeSubscription(subscription);
            
            /*
             * let's cancel the the subscription 
             */
            C2monServiceGateway.getTagManager().unsubscribeDataTags(toRemove, this);
            
        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

    /**
     * @return Returns the registry.
     */
    public SubscriptionRegistry getRegistry() {
        return registry;
    }

    /**
     * @param registry The registry to set.
     */
    public void setRegistry(SubscriptionRegistry registry) {
        this.registry = registry;
    }
    
    
}
