package cern.c2mon.server.configuration.api.util;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.dbaccess.*;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.test.TestDataInserter;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.datatag.address.impl.PLCHardwareAddressImpl;
import cern.c2mon.shared.common.type.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author Franz Ritter
 */
@Service
public class CacheObjectFactoryWithProperties {


  @Autowired
  private DataTagCache dataTagCache;

  @Autowired
  private ControlTagCache controlTagCache;

  @Autowired
  private CommandTagCache commandTagCache;

  @Autowired
  private RuleTagCache ruleTagCache;

  @Autowired
  private EquipmentCache equipmentCache;

  @Autowired
  private SubEquipmentCache subEquipmentCache;

  @Autowired
  private ProcessCache processCache;

  @Autowired
  private AliveTimerCache aliveTimerCache;

  @Autowired
  private CommFaultTagCache commFaultTagCache;

  @Autowired
  private AlarmCache alarmCache;

  @Autowired
  private TestDataInserter testDataInserter;

  @Value("${c2mon.jms.daq.queue.trunk}")
  private String jmsDaqQueueTrunk;

  @Value("${c2mon.jms.tag.publication.topic}")
  private String tagPublicationTrunk = "c2mon.client.tag.default";

  @Value("${c2mon.jms.controltag.publication.topic}")
  private String controlTagPublicationTopic;

  public ProcessCacheObject buildProcessCacheObject(Long id, Properties props){

    ProcessCacheObject expectedObject = new ProcessCacheObject(id);

    if(props.getProperty("name") != null){
      expectedObject.setName(props.getProperty("name"));
    }
    if(props.getProperty("aliveInterval") != null){
      expectedObject.setAliveInterval(Integer.valueOf(props.getProperty("aliveInterval")));
    }
    if(props.getProperty("aliveTagId") != null){
      expectedObject.setAliveTagId(Long.valueOf(props.getProperty("aliveTagId")));
    }
    if(props.getProperty("stateTagId") != null){
      expectedObject.setStateTagId(Long.valueOf(props.getProperty("stateTagId")));
    }
    if(props.getProperty("maxMessageSize") != null){
      expectedObject.setMaxMessageSize(Integer.valueOf(props.getProperty("maxMessageSize")));
    }
    if(props.getProperty("maxMessageDelay") != null){
      expectedObject.setMaxMessageDelay(Integer.valueOf(props.getProperty("maxMessageDelay")));
    }
    if(props.getProperty("description") != null){
      expectedObject.setDescription(props.getProperty("description"));
    }
    // Current host and PIK will be null
    expectedObject.setJmsDaqCommandQueue(this.jmsDaqQueueTrunk + ".command.null." + expectedObject.getName() + ".null");

    return expectedObject;
  }

  public EquipmentCacheObject buildEquipmentCacheObject(Long id, Properties props){

    EquipmentCacheObject expectedObject = new EquipmentCacheObject(id);

    if(props.getProperty("name") != null){
      expectedObject.setName(props.getProperty("name"));
    }
    if(props.getProperty("address") != null){
      expectedObject.setAddress(props.getProperty("address"));
    }
    if(props.getProperty("aliveInterval") != null){
      expectedObject.setAliveInterval(Integer.valueOf(props.getProperty("aliveInterval")));
    }
    if(props.getProperty("aliveTagId") != null){
      expectedObject.setAliveTagId(Long.valueOf(props.getProperty("aliveTagId")));
    }
    if(props.getProperty("stateTagId") != null){
      expectedObject.setStateTagId(Long.valueOf(props.getProperty("stateTagId")));
    }
    if(props.getProperty("commFaultTagId") != null){
      expectedObject.setCommFaultTagId(Long.valueOf(props.getProperty("commFaultTagId")));
    }
    if(props.getProperty("handlerClass") != null){
      expectedObject.setHandlerClassName(props.getProperty("handlerClass"));
    }
    if(props.getProperty("processId") != null){
      expectedObject.setProcessId(Long.valueOf(props.getProperty("processId")));
    }
    if(props.getProperty("description") != null){
      expectedObject.setDescription(props.getProperty("description"));
    }
    return expectedObject;
  }

