/*
 * 
 * Copyright CERN 2013, All Rights Reserved.
 */
package cern.c2mon.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.common.tag.TypeNumeric;
import cern.c2mon.notification.shared.Subscription;
import cern.dmn2.core.Status;

/**
 * A class which represents a Rule or Metric Tag for c2mon.
 */
public class Tag {

    public static int MAX_STATE_HISTORY_ENTRIES = 10;

    private static Logger logger = LoggerFactory.getLogger(Tag.class);

    private boolean isRule = false;

    private final Long id;

    private ClientDataTagValue latest = null;
    private ClientDataTagValue previous = null;

    /**
     * only if we are a rule this field is used.
     */
    private HashSet<Tag> children = new HashSet<Tag>();
    /**
     * this can be used to a rule or metric
     */
    private HashMap<Long, Tag> parents = new HashMap<Long, Tag>();

    private Status[] history;
    private int currentHistoryPtr = 0;

    private HashSet<Subscription> subscribers = new HashSet<Subscription>();

    private AtomicBoolean toBeNotified = new AtomicBoolean(false);
    /**
     * 
     */
//    private boolean isSourceDown = false;
//    private boolean wasSourceDown = false;

    // private int subscribers = 0;

    /**
     * @param id the tag id
     * @param isRule flag to set if this tag is a rule or not.
     */
    public Tag(Long id, boolean isRule) {
        this.id = id;
        this.isRule = isRule;

        history = new Status[MAX_STATE_HISTORY_ENTRIES];
        for (int i = 0; i < MAX_STATE_HISTORY_ENTRIES; i++) {
            history[i] = Status.OK;
        }
        currentHistoryPtr = 0;
    }

    /**
     * thread-safe call to remove the passed subscription from this tag.
     * @param s the Subscription of the user.
     */
    public synchronized void removeSubscription(Subscription s) {
        for (Tag t : children) {
            t.removeSubscription(s);
        }
        subscribers.remove(s);
    }

    /**
     * thread-safe call to add the passed subscription to this tag.
     * @param s the Subscription of the user.
     */
    public synchronized void addSubscription(Subscription s) {
        for (Tag t : children) {
            t.addSubscription(s);
        }
        subscribers.add(s);
    }

    public synchronized HashSet<Subscription> getSubscribers() {
        return subscribers;
    }

