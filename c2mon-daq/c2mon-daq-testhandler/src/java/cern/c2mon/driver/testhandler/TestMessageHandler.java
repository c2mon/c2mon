/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.driver.testhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import cern.c2mon.driver.common.EquipmentMessageHandler;
import cern.c2mon.driver.common.ICommandRunner;
import cern.c2mon.driver.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.driver.common.conf.equipment.IDataTagChanger;
import cern.c2mon.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.driver.tools.equipmentexceptions.EqIOException;
import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.common.type.TagDataType;
import cern.tim.shared.daq.command.ISourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTag;

/**
 * This class simulates the behavior of a DAQ connected to some equipment, by
 * generating random values as tag update values. A number of parameters
 * influence the way these values generated. The values are then forwarded to
 * the Application Server or Filter Module as usual.
 * 
 * To run this test version, you should use a local configuration of the DAQ,
 * containing the necessary configuration parameters in the equipment unit
 * "address" XML field.
 * 
 * The parameters are all compulsory, and should be included in the format
 * parameter_name=parameter_value:
 * 
 * - interval           : the interval in milliseconds separating times at which events
 *                        can take place (we call these update points); if 0 there is 
 *                        only one update point 
 * - eventProb          : the probability that a tag will be updated at a given
 *                        update point 
 * - inRangeProb        : the probability that a given update will lie in
 *                        the min-max tag value range 
 * - outDeadBandProb    : the probability that an
 *                        update in the min-max range will lie outside the value deadband 
 * - switchProb         : for boolean data tags, the probability that a value will switch 
 *                        (initially set to true) 
 * - startIn            : the time in minutes before tag updates should start (double value) 
 * - aliveInterval      : the time in milliseconds between "equipment status OK" messages
 */
public class TestMessageHandler extends EquipmentMessageHandler {
    
    /**
     * Constant used in generating values outside the value deadband
     * (step is max-min/OUT_DEADBAND_STEP_CONST).
     */
    private static final int OUT_DEADBAND_STEP_CONST = 100;
    
    /**
     * Constant used in generating values inside the value deadband
     * (step is value_deadband/IN_DEADBAND_STEP_CONST).
     */
    private static final int IN_DEADBAND_STEP_CONST = 10;
 
    /**
     * hash table containing the value of the configuration parameters (interval etc.)
     */
    private Map<String, String> configurationParams = new HashMap<String, String>();

    /**
     * The standard java timer
     */
    private Timer timer = null;

    /**
     * The random number generator used throughout the testHandler.
     */
    private Random rand = new Random(System.currentTimeMillis());

    /**
     * This class models the action/task that is taken each timer's 'tick'
     */
    class SendUpdatesTask extends TimerTask {

        /**
         * The central method that generates values for the tags in the configuration
         * file at every tick of the timer.
         */
        public void run() {

            for (ISourceDataTag sourceDataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
                SourceDataTag sdt = (SourceDataTag)sourceDataTag;
                if (rand.nextFloat() <= Float.parseFloat(((String) configurationParams.get("eventProb")))) {
                    if (getEquipmentLogger().isDebugEnabled()) {
                        getEquipmentLogger().debug("generating tag value for tag " + sdt.getId());
                    }
                    Object value = null;
                    switch (sdt.getDataTypeNumeric()) {
                    case TagDataType.TYPE_BOOLEAN:
                        value = generateNewBoolean(sdt);
                        break;

                    case TagDataType.TYPE_DOUBLE:
                        value = generateNewDouble(sdt);
                        break;

                    case TagDataType.TYPE_FLOAT:
                        value = generateNewFloat(sdt);
                        break;

                    case TagDataType.TYPE_INTEGER:
                        value = generateNewInteger(sdt);
                        break;

                    case TagDataType.TYPE_LONG:
                        value = generateNewLong(sdt);
                        break;

                    case TagDataType.TYPE_STRING:
                        value = "some random string..." + rand.nextInt();
                        break;
                        
                    default :
                        //do nothing: type not recognized
                        
                    } //switch
                    getEquipmentMessageSender().sendTagFiltered(sdt, value, System.currentTimeMillis());
                } //if

            } //while
        }

    }

