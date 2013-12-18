package cern.c2mon.daq.common.impl;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;

public class EquipmentTimeDeadbandTester extends EquipmentTimeDeadband {

  public EquipmentTimeDeadbandTester(final IDynamicTimeDeadbandFilterer dynamicTimeDeadbandFilterer, 
                                     final IProcessMessageSender processMessageSender,
                                     final EquipmentSenderFilterModule equipmentSenderFilterModule, 
                                     final EquipmentLoggerFactory equipmentLoggerFactory) {
    super(dynamicTimeDeadbandFilterer, processMessageSender, equipmentSenderFilterModule, equipmentLoggerFactory);
    // TODO Auto-generated constructor stub
  }

  @Override
  /**
   * 
   * @param tagScheduler The scheduler to start
   */
   protected void startSDTtimeDeadbandScheduler(final SDTTimeDeadbandScheduler tagScheduler) {
     // Don't do anything
   }

  
}
