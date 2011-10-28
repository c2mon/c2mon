package cern.tim.driver.test.c2mon
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.mutable.Map
import cern.tim.driver.common.conf.core.EquipmentConfiguration
import cern.tim.driver.common.conf.core.ProcessConfigurationLoader
import cern.tim.driver.common.EquipmentLoggerFactory
import cern.tim.driver.common.EquipmentMessageHandler
import cern.tim.shared.daq.command.SourceCommandTagValue
import cern.tim.driver.test.LogHelper
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import java.io.File
import cern.tim.shared.daq.config.ConfigurationObjectFactory
import cern.tim.shared.daq.config.Change
import cern.tim.shared.daq.config.DataTagAdd
import cern.tim.shared.daq.config.ChangeReport
import cern.tim.shared.daq.config.DataTagRemove
import cern.tim.shared.daq.config.DataTagUpdate
import cern.tim.driver.common.conf.core.ConfigurationUpdater
import cern.tim.shared.daq.config.CommandTagUpdate
import cern.tim.shared.daq.config.CommandTagRemove
import cern.tim.shared.daq.config.CommandTagAdd
import cern.tim.shared.daq.config.EquipmentConfigurationUpdate

/**
 * This connector object forms the connection to the C2MON DAQ module.
 */
object C2MONConnector extends LogHelper {

  /**
   * Map of handler/equipment ids to equipment message handlers.
   */
  val handlerIdToHandlerMap: Map[Long, EquipmentMessageHandler] = Map()

 /**
   * Map of handler/equipment ids to command handlers.
   */
  val handlerIdToCommandHandlerMap: Map[Long, CommandHandler] = Map()

  /**
   * Map of tag ids to equipment message handlers.
   */
  val tagIdToHandlerMap: Map[Long, EquipmentMessageHandler] = Map()

  /**
   * The process configuration loader.
   */
  val loader = new ProcessConfigurationLoader()

  /**
   * Factory for document builders.
   */
  val documentBuilderFactory = DocumentBuilderFactory.newInstance()

  /**
   * Factory for reconfiguration objects.
   */
  val configurationObjectFactory = new ConfigurationObjectFactory()

  /**
   * Map of change ids to changes.
   */
  val changeMap: Map[Long, Change] = Map()

  /**
   * Updater for configuration.
   */
  val configurationUpdater = new ConfigurationUpdater()

  /**
   * Loads an equipment xml from the specified path and adds its contents
   * to this connectors data stores.
   */
  def loadEquipment(path: String) = {
    logger.info("Loading equipment with path " + path)
    val config = loader.createEquipmentConfiguration(
      loadDOM(path).getDocumentElement())
    createHandler(config);
  }

  /**
   * Loads a process xml from the specified path and adds its contents
   * to this connectors data stores.
   */
  def loadProcess(path: String) = {
    logger.info("Loading process with path " + path)
    val config = loader.createProcessConfiguration(
      "path", loadDOM(path), true)
    for (equipmentConfig <- config.getEquipmentConfigurations().values()) {
      createHandler(equipmentConfig)
    }
  }

  /**
   * Loads changes from an xml from the specified path and adds its contents
   * to this connectors data stores.
   */
  def loadChanges(path: String) = {
    logger.info("Loading changes with path " + path)
    val builder = documentBuilderFactory.newDocumentBuilder()
    val document = builder.parse(new File(path))
    def changes =
      configurationObjectFactory.generateChanges(document.getDocumentElement());
    for (change <- changes) {
      changeMap += (change.getChangeId() -> change)
    }
    logger.info("Loaded " + changes.size() + " changes.")
  }

  /**
   * Applies the provided change.
   */
  def applyChange(changeId: Long, report: ChangeReport) {
    val change = changeMap(changeId)
    change match {
      case dataTagAdd: DataTagAdd =>
        applyDataTagAdd(dataTagAdd, report)
      case dataTagRemove: DataTagRemove =>
        applyDataTagRemove(dataTagRemove, report)
      case dataTagUpdate: DataTagUpdate =>
        applyDataTagUpdate(dataTagUpdate, report)
      case commandTagAdd: CommandTagAdd =>
        applyCommandTagAdd(commandTagAdd, report)
      case commandTagRemove: CommandTagRemove =>
        applyCommandTagRemove(commandTagRemove, report)
      case commandTagUpdate: CommandTagUpdate =>
        applyCommandTagUpdate(commandTagUpdate, report)
      case equipmentUpdate : EquipmentConfigurationUpdate =>
        applyEquipmentConfigurationUpdate(equipmentUpdate, report)
      case other:Change => 
        throw new Exception(
            "Unknown change event " + other.getClass().getSimpleName)
    }
  }

