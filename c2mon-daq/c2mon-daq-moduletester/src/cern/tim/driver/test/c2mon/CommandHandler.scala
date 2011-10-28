package cern.tim.driver.test.c2mon

import cern.tim.driver.common.IEquipmentCommandHandler
import cern.tim.driver.common.ICommandRunner

class CommandHandler extends IEquipmentCommandHandler {
	
  var commandRunner:ICommandRunner = null;
  
  def setCommandRunner(runner: ICommandRunner): Unit = { 
     commandRunner = runner;
  }

}