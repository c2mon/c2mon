/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.driver.filter.dynamic;

/**
 * Class to calculate an average difference between provided values. It will
 * calculate the average based on a window size (The number of the last incoming
 * values that should be used to calculate the average.
 * 
 * @author alang
 * 
 */
public class DiffMovingAverage {
    /**
     * The default size of the moving window.
     */
    public static final int DEFAULT_SIZE = 10;

    /**
     * The last differences. The number depends on the window size.
     */
    private long[] diffs;

    /**
     * The last value added (to calculate the next difference.
     */
    private long lastValue = -1;

    /**
     * The current average of the differences.
     */
    private float currentAverage = -1;

    /**
     * The current position in the array where the next difference will be
     * written.
     */
    private int curPosition = 0;

    /**
     * The used fields in the array.
     */
    private int usedFields = 0;

    /**
     * Creates a new DiffMovingAverage object based on a default window size.
     * The window size is the number of the last incoming values which should be
     * used to calculate the average of the differences between them.
     */
    public DiffMovingAverage() {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a new DiffMovingAverage object based on the provided window size.
     * The window size is the number of the last incoming values which should be
     * used to calculate the average of the differences between them.
     * 
     * @param windowSize
     *            The window size to use.
     */
    public DiffMovingAverage(final int windowSize) {
        diffs = new long[windowSize];
    }

    /**
     * Adds a value and calculates the new average of differences between the
     * values within the window size provided at creation.
     */
    public void recordTimestamp() {
        long curTimestamp = System.currentTimeMillis();
        if (usedFields > 0) {
            long diff = curTimestamp - lastValue;
            if (usedFields == diffs.length) {
                long removedValue = diffs[curPosition];
                diffs[curPosition] = diff;
                calculateNewAverage(removedValue, diff, false);
                shiftEnd();
            } else {
                calculateNewAverage(0, diff, true);
                diffs[curPosition] = diff;
                shiftEnd();
            }
        } else if (lastValue != -1) {
            long diff = curTimestamp - lastValue;
            currentAverage = 0;
            diffs[curPosition] = diff;
            calculateNewAverage(0, diff, true);
            shiftEnd();
        }
        lastValue = curTimestamp;
//        return Math.round(currentAverage);
    }

    /**
     * Returns the average of the currently contained timestamps or -1
     * if there is no valid value.
     * 
     * @return The average of the currently contained timestamps or -1
     * if there is no valid value.
     */
    public long getCurrentAverage() {
        return Math.round(currentAverage);
    }

    /**
     * Clears all content out of the list.
     */
    public void clear() {
        currentAverage = -1;
        usedFields = 0;
    }

    /**
     * Increases the pointer to the end of the array by one (if it is at the end
     * it will return to the start of the array).
     */
    private void shiftEnd() {
        curPosition = (curPosition + 1) % diffs.length;
    }

    /**
     * Calculates and sets the new average of time differences.
     * 
     * @param removedValue
     *            The value (difference) which is removed from the array.
     * @param timeDifference
     *            The new difference to be added to the average value.
     * @param increaseUsedFields
     *            True if the number of field should be increased alse false.
     */
    private void calculateNewAverage(final long removedValue, 
            final long timeDifference, final boolean increaseUsedFields) {
        float oldSum = currentAverage * usedFields;
        if (increaseUsedFields)
            usedFields++;
        currentAverage = (oldSum - removedValue + timeDifference) / usedFields;
    }

    /**
     * Returns a String representation of this class.
     * @return A String representation of this class.
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(DiffMovingAverage.class.getSimpleName() + "; Current average: " 
                + currentAverage + "; Last Diffs: ");
        for (long i : diffs) {
            buffer.append("[" + i + "]");
        }
        return buffer.toString();
    }
}