  /**
   * Returns the configuration handler for the provided equipment id.
   */
  private def getConfigurationHandler(equipmentId: Long) = {
    val handler = handlerIdToHandlerMap(equipmentId)
    val configurationHandler =
      handler.getEquipmentConfigurationHandler().asInstanceOf[EquipmentConfigurationHandler]
    configurationHandler
  }
  
  /**
   * Applies the provided equipment configuration update.
   */
  private def applyEquipmentConfigurationUpdate(
      equipmentUpdate:EquipmentConfigurationUpdate, report:ChangeReport) = {
    val confHandler = getConfigurationHandler(equipmentUpdate.getEquipmentId())
    val changer = confHandler.equipmentConfigurationChanger
    if (changer != null) {
      val handler = handlerIdToHandlerMap(equipmentUpdate.getEquipmentId())
      val newConf = confHandler.config
      val oldConf = newConf.clone
      configurationUpdater.updateEquipmentConfiguration(equipmentUpdate, newConf)
      changer.onUpdateEquipmentConfiguration(newConf, oldConf, report)
    }
  }

  /**
   * Adds a command tag.
   */
  private def applyCommandTagAdd(commandTagAdd: CommandTagAdd, report: ChangeReport) = {
    val confHandler = getConfigurationHandler(commandTagAdd.getEquipmentId())
    val changer = confHandler.commandTagChanger
    val tag = commandTagAdd.getSourceCommandTag()
    confHandler.config.getCommandTags().put(tag.getId(), tag)
    val handler = handlerIdToHandlerMap(commandTagAdd.getEquipmentId())
    tagIdToHandlerMap.put(tag.getId(), handler)
    if (changer != null)
      changer.onAddCommandTag(tag, report)
  }

  /**
   * Removes a command tag.
   */
  private def applyCommandTagRemove(
    commandTagRemove: CommandTagRemove, report: ChangeReport) = {
    val confHandler = getConfigurationHandler(commandTagRemove.getEquipmentId())
    tagIdToHandlerMap.remove(commandTagRemove.getCommandTagId())
    val changer = confHandler.commandTagChanger
    if (changer != null) {
      val tag =
        confHandler.config.getCommandTags().remove(
            commandTagRemove.getCommandTagId())
      changer.onRemoveCommandTag(tag, report)
    }
  }

  /**
   * Updates a command tag.
   */
  private def applyCommandTagUpdate(commandTagUpdate: CommandTagUpdate, report: ChangeReport) = {
    val confHandler = getConfigurationHandler(commandTagUpdate.getEquipmentId())
    val changer = confHandler.commandTagChanger
    if (changer != null) {
      val handler = handlerIdToHandlerMap(commandTagUpdate.getEquipmentId())
      val newTag =
        confHandler.config.getCommandTags().get(commandTagUpdate.getCommandTagId())
      val oldTag = newTag.clone
      configurationUpdater.updateCommandTag(commandTagUpdate, newTag)
      changer.onUpdateCommandTag(newTag, oldTag, report)
    }
  }

  /**
   * Adds a data tag.
   */
  private def applyDataTagAdd(dataTagAdd: DataTagAdd, report: ChangeReport) = {
    val confHandler = getConfigurationHandler(dataTagAdd.getEquipmentId())
    val changer = confHandler.dataTagChanger
    val tag = dataTagAdd.getSourceDataTag()
    val handler = handlerIdToHandlerMap(dataTagAdd.getEquipmentId())
    tagIdToHandlerMap.put(tag.getId(), handler)
    confHandler.config.getDataTags().put(tag.getId(), tag)
    if (changer != null)
      changer.onAddDataTag(tag, report)
  }

  /**
   * Removes a data tag.
   */
  private def applyDataTagRemove(
    dataTagRemove: DataTagRemove, report: ChangeReport) = {
    val confHandler = getConfigurationHandler(dataTagRemove.getEquipmentId())
    val changer = confHandler.dataTagChanger
    tagIdToHandlerMap.remove(dataTagRemove.getDataTagId())
    if (changer != null) {
      val tag = confHandler.config.getDataTags().remove(dataTagRemove.getDataTagId())
      changer.onRemoveDataTag(tag, report)
    }
  }

  /**
   * Updates a data tag.
   */
  private def applyDataTagUpdate(dataTagUpdate: DataTagUpdate, report: ChangeReport) = {
    val confHandler = getConfigurationHandler(dataTagUpdate.getEquipmentId())
    val changer = confHandler.dataTagChanger
    if (changer != null) {
      val handler = handlerIdToHandlerMap(dataTagUpdate.getEquipmentId())
      val newTag = confHandler.config.getDataTags().get(dataTagUpdate.getDataTagId())
      val oldTag = newTag.clone
      configurationUpdater.updateDataTag(dataTagUpdate, newTag)
      changer.onUpdateDataTag(newTag, oldTag, report)
    }
  }

