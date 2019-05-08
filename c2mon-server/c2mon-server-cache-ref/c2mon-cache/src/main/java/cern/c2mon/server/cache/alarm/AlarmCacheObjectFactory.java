package cern.c2mon.server.cache.alarm;

import java.sql.Timestamp;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.alarm.AlarmCondition;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

import static cern.c2mon.server.cache.alarm.AlarmProperties.MAX_FAULT_FAMILY_LENGTH;
import static cern.c2mon.server.cache.alarm.AlarmProperties.MAX_FAULT_MEMBER_LENGTH;

/**
 * @author Szymon Halastra
 */
@Component
public class AlarmCacheObjectFactory extends AbstractCacheObjectFactory<Alarm> {

  private AlarmService alarmService;

  @Autowired
  public AlarmCacheObjectFactory(AlarmService alarmService) {
    this.alarmService = alarmService;
  }

  /**
   * Create an AlarmCacheObject from a collection of named properties.
   * The following properties are expected in the collection:
   * <ul>
   * <li>id</li>
   * <li>dataTagId</li>
   * <li>faultMember</li>
   * <li>faultFamily</li>
   * <li>faultCode</li>
   * <li>alarmCondition</li>
   * </ul>
   * <p>
   * A ConfigurationException will be thrown if one of the parameters cannot be
   * decoded to the right format. Even if no exception is thrown, it is
   * advisable to call the validate() method on the newly created object, which
   * will perform further consistency checks.
   * <p>
   * Please note that neither this constructor nor the validate method can
   * perform dependency checks. It is up to the user to ensure that the DataTag
   * to which the alarm is attached exists.
   *
   * @param id         the id of the alarm object
   * @param properties the properties containing the values for the alarm fields
   *
   * @return the alarm object created
   */
  @Override
  public Alarm createCacheObject(Long id) {
    AlarmCacheObject alarmCacheObject = new AlarmCacheObject(id);

    // Initialise run-time parameters with default values
//    alarmCacheObject.setState(AlarmCondition.TERMINATE);
    alarmCacheObject.setTimestamp(new Timestamp(0));
    alarmCacheObject.setInfo("");

    return alarmCacheObject;
  }

  @Override
  public Change configureCacheObject(Alarm alarm, Properties properties) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;
    String tmpStr = null;
    if ((tmpStr = properties.getProperty("dataTagId")) != null) {
      alarmCacheObject.setDataTagId(parseLong(tmpStr, "dataTagId"));
    }
    if (properties.getProperty("faultFamily") != null) {
      alarmCacheObject.setFaultFamily(properties.getProperty("faultFamily"));
    }
    if (properties.getProperty("faultMember") != null) {
      alarmCacheObject.setFaultMember(properties.getProperty("faultMember"));
    }

    if ((tmpStr = properties.getProperty("faultCode")) != null) {
      alarmCacheObject.setFaultCode(parseInt(tmpStr, "faultCode"));
    }

    if ((tmpStr = properties.getProperty("alarmCondition")) != null) {
      try {
        alarmCacheObject.setCondition(AlarmCondition.fromConfigXML(tmpStr));
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
                "Exception: Unable to create AlarmCondition object from parameter \"alarmCondition\": \n" + tmpStr);
      }
    }

    // ALARM metadata
    tmpStr = properties.getProperty("metadata");
    if (tmpStr != null) {
      Metadata metadata = new Metadata();
      metadata.setMetadata(Metadata.fromJSON(tmpStr));
      alarmCacheObject.setMetadata(metadata);
    }

    // set the JMS topic
    alarmCacheObject.setTopic(alarmService.getTopicForAlarm(alarmCacheObject));

    return null;
  }

  @Override
  public void validateConfig(Alarm alarm) throws ConfigurationException {
    if (alarm.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (alarm.getTagId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataTagId\" cannot be null");
    }
    if (alarm.getFaultFamily() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultFamily\" cannot be null");
    }
    if (alarm.getFaultFamily().length() == 0 || alarm.getFaultFamily().length() > MAX_FAULT_FAMILY_LENGTH) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultFamily\" must be 1 to 20 characters long");
    }
    if (alarm.getFaultMember() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultMember\" cannot be null");
    }
    if (alarm.getFaultMember().length() == 0 || alarm.getFaultMember().length() > MAX_FAULT_MEMBER_LENGTH) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultMember\" must be 1 to 64 characters long");
    }
    if (alarm.getFaultCode() < 0) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"faultCode\" must be >= 0");
    }
    if (alarm.getCondition() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"alarmCondition\" cannot be null");
    }
  }
}
