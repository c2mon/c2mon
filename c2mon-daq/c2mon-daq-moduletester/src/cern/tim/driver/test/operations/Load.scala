package cern.tim.driver.test.operations
import cern.tim.driver.test.c2mon.C2MONConnector
import cern.tim.driver.test.LogHelper

object Load extends LogHelper {
  
  def equipment(path:String) = {
//    logger.info("Loading equipment '" + path + "'")
    C2MONConnector.loadEquipment(path)
  }
  
  def process(path:String) = {
//    logger.info("Loading process '" + path + "'")
    C2MONConnector.loadProcess(path)
  }
  
  def changes(path:String) = {
//    logger.info("Loading changes '" + path + "'");
    C2MONConnector.loadChanges(path);
  }
 
}