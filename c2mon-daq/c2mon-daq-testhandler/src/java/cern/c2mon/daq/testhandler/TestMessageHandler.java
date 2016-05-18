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

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.daq.common.jmx.JmxRegistrationMXBean;
import cern.c2mon.daq.common.jmx.JmxRegistrationMXBean.MBeanType;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.process.SubEquipmentConfiguration;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.ChangeReport;
import cern.c2mon.shared.daq.config.ChangeReport.CHANGE_STATE;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.*;

/**
 * This class simulates the behaviour of a DAQ connected to some equipment, by
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
 * - interval : the interval in milliseconds separating times at which events
 * can take place (we call these update points); if 0 there is only one update
 * point
 * - eventProb : the probability that a tag will be updated at a given
 * update point
 * - inRangeProb : the probability that a given update will lie in
 * the min-max tag value range
 * - outDeadBandProb : the probability that an
 * update in the min-max range will lie outside the value deadband
 * - switchProb: for boolean data tags, the probability that a value will switch (initially
 * set to true)
 * - startIn : the time in minutes before tag updates should start
 * (double value) - aliveInterval : the time in milliseconds between
 * "equipment status OK" messages
 */
@ManagedResource(objectName = "cern.c2mon:name=testMessageHandler", log = true)
public class TestMessageHandler extends EquipmentMessageHandler implements TestMessageHandlerMBean {

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

  public static final String PACKAGE_PREFIX = "java.lang.";

  private static final String ARRAY_PACKAGE_PREFIX = "[L" + PACKAGE_PREFIX;

  /**
   * hash table containing the value of the configuration parameters (interval
   * etc.)
   */
  private Map<String, String> configurationParams = new HashMap<String, String>();

  EquipmentAliveTimer equipmentAliveTimer;

