/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification.test;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public class ProblemReminder {

    private static long DEFAULT_REMINDER_TIME = 1000 * 60 * 60 * 2; // 2 hours
    
    private long reminderTime = DEFAULT_REMINDER_TIME;
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public ProblemReminder() {
        
    }
    
    public void start() {
        
    }
    
    public void setDefaultReminderTime(TimeUnit unit, long time) {
        reminderTime = unit.toMillis(time);
    }
    
    /**
     * 
     * @return the default reminder time in [min]
     */
    public long getDefaultReminderTime() {
        return reminderTime;
    }
    
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
