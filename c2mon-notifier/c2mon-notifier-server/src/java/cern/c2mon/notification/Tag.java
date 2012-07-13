/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import org.apache.log4j.Logger;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.notification.shared.Status;
import cern.c2mon.notification.shared.Subscription;

/**
 * 
 * 
 */
public class Tag {

    public static int MAX_STATE_HISTORY_ENTRIES = 10;
    
    private static Logger logger = Logger.getLogger(Tag.class);
    
    private boolean isRule = false;
    
    private final Long id;
    
    private ClientDataTagValue latest = null;
    private ClientDataTagValue previous = null;
    
//    private int latestStatus = Status.OK.toInteger();
//    private int previousStatus = Status.OK.toInteger();
    
    /**
     * only if we are a rule this field is used.
     */
    private HashSet<Tag> children = new HashSet<Tag>();
    /**
     * this can be used to a rule or metric
     */
    private HashMap<Long, Tag> parents = new HashMap<Long, Tag>();
    
    private Status [] history;
    private int currentHistoryPtr = 0;
    
    private HashSet<Subscription> subscribers = new HashSet<Subscription>();
    
    /**
     * 
     */
    private boolean isSourceDown = false;
    private boolean wasSourceDown = false;
    
    //private int subscribers = 0;
    
    
    public Tag(Long id, boolean isRule) {
       this.id = id; 
       this.isRule = isRule;
       
       history = new Status[MAX_STATE_HISTORY_ENTRIES]; 
       for (int i = 0; i < MAX_STATE_HISTORY_ENTRIES; i++) {
           history[i] = Status.OK;
       }
       currentHistoryPtr = 0;
    }

    public synchronized void removeSubscription(Subscription s) {
        for (Tag t : children) {
            t.removeSubscription(s);
        }
        subscribers.remove(s);
    }
    public synchronized void addSubscription(Subscription s) {
        for (Tag t : children) {
            t.addSubscription(s);
        }
        subscribers.add(s);
    }
    public synchronized HashSet<Subscription> getSubscribers() {
        return subscribers;
    }
    public synchronized void removeAllSubscriptions() {
        subscribers.clear();
    }
    
    public boolean isRule() {
        return isRule;
    }
    
    public Long getId() {
        return id;
    }
    
    public int getLatestStatusInt() {
        return getLatestStatus().toInteger();
    }
    
    public Status getLatestStatus() {
        return history[currentHistoryPtr];
    }
    
    /**
     * @return Returns the previousStatus.
     */
    public int getPreviousStatusInt() {
        return getPreviousStatus().toInteger();
    }
    /**
     * @return Returns the previousStatus.
     */
    public Status getPreviousStatus() {
        if (currentHistoryPtr == 0) {
            return history[MAX_STATE_HISTORY_ENTRIES - 1];
        }
        return history[currentHistoryPtr - 1];
    }

    public Status [] getHistory() {
        return history;
    }
    
    /** Used to set the history after restart of the server.
     * 
     * @param h a new history represented by elements of {@link Status}.
     */
    public void setHistory(Status [] h) {
        int toCopy = h.length < MAX_STATE_HISTORY_ENTRIES ? h.length : MAX_STATE_HISTORY_ENTRIES;
        
        for (int i = 0; i < toCopy; i++) {
            this.history[i] = h[i];
        }
    }
    
    public boolean hasStatusChanged() {
        if (this.isRule()) {
            // for rule only true if the status has changed 
            return (!getPreviousStatus().equals(getLatestStatus()));
        } else {
          // for metric always true.
          return true;  
        }
    }
    
    public boolean hasValueChanged() {
        if (previous != null) {
            return Tag.hasValueChanged(previous, latest);
        } else {
            return true;
        }
    }
    
    public HashSet<Tag> getChildTags() {
        return children;
    }
    
    public HashSet<Tag> getAllChildTagsRecursive() {
        HashSet<Tag> list = new HashSet<Tag>();
        for (Tag child : children) {
            list.addAll(child.getAllChildTagsRecursive());
            list.add(child);
        }
        return list;
    }

