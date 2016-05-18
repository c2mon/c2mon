/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package cern.c2mon.daq.testhandler;

import arbitrary.type.ComplexObject;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.common.type.TypeConverter;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;

/**
 * Created by fritter on 03/03/16.
 */
public class ValueGenerator {

  public static final String PACKAGE_PREFIX = "java.lang.";

  /**
   * Constant used in generating values outside the value deadband (step is
   * max-min/OUT_DEADBAND_STEP_CONST).
   */
  private static final int OUT_DEADBAND_STEP_CONST = 100;

  /**
   * Constant used in generating values inside the value deadband (step is
   * value_deadband/IN_DEADBAND_STEP_CONST).
   */
  private static final int IN_DEADBAND_STEP_CONST = 10;

  private EquipmentLogger equipmentLogger;

  private IEquipmentConfiguration equipmentConfiguration;

  private Map<String, String> configurationParams;

  /**
   * The random number generator used throughout the testHandler.
   */
  private Random rand = new Random(System.currentTimeMillis());

  public ValueGenerator(EquipmentLogger logger, IEquipmentConfiguration equipmentConfiguration, Map<String, String> equipmentConfigParam){
    this.equipmentLogger = logger;
    this.equipmentConfiguration = equipmentConfiguration;
    this.configurationParams =equipmentConfigParam;
  }

  private EquipmentLogger getEquipmentLogger(){
    return this.equipmentLogger;
  }

  private IEquipmentConfiguration getEquipmentConfiguration(){
    return this.equipmentConfiguration;
  }

  public Object generateValue(SourceDataTag sdt){
    Object newValue = null;

    if(TypeConverter.isKnownClass(sdt.getDataType())){
      Class<?> clazz =  TypeConverter.getType(sdt.getDataType());

      if(clazz.isArray()){

        newValue = generateArray(sdt, clazz.getComponentType());

      } else {

        newValue = createNormalValue(sdt);
      }
    }
    return newValue;
  }


  /**
   * Generates a new boolean value. Initially the value is set to true. It then
   * uses the switchProb to determine if the tag should switch value.
   *
   * @param sdt the SourceDataTag a new value is being generated for
   * @return returns the generated value
   */
  private Boolean generateNewBoolean(final SourceDataTag sdt) {
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("generating a new boolean value...");
    }

