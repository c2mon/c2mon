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

import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.notification.shared.Status;

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
    
//    private int latestStatus = Status.OK.toInteger();
//    private int previousStatus = Status.OK.toInteger();
    
    /**
     * only if we are a rule this field is used.
     */
    private List<Tag> children = new ArrayList<Tag>();
    private HashMap<Long, Tag> parents = new HashMap<Long, Tag>();
    
    private Status [] history;
    private int currentHistoryPtr = 0;
    
    
    public Tag(Long id, boolean isRule) {
       this.id = id; 
       this.isRule = isRule;
       
       history = new Status[MAX_STATE_HISTORY_ENTRIES]; 
       for (int i = 0; i < MAX_STATE_HISTORY_ENTRIES; i++) {
           history[i] = Status.OK;
       }
       currentHistoryPtr = 0;
    }

    public boolean isRule() {
        return isRule;
    }
    
    public Long getId() {
        return id;
    }
    
//    public void setStatus(int status) {
//        if (latest == null) {
//            previousStatus = latestStatus;
//            latestStatus = status;
//        } else {
//            throw new IllegalStateException("Attempt to set status of Tag " + getId() + " to " + status + "  but I have a ClientDataTagValue assigned. Not allowed.");
//        }
//    }
    
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
            return history[MAX_STATE_HISTORY_ENTRIES-1];
        }
        return history[currentHistoryPtr];
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
            return (getPreviousStatus().equals(getLatestStatus()));
        } else {
          // for metric always true.
          return true;  
        }
    }
    
    public List<Tag> getChildTags() {
        return children;
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
        
        if (update.isRuleResult()) {
            if (update.getValue() != null) {
                newStatus = ((Double) update.getValue()).intValue();
            }
        }
        
        //previousStatus = latestStatus;
        //latestStatus = newStatus;
        latest = update;
        
        
        currentHistoryPtr++;
        if (currentHistoryPtr == MAX_STATE_HISTORY_ENTRIES)
            currentHistoryPtr = 0;
        history[currentHistoryPtr] = Status.fromInt(newStatus);
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<Long, Tag> getParents() {
        return (HashMap<Long, Tag>) parents.clone();
    }
    
    public ClientDataTagValue getLatestUpdate() {
        return latest;
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
       
       
//       for (Tag child : getChildTags()) {
//           if (child.isRule()) {
//               sb.append("Rule{ID=").append(child.getId());
//               if (child.getLatestUpdate() != null) {
//                   sb
//                   .append(",isValid=" + child.getLatestUpdate().isValid())
//                   .append(",Description=" + child.getLatestUpdate().getDescription())
//                   .append(",VDescription=" + child.getLatestUpdate().getValueDescription())
//                   .append(",Name=" + child.getLatestUpdate().getName())
//                   .append(",Value=" + child.getLatestUpdate().getValue()).append(",");
//               }
//               for (Tag t : child.getChildTags()) {
//                   sb.append("\n").append(t.toString()).append(",\n");
//               }
//               sb.append("}");
//           } else {
//               sb.append("Metric{ID=").append(child.getId()); 
//               if (child.getLatestUpdate() != null) {
//                   sb.append(",DTQDescription=" + child.getLatestUpdate().getDataTagQuality().getDescription())
//                   .append(",Description=" + child.getLatestUpdate().getDescription())
//                   .append(",VDescription=" + child.getLatestUpdate().getValueDescription())
//                   .append(",isValid=" + child.getLatestUpdate().getDataTagQuality().isValid())
//                   .append(",Value=" + child.getLatestUpdate().getValue());
//               }
//               sb.append("}");
//           }
//       }
       return sb.toString();
    }
    
    /**
     * 
     * @return list of all rules with status != OK.<br> 
     *          This happens recursively so that all tags returned will have exactly 
     *          one metric assigned 
     *          (if a rule has > 1 metric it will appear in the list the number of times it has metrics).
     *        
     */
//    public List<Tag> getInterestingMetrics() {
//        ArrayList<Tag> result = new ArrayList<Tag>();
//        
//        /*
//         * we need to add ourself as we may have children
//         */
//        if (this.isRule()) {
//            for (Tag c : getChildTags()) {
//                List<Tag> problemChilds = c.getInterestingMetrics();
//                result.addAll(problemChilds);
//            }
//        } else {
//            if (logger.isDebugEnabled()) {
//                if (getParent() != null) {
//                    logger.trace("Checking child tag " + getId() + " with parent tagID " + getParent().getId() + " and its status " + getParent().getLatestStatus());
//                } else {
//                    logger.trace("Checking child tag " + getId());
//                }
//            }
//            if (
//                    getParent() != null 
//                    && getParent().getLatestUpdate().getRuleExpression().getInputTagIds().contains((getId()))) {
//                logger.debug("Adding metric tag " + getId() + " for parent TagID=" + getParent().getId());
//                result.add(this);
//            }
//        } 
//        return result;
//    }
    
//    /**
//     * 
//     * @param rule the Tag
//     * @return a list of all metrics (not rules) for the given Tag
//     */
//    public HashSet<Tag> getAllMetrics() {
//        //ArrayList<Tag> result = new ArrayList<Tag>();
//        HashSet<Tag> toReturn = new HashSet<Tag>();
//        
//        for (Tag c : getChildTags()) {
//            if (c.isRule()) {
//                toReturn.addAll(getAllMetrics());
//                //result.addAll(c.getAllMetrics());
//            } else {
//                toReturn.add(c);
//            }
//        }
//        return toReturn;
//    }
    
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    //
    // -- implements XXXX -----------------------------------------------
    //

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    // 
    // -- INNER CLASSES -----------------------------------------------
    //
}