    /**
     * The default constructor.
     */
    public TestMessageHandler() {
    }

    /**
     * Normally this method is responsible for opening subscriptions for all
     * supervised SourceDataTags (data point elements). Since TestMessageHandler
     * does not connect to any physical device, this method only creates and
     * starts the handler's thread.
     * 
     * @throws EqIOException throws this exception if the equipment is badly configured
     *          (usually bad parameters in this case)
     */
    public final void connectToDataSource() throws EqIOException {
        getEquipmentLogger().info("using TestMessageHandler to simulate equipment connected to DAQ");
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("entering connectToDataSource...");
        }

        // get the parameters from the configuration file
        try {
            this.configure();
        } catch (EqIOException ex) {
            throw ex;
        }

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("setting equipment alive message...");
        }
        EquipmentAliveTimer equipmentAliveTimer = new EquipmentAliveTimer(this);
        int aliveInterval = Integer.parseInt((String) configurationParams.get("aliveInterval"));

        // sets the interval between alive signals and starts the timer
        equipmentAliveTimer.setInterval(aliveInterval);
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("equipment alive configuration done");
        }       

        timer = new Timer("SendTimer");

        // the interval between possible event points
        int interval = Integer.parseInt(((String) configurationParams.get("interval")));

        // the delay in minutes before tag updates should start
        double delay = Double.parseDouble((String) configurationParams.get("startIn"));

        // the delay in milliseconds before the updates should start
        // (converting from minutes to milliseconds)
        long millisecondDelay = new Double(Math.floor((delay * 60 * 1000))).longValue();
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("data transmision will begin in " + millisecondDelay + " milliseconds");
        }

        // only one event point
        if (interval == 0) {
            getEquipmentLogger().info("NOTE: data will be sent only once!");
            timer.schedule(new SendUpdatesTask(), millisecondDelay);
        }
        // event points separated by millisecondDelay milliseconds
        else {
            timer.schedule(new SendUpdatesTask(), millisecondDelay, interval);
        }

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("exiting connectToDataSource");
        }
        getEquipmentConfigurationHandler().setDataTagChanger(new IDataTagChanger() {
            
            @Override
            public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
                changeReport.setState(CHANGE_STATE.SUCCESS);
            }
            
            @Override
            public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
                changeReport.setState(CHANGE_STATE.SUCCESS);
            }
            
            @Override
            public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
                changeReport.setState(CHANGE_STATE.SUCCESS);
            }
        });
        getEquipmentConfigurationHandler().setCommandTagChanger(new ICommandTagChanger() {
            
            @Override
            public void onUpdateCommandTag(ISourceCommandTag sourceCommandTag, ISourceCommandTag oldSourceCommandTag, ChangeReport changeReport) {
                changeReport.setState(CHANGE_STATE.SUCCESS);
            }
            
            @Override
            public void onRemoveCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
                changeReport.setState(CHANGE_STATE.SUCCESS);
            }
            
            @Override
            public void onAddCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
                changeReport.setState(CHANGE_STATE.SUCCESS);
            }
        });
        getEquipmentCommandHandler().setCommandRunner(new ICommandRunner() {
            @Override
            public String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
                System.out.println("Command received");
                return null;
            }
        });
    }

    /**
     * This method is responsible for TestMessageHandler configuration. It
     * accesses and parses the address field of the equipment configuration,
     * setting the required parameters in the configurationParams hash table.
     * 
     * @throws EqIOException usually if the parameters are not set correctly
     */
    private void configure() throws EqIOException {
        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("entering configure...");
        }
        
        try {
            // extract the relevant information from the address string
            StringTokenizer tokens = new StringTokenizer(super.getEquipmentConfiguration().getAddress(), ";");
            String token = "";
            String token2 = "";
            while (tokens.hasMoreTokens()) {
                token = tokens.nextToken();
                StringTokenizer tokens2 = new StringTokenizer(token, "=");
                while (tokens2.hasMoreTokens()) {
                    token2 = tokens2.nextToken();
                    if (token2.equalsIgnoreCase("interval")) {
                        configurationParams.put("interval", tokens2.nextToken());
                    } else if (token2.equalsIgnoreCase("eventProb")) {
                        configurationParams.put("eventProb", tokens2.nextToken());
                    } else if (token2.equalsIgnoreCase("startIn")) {
                        configurationParams.put("startIn", tokens2.nextToken());
                    } else if (token2.equalsIgnoreCase("inRangeProb")) {
                        configurationParams.put("inRangeProb", tokens2.nextToken());
                    } else if (token2.equalsIgnoreCase("outDeadBandProb")) {
                        configurationParams.put("outDeadBandProb", tokens2.nextToken());
                    } else if (token2.equalsIgnoreCase("switchProb")) {
                        configurationParams.put("switchProb", tokens2.nextToken());
                    } else if (token2.equalsIgnoreCase("aliveInterval")) {
                        configurationParams.put("aliveInterval", tokens2.nextToken());
                    }
                } //while tokens2
            } //while tokens
        }

        // checks parsing was successful, and all parameters were found
        catch (Exception ex) {
            throw new EqIOException("Problem with parsing equipment address to obtain obligatory simulation parameters!");
        }

        if (!configurationParams.containsKey("interval") 
                || !configurationParams.containsKey("eventProb") 
                || !configurationParams.containsKey("startIn") 
                || !configurationParams.containsKey("inRangeProb") 
                || !configurationParams.containsKey("outDeadBandProb") 
                || !configurationParams.containsKey("switchProb") 
                || !configurationParams.containsKey("aliveInterval")) {
            throw new EqIOException("Problem with parsing equipment address to obtain obligatory simulation parameters!");
        }

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("\tinterval : " + configurationParams.get("interval"));
            getEquipmentLogger().debug("\teventProb : " + configurationParams.get("eventProb"));
            getEquipmentLogger().debug("\tstartIn : " + configurationParams.get("startIn"));
            getEquipmentLogger().debug("\tinRangeProb : " + configurationParams.get("inRangeProb"));
            getEquipmentLogger().debug("\toutDeadBandProb : " + configurationParams.get("outDeadBandProb"));
            getEquipmentLogger().debug("\tswitchProb : " + configurationParams.get("switchProb"));
            getEquipmentLogger().debug("\taliveInterval : " + configurationParams.get("aliveInterval"));
    
            getEquipmentLogger().debug("leaving configure");
        }
        

    }

    /**
     * Shutdown the thread producing the data. Will finish the current
     * iteration through the tags, but not start a new one.
     * 
     * @throws EqIOException equipment IO exception
     */
    public void disconnectFromDataSource() throws EqIOException {      
      timer.cancel();
      //wait for messages to be processed
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        getEquipmentLogger().error("Interrupted while waiting 2s for TestHandler to stop.");
      }
    }
