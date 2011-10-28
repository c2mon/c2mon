package cern.tim.driver.test.c2mon
import cern.tim.driver.common.IEquipmentMessageSender
import cern.tim.shared.daq.datatag.ISourceDataTag
import java.util.Date
import java.sql.Timestamp
import cern.tim.driver.test.LogHelper
import cern.tim.driver.test.events.EventErrors
import cern.tim.driver.test.events.Update
import cern.tim.shared.common.`type`.TypeConverter
import cern.tim.driver.tools.TIMDriverSimpleTypeConverter

class EquipmentMessageSender extends IEquipmentMessageSender
  with LogHelper {

  var connected = false

  def sendSupervisionAlive() = {}
  def sendSupervisionAlive(timestamp: Long) = {}
  def sendTagFiltered(
    tag: ISourceDataTag, tagValue: AnyRef, timestamp: Long) = {
    if (!connected)
      EventErrors.addEventError(
        new Update(new Date()),
        "Got Update while disconnected")
    logger.debug(
      "Update (" + tag.getId() + ", "
        + tagValue + ")")
    true
  }
  def sendTagFiltered(
    tag: ISourceDataTag, tagValue: AnyRef, timestamp: Long,
    valueDescr: String) = {
    sendTagFiltered(tag, tagValue, timestamp)
  }
  def sendInvalidTag(
    tag: ISourceDataTag, pQualityCode: Short, pDescription: String) = {

  }
  def sendInvalidTag(
    tag: ISourceDataTag, pQualityCode: Short, pDescription: String,
    timestamp: Timestamp) = {

  }
  def confirmEquipmentStateIncorrect() = {}
  def confirmEquipmentStateIncorrect(description: String) = {}
  def confirmEquipmentStateOK() = {}
  def confirmEquipmentStateOK(description: String) = {}
  def sendDelayedTimeDeadbandValues() = {}
}