  private ValueGenerator valueGenerator;

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
     * The central method that generates values for the tags in the
     * configuration file at every tick of the timer.
     */
    @Override
    public void run() {

      try {
        for (ISourceDataTag sourceDataTag : getEquipmentConfiguration().getSourceDataTags().values()) {
          SourceDataTag sdt = (SourceDataTag) sourceDataTag;

          // Don't send updates for control tags
          if (sdt.isControlTag()) {
            continue;
          }

          if (rand.nextFloat() <= Float.parseFloat(((String) configurationParams.get("eventProb")))) {
            if (getEquipmentLogger().isDebugEnabled()) {
              getEquipmentLogger().debug("generating tag value for tag " + sdt.getId());
            }
            Object value = valueGenerator.generateValue(sdt);

            getEquipmentMessageSender().sendTagFiltered(sdt, value, System.currentTimeMillis());
          }// if

        } // while
      } catch (Exception e) {
        getEquipmentLogger().error("Error occured at test data generation.", e);
      }
    }

  }

  /**
   * The default constructor.
   */
  public TestMessageHandler() {
    JmxRegistrationMXBean jmxRegistrationMXBean = new JmxRegistrationMXBean(MBeanType.JMS, "testMessageHandler");
    jmxRegistrationMXBean.registerMBean(this);
  }

  /**
   * Normally this method is responsible for opening subscriptions for all
   * supervised SourceDataTags (data point elements). Since TestMessageHandler
   * does not connect to any physical device, this method only creates and
   * starts the handler's thread.
   *
   * @throws EqIOException throws this exception if the equipment is badly
   *           configured (usually bad parameters in this case)
   */
  @Override
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

    // initialize the value generator:
    this.valueGenerator = new ValueGenerator(getEquipmentLogger(), getEquipmentConfiguration(), configurationParams);

    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("setting equipment alive message...");
    }
    equipmentAliveTimer = new EquipmentAliveTimer(this);
    int aliveInterval = Integer.parseInt((String) configurationParams.get("aliveInterval"));

    // sets the interval between alive signals and starts the timer
    equipmentAliveTimer.scheduleEquipmentAliveTimer(aliveInterval);
    if (getEquipmentLogger().isDebugEnabled()) {
      getEquipmentLogger().debug("equipment alive configuration done");
    }

    // create alive messages for sub equipments
    for (SubEquipmentConfiguration subEquipmentConfiguration : getEquipmentConfiguration().getSubEquipmentConfigurations().values()) {
      equipmentAliveTimer.scheduleSubEquipmentAliveTimer(subEquipmentConfiguration.getAliveTagId(), aliveInterval);
    }

    // send commfault tag for equipment
    getEquipmentMessageSender().confirmEquipmentStateOK("Test Equipment is OK");

    // send commfault tags for subequipments
    for (SubEquipmentConfiguration subEquipmentConfiguration : getEquipmentConfiguration().getSubEquipmentConfigurations().values()) {
      getEquipmentMessageSender().confirmSubEquipmentStateOK(subEquipmentConfiguration.getCommFaultTagId(), "Test SubEquipment is OK");
    }

    timer = new Timer("SendTimer");

    // the interval between possible event points
    int interval = Integer.parseInt(((String) configurationParams.get("interval")));

    // the delay in minutes before tag updates should start
    double delay = Double.parseDouble((String) configurationParams.get("startIn"));

    // the delay in milliseconds before the updates should start
    // (converting from minutes to milliseconds)
    long millisecondDelay = new Double(Math.floor((delay * 10 * 1000))).longValue();
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

  @Override
  public void suppressEquipmentAliveTag() {
    getEquipmentLogger().debug("Suppressing the Equipment alive tag");
    equipmentAliveTimer.terminateEquipmentAliveTimer();
  }

  @Override
  public void suppressSubEquipmentAliveTag(int aliveTagId) {
    getEquipmentLogger().debug("Suppressing a SubEquipment alive tag (id: " + aliveTagId + ")");
    equipmentAliveTimer.terminateSubEquipmentAliveTimer(new Long(aliveTagId));
  }

  @Override
  public void activateEquipmentAliveTag() {
    try {
      equipmentAliveTimer.terminateEquipmentAliveTimer();
    } catch (IllegalStateException e) {
    }
    equipmentAliveTimer.scheduleEquipmentAliveTimer(Integer.parseInt((String) configurationParams.get("aliveInterval")));
  }

  @Override
  public void activateSubEquipmentAliveTag(int aliveTagId) {
    try {
      equipmentAliveTimer.terminateSubEquipmentAliveTimer(new Long(aliveTagId));
    } catch (IllegalStateException e) {
    }
    equipmentAliveTimer.scheduleSubEquipmentAliveTimer(new Long(aliveTagId), Integer.parseInt((String) configurationParams.get("aliveInterval")));
  }

  @Override
  public void sendEquipmentCommFaultTag(boolean value) {
    if (value == false) {
      getEquipmentMessageSender().confirmEquipmentStateIncorrect("Test Equipment is having some problems...");
    } else {
      getEquipmentMessageSender().confirmEquipmentStateOK("Test Equipment is OK");
    }
  }

  @Override
  public void sendSubEquipmentCommFaultTag(int commFaultTagId, boolean value) {
    if (value == false) {
      getEquipmentMessageSender().confirmSubEquipmentStateIncorrect(new Long(commFaultTagId), "Test SubEquipment is having some problems...");
    } else {
      getEquipmentMessageSender().confirmSubEquipmentStateOK(new Long(commFaultTagId), "Test SubEquipment is OK");
    }
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
        } // while tokens2
      } // while tokens
    }

    // checks parsing was successful, and all parameters were found
    catch (Exception ex) {
      throw new EqIOException("Problem with parsing equipment address to obtain obligatory simulation parameters!");
    }

    if (!configurationParams.containsKey("interval") || !configurationParams.containsKey("eventProb") || !configurationParams.containsKey("startIn")
        || !configurationParams.containsKey("inRangeProb") || !configurationParams.containsKey("outDeadBandProb")
        || !configurationParams.containsKey("switchProb") || !configurationParams.containsKey("aliveInterval")) {
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
   * Shutdown the thread producing the data. Will finish the current iteration
   * through the tags, but not start a new one.
   *
   * @throws EqIOException equipment IO exception
   */
  @Override
  public void disconnectFromDataSource() throws EqIOException {
    timer.cancel();
    // wait for messages to be processed
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      getEquipmentLogger().error("Interrupted while waiting 2s for TestHandler to stop.");
    }
  }

  //
  // /**
  // * This method is a remove SourceDataTag precondition. Not implemented.
  // *
  // * @param id the unique SourceDataTag identifier
  // * @throws EqDataTagException equipment datatag exception
  // */
  // protected void preRemoveDataTag(final Long id) throws EqDataTagException {
  //
  // }
  //
  // /**
  // * This method is a remove SourceDataTag postcondition. Not implemented.
  // *
  // * @param id the unique SourceDataTag identifier
  // * @throws EqDataTagException equipment datatag exception
  // */
  // protected void postRemoveDataTag(final Long id) throws EqDataTagException {
  //
  // }
  //
  // /**
  // * This method is a remove SourceCommandTag precondition. Not implemented.
  // *
  // * @param id the unique SourceCommandTag identifier
  // * @throws EqCommandTagException equipment commandtag exception
  // */
  // protected void preRemoveCommandTag(final Long id) throws
  // EqCommandTagException {
  //
  // }
  //
  // /**
  // * This method is a remove SourceCommandTag postcondition. Not implemented.
  // *
  // * @param id the unique SourceCommandTag identifier
  // * @throws EqCommandTagException equipment commandtag exception
  // */
  // protected void postRemoveCommandTag(final Long id) throws
  // EqCommandTagException {
  //
  // }

  /**
   * TestMessageHandler does not support commands. This method is not
   * implemented.
   *
   * @param sourceCommandTagValue the SourceDataTagValue object
   * @throws EqCommandTagException equipment commandtag exception
   */
  protected void sendCommand(final SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
  }

  // /**
  // * This method is a add SourceDataTag precondition. Not implemented.
  // *
  // * @param sourceDataTag the SourceDataTag object
  // * @throws EqDataTagException equipment datatag exception
  // */
  // protected void preAddDataTag(final SourceDataTag sourceDataTag) throws
  // EqDataTagException {
  //
  // }
  //
  // /**
  // * This method is a add SourceDataTag postcondition. Not implemented.
  // *
  // * @param sourceDataTag the SourceDataTag object
  // * @throws EqDataTagException equipment datatag exception
  // */
  // protected void postAddDataTag(final SourceDataTag sourceDataTag) throws
  // EqDataTagException {
  //
  // }
  //
  // /**
  // * This method is a update SourceDataTag precondition. Not implemented.
  // *
  // * @param sourceDataTag the SourceDataTag object
  // * @throws EqDataTagException equipment datatag exception
  // */
  // protected void preUpdateDataTag(final SourceDataTag sourceDataTag) throws
  // EqDataTagException {
  //
  // }
  //
  // /**
  // * This method is a update SourceDataTag postcondition. Not implemented.
  // *
  // * @param sourceDataTag the SourceDataTag object
  // * @throws EqDataTagException equipment datatag exception
  // */
  // protected void postUpdateDataTag(final SourceDataTag sourceDataTag) throws
  // EqDataTagException {
  //
  // }
  //
  // /**
  // * This method is a add SourceCommandTag precondition. Not implemented. Test
  // * handler does not support commands.
  // *
  // * @param commandTag the SourceCommandTag object
  // * @throws EqCommandTagException equipment commandtag exception
  // */
  // protected void preAddCommandTag(final SourceCommandTag commandTag) throws
  // EqCommandTagException {
  //
  // }
  //
  // /**
  // * This method is a add SourceCommandTag postcondition. Not implemented.
  // * Test handler does not support commands.
  // *
  // * @param commandTag the SourceCommandTag object
  // * @throws EqCommandTagException equipment commandtag exception
  // */
  // protected void postAddCommandTag(final SourceCommandTag commandTag) throws
  // EqCommandTagException {
  //
  // }
  //
  // /**
  // * This method is a update SourceCommandTag precondition. Not implemented.
  // * Test handler does not support commands.
  // *
  // * @param commandTag the SourceCommandTag object
  // * @throws EqCommandTagException equipment commandtag exception
  // */
  // protected void preUpdateCommandTag(final SourceCommandTag commandTag)
  // throws EqCommandTagException {
  //
  // }
  //
  // /**
  // * This method is a update SourceCommandTag postcondition. Not implemented.
  // * Test handler does not support commands.
  // *
  // * @param commandTag the SourceCommandTag object
  // * @throws EqCommandTagException equipment commandtag exception
  // */
  // protected void postUpdateCommandTag(final SourceCommandTag commandTag)
  // throws EqCommandTagException {
  //
  // }


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
    // TODO Implement this method at the moment it might be part of the
    // connectToDataSourceMehtod
  }

  @Override
  public void refreshDataTag(long dataTagId) {
    // TODO Implement this method.
  }

}
