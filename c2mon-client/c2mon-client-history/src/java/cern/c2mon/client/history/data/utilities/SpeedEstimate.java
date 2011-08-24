/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
package cern.c2mon.client.history.data.utilities;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used to estimate speed based experience which is manually
 * registered.
 * This class supports multithreading as all calls are thread safe.
 * 
 * @author vdeila
 */
public class SpeedEstimate {

  /**
   * The number of units that is measured
   */
  private long unitsMeasured;

  /**
   * The total time of the measured units
   */
  private long unitsTotalTime;

  /**
   * Lock for <code>unitsMeasured</code> and <code>unitsTotalTime</code>
   */
  private ReentrantReadWriteLock unitsLock = new ReentrantReadWriteLock();

  /**
   * How many percent a new measurement is worth compared to an old one.
   */
  private double newMeasurementsRatio = 1.5;

  /**
   * When the unit count reaches this number it reduces the unit count to
   * <code>reducedMeasurementNumber</code>
   */
  private long rollingMeasurementNumber = 9000000000000L;

  /**
   * When the unit count reaches <code>rollingMeasurementNumber</code> it
   * reduces the unit count to this number
   */
  private long reducedMeasurementNumber = 900000000000L;
  
  /**
   * Is <code>true</code> when the user only have guessed the speed. The first
   * measurement will then override these values
   */
  private boolean haveTemporaryValues;
  
  /**
   * 
   * @param guessedSpeed
   *          The predicted speed (units / time). This value will be used if
   *          getSpeed is called before any measurement data have been added
   */
  public SpeedEstimate(final double guessedSpeed) {
    this.haveTemporaryValues = true;
    final long units = 10000L;
    this.setUnits(units, (long) (units / guessedSpeed));
  }

  /**
   * 
   * @return Units per 1 time. If the total time is zero, it will return
   *         <code>Long.MAX_VALUE</code>
   */
  public double getSpeed() {
    try {
      this.unitsLock.readLock().lock();
      if (this.unitsTotalTime != 0)
        return this.unitsMeasured / (double)this.unitsTotalTime;
      else
        return Long.MAX_VALUE;
    }
    finally {
      this.unitsLock.readLock().unlock();
    }
  }

  /**
   * Adds a measurement
   * 
   * @param units
   *          How many units were measured
   * @param time
   *          How long time it takes for the given amount of units
   */
  public void addMeasurement(final long units, final long time) {
    try {
      this.unitsLock.writeLock().lock();
      
      // If temporary values have been used we first delete them
      if (this.haveTemporaryValues) {
        this.unitsMeasured = 0;
        this.unitsTotalTime = 0;
        this.haveTemporaryValues = false;
      }

      final long newUnitsMeasured = this.unitsMeasured + (long) (this.newMeasurementsRatio * units);
      final long newTime = this.unitsTotalTime + (long) (this.newMeasurementsRatio * time);

      // Calculates the new values
      long newNumberOfMeasurements = this.unitsMeasured + units;
      if (newNumberOfMeasurements > rollingMeasurementNumber) {
        newNumberOfMeasurements = reducedMeasurementNumber;
      }

      final double ratio = newUnitsMeasured / (double) newTime;
      this.unitsMeasured = newNumberOfMeasurements;
      this.unitsTotalTime = (long) (newNumberOfMeasurements / (double) ratio);
    }
    finally {
      this.unitsLock.writeLock().unlock();
    }
  }

  /*
   * Protected getters and setters
   */

  /**
   * @return the unitsMeasured
   */
  protected long getUnitsMeasured() {
    try {
      this.unitsLock.readLock().lock();
      return unitsMeasured;
    }
    finally {
      this.unitsLock.readLock().unlock();
    }
  }

  /**
   * @return the unitsTotalTime
   */
  protected long getUnitsTotalTime() {
    try {
      this.unitsLock.readLock().lock();
      return unitsTotalTime;
    }
    finally {
      this.unitsLock.readLock().unlock();
    }
  }

  /**
   * @param unitsMeasured
   *          the unitsMeasured to set
   */
  protected void setUnits(final long unitsMeasured, final long unitsTotalTime) {
    try {
      this.unitsLock.writeLock().lock();
      this.unitsMeasured = unitsMeasured;
      this.unitsTotalTime = unitsTotalTime;
    }
    finally {
      this.unitsLock.writeLock().unlock();
    }
  }

  /*
   * Public Getters and setters
   */
  
  /**
   * @return How many percent a new measurement is worth compared to an old one.
   *         Default is <code>1.5</code>
   */
  public double getNewMeasurementsRatio() {
    return newMeasurementsRatio;
  }

  /**
   * @param newMeasurementsRatio
   *          How many percent a new measurement is worth compared to an old
   *          one.
   */
  public void setNewMeasurementsRatio(double newMeasurementsRatio) {
    this.newMeasurementsRatio = newMeasurementsRatio;
  }

  /**
   * If the unit count goes above this number, the unit count will be reduced to
   * <code>getReducedMeasurementNumber()</code>
   * 
   * @return the rollingMeasurementNumber
   */
  public long getRollingMeasurementNumber() {
    return rollingMeasurementNumber;
  }

  /**
   * If the unit count goes above this number, the unit count will be reduced to
   * <code>getReducedMeasurementNumber()</code>
   * 
   * @param rollingMeasurementNumber
   *          the rollingMeasurementNumber to set
   */
  public void setRollingMeasurementNumber(long rollingMeasurementNumber) {
    this.rollingMeasurementNumber = rollingMeasurementNumber;
  }

  /**
   * If the unit count goes above <code>getRollingMeasurementNumber()</code>,
   * the unit count will be reduced to this number
   * 
   * @return the reducedMeasurementNumber
   */
  public long getReducedMeasurementNumber() {
    return reducedMeasurementNumber;
  }

  /**
   * If the unit count goes above <code>getRollingMeasurementNumber()</code>,
   * the unit count will be reduced to this number
   * 
   * @param reducedMeasurementNumber
   *          the reducedMeasurementNumber to set
   */
  public void setReducedMeasurementNumber(long reducedMeasurementNumber) {
    this.reducedMeasurementNumber = reducedMeasurementNumber;
  }

}
