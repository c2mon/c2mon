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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.shared.Status;
import cern.dmn2.db.DiamonDbGateway;
import cern.dmn2.db.MetricData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TagCache {

    private Logger logger = Logger.getLogger(TagCache.class);

    private HashMap<Long, Tag> cache = new HashMap<Long, Tag>();

    private String fileName = "tag.cache";
    
    private Thread thread;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TagCache() {
        
    }
    
    public Tag updateTag(ClientDataTagValue update) {
        Tag fromCache = get(update.getId());
        if (fromCache == null) {
            return null;
        }
        fromCache.update(update);
        return fromCache;
    }
    
    public void start() {
        scheduler.scheduleAtFixedRate(new CacheBackupThread(), 30, 30, TimeUnit.SECONDS);
    }
    
    public void initAndResolve(HashSet<Long> registeredSubscriptionIds) {
        resolveSubTags(registeredSubscriptionIds);
    }
    
    /** for each item in the passed list it resolves the sub tags and adds them all to it's internal list. 
     * Here, a tag can then be updated using {@link #updateTag(ClientDataTagValue)}.
     * 
     * @param registeredSubscriptionIds a list of tag ids (rules) which users are subscribed to. 
     * @return 
     */
    public void resolveSubTags(HashSet<Long> registeredSubscriptionIds) {
        logger.info("Resolving " + registeredSubscriptionIds.size() + " tags with info from DB...");
        long t1 = System.currentTimeMillis();
        Tag root = null;
        
        for (Long id : registeredSubscriptionIds) {
            root = new Tag(id, true);
            root = metricTagResolver(root, cache);
            if (root == null) {
                logger.fatal("Tag " + id + " cannot be resolved!");
            } else {
                cache.put(root.getId(), root);
            }
        }
        
        logger.info("Loaded " + getSize() + " related tag id's in " + (System.currentTimeMillis() - t1) + " msec");
    }

    /**
     * @param tagID a {@link Tag} object. No information is used from this obejct 
     *              except the {@link Tag#getId()}. 
     * @param overallList if not null all Tag objects will be put into the passed object
     * @return The 
     */
    public Tag metricTagResolver(Tag tagID, HashMap<Long, Tag> overallList) {
        List<MetricData> list = DiamonDbGateway.getDbService().getEntityMetrics(tagID.getId());
        Tag ruleTag = tagID;

        for (MetricData md : list) {
            Tag childTag = null;
            if (md.hasLimit()) {
                ruleTag = new Tag(md.getLimitId(), true);
                childTag = new Tag(md.getId(), false);
                logger.trace("Adding Metric " + childTag.getId() + " to " + ruleTag.getId());
                //children.put(childTag.getId(), childTag);
                ruleTag.addChildTag(childTag);
                
                logger.trace("Adding Rule " + md.getLimitId() + " to " + tagID.getId());
                tagID.addChildTag(ruleTag);
                //parents.put(ruleTag.getId(), ruleTag);

                if (overallList != null) {
                    overallList.put(ruleTag.getId(), ruleTag);
                    overallList.put(childTag.getId(), childTag);
                }
            } else {
                childTag = new Tag(md.getId(), false);
                logger.trace("Adding Metric " + childTag.getId() + " to " + tagID.getId());
                tagID.addChildTag(childTag);
                //children.put(childTag.getId(), childTag);
                if (overallList != null) {
                    overallList.put(childTag.getId(), childTag);
                }
            }
        }
        return tagID;
    }

    

    public Tag get(Long id) {
        return cache.get(id);
    }

    public Set<Long> getTagIds() {
        return cache.keySet();
    }

    /** Removes a tag from the cache. Nothing happens if it does not exist.
     * 
     * @param id the id of the tag to remove.
     */
    public void removeTag(Long id) {
        synchronized (cache) {
            Tag parent = cache.get(id);
            
            if (parent == null) {
                return;
            }
            
            for (Tag toRemove : parent.getChildTags()) {
                cache.remove(toRemove.getId());
            }
            cache.remove(id);
        }
    }
    
    
    public void initStatusFromPersistence() {
        logger.trace("entering initStatusFromPersistence()");
        
        long t1 = System.currentTimeMillis();
        
        File f = new File(fileName);
        if (f.exists() && f.canRead()) {
            try {
                for (SimpleTagInformation si : getFromPersistence()) {
                    Tag t = cache.get(si.tagID);
                    if (t != null) {
                        t.setHistory(si.history);
                        //t.setStatus(si.status);
                        //t.setPreviousStatus(si.previousState);
                        logger.debug("Updating Tag " + si.tagID + " to " + t.getLatestStatusInt());
                    } else {
                        logger.debug("Tag " + si.tagID + " does not exist in my cache.");
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot load tag cache from disk (" + fileName + ". Reason : " + e.getMessage(), e);
            }
        } else {
            /*
             * we do nothing (no status from disk to recover);
             */
        }
        logger.info("Updated status from local file persistence within " + (System.currentTimeMillis() - t1) + "msec");
    }

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
    
}