    public void addChildTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Passed a null object when adding child tag!");
        }
        this.children.add(tag);
        tag.addParentTag(this);
    }
    public void removeChildTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Passed a null object when removing child tag!");
        }
        this.children.remove(tag.getId());
    }
    public void addParentTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Passed a null object when adding child tag!");
        }
        if (!tag.isRule()) {
            throw new IllegalArgumentException("Tag " + getId() + "cannot be assigned to a non-rule tag " + tag.getId());
        }
        this.parents.put(tag.getId(), tag);
    }
    public void removeParentTag(Tag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Passed a null object when removing parent tag!");
        }
        this.parents.remove(tag.getId());
    }

    
    public void update(ClientDataTagValue update) {
        int newStatus = Status.UNKNOWN.toInteger();
        
        previous = latest;
        latest = update;
        
        if (latest.isRuleResult()) {
            /*
             * Rule
             */
            if (latest.getValue() != null) {
                newStatus = ((Double) latest.getValue()).intValue();
            }
            currentHistoryPtr++;
            if (currentHistoryPtr == MAX_STATE_HISTORY_ENTRIES)
                currentHistoryPtr = 0;
            history[currentHistoryPtr] = Status.fromInt(newStatus);
            
        } 
        
        if (update != null && !update.getDataTagQuality().isAccessible()) {
            this.isSourceDown = true; 
        } else {
            this.isSourceDown  = false;
        }
        /*
            else {
            
            // nothing, just update.
            } 
         */ 
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Long, Tag> getParents() {
        return (HashMap<Long, Tag>) parents.clone();
    }
    
    public ClientDataTagValue getLatestUpdate() {
        return latest;
    }
    
    /**
     * @return Returns the isSourceDown.
     */
    public boolean isSourceDown() {
        return isSourceDown;
    }

    /**
     * @param isSourceDown The isSourceDown to set.
     */
    public void setSourceDown(boolean isSourceDown) {
        this.wasSourceDown = this.isSourceDown;
        this.isSourceDown = isSourceDown;
    }
    
    public boolean wasSourceDown() {
        return this.wasSourceDown;
    }

    public String toString() {
       StringBuilder sb = new StringBuilder();

       if (this.isRule()) {
           sb.append("Rule ID=").append(getId())
             .append(",Prev.State=").append(getPreviousStatus().toString())
             .append(",Status=").append(this.getLatestStatus().toString());
           if (getLatestUpdate() != null) {
               sb
               .append(",isValid=" + getLatestUpdate().isValid())
               .append(",Description=" + getLatestUpdate().getDescription())
               .append(",VDescription=" + getLatestUpdate().getValueDescription())
               .append(",Name=" + getLatestUpdate().getName());
           }
           sb.append(",Children={");
           for (Tag child : getChildTags()) {
               sb.append(child.toString()).append(",");
           }
           sb.append("}");
       } else {
           sb.append("Metric ID=").append(getId());
           sb.append(",Value=");
           if (getLatestUpdate() != null) {
               sb
               .append(",DTQDescription=" + getLatestUpdate().getDataTagQuality().getDescription())
               .append(",Description=" + getLatestUpdate().getDescription())
               .append(",VDescription=" + getLatestUpdate().getValueDescription())
               .append(",isValid=" + getLatestUpdate().getDataTagQuality().isValid())
               .append(",Value=" + getLatestUpdate().getValue());
           } else {
               sb.append("UNKNOWN");
           }
           
       }
       return sb.toString();
    }
    
    
    
    public static boolean hasValueChanged(ClientDataTagValue before, ClientDataTagValue after) {
        boolean result = false;
        if (before.getTypeNumeric().equals(TypeNumeric.TYPE_DOUBLE)) {
            result = (Double) before.getValue() != (Double) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_INTEGER)) {
            result = ((Integer) before.getValue()) != (Integer) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_STRING)) {
            result = ((String) before.getValue()) != (String) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_FLOAT)) {
            result = ((Float) before.getValue()) != (Float) after.getValue();
        } else if (before.getTypeNumeric().equals(TypeNumeric.TYPE_LONG)) {
            result = ((Long) before.getValue()) != (Long) after.getValue();
        } else {
            result = false;
        }
        return result;
    }
    
    
}