    /**
     * thread-safe call to remove all subscriptions from this tag
     */
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
        return getLatestStatus().toInt();
    }

    public Status getLatestStatus() {
        return history[currentHistoryPtr];
    }

    /**
     * @return Returns the previousStatus.
     */
    public int getPreviousStatusInt() {
        return getPreviousStatus().toInt();
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

    public Status[] getHistory() {
        return history;
    }

    /**
     * Used to set the history after restart of the server.
     * 
     * @param h a new history represented by elements of {@link Status}.
     */
    public void setHistory(Status[] h) {
        int toCopy = h.length < MAX_STATE_HISTORY_ENTRIES ? h.length : MAX_STATE_HISTORY_ENTRIES;

        for (int i = 0; i < toCopy; i++) {
            this.history[i] = h[i];
        }
    }

    /**
     * 
     * @return TRUE in case this tag has changed its status, FALSE otherwise. 
     *         n.b. A metric tag will ALWAYS 
     * 
     */
    public boolean hasStatusChanged() {
        if (this.isRule()) {
            // for rule only true if the status has changed
            return (!getPreviousStatus().equals(getLatestStatus()));
        } else {
            // for metric always true.
            return true;
        }
    }
    
    public List<Tag> getChangedChildRules() {
        ArrayList<Tag> result = new ArrayList<>();
        for (Tag t : this.getAllChildRules()) {
            if (t.hasStatusChanged() && t.getToBeNotified()) {
                result.add(t);
            }
        }
        return result;
    }
    
    public List<Tag> getChangedChildMetrics() {
        ArrayList<Tag> result = new ArrayList<>();
        for (Tag t : this.getAllChildMetrics()) {
            if (t.hasValueChanged() && t.getToBeNotified()) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 
     * @return TRUE in case this (metric) tag has changed its value, FALSE otherwise. 
     */
    public boolean hasValueChanged() {
        if (previous != null) {
            return Tag.hasValueChanged(previous, latest);
        } else {
            return true;
        }
    }

    /**
     * @return Returns the toBeNotified.
     */
    public boolean getToBeNotified() {
        return toBeNotified.get();
    }

    /**
     * @param flag if this tag is to be notified 
     */
    public void setToBeNotified(boolean flag) {
        this.toBeNotified.set(flag);
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

    public HashSet<Tag> getAllChildRules() {
        HashSet<Tag> list = new HashSet<Tag>();
        for (Tag c : children) {
            if (c.isRule()) {
                list.add(c);
            }
        }
        return list;
    }

    public HashSet<Tag> getAllChildMetrics() {
        HashSet<Tag> list = new HashSet<Tag>();
        for (Tag c : children) {
            if (!c.isRule()) {
                list.add(c);
            }
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

    /**
     * Updates this Tag object with the incoming {@link ClientDataTagValue}. If
     * ClientDataTagValue.getDataTagQuality().isAccessible() is true the {@link #isSourceDown()} will also return true;
     * 
     * @param update the {@link ClientDataTagValue}
     */
    public void update(ClientDataTagValue update) {
        int newStatus = Status.UNKNOWN.toInt();

        if (update.isRuleResult()) {
            /*
             * Rule
             */
            if (update.getValue() != null) {

                if (update.getValue() instanceof Integer) {
                    newStatus = ((Integer)update.getValue()).intValue();
                } else if (update.getValue() instanceof Double) {
                    newStatus = ((Double) update.getValue()).intValue();
                } else {
                    throw new IllegalArgumentException("TagID=" + update.getId()
                            + ": I cannot interprete the passed ClientDataTagValue as a status :" + update.getValue());
                }
            }

            currentHistoryPtr++;
            if (currentHistoryPtr == MAX_STATE_HISTORY_ENTRIES) {
                currentHistoryPtr = 0;
            }
            history[currentHistoryPtr] = Status.fromInt(newStatus);

            if (logger.isTraceEnabled()) {
                logger.trace("updated RuleTag=" + update.getId() + " to status " + getLatestStatus() + ", value="
                        + update.getValue() + ", interpreted=" + newStatus);
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("updated MetricTag=" + update.getId() + " to value=" + update.getValue());
            }
        }

        previous = latest;
        latest = update;

//        if (!update.getDataTagQuality().isAccessible()) {
//            this.isSourceDown = true;
//        } else {
//            this.isSourceDown = false;
//        }
    }

    @SuppressWarnings("unchecked")
    public HashMap<Long, Tag> getParents() {
        return (HashMap<Long, Tag>) parents.clone();
    }

    public ClientDataTagValue getLatestUpdate() {
        return latest;
    }

    
    public boolean hasStatusRecovered() {
        return getPreviousStatus().worserThan(Status.OK) && getLatestStatus().equals(Status.OK);
    }
    
//    /**
//     * @return Returns the isSourceDown.
//     */
//    public boolean isSourceDown() {
//        return isSourceDown;
//    }

//    /**
//     * @param isSourceDown The isSourceDown to set.
//     */
//    public void setSourceDown(boolean isSourceDown) {
//        this.wasSourceDown = this.isSourceDown;
//        this.isSourceDown = isSourceDown;
//    }
//
//    public boolean wasSourceDown() {
//        return this.wasSourceDown;
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.isRule()) {
            sb.append("Rule ID=").append(getId()).append(",Prev.State=").append(getPreviousStatus().toString()).append(
                    ",Status=").append(this.getLatestStatus().toString()).append(",NotifyRequired=").append(
                    this.getToBeNotified());

            if (getLatestUpdate() != null) {
                sb.append(",isValid=" + getLatestUpdate().isValid()).append(
                        ",Description=" + getLatestUpdate().getDescription()).append(
                        ",VDescription=" + getLatestUpdate().getValueDescription()).append(
                        ",Name=" + getLatestUpdate().getName());
            }
            sb.append(",Children={");
            for (Tag child : getChildTags()) {
                sb.append(child.toString()).append(",");
            }
            sb.append("}");
        } else {
            sb.append("Metric ID=").append(getId()).append(",NotifyRequired=").append(this.getToBeNotified());
            sb.append(",Value=");
            if (getLatestUpdate() != null) {
                sb.append(",DTQDescription=" + getLatestUpdate().getDataTagQuality().getDescription()).append(
                        ",Description=" + getLatestUpdate().getDescription()).append(
                        ",VDescription=" + getLatestUpdate().getValueDescription()).append(
                        ",isValid=" + getLatestUpdate().getDataTagQuality().isValid()).append(
                        ",Value=" + getLatestUpdate().getValue());
            } else {
                sb.append("UNKNOWN");
            }

        }
        return sb.toString();
    }

    /**
     * 
     * @param before the value before 
     * @param after the value after
     * @return TRUE in case the two passed (numerical) values are different.
     */
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

    @Override
    public boolean equals(Object o) {
        Tag s = (Tag) o;
        return (s.getId().equals(this.getId()));
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
