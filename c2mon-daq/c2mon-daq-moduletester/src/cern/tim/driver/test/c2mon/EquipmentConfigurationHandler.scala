package cern.tim.driver.test.c2mon
import cern.tim.driver.common.conf.equipment.IDataTagChanger
import cern.tim.driver.common.conf.equipment.IEquipmentConfiguration
import cern.tim.driver.common.conf.equipment.ICommandTagChanger
import cern.tim.driver.common.conf.equipment.IEquipmentConfigurationChanger
import cern.tim.driver.common.conf.equipment.IEquipmentConfigurationHandler
import cern.tim.driver.common.conf.core.EquipmentConfiguration

class EquipmentConfigurationHandler(val config:EquipmentConfiguration) 
        extends IEquipmentConfigurationHandler {
  var dataTagChanger:IDataTagChanger = null
  var commandTagChanger:ICommandTagChanger = null
  var equipmentConfigurationChanger:IEquipmentConfigurationChanger = null
  
  def getEquipmentId() = {
    config.getId()
  }
  def getEquipmentConfiguration() = {
    config
  }
  def setDataTagChanger(changer:IDataTagChanger) = {
    dataTagChanger = changer;
  }
  def setCommandTagChanger(changer:ICommandTagChanger) = {
    commandTagChanger = changer
  }
  def setEquipmentConfigurationChanger(changer:IEquipmentConfigurationChanger) = {
    equipmentConfigurationChanger = changer
  }
}