//
//    /**
//     * This method is a remove SourceDataTag precondition. Not implemented.
//     * 
//     * @param id the unique SourceDataTag identifier
//     * @throws EqDataTagException equipment datatag exception
//     */
//    protected void preRemoveDataTag(final Long id) throws EqDataTagException {
//
//    }
//
//    /**
//     * This method is a remove SourceDataTag postcondition. Not implemented.
//     * 
//     * @param id the unique SourceDataTag identifier         
//     * @throws EqDataTagException equipment datatag exception
//     */
//    protected void postRemoveDataTag(final Long id) throws EqDataTagException {
//
//    }
//
//    /**
//     * This method is a remove SourceCommandTag precondition. Not implemented.
//     * 
//     * @param id the unique SourceCommandTag identifier       
//     * @throws  EqCommandTagException equipment commandtag exception
//     */
//    protected void preRemoveCommandTag(final Long id) throws EqCommandTagException {
//
//    }
//
//    /**
//     * This method is a remove SourceCommandTag postcondition. Not implemented.
//     * 
//     * @param id the unique SourceCommandTag identifier
//     * @throws  EqCommandTagException equipment commandtag exception
//     */
//    protected void postRemoveCommandTag(final Long id) throws EqCommandTagException {
//
//    }

    /**
     * TestMessageHandler does not support commands. This method is not
     * implemented.
     * 
     * @param sourceCommandTagValue the SourceDataTagValue object
     * @throws  EqCommandTagException equipment commandtag exception
     */
    protected void sendCommand(final SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
    }

