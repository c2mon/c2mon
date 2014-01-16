package cern.c2mon.server.common.alive;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import cern.c2mon.shared.common.Cacheable;

public class AliveTimerCacheObject implements AliveTimer, Cacheable, Cloneable {

    private static final long serialVersionUID = 2151886747282763819L;

    /**
     * Unique id of the alive tag.
     */
    private Long aliveTagId;

    /**
     * Interval at which the alive tag must be updated. This interval includes the ALIVE_TOLERANCE_FACTOR in order to
     * make sure that slight delays in data transmission or minor interruptions do not cause BigBrother to invalidate
     * data.
     */
    private Integer aliveInterval;

    /**
     * Type of alive tag: either ALIVE_TYPE_PROCESS, ALIVE_TYPE_EQUIPMENT or ALIVE_TYPE_SUBEQUIPMENT The aliveType, in
     * combination with the relatedId allows BigBrother to determine what action to take if the alive expires.
     */
    private String aliveType;

    /**
     * Contains the description of the aliveTimer type
     */
    private String aliveTypeDescription;

    /**
     * Identifier of the equipment or process represented by the alive tag. The relatedId, in combination with the
     * aliveType allows BigBrother to determine what action to take if the alive expires.
     */
    private Long relatedId;

    /**
     * Name of the equipment or process represented by the alive tag. This information is used by BigBrother to generate
     * human-readable error messages.
     */
    private String relatedName;

    /**
     * Identifier of the state tag of the equipment/process to which the alive tag is attached.
     */
    private Long relatedStateTagId;

    /**
     * Timestamp of the last update of the alive tag. This information is used to periodically check which alive timers
     * have expired.
     */
    private long lastUpdate;

    /**
     * All dependent alive timers. In the case of a "process alive", this collection will contain the ids of the alive
     * timers representing equipment attached to the DAQ process. If a process alive expires, BigBrother will stop the
     * alive timers of all equipment attached to the process as no data can possibly be acquired from the equipment if
     * the DAQ process is stopped. TODO are these used??? remove if not necessary
     */
    private Collection<Long> dependentAliveTimerIds = null;

    /**
     * Flag indicating whether the alive timer is active or not.
     */
    private boolean active = false;

    // ---------------------------------------------------------------------------
    // CONSTRUCTORS
    // ---------------------------------------------------------------------------

    /**
     * Constructor setting minimal set of non-null fields.
     */
    public AliveTimerCacheObject(Long id) {
        this.aliveTagId = id;
    }

    /**
     * Constructor used in iBatis sqlmap.
     * 
     * @param aliveTagId
     * @param relatedId
     * @param relatedName
     * @param relatedStateTagId
     * @param aliveType
     * @param aliveInterval
     */
    public AliveTimerCacheObject(final Long aliveTagId, final Long relatedId, final String relatedName,
            final Long relatedStateTagId, final String aliveType, final Integer aliveInterval) {
        this(aliveTagId);
        this.relatedId = relatedId;
        this.relatedName = relatedName;
        this.relatedStateTagId = relatedStateTagId;
        setAliveType(aliveType); // also sets alive description field
        this.aliveInterval = aliveInterval;
    }

    // public AliveTimer(final Long pAliveTagId, final Long pRelatedId, final String pRelatedName,
    // final Long pRelatedStateTagId, final String pAliveType, final int pAliveInterval,
    // final Collection pRelatedAliveTimerIds) {
    // this.aliveTagId = pAliveTagId;
    // this.aliveType = pAliveType;
    // this.relatedId = pRelatedId;
    // this.relatedName = pRelatedName;
    // this.aliveInterval = pAliveInterval * ALIVE_TOLERANCE_FACTOR;
    // this.relatedStateTagId = pRelatedStateTagId;
    // this.relatedAliveTimerIds = pRelatedAliveTimerIds;
    // if (aliveType.equals(AliveTimer.ALIVE_TYPE_PROCESS)) {
    // this.aliveTypeDescription = AliveTimer.PROCESS_MSG;
    // } else {
    // if (aliveType.equals(AliveTimer.ALIVE_TYPE_EQUIPMENT)) {
    // this.aliveTypeDescription = AliveTimer.EQUIPMENT_MSG;
    // } else {
    // this.aliveTypeDescription = AliveTimer.SUBEQUIPMENT_MSG;
    // }
    // }
    //
    // if (LOG.isDebugEnabled()) {
    // StringBuffer str = new StringBuffer("AliveTimer() : creating alive for ");
    // str.append(this.aliveTypeDescription);
    // str.append(this.relatedName);
    // str.append(".");
    // LOG.debug(str);
    // }
    // }