  public SubEquipmentCacheObject buildSubEquipmentCacheObject(Long id, Properties props){

    SubEquipmentCacheObject expectedObject = new SubEquipmentCacheObject(id);

    if(props.getProperty("name") != null){
      expectedObject.setName(props.getProperty("name"));
    }
    if(props.getProperty("aliveInterval") != null){
      expectedObject.setAliveInterval(Integer.valueOf(props.getProperty("aliveInterval")));
    }
    if(props.getProperty("aliveTagId") != null){
      expectedObject.setAliveTagId(Long.valueOf(props.getProperty("aliveTagId")));
    }
    if(props.getProperty("stateTagId") != null){
      expectedObject.setStateTagId(Long.valueOf(props.getProperty("stateTagId")));
    }
    if(props.getProperty("commFaultTagId") != null){
      expectedObject.setCommFaultTagId(Long.valueOf(props.getProperty("commFaultTagId")));
    }
    if(props.getProperty("handlerClass") != null){
      expectedObject.setHandlerClassName(props.getProperty("handlerClass"));
    }
    if(props.getProperty("equipmentId") != null){
      expectedObject.setParentId(Long.valueOf(props.getProperty("equipmentId")));
    }
    if(props.getProperty("description") != null){
      expectedObject.setDescription(props.getProperty("description"));
    }
    return expectedObject;
  }

  public ControlTagCacheObject buildControlTagCacheObject(Long id, Properties props, Long processId, Long equipmentId, Long subEquipmentId){

    ControlTagCacheObject expectedObject = new ControlTagCacheObject(id);

    if(props.getProperty("name") != null){
      expectedObject.setName(props.getProperty("name"));
    }
    if(props.getProperty("mode") != null){
      expectedObject.setMode(Short.valueOf(props.getProperty("mode")));
    }
    if(props.getProperty("dataType") != null){
      expectedObject.setDataType(props.getProperty("dataType"));
    }
    if(props.getProperty("description") != null){
      expectedObject.setDescription(props.getProperty("description"));
    }
    if(props.getProperty("isLogged") != null){
      expectedObject.setLogged(Boolean.parseBoolean(props.getProperty("isLogged")));
    }
    expectedObject.setTopic(controlTagPublicationTopic);
    expectedObject.setDataTagQuality(new DataTagQualityImpl());

    if(processId != null){
      expectedObject.setProcessId(processId);
    }
    if(equipmentId != null){
      expectedObject.setEquipmentId(equipmentId);
    }
    if(subEquipmentId != null){
      expectedObject.setSubEquipmentId(subEquipmentId);
    }

    return expectedObject;
  }
  public DataTagCacheObject buildDataTagCacheObject(Long id, Properties props, Long processId) {

    DataTagCacheObject expectedObject = new DataTagCacheObject(id);

    if (props.getProperty("name") != null) {
      expectedObject.setName(props.getProperty("name"));
    }
    if (props.getProperty("mode") != null) {
      expectedObject.setMode(Short.valueOf(props.getProperty("mode")));
    }
    if (props.getProperty("dataType") != null) {
      expectedObject.setDataType(props.getProperty("dataType"));
    }
    if (props.getProperty("description") != null) {
      expectedObject.setDescription(props.getProperty("description"));
    }
    if (props.getProperty("unit") != null) {
      expectedObject.setUnit(props.getProperty("unit"));
    }
    if (props.getProperty("minValue") != null) {
      expectedObject.setMinValue((Comparable)TypeConverter.cast(props.getProperty("minValue"), props.getProperty("dataType")));
    }
    if (props.getProperty("maxValue") != null) {
      expectedObject.setMaxValue((Comparable) TypeConverter.cast(props.getProperty("maxValue"), props.getProperty("dataType")));
    }
    if (props.getProperty("address") != null) {
      expectedObject.setAddress(DataTagAddress.fromConfigXML(props.getProperty("address")));
    }
    if (props.getProperty("dipAddress") != null) {
      expectedObject.setDipAddress(props.getProperty("dipAddress"));
    }
    if (props.getProperty("japcAddress") != null) {
      expectedObject.setJapcAddress(props.getProperty("japcAddress"));
    }
    if (props.getProperty("isLogged") != null) {
      expectedObject.setLogged(Boolean.parseBoolean(props.getProperty("isLogged")));
    }
    if (props.getProperty("equipmentId") != null) {
      expectedObject.setEquipmentId(Long.valueOf(props.getProperty("equipmentId")));
    }else if (props.getProperty("subEquipmentId") != null) {
      expectedObject.setSubEquipmentId(Long.valueOf(props.getProperty("subEquipmentId")));
    }
    expectedObject.setProcessId(processId);
    expectedObject.setTopic(tagPublicationTrunk + "." +processId);
    expectedObject.setDataTagQuality(new DataTagQualityImpl());

    return expectedObject;
  }