//    /**
//     * This method is a add SourceDataTag precondition. Not implemented.
//     * 
//     * @param sourceDataTag the SourceDataTag object
//     * @throws EqDataTagException equipment datatag exception
//     */
//    protected void preAddDataTag(final SourceDataTag sourceDataTag) throws EqDataTagException {
//
//    }
//
//    /**
//     * This method is a add SourceDataTag postcondition. Not implemented.
//     * 
//     * @param sourceDataTag the SourceDataTag object
//     * @throws EqDataTagException equipment datatag exception
//     */
//    protected void postAddDataTag(final SourceDataTag sourceDataTag) throws EqDataTagException {
//
//    }
//
//    /**
//     * This method is a update SourceDataTag precondition. Not implemented.
//     * 
//     * @param sourceDataTag the SourceDataTag object
//     * @throws EqDataTagException equipment datatag exception
//     */
//    protected void preUpdateDataTag(final SourceDataTag sourceDataTag) throws EqDataTagException {
//
//    }
//
//    /**
//     * This method is a update SourceDataTag postcondition. Not implemented.
//     * 
//     * @param sourceDataTag the SourceDataTag object
//     * @throws EqDataTagException equipment datatag exception
//     */
//    protected void postUpdateDataTag(final SourceDataTag sourceDataTag) throws EqDataTagException {
//
//    }
//
//    /**
//     * This method is a add SourceCommandTag precondition. Not implemented. Test
//     * handler does not support commands.
//     * 
//     * @param commandTag the SourceCommandTag object
//     * @throws  EqCommandTagException equipment commandtag exception
//     */
//    protected void preAddCommandTag(final SourceCommandTag commandTag) throws EqCommandTagException {
//
//    }
//
//    /**
//     * This method is a add SourceCommandTag postcondition. Not implemented.
//     * Test handler does not support commands.
//     * 
//     * @param commandTag the SourceCommandTag object
//     * @throws  EqCommandTagException equipment commandtag exception
//     */
//    protected void postAddCommandTag(final SourceCommandTag commandTag) throws EqCommandTagException {
//
//    }
//
//    /**
//     * This method is a update SourceCommandTag precondition. Not implemented.
//     * Test handler does not support commands.
//     * 
//     * @param commandTag the SourceCommandTag object
//     * @throws  EqCommandTagException equipment commandtag exception
//     */
//    protected void preUpdateCommandTag(final SourceCommandTag commandTag) throws EqCommandTagException {
//
//    }
//
//    /**
//     * This method is a update SourceCommandTag postcondition. Not implemented.
//     * Test handler does not support commands.
//     * 
//     * @param commandTag the SourceCommandTag object
//     * @throws  EqCommandTagException equipment commandtag exception         
//     */
//    protected void postUpdateCommandTag(final SourceCommandTag commandTag) throws EqCommandTagException {
//
//    }

    /**
     * Generates a new boolean value. Initially the value is set to true. It
     * then uses the switchProb to determine if the tag should switch value.
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
     * @param sdt
     *            SourceDataTag to generate a new value for
     * @return a new Double datatag value
     */

    private Double generateNewDouble(final SourceDataTag sdt) {
        double max = ((Double) sdt.getMaxValue()).doubleValue();
        double min = ((Double) sdt.getMinValue()).doubleValue();

        // step is an arbitrary small value, by which the tag value can be
        // increased or decreased (should be small, so that moving from max-step
        // to min+step is not in the value deadband)
        double step = max - min / OUT_DEADBAND_STEP_CONST;

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
                return Double.valueOf(min + step);
            } else { //value is not null
                double currentValue = ((Double) sdt.getCurrentValue().getValue()).doubleValue();
                if (getEquipmentLogger().isDebugEnabled()) {
                    getEquipmentLogger().debug("current datatag value is " + currentValue);
                }
                short deadbandType = sdt.getAddress().getValueDeadbandType();
                // if the data tag has a value deadband
                if (sdt.getAddress().isValueDeadbandEnabled() && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                        || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
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
                    if (rand.nextDouble() < Float.parseFloat(((String) configurationParams.get("outDeadBandProb")))) {
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

                        if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
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
                        } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
                            if (getEquipmentLogger().isDebugEnabled()) {
                                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
                            }                            

                            // notice that move from percentage to decimal below
                            // (relative deadband value is percentage in XML
                            // config)
                            deadbandValue = deadbandValue / 100; //100 is for moving from percentage to decimal
                            // choose to move by half the percentage deadband
                            // value
                            double percentageChange = deadbandValue - deadbandValue / 2;
                            deadbandStep = currentValue * percentageChange;

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

            } //not null

        } //inRangeProb
        
        // choose a value out of the min-max range (this value is not sent to
        // Application Server, so can always be the same for test purposes - 
        // it is not filtered out if the same, and is saved as tag value)
        else {
            if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("returning a value outside the min-max range");
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
      float max = 100; //default values if not set in DB
      float min = 0;
      if (sdt.getMaxValue() != null && sdt.getMinValue() != null) {
        max = ((Float) sdt.getMaxValue()).floatValue();
        min = ((Float) sdt.getMinValue()).floatValue();
      } else {
        sdt.setMaxValue(max); //set defaults
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
                if (sdt.getAddress().isValueDeadbandEnabled() && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                        || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
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

                        if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
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
                        } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
                            if (getEquipmentLogger().isDebugEnabled()) {
                                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
                            }                            

                            // notice that move from percentage to decimal below
                            // (relative deadband value is percentage in XML config)
                            deadbandValue = deadbandValue / 100; //100 to move from percentage to decimal
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

            } //not null

        } //inRangeProb
        
        // choose a value out of the min-max range (this value is not sent to
        // Application Server, so can always be the same for test purposes - 
        // it is not filtered out if the same, and is saved as tag value)
        else {
            if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("returning a value outside the min-max range");
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
        int max = 100; //default values if not set in DB
        int min = 0;
        if (sdt.getMaxValue() != null && sdt.getMinValue() != null) {
          max = ((Integer) sdt.getMaxValue()).intValue();
          min = ((Integer) sdt.getMinValue()).intValue();
        } else {
          sdt.setMaxValue(max); //set defaults
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
                        && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                        || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
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

                        if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
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
                        } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
                            if (getEquipmentLogger().isDebugEnabled()) {
                                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
                            }
                            

                            // notice that move from percentage to decimal below
                            // (relative deadband value is percentage in XML config)
                            deadbandValue = deadbandValue / 100; //100 to move from percentage to decimal
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

            } //not null

        } //inRangeProb
        
        // choose a value out of the min-max range (this value is not sent to Application Server,
        // so can always be the same for test purposes - it is not filtered out if the same, and is
        // saved as tag value)
        else {
            if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("returning a value outside the min-max range");
            }
            
            return Integer.valueOf(max + 1);
        }
    }

    /**
     * Same method as generateNewInteger, but for Long values.
     * 
     * @param sdt
     *            SourceDataTag to generate a new value for
     * @return a new Long datatag value
     */
    private Long generateNewLong(final SourceDataTag sdt) {
        long max = ((Long) sdt.getMaxValue()).longValue();
        long min = ((Long) sdt.getMinValue()).longValue();

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
                        && (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                                || deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE)) {
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

                        if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_ABSOLUTE) {
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
                        } else if (deadbandType == DataTagDeadband.DEADBAND_PROCESS_RELATIVE 
                                || deadbandType == DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE) {
                            if (getEquipmentLogger().isDebugEnabled()) {
                                getEquipmentLogger().debug("\t\tchoosing a value inside the relative deadband");
                            }
                            

                            // notice that move from percentage to decimal below
                            // (relative deadband value is percentage in XML
                            // config)
                            deadbandValue = deadbandValue / 100; //100 to move from percentage to decimal
                            
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

            } //not null

        } //inRangeProb
        
        // choose a value out of the min-max range (this value is not sent to Application Server,
        // so can always be the same for test purposes - it is not filtered out if the same, and is
        // saved as tag value)
        else {
            if (getEquipmentLogger().isDebugEnabled()) {
                getEquipmentLogger().debug("returning a value outside the min-max range");
            }
            
            return Long.valueOf(max + 1);
        }
    }

    /**
     * Determines whether a given value is within the SourceDataTag min-max
     * bracket.
     * 
     * @return boolean indicating whether the value is in range or not
     * @param sdt the SourceDataTag object
     * @param value the value we wish to check is in the min-max range of the datatag
     */
    private boolean inRange(final SourceDataTag sdt, final Object value) {
        return (sdt.getMinValue().compareTo(value) < 0 && sdt.getMaxValue().compareTo(value) > 0);
    }

    /**
     * Checks whether the value is within the value deadband or not. Same as the
     * (private) method in EquipmentMessageHandler.
     * 
     * @param tagID - the unique identifier of the tag
     * @param tagValue - new value of the SourceDataTag, received from a data source.
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
                switch (sdt.getDataTypeNumeric()) {
                case TagDataType.TYPE_DOUBLE:
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
                        doubleRatio = ((Double) sdt.getCurrentValue().getValue()).doubleValue() * (valueDeadband / 100); //100 to convert percentages
                        doubleDiff = ((Double) tagValue).doubleValue() - ((Double) sdt.getCurrentValue().getValue()).doubleValue();

                        // value != 0 --> condition
                        if ((((Double) sdt.getCurrentValue().getValue()).doubleValue() != 0.0f) 
                                && (Math.abs(doubleDiff) < Math.abs(doubleRatio))) {
                            result = true;
                        }
                        break;

                    // same as above case
                    case DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE:
                        // compute: abs(v1-v2) < abs(v1*valueDeadband)
                        doubleRatio = ((Double) sdt.getCurrentValue().getValue()).doubleValue() * (valueDeadband / 100);
                        doubleDiff = ((Double) tagValue).doubleValue() - ((Double) sdt.getCurrentValue().getValue()).doubleValue();

                        // value != 0 --> condition
                        if ((((Double) sdt.getCurrentValue().getValue()).doubleValue() != 0.0f) 
                                && (Math.abs(doubleDiff) < Math.abs(doubleRatio))) {
                            result = true;
                        }
                        break;
                        
                    default : //do nothing, deadband type not recognised, returning false
                    
                    } //switch
                    break;

                case TagDataType.TYPE_FLOAT:
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
                        if ((((Float) sdt.getCurrentValue().getValue()).floatValue() != 0.0f) 
                                && (Math.abs(floatDiff) < Math.abs(floatRatio))) {
                            if (getEquipmentLogger().isDebugEnabled()) {
                                getEquipmentLogger().debug("checking relative value-based deadband filtering");
                            }
                            result = true;
                        }
                        break;
                        
                        default : //do nothing, deadband type not recognised, returning false

                    } //switch
                    break;

                case TagDataType.TYPE_INTEGER:
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
                        if ((((Integer) sdt.getCurrentValue().getValue()).intValue() != 0) 
                                && (Math.abs(integerDiff) < Math.abs(doubleRatio))) {
                            result = true;
                        }
                        break;
                        
                        default : //do nothing, deadband type not recognised, returning false
                            
                    } //switch
                    break;

                case TagDataType.TYPE_LONG:
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
                        if ((((Long) sdt.getCurrentValue().getValue()).longValue() != 0) 
                                && (Math.abs(longDiff) < Math.abs(doubleRatio))) {
                            result = true;
                        }
                        break;
                        
                        default : //do nothing, deadband type not recognised, returning false
                    
                    } //switch
                    break;
                    
                    default : //do nothing, datatype not recognized, returning false
                    
                } //switch
            } //if deadband-value enabled
        } //if

        if (getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("leaving isInValueDeadband(); result is " + result);
        }
        
        return result;
    }

    /**
     * Main method is used just for testing
     * 
     * @param args no args needed
     */
    public static void main(final String[] args) {
        new TestMessageHandler();
        System.exit(0);
    }
    
    @Override
    public void refreshAllDataTags() {
        // TODO Implement this method at the moment it might be part of the connectToDataSourceMehtod
    }
    
    @Override
    public void refreshDataTag(long dataTagId) {
        // TODO Implement this method.
    }

}
