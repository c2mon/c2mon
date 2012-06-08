/*
 * $Id $
 * 
 * $Date$ $Revision$ $Author$
 * 
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.shared;

/**
 * An object representing a status in Diamon-2
 * 
 * @author felixehm
 */
public enum Status {

    UNKNOWN(-1),
    OK(0), 
    WARNING(1), 
    ERROR(2), 
    FATAL(3);

    /**
     * private field to keep the status.
     */
    private int statusIntVal = -1;

    /**
     * @param i The status as int.
     */
    private Status(int i) {
        if (i >= -1 && i < 4) {
            statusIntVal = i;
        } else {
            throw new IllegalArgumentException("Int value " + i + " is not a valid Status!");
        }

    }

    /**
     * Gets a status from a a string representation (not case sensitive).
     * 
     * @param status The integer code of the status.
     * @return The status, null if the status cannot be parsed.
     */
    public static Status fromString(String status) {
        if (status == null) {
            return UNKNOWN;
        }

        String s = status.toLowerCase();

        if (s.equals("ok") || s.equals("inf")) {
            return OK;
        } else if (s.equals("warn") || s.equals("warning")) {
            return WARNING;
        } else if (s.equals("error") || s.equals("fatal")) {
            return ERROR;
        }

        return UNKNOWN;
    }

    /**
     * Returns the status corresponding to the given index.
     * 
     * @param index the status index.
     * @return the status, or {@link #UNKNOW} if the index is out of range.
     */
    public static Status fromInt(int index) {
        if (index == 0) 
            return Status.OK;
        if (index == 1)
            return Status.WARNING;
        if (index == 2)
            return Status.ERROR;
        if (index == 3)
            return Status.FATAL;
        if (index == -1)
            return Status.UNKNOWN;

        return UNKNOWN;
    }

    /**
     * @return the Integer value of this object.
     */
    public int toInteger() {
        return this.statusIntVal;
    }

    /**
     * Indicates if this status is worser than another status.
     * <ul>
     * <li>FATAL is worser than ERROR</li>
     * <li>ERROR is worser than WARNING</li>
     * <li>WARNING is worser than OK</li>
     * <li>OK is worser than UNKNOWN (may seem to be not logical, but like this OK overwrites UNKNOWN)</li>
     * </ul>
     * 
     * @param aStatus
     * @return True if this status is worser the other status.
     */
    public boolean worserThan(Status aStatus) {
        return this.toInteger() > aStatus.toInteger();
    }

    /**
     * Indicates if this better is better than another status.
     * <ul>
     * <li>OK is worser than UNKNOWN (may seem to be not logical, but like this OK overwrites UNKNOWN)</li>
     * <li>OK is better than WARNING</li>
     * <li>WARNING is better than ERROR</li>
     * <li>ERROR is better than FATAL</li>
     * </ul>
     * 
     * @param aStatus
     * @return True if this status is better the other status.
     */
    public boolean betterThan(Status aStatus) {
        return this.toInteger() < aStatus.toInteger();
    }
}