  public CommandTagCacheObject buildCommandTagCacheObject(Long id, Properties props) {

    CommandTagCacheObject expectedObject = new CommandTagCacheObject(id);

    if (props.getProperty("name") != null) {
      expectedObject.setName(props.getProperty("name"));
    }
    if (props.getProperty("mode") != null) {
      expectedObject.setMode(Short.valueOf(props.getProperty("mode")));
    }
    if (props.getProperty("dataType") != null) {
      expectedObject.setDataType(props.getProperty("dataType"));
    }
    if (props.getProperty("description") != null) {
      expectedObject.setDescription(props.getProperty("description"));
    }
    if (props.getProperty("equipmentId") != null) {
      expectedObject.setEquipmentId(Long.valueOf(props.getProperty("equipmentId")));
    }
    if (props.getProperty("clientTimeout") != null) {
      expectedObject.setClientTimeout(Integer.parseInt(props.getProperty("clientTimeout")));
    }
    if (props.getProperty("execTimeout") != null) {
      expectedObject.setExecTimeout(Integer.parseInt(props.getProperty("execTimeout")));
    }
    if (props.getProperty("sourceTimeout") != null) {
      expectedObject.setSourceTimeout(Integer.parseInt(props.getProperty("sourceTimeout")));
    }
    if (props.getProperty("sourceRetries") != null) {
      expectedObject.setSourceRetries(Integer.parseInt(props.getProperty("sourceRetries")));
    }
    if (props.getProperty("hardwareAddress") != null) {
      expectedObject.setHardwareAddress((HardwareAddressFactory.getInstance().fromConfigXML(props.getProperty("hardwareAddress"))));
    }
    RbacAuthorizationDetails details = new RbacAuthorizationDetails();
    details.setRbacClass(props.getProperty("rbacClass"));
    details.setRbacDevice(props.getProperty("rbacDevice"));
    details.setRbacProperty(props.getProperty("rbacProperty"));
    expectedObject.setAuthorizationDetails(details);

    return expectedObject;
  }

  public RuleTagCacheObject buildRuleTagCacheObject(Long id, Properties props, Long processId, Long equipmentId) {

    RuleTagCacheObject expectedObject = new RuleTagCacheObject(id);

    if (props.getProperty("name") != null) {
      expectedObject.setName(props.getProperty("name"));
    }
    if (props.getProperty("mode") != null) {
      expectedObject.setMode(Short.valueOf(props.getProperty("mode")));
    }
    if (props.getProperty("dataType") != null) {
      expectedObject.setDataType(props.getProperty("dataType"));
    }
    if (props.getProperty("description") != null) {
      expectedObject.setDescription(props.getProperty("description"));
    }
    if (props.getProperty("unit") != null) {
      expectedObject.setUnit(props.getProperty("unit"));
    }
    if (props.getProperty("dipAddress") != null) {
      expectedObject.setDipAddress(props.getProperty("dipAddress"));
    }
    if (props.getProperty("japcAddress") != null) {
      expectedObject.setJapcAddress(props.getProperty("japcAddress"));
    }
    if (props.getProperty("isLogged") != null) {
      expectedObject.setLogged(Boolean.parseBoolean(props.getProperty("isLogged")));
    }
    if (props.getProperty("ruleText") != null) {
      expectedObject.setRuleText(props.getProperty("ruleText"));
    }
    expectedObject.setTopic(tagPublicationTrunk + "." +processId);
    Set<Long> eqIds = new HashSet<>();
    eqIds.add(equipmentId);
    expectedObject.setEquipmentIds(eqIds);
    Set<Long> procIds = new HashSet<>();
    procIds.add(processId);
    expectedObject.setProcessIds(procIds);

    return expectedObject;
  }

  public AlarmCacheObject buildRuleTagCacheObject(Long id, Properties props) {

    AlarmCacheObject expectedObject = new AlarmCacheObject(id);

    if (props.getProperty("dataTagId") != null) {
      expectedObject.setDataTagId(Long.valueOf(props.getProperty("dataTagId")));
    }
    if (props.getProperty("faultFamily") != null) {
      expectedObject.setFaultFamily(props.getProperty("faultFamily"));
    }
    if (props.getProperty("faultMember") != null) {
      expectedObject.setFaultMember(props.getProperty("faultMember"));
    }
    if (props.getProperty("faultCode") != null) {
      expectedObject.setFaultCode(Integer.parseInt(props.getProperty("faultCode")));
    }
    if (props.getProperty("alarmCondition") != null) {
      expectedObject.setCondition(AlarmCondition.fromConfigXML(props.getProperty("alarmCondition")));
    }
    return expectedObject;
  }
}