  /**
   * Called to execute the command with the provided id and value.
   */
  def onCommand(tagId: Long, value: AnyRef): String = {
    try {
      val messageHandler = tagIdToHandlerMap(tagId);
      onCommand(messageHandler, tagId, value)
    } catch {
      case ex: NoSuchElementException =>
        throw new Exception(
          "Command not found! If you try to send an unknown command you'll need "
            + "to specify the equipment id (onEquipment).", ex)
    }
  }

  /**
   * Executes the command on the equipment with the provided id.
   */
  def onCommand(equipmentId: Long, tagId: Long, value: AnyRef): String = {
    val messageHandler = handlerIdToHandlerMap(equipmentId)
    onCommand(messageHandler, tagId, value)
  }

  /**
   * Executes the command on the provided equipment.
   */
  private def onCommand(
    handler: EquipmentMessageHandler, tagId: Long, value: AnyRef) = {
    val handlerId = handler.getEquipmentConfiguration().getId()
    val commandHandler = handlerIdToCommandHandlerMap(handlerId)
    val sourceCommandTagValue =
      new SourceCommandTagValue(
        1L, "asd", handlerId, 0, value, value.getClass().getSimpleName())
    commandHandler.commandRunner.runCommand(sourceCommandTagValue)
  }

  /**
   * Refreshes the value of a tag in an equipment.
   */
  def onRefresh(equipmentId: Long, dataTagId: Long) {
    val messageHandler = handlerIdToHandlerMap(equipmentId)
    messageHandler.refreshDataTag(dataTagId)
  }

  /**
   * Refreshes a whole equipment.
   */
  def onRefresh(equipmentId: Long) {
    val messageHandler = handlerIdToHandlerMap(equipmentId)
    messageHandler.refreshAllDataTags()
  }

  /**
   * Connects to an equipment.
   */
  def onConnect(equipmentId: Long) {
    logger.info("Entering connect to equipment '" + equipmentId + "'.")
    val handler = handlerIdToHandlerMap(equipmentId)
    handler.getEquipmentMessageSender()
      .asInstanceOf[EquipmentMessageSender].connected = true
    handler.connectToDataSource()
    logger.info("Connected to equipment '" + equipmentId + "'.")
  }

  /**
   * Disconnects from an equipment.
   */
  def onDisconnect(equipmentId: Long) {
    val handler = handlerIdToHandlerMap(equipmentId)
    handler.disconnectFromDataSource()
    handler.getEquipmentMessageSender()
      .asInstanceOf[EquipmentMessageSender].connected = false
  }

  /**
   * Shuts all DAQs down.
   */
  def onShutdown() {
    for (equipmentId <- handlerIdToHandlerMap.keys) {
      val handler = handlerIdToHandlerMap(equipmentId)
      handler.shutdown()
      handler.getEquipmentMessageSender()
        .asInstanceOf[EquipmentMessageSender].connected = false
    }
  }

  /**
   * Creates an equipment message handler with the provided configuration.
   */
  private def createHandler(config: EquipmentConfiguration) = {
    val equipmentId = config.getId()
    val equipmentName = config.getName()
    logger.info("Creating Equipment configuration " + equipmentId)
    val handlerClassName = config.getHandlerClassName()
    val commandHandler = new CommandHandler()
    val messageSender = new EquipmentMessageSender()
    val configHandler = new EquipmentConfigurationHandler(config)
    val handler = EquipmentMessageHandler.createEquipmentMessageHandler(
      handlerClassName, commandHandler, configHandler, messageSender)
    handlerIdToHandlerMap.put(equipmentId, handler)
    handlerIdToCommandHandlerMap.put(equipmentId, commandHandler)
    for (tag <- config.getSourceDataTags().values()) {
      tagIdToHandlerMap.put(tag.getId(), handler);
    }
    for (tag <- config.getSourceCommandTags().values()) {
      tagIdToHandlerMap.put(tag.getId(), handler);
    }
    handler.setEquipmentLoggerFactory(
      new EquipmentLoggerFactory(
        handlerClassName, equipmentId,
        equipmentName, "dummy", false, false))
    handler
  }

  /**
   * Loads the DOM document of an XML file at the provided position in the file system. 
   */
  private def loadDOM(path: String) = {
    loader.loadConfigLocal(path)
  }
}