    // if not yet set, set as true
    if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("chosen intial value is true");
      }
      return new Boolean(true);
    } else {
      Boolean returnValue;
      // with switchProb probability, return the opposite of the current
      // value
      if (rand.nextFloat() < Float.parseFloat(((String) configurationParams.get("switchProb")))) {
        returnValue = Boolean.valueOf(!((Boolean) sdt.getCurrentValue().getValue()).booleanValue());
      }
      // else leave as is
      else {
        returnValue = (Boolean) sdt.getCurrentValue().getValue();
      }
      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("...chosen value is " + returnValue);
      }
      return returnValue;
    }

  }

  /**
   * Generates a new value for SourceDataTags with double values. The value is
   * generated using the probabilities for a value landing in the min-max
   * bracket and within the deadband value range.
   *
   * @param sdt SourceDataTag to generate a new value for
   * @return a new Double datatag value
   */

  private Double generateNewDouble(final SourceDataTag sdt) {
    double max = 10000d;
    double min = 0.0d;
    if (sdt.getMaxValue() != null && sdt.getMinValue() != null) {
      max = ((Double) sdt.getMaxValue()).doubleValue();
      min = ((Double) sdt.getMinValue()).doubleValue();
    } else {
      sdt.setMaxValue(max); // set defaults
      sdt.setMinValue(min);
    }

    // step is an arbitrary small value, by which the tag value can be
    // increased or decreased (should be small, so that moving from max-step
    // to min+step is not in the value deadband)
    double step = (max - min) / OUT_DEADBAND_STEP_CONST;

    // with probability inRangeProb, choose a value in the min-max range
    if (rand.nextDouble() < Double.parseDouble(((String) configurationParams.get("inRangeProb")))) {
      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("choosing a value in the min-max interval");
      }
      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }
        // choose value in range
        return Double.valueOf((min + step));
      } else { // value is not null
        double currentValue = ((Double) sdt.getCurrentValue().getValue()).doubleValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }
        short deadbandType = sdt.getAddress().getValueDeadbandType();

        // if the data tag has a value deadband
        if (sdt.getAddress().isValueDeadbandEnabled()
            && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\ttag has a value deadband");
          }
          float deadbandValueFloat = sdt.getAddress().getValueDeadband();
          double deadbandValue = (Float.valueOf(deadbandValueFloat)).doubleValue();
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\tdeadband value is " + deadbandValue);
          }
          double deadbandStep = 0;

          // with probability outDeadBandProb, choose a value that
          // will be propagated to Appl.Server
          if (rand.nextDouble() < Double.parseDouble(((String) configurationParams.get("outDeadBandProb")))) {
            if (getEquipmentLogger().isDebugEnabled()) {
              getEquipmentLogger().debug("\t\tchoosing a value outside the deadband");
            }

            // we are assuming one of min+step or max-step are
            // outside the deadband for any current value,
            // so we choose one or the other
            if (isInValueDeadband(sdt.getId(), Double.valueOf(min + step))) {
              return Double.valueOf(max - step);
            } else {
              return Double.valueOf(min + step);
            }
          }
          // else must return a value within the value deadband
          else {

            if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the absolute deadband");
              }

              // step by which we will move within the deadband
              deadbandStep = deadbandValue / IN_DEADBAND_STEP_CONST;

              // step from the current value to another one in the
              // min-max interval, by the step amount
              if (inRange(sdt, Double.valueOf(currentValue + deadbandStep))) {
                double returnValue = currentValue + deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }
                return Double.valueOf(returnValue);
              } else {
                double returnValue = currentValue - deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }
                return Double.valueOf(returnValue);
              }
            } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
              }

              // notice that move from percentage to decimal below
              // (relative deadband value is percentage in XML config)
              deadbandValue = deadbandValue / 100; // 100 to move from
              // percentage to decimal
              // choose to move by half the percentage deadband value
              double percentageChange = deadbandValue - deadbandValue / 2;
              deadbandStep = currentValue * percentageChange;
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("percentage change = " + percentageChange + " deadbandStep = " + deadbandStep);
              }

              // move in direction that keeps value in range
              // (assuming that this should hold in one direction
              // or the other; if not, may end up with
              // out-of-bounds value)
              if (inRange(sdt, Double.valueOf(currentValue + deadbandStep))) {
                return Double.valueOf(currentValue + deadbandStep);
              } else {
                return Double.valueOf(currentValue - deadbandStep);
              }
            }

            // will end up here if other deadband types are specified;
            // ignore and return values as if no deadband enabled
            else {
              getEquipmentLogger().error("deadband type not recognised; ignoring deadband info");
              return Double.valueOf(currentValue);
            }
          }
        }
        // no value deadband for this tag
        // jump by step, staying in range
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\tno value deadband for this tag; choosing a value");
        }

        if (inRange(sdt, Double.valueOf(currentValue + step))) {
          return Double.valueOf(currentValue + step);
        } else {
          return Double.valueOf(currentValue - step);
        }

      } // not null

    } // inRangeProb

    // choose a value out of the min-max range (this value is not sent to
    // Application Server, so can always be the same for test purposes -
    // it is not filtered out if the same, and is saved as tag value)
    else {
      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }
      } else {
        double currentValue = ((Double) sdt.getCurrentValue().getValue()).doubleValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }

        // Swap between 2 values: max+1 and max+2
        if (currentValue == Double.valueOf(max + 1)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("returning a value outside the min-max range " + Double.valueOf(max + 2));
          }
          return Double.valueOf(max + 2);
        }
      }

      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("returning a value outside the min-max range " + Double.valueOf(max + 1));
      }
      return Double.valueOf(max + 1);
    }
  }

  /**
   * Same method as generateNewDouble, but for Float values.
   *
   * @param sdt SourceDataTag to generate a new value for
   * @return a new Float datatag value
   */
  private Float generateNewFloat(final SourceDataTag sdt) {
    float max = 100f; // default values if not set in DB
    float min = 0.0f;
    if (sdt.getMaxValue() != null && sdt.getMinValue() != null) {
      max = ((Float) sdt.getMaxValue()).floatValue();
      min = ((Float) sdt.getMinValue()).floatValue();
    } else {
      sdt.setMaxValue(max); // set defaults
      sdt.setMinValue(min);
    }

    // step is an arbitrary small value, by which the tag value can be
    // increased or decreased (should be small, so that moving from max-step
    // to min+step is not in the value deadband)
    float step = (max - min) / OUT_DEADBAND_STEP_CONST;

    // with probability inRangeProb, choose a value in the min-max range
    if (rand.nextFloat() < Float.parseFloat(((String) configurationParams.get("inRangeProb")))) {
      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("choosing a value in the min-max interval");
      }
      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }
        // choose value in range
        return Float.valueOf((min + step));
      } else { // value is not null
        float currentValue = ((Float) sdt.getCurrentValue().getValue()).floatValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }
        short deadbandType = sdt.getAddress().getValueDeadbandType();

        // if the data tag has a value deadband
        if (sdt.getAddress().isValueDeadbandEnabled()
            && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\ttag has a value deadband");
          }

          float deadbandValue = sdt.getAddress().getValueDeadband();
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\tdeadband value is " + deadbandValue);
          }
          float deadbandStep = 0;

          // with probability outDeadBandProb, choose a value that
          // will be propagated to Appl.Server
          if (rand.nextDouble() < Float.parseFloat(((String) configurationParams.get("outDeadBandProb")))) {
            if (getEquipmentLogger().isDebugEnabled()) {
              getEquipmentLogger().debug("\t\tchoosing a value outside the deadband");
            }

            // we are assuming one of min+step or max-step are
            // outside the deadband for any current value,
            // so we choose one or the other
            if (isInValueDeadband(sdt.getId(), Float.valueOf(min + step))) {
              return Float.valueOf(max - step);
            } else {
              return Float.valueOf(min + step);
            }
          }
          // else must return a value within the value deadband
          else {

            if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the absolute deadband");
              }

              // step by which we will move within the deadband
              deadbandStep = deadbandValue / IN_DEADBAND_STEP_CONST;

              // step from the current value to another one in the
              // min-max interval, by the step amount
              if (inRange(sdt, Float.valueOf(currentValue + deadbandStep))) {
                float returnValue = currentValue + deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }
                return Float.valueOf(returnValue);
              } else {
                float returnValue = currentValue - deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }
                return Float.valueOf(returnValue);
              }
            } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
              }

              // notice that move from percentage to decimal below
              // (relative deadband value is percentage in XML config)
              deadbandValue = deadbandValue / 100; // 100 to move from
              // percentage to decimal
              // choose to move by half the percentage deadband value
              float percentageChange = deadbandValue - deadbandValue / 2;
              deadbandStep = currentValue * percentageChange;
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("percentage change = " + percentageChange + " deadbandStep = " + deadbandStep);
              }

              // move in direction that keeps value in range
              // (assuming that this should hold in one direction
              // or the other; if not, may end up with
              // out-of-bounds value)
              if (inRange(sdt, Float.valueOf(currentValue + deadbandStep))) {
                return Float.valueOf(currentValue + deadbandStep);
              } else {
                return Float.valueOf(currentValue - deadbandStep);
              }
            }

            // will end up here if other deadband types are specified;
            // ignore and return values as if no deadband enabled
            else {
              getEquipmentLogger().error("deadband type not recognised; ignoring deadband info");
              return Float.valueOf(currentValue);
            }
          }
        }
        // no value deadband for this tag
        // jump by step, staying in range
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\tno value deadband for this tag; choosing a value");
        }

        if (inRange(sdt, Float.valueOf(currentValue + step))) {
          return Float.valueOf(currentValue + step);
        } else {
          return Float.valueOf(currentValue - step);
        }

      } // not null

    } // inRangeProb

    // choose a value out of the min-max range (this value is not sent to
    // Application Server, so can always be the same for test purposes -
    // it is not filtered out if the same, and is saved as tag value)
    else {
      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }
      } else {
        float currentValue = ((Float) sdt.getCurrentValue().getValue()).floatValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }

        // Swap between 2 values: max+1 and max+2
        if (currentValue == Float.valueOf(max + 1)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("returning a value outside the min-max range " + Float.valueOf(max + 2));
          }
          return Float.valueOf(max + 2);
        }
      }

      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("returning a value outside the min-max range " + Float.valueOf(max + 1));
      }
      return Float.valueOf(max + 1);
    }
  }

  /**
   * Same method as generateNewDouble, but for Integer values.
   *
   * @param sdt SourceDataTag to generate a new value for
   * @return a new Integer datatag value
   */
  private Integer generateNewInteger(final SourceDataTag sdt) {
    int max = 100; // default values if not set in DB
    int min = -100;
    if (sdt.getMaxValue() != null && sdt.getMinValue() != null) {
      max = ((Integer) sdt.getMaxValue()).intValue();
      min = ((Integer) sdt.getMinValue()).intValue();
    } else {
      sdt.setMaxValue(max); // set defaults
      sdt.setMinValue(min);
    }

    // step is an arbitrary small value, by which the tag value can be
    // increased or decreased
    // (should be small, so that moving from max-step to min+step is not in
    // the
    // value deadband)
    int step = (Double.valueOf(Math.ceil((double) (max - min) / OUT_DEADBAND_STEP_CONST))).intValue();

    // with probability inRangeProb, choose a value in the min-max range
    if (rand.nextDouble() < Float.parseFloat(((String) configurationParams.get("inRangeProb")))) {
      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("choosing a value in the min-max interval");
      }

      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }

        // choose value in range
        return Integer.valueOf(min + step);
      } else { // value is not null
        int currentValue = ((Integer) sdt.getCurrentValue().getValue()).intValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }

        short deadbandType = sdt.getAddress().getValueDeadbandType();

        // if the data tag has a value deadband
        if (sdt.getAddress().isValueDeadbandEnabled()
            && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\ttag has a value deadband");
          }

          float deadbandValue = sdt.getAddress().getValueDeadband();
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\tdeadband value is " + deadbandValue);
          }

          int deadbandStep = 0;

          // with probability outDeadBandProb, choose a value that
          // will be propagated to Appl.Server
          if (rand.nextDouble() < Float.parseFloat(((String) configurationParams.get("outDeadBandProb")))) {
            if (getEquipmentLogger().isDebugEnabled()) {
              getEquipmentLogger().debug("\t\tchoosing a value outside the deadband");
            }

            // we are assuming one of min+step or max-step are
            // outside the deadband for any current value,
            // so we choose one or the other
            if (isInValueDeadband(sdt.getId(), Integer.valueOf(min + step))) {
              return Integer.valueOf(max - step);
            } else {
              return Integer.valueOf(min + step);
            }
          }
          // else must return a value within the value deadband
          else {

            if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the absolute deadband");
              }

              // step by which we will move within the deadband
              // if the integer deadband is 1, we want to choose 0
              // so the value is not sent to AS
              if (deadbandValue == 1) {
                deadbandStep = 0;
              } else {
                deadbandStep = (Double.valueOf(Math.ceil(deadbandValue / IN_DEADBAND_STEP_CONST))).intValue();
              }

              // step from the current value to another one in the
              // min-max interval, by the step amount
              if (inRange(sdt, Integer.valueOf(currentValue + deadbandStep))) {
                int returnValue = currentValue + deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }

                return Integer.valueOf(returnValue);
              } else {
                int returnValue = currentValue - deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }

                return Integer.valueOf(returnValue);
              }
            } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
              }

              // notice that move from percentage to decimal below
              // (relative deadband value is percentage in XML config)
              deadbandValue = deadbandValue / 100; // 100 to move from
              // percentage to decimal
              // choose to move by half the percentage deadband
              // value
              float percentageChange = deadbandValue - deadbandValue / 2;
              deadbandStep = (Double.valueOf(Math.ceil(currentValue * percentageChange))).intValue();
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("percentage change = " + percentageChange + " deadbandStep = " + deadbandStep);
              }

              // move in direction that keeps value in range
              // (assuming that this should hold in one direction
              // or the other; if not, may end up with
              // out-of-bounds value)
              if (inRange(sdt, Integer.valueOf(currentValue + deadbandStep))) {
                return Integer.valueOf(currentValue + deadbandStep);
              } else {
                return Integer.valueOf(currentValue - deadbandStep);
              }
            }

            // will end up here if other deadband types are
            // specified; ignore and return values as if no deadband enabled
            else {

              getEquipmentLogger().error("deadband type not recognised; ignoring deadband info");
              return Integer.valueOf(currentValue);
            }
          }
        }
        // no value deadband for this tag
        // jump by step, staying in range
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\tno value deadband for this tag; choosing a value...");
        }
        Integer returnValue;
        if (inRange(sdt, Integer.valueOf(currentValue + step))) {
          returnValue = Integer.valueOf(currentValue + step);
        } else {
          returnValue = Integer.valueOf(currentValue - step);
        }

        return returnValue;

      } // not null

    } // inRangeProb

    // choose a value out of the min-max range (this value is not sent to
    // Application Server,
    // so can always be the same for test purposes - it is not filtered out if
    // the same, and is
    // saved as tag value)
    else {
      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }
      } else {
        int currentValue = ((Integer) sdt.getCurrentValue().getValue()).intValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }

        // Swap between 2 values: max+1 and max+2
        if (currentValue == Integer.valueOf(max + 1)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("returning a value outside the min-max range " + Integer.valueOf(max + 2));
          }
          return Integer.valueOf(max + 2);
        }
      }

      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("returning a value outside the min-max range " + Integer.valueOf(max + 1));
      }
      return Integer.valueOf(max + 1);
    }
  }

  /**
   * Same method as generateNewInteger, but for Long values.
   *
   * @param sdt SourceDataTag to generate a new value for
   * @return a new Long datatag value
   */
  private Long generateNewLong(final SourceDataTag sdt) {
    long max = 1000000l;
    long min = 0;
    if (sdt.getMaxValue() != null && sdt.getMinValue() != null) {
      max = ((Long) sdt.getMaxValue()).longValue();
      min = ((Long) sdt.getMinValue()).longValue();
    } else {
      sdt.setMaxValue(max); // set defaults
      sdt.setMinValue(min);
    }

    // step is an arbitrary small value, by which the tag value can be
    // increased or decreased
    // (should be small, so that moving from max-step to min+step is not in
    // the value deadband)
    long step = (Double.valueOf(Math.ceil((double) (max - min) / OUT_DEADBAND_STEP_CONST))).longValue();

    // with probability inRangeProb, choose a value in the min-max range
    if (rand.nextDouble() < Float.parseFloat(((String) configurationParams.get("inRangeProb")))) {
      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("choosing a value in the min-max interval");
      }

      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }

        // choose value in range
        return Long.valueOf(min + step);
      } else { // value is not null
        long currentValue = ((Long) sdt.getCurrentValue().getValue()).longValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }

        short deadbandType = sdt.getAddress().getValueDeadbandType();

        // if the data tag has a value deadband
        if (sdt.getAddress().isValueDeadbandEnabled()
            && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\ttag has a value deadband");
          }

          float deadbandValue = sdt.getAddress().getValueDeadband();
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\tdeadband value is " + deadbandValue);
          }

          long deadbandStep = 0;

          // with probability outDeadBandProb, choose a value that
          // will be propagated to Appl.Server
          if (rand.nextDouble() < Float.parseFloat(((String) configurationParams.get("outDeadBandProb")))) {
            if (getEquipmentLogger().isDebugEnabled()) {
              getEquipmentLogger().debug("\t\tchoosing a value outside the deadband");
            }

            // we are assuming one of min+step or max-step are
            // outside the deadband for any current value,
            // so we choose one or the other
            if (isInValueDeadband(sdt.getId(), Long.valueOf(min + step))) {
              return Long.valueOf(max - step);
            } else {
              return Long.valueOf(min + step);
            }
          }
          // else must return a value within the value deadband
          else {

            if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the absolute deadband");
              }

              // step by which we will move within the deadband
              // if the long deadband is 1, we want to choose 0 so
              // the value is not sent to AS
              if (deadbandValue == 1) {
                deadbandStep = 0;
              } else {
                deadbandStep = (Double.valueOf(Math.ceil(deadbandValue / IN_DEADBAND_STEP_CONST))).longValue();
              }

              // step from the current value to another one in the
              // min-max interval, by the step amount
              if (inRange(sdt, Long.valueOf(currentValue + deadbandStep))) {
                long returnValue = currentValue + deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }

                return Long.valueOf(returnValue);
              } else {
                long returnValue = currentValue - deadbandStep;
                if (getEquipmentLogger().isDebugEnabled()) {
                  getEquipmentLogger().debug("chosen value is " + returnValue);
                }

                return Long.valueOf(returnValue);
              }
            } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
              }

              // notice that move from percentage to decimal below
              // (relative deadband value is percentage in XML
              // config)
              deadbandValue = deadbandValue / 100; // 100 to move from
              // percentage to decimal

              // choose to move by half the percentage deadband
              // value
              float percentageChange = deadbandValue - deadbandValue / 2;
              deadbandStep = (Double.valueOf(Math.ceil(currentValue * percentageChange))).longValue();
              if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("percentage change = " + percentageChange + " deadbandStep = " + deadbandStep);
              }

              // move in direction that keeps value in range
              // (assuming that this should hold in one direction
              // or the other; if not, may end up with
              // out-of-bounds value)
              if (inRange(sdt, Long.valueOf(currentValue + deadbandStep))) {
                return Long.valueOf(currentValue + deadbandStep);
              } else {
                return Long.valueOf(currentValue - deadbandStep);
              }
            }
            // will end up here if other deadband types are
            // specified;
            // ignore and return values as if no deadband enabled
            else {
              getEquipmentLogger().error("deadband type not recognised; ignoring deadband info");
              return Long.valueOf(currentValue);
            }
          }
        }
        // no value deadband for this tag
        // jump by step, staying in range
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\tno value deadband for this tag; choosing a value");
        }

        if (inRange(sdt, Long.valueOf(currentValue + step))) {
          return Long.valueOf(currentValue + step);
        } else {
          return Long.valueOf(currentValue - step);
        }

      } // not null

    } // inRangeProb

    // choose a value out of the min-max range (this value is not sent to
    // Application Server,
    // so can always be the same for test purposes - it is not filtered out if
    // the same, and is
    // saved as tag value)
    else {
      // if the datatag has not being set yet
      if (sdt.getCurrentValue() == null || sdt.getCurrentValue().getValue() == null) {
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("\ttag value is currently null; setting initial value");
        }
      } else {
        long currentValue = ((Long) sdt.getCurrentValue().getValue()).longValue();
        if (getEquipmentLogger().isDebugEnabled()) {
          getEquipmentLogger().debug("current datatag value is " + currentValue);
        }

        // Swap between 2 values: max+1 and max+2
        if (currentValue == Long.valueOf(max + 1)) {
          if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("returning a value outside the min-max range " + Long.valueOf(max + 2));
          }
          return Long.valueOf(max + 2);
        }
      }

      if (getEquipmentLogger().isDebugEnabled()) {
        getEquipmentLogger().debug("returning a value outside the min-max range " + Long.valueOf(max + 1));
      }
      return Long.valueOf(max + 1);
    }
  }

  /**
   * Checks whether the value is within the value deadband or not. Same as the
   * (private) method in EquipmentMessageHandler.
   *
   * @param tagID - the unique identifier of the tag
   * @param tagValue - new value of the SourceDataTag, received from a data
   *          source.
   * @return - boolean indicating if the value is in the value deadband or not
   */
  private boolean isInValueDeadband(final Long tagID, final Object tagValue) {
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("entering isInValueDeadband()");
    }

    boolean result = false;
    float valueDeadband;
    double doubleDiff, doubleRatio;
    float floatDiff, floatRatio;
    int integerDiff;
    long longDiff;

    SourceDataTag sdt = (SourceDataTag) getEquipmentConfiguration().getSourceDataTags().get(tagID);

    // check if the tag is valid

    // compute the deadbands if this is not the first value that comes
    if (sdt.getCurrentValue() != null && sdt.getCurrentValue().getValue() != null && sdt.getCurrentValue().isValid()) {

      // check the value-based deadbands
      if (sdt.getAddress().isProcessValueDeadbandEnabled()) {

        valueDeadband = sdt.getAddress().getValueDeadband();

        // first of all, check if the tag is of some numeric type
        switch (TypeConverter.getType(sdt.getDataType()).getSimpleName()) {
          case "Double":
            switch (sdt.getAddress().getValueDeadbandType()) {
              case (DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE):
                // compute: abs(v1-v2) < valueDeadband
                doubleDiff = ((Double) sdt.getCurrentValue().getValue()).doubleValue() - ((Double) tagValue).doubleValue();

                // if the value is smaller, it meens that the change is
                // too little
                // and this value should be filtered out
                if (Math.abs(doubleDiff) < valueDeadband) {
                  result = true;
                }
                break;

              // same as above
              case DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE:
                // compute: abs(v1-v2) < valueDeadband
                doubleDiff = ((Double) sdt.getCurrentValue().getValue()).doubleValue() - ((Double) tagValue).doubleValue();

                // if the value is smaller, it meens that the change is
                // too little
                // and this value should be filtered out
                if (Math.abs(doubleDiff) < valueDeadband) {
                  result = true;
                }
                break;

              case DataTagDeadband.DEADBAND_PROCESS_RELATIVE:
                // compute: abs(v1-v2) < abs(v1*valueDeadband)
                doubleRatio = ((Double) sdt.getCurrentValue().getValue()).doubleValue() * (valueDeadband / 100); // 100
                // to
                // convert
                // percentages
                doubleDiff = ((Double) tagValue).doubleValue() - ((Double) sdt.getCurrentValue().getValue()).doubleValue();

                // value != 0 --> condition
                if ((((Double) sdt.getCurrentValue().getValue()).doubleValue() != 0.0f) && (Math.abs(doubleDiff) < Math.abs(doubleRatio))) {
                  result = true;
                }
                break;

              // same as above case
              case DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE:
                // compute: abs(v1-v2) < abs(v1*valueDeadband)
                doubleRatio = ((Double) sdt.getCurrentValue().getValue()).doubleValue() * (valueDeadband / 100);
                doubleDiff = ((Double) tagValue).doubleValue() - ((Double) sdt.getCurrentValue().getValue()).doubleValue();

                // value != 0 --> condition
                if ((((Double) sdt.getCurrentValue().getValue()).doubleValue() != 0.0f) && (Math.abs(doubleDiff) < Math.abs(doubleRatio))) {
                  result = true;
                }
                break;

              default: // do nothing, deadband type not recognised, returning false

            } // switch
            break;

          case "Float":
            switch (sdt.getAddress().getValueDeadbandType()) {
              case DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE:
                // compute: abs(v1-v2) < valueDeadband
                floatDiff = ((Float) sdt.getCurrentValue().getValue()).floatValue() - ((Float) tagValue).floatValue();

                // if the value is smaller, it meens that the change is too little
                // and this value should be filtered out
                if (Math.abs(floatDiff) < valueDeadband) {
                  if (getEquipmentLogger().isDebugEnabled()) {
                    getEquipmentLogger().debug("checking absolute value-based deadband filtering");
                  }

                  result = true;
                }
                break;

              case DataTagDeadband.DEADBAND_PROCESS_RELATIVE:
                // compute: abs(v1-v2) < abs(v1*valueDeadband)
                floatRatio = ((Float) sdt.getCurrentValue().getValue()).floatValue() * (valueDeadband / 100);
                floatDiff = ((Float) tagValue).floatValue() - ((Float) sdt.getCurrentValue().getValue()).floatValue();

                // value != 0 --> condition
                if ((((Float) sdt.getCurrentValue().getValue()).floatValue() != 0.0f) && (Math.abs(floatDiff) < Math.abs(floatRatio))) {
                  if (getEquipmentLogger().isDebugEnabled()) {
                    getEquipmentLogger().debug("checking relative value-based deadband filtering");
                  }
                  result = true;
                }
                break;

              default: // do nothing, deadband type not recognised, returning false

            } // switch
            break;

          case "Integer":
            switch (sdt.getAddress().getValueDeadbandType()) {
              case DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE:
                // compute: abs(v1-v2) < valueDeadband
                integerDiff = ((Integer) sdt.getCurrentValue().getValue()).intValue() - ((Integer) tagValue).intValue();

                // if the value is smaller, it meens that the change is
                // too little
                // and this value should be filtered out
                if (Math.abs(integerDiff) < valueDeadband) {
                  result = true;
                }
                break;

              case DataTagDeadband.DEADBAND_PROCESS_RELATIVE:
                // compute: abs(v1-v2) < abs(v1*valueDeadband)
                doubleRatio = ((Integer) sdt.getCurrentValue().getValue()).intValue() * (valueDeadband / 100);
                integerDiff = ((Integer) tagValue).intValue() - ((Integer) sdt.getCurrentValue().getValue()).intValue();

                // value != 0 --> condition
                if ((((Integer) sdt.getCurrentValue().getValue()).intValue() != 0) && (Math.abs(integerDiff) < Math.abs(doubleRatio))) {
                  result = true;
                }
                break;

              default: // do nothing, deadband type not recognised, returning false

            } // switch
            break;

          case "Long":
            switch (sdt.getAddress().getValueDeadbandType()) {
              case DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE:
                // compute: abs(v1-v2) < valueDeadband
                longDiff = ((Long) sdt.getCurrentValue().getValue()).longValue() - ((Long) tagValue).longValue();

                // if the value is smaller, it meens that the change is
                // too little
                // and this value should be filtered out
                if (Math.abs(longDiff) < valueDeadband) {
                  result = true;
                }
                break;

              case DataTagDeadband.DEADBAND_PROCESS_RELATIVE:
                // compute: abs(v1-v2) < abs(v1*valueDeadband)
                doubleRatio = ((Long) sdt.getCurrentValue().getValue()).longValue() * (valueDeadband / 100);
                longDiff = ((Long) tagValue).longValue() - ((Long) sdt.getCurrentValue().getValue()).longValue();

                // value != 0 --> condition
                if ((((Long) sdt.getCurrentValue().getValue()).longValue() != 0) && (Math.abs(longDiff) < Math.abs(doubleRatio))) {
                  result = true;
                }
                break;

              default: // do nothing, deadband type not recognised, returning false

            } // switch
            break;

          default: // do nothing, datatype not recognized, returning false

        } // switch
      } // if deadband-value enabled
    } // if

    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("leaving isInValueDeadband(); result is " + result);
    }

    return result;
  }

  private Object[] generateArray(SourceDataTag sdt, final Class<?> clazz){

      Integer length = Integer.parseInt(sdt.getAddressParameters().get("arrayLength"));

      Object[] result = (Object[]) Array.newInstance(clazz, length);

      for (int i = 0; i < length; i++) {
        if(clazz.isArray()){

          result[i] = generateArray(sdt, clazz.getComponentType());

        } else {

          result[i] = createArrayValue(clazz.getName());
        }
      }

      return result;
  }

  private Object createArrayValue(String dataType){
    Object result = null;
    switch (dataType) {
      case TestMessageHandler.PACKAGE_PREFIX+"Boolean":
        result = rand.nextBoolean();
        break;
      case TestMessageHandler.PACKAGE_PREFIX+"Double":
        result = rand.nextDouble();
        break;
      case TestMessageHandler.PACKAGE_PREFIX+"Float":
        result = rand.nextFloat();
        break;
      case TestMessageHandler.PACKAGE_PREFIX+"Integer":
        result = rand.nextInt(100);
        break;
      case TestMessageHandler.PACKAGE_PREFIX+"Long":
        result = (long) rand.nextInt(100);
        break;
      case TestMessageHandler.PACKAGE_PREFIX+"String":
        result = ("some random string..." + rand.nextInt(100));
        break;

      case TestMessageHandler.PACKAGE_PREFIX+"Short":
        result = (short) rand.nextInt(Short.MAX_VALUE+1);
        break;

      case TestMessageHandler.PACKAGE_PREFIX+"Byte":
        result = (byte) rand.nextInt(Byte.MAX_VALUE+1);
        break;
      default:
        ; // TODO Add also arbitrary Objects!!!!
    }
    return result;
  }

  private Object createNormalValue(SourceDataTag sdt){
    Object value = null;

    switch (sdt.getDataType()) {
      case PACKAGE_PREFIX + "Boolean":
        value = generateNewBoolean(sdt);
        break;

      case PACKAGE_PREFIX + "Double":
        value = generateNewDouble(sdt);
        break;

      case PACKAGE_PREFIX + "Float":
        value = generateNewFloat(sdt);
        break;

      case PACKAGE_PREFIX + "Integer":
        value = generateNewInteger(sdt);
        break;

      case PACKAGE_PREFIX + "Long":
        value = generateNewLong(sdt);
        break;

      case PACKAGE_PREFIX + "String":
        value = "some random string..." + rand.nextInt();
        break;

      case PACKAGE_PREFIX + "Short":
        value = (short) rand.nextInt(Short.MAX_VALUE + 1);
        break;

      case PACKAGE_PREFIX + "Byte":
        value = (byte) rand.nextInt(Byte.MAX_VALUE + 1);
        break;

      case "arbitrary.type.ComplexObject":
        value = ComplexObject.createRandomInstance();
        break;
      case "cern.c2mon.client.apitest.ClientObject":
        value = "{\"id\":" + rand.nextInt(100) + ",\"description\":\"randomText" + rand.nextInt(50) + "\"}";
        break;
      default:
        // do nothing: type not recognized
        break;

    }
    return value;
  }


  /**
   * Determines whether a given value is within the SourceDataTag min-max
   * bracket.
   *
   * @return boolean indicating whether the value is in range or not
   * @param sdt the SourceDataTag object
   * @param value the value we wish to check is in the min-max range of the
   *          datatag
   */
  private boolean inRange(final SourceDataTag sdt, final Object value) {
    return (sdt.getMinValue().compareTo(value) < 0 && sdt.getMaxValue().compareTo(value) > 0);
  }


}