    @Override
    public AliveTimerCacheObject clone() throws CloneNotSupportedException {
        AliveTimerCacheObject aliveTimer = (AliveTimerCacheObject) super.clone();
        aliveTimer.dependentAliveTimerIds = new ArrayList<Long>(this.dependentAliveTimerIds);
        
        return aliveTimer;
    }

    @Override
    public Long getId() {
        return getAliveTagId();

    }

    public synchronized String getAliveType() {
        return this.aliveType;
    }

    /**
     * @return the aliveTypeDescription
     */
    @Override
    public final synchronized String getAliveTypeDescription() {
        return aliveTypeDescription;
    }

    @Override
    public synchronized Long getRelatedId() {
        return this.relatedId;
    }

    @Override
    public synchronized String getRelatedName() {
        return this.relatedName;
    }

    @Override
    public synchronized Long getRelatedStateTagId() {
        return this.relatedStateTagId;
    }

    public synchronized Timestamp getUpdateTimestamp() {
        return new Timestamp(this.lastUpdate);
    }

    /**
     * Check whether this alive timer is related to a process alive tag.
     * 
     * @return true if this alive timer is related to a process alive tag.
     */
    @Override
    public synchronized boolean isProcessAliveType() {
        return (getAliveType().equals(AliveTimer.ALIVE_TYPE_PROCESS));
    }

    /**
     * Check whether this alive timer is related to an equipemnt alive tag.
     * 
     * @return true if this alive timer is related to an equipemnt alive tag.
     */
    @Override
    public synchronized boolean isEquipmentAliveType() {
        return (getAliveType().equals(AliveTimer.ALIVE_TYPE_EQUIPMENT));
    }

    /**
     * Check whether this alive timer is related to a subequipemnt alive tag.
     * 
     * @return true if this alive timer is related to an subequipent alive tag.
     */
    public synchronized boolean isSubEquipmentAliveType() {
        return (getAliveType().equals(AliveTimer.ALIVE_TYPE_SUBEQUIPMENT));
    }

    @Override
    public synchronized Collection<Long> getDependentAliveTimerIds() {
        return this.dependentAliveTimerIds;
    }

    /**
     * Check whether this alive timer is currently active. An alive timer is considered active it has been started and
     * has not expired yet.
     * 
     * @return true if this alive timer is currently active.
     */
    @Override
    public synchronized boolean isActive() {
        return this.active;
    }

    /**
     * @param aliveTagId the aliveTagId to set
     */
    public void setAliveTagId(Long aliveTagId) {
        this.aliveTagId = aliveTagId;
    }

    /**
     * @param aliveInterval the aliveInterval to set
     */
    public void setAliveInterval(Integer aliveInterval) {
        this.aliveInterval = aliveInterval;
    }

    /**
     * Sets the aliveType and accordingly the alive type description field.
     * 
     * @param aliveType the aliveType to set
     */
    public void setAliveType(String aliveType) {
        this.aliveType = aliveType.trim();
        if (getAliveType().equals(AliveTimer.ALIVE_TYPE_PROCESS)) {
            this.aliveTypeDescription = AliveTimer.PROCESS_MSG;
        } else {
            if (getAliveType().equals(AliveTimer.ALIVE_TYPE_EQUIPMENT)) {
                this.aliveTypeDescription = AliveTimer.EQUIPMENT_MSG;
            } else {
                this.aliveTypeDescription = AliveTimer.SUBEQUIPMENT_MSG;
            }
        }
    }

    /**
     * @param aliveTypeDescription the aliveTypeDescription to set
     */
    public void setAliveTypeDescription(String aliveTypeDescription) {
        this.aliveTypeDescription = aliveTypeDescription;
    }

    /**
     * @param relatedId the relatedId to set
     */
    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    /**
     * @param relatedName the relatedName to set
     */
    public void setRelatedName(String relatedName) {
        this.relatedName = relatedName;
    }

    /**
     * @param relatedStateTagId the relatedStateTagId to set
     */
    public void setRelatedStateTagId(Long relatedStateTagId) {
        this.relatedStateTagId = relatedStateTagId;
    }

    /**
     * @param lastUpdate the lastUpdate to set
     */
    @Override
    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    /**
     * @param active the active to set
     */
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the aliveInterval
     */
    @Override
    public synchronized Integer getAliveInterval() {
        return aliveInterval;
    }

    /**
     * @return the aliveTagId
     */
    public synchronized Long getAliveTagId() {
        return aliveTagId;
    }

    /**
     * @return the lastUpdate
     */
    @Override
    public synchronized long getLastUpdate() {
        return lastUpdate;
    }

    /**
     * @return the relatedAliveTimerIds
     */
    protected synchronized Collection<Long> getRelatedAliveTimerIds() {
        return dependentAliveTimerIds;
    }
}
