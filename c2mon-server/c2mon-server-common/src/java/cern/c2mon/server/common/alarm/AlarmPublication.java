package cern.c2mon.server.common.alarm;

import java.io.Serializable;
import java.sql.Timestamp;

import cern.c2mon.server.common.alarm.AlarmCondition;

/**
 * Details of an alarm publication for a given Alarm.
 * 
 * @author Mark Brightwell
 */
public class AlarmPublication implements Cloneable, Serializable {

    private static final long serialVersionUID = -7861832553423980278L;

    /**
     * Alarm state.
     */
    private String state;

    /**
     * LASER info
     */
    private String info;

    /**
     * Alarm publication time.
     */
    private Timestamp publicationTime;

    /**
     * Default constructor.
     */
    public AlarmPublication() {
        super();
    };

    /**
     * Constructor.
     * 
     * @param state
     * @param info
     * @param publicationTime
     */
    public AlarmPublication(String state, String info, Timestamp publicationTime) {
        super();
        this.state = state;
        this.info = info;
        this.publicationTime = publicationTime;
    }

    /**
     * Clone implementation.
     */
    public Object clone() throws CloneNotSupportedException {
        AlarmPublication alarmPublication = (AlarmPublication) super.clone();
        if (this.publicationTime != null) {
            alarmPublication.publicationTime = (Timestamp) this.publicationTime.clone();
        }
        return alarmPublication;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * @return the timestamp
     */
    public Timestamp getPublicationTime() {
        return publicationTime;
    }

    /**
     * @param publicationTime the timestamp to set
     */
    public void setPublicationTime(Timestamp publicationTime) {
        this.publicationTime = publicationTime;
    }

    /**
     * Returns true if the publication was an alarm activation.
     * 
     * @return true if was activate publication
     */
    public boolean isActivePublication() {
        return state != null && state.equals(AlarmCondition.ACTIVE);
    }

}
