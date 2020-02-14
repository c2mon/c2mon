package cern.c2mon.cache.actions.alarm;

import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.server.common.util.MetadataUtils;
import cern.c2mon.shared.client.alarm.condition.AlarmCondition;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Properties;

import static cern.c2mon.cache.actions.alarm.AlarmProperties.MAX_FAULT_FAMILY_LENGTH;

/**
 * @author Szymon Halastra
 */
@Component
public class AlarmCacheObjectFactory extends AbstractCacheObjectFactory<Alarm> {


  /**
   * Create an AlarmCacheObject from a collection of named properties. The
   * following properties are expected in the collection:
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
   * @param id the id of the alarm object
   * @return the alarm object created
   */
  @Override
  public Alarm createCacheObject(Long id) {
    AlarmCacheObject alarm = new AlarmCacheObject(id);

    // Initialise run-time parameters with default values
    alarm.setActive(false);
    alarm.setInternalActive(false);
    alarm.setTriggerTimestamp(new Timestamp(0));
    alarm.setSourceTimestamp(new Timestamp(0));
    alarm.setInfo("");

    return alarm;
  }

  /**
   * Given an alarm object, reset some of its fields according to the passed
   * properties.
   *
   * @param alarmProperties the properties object containing the fields
   * @param alarm           the alarm object to modify (is modified by this method)
   * @return always returns null, as no alarm change needs propagating to the
   * DAQ layer
   * @throws ConfigurationException if cannot configure the Alarm from the properties
   */
  @Override
  public Change configureCacheObject(Alarm alarm, Properties alarmProperties) {
    AlarmCacheObject alarmCacheObject = (AlarmCacheObject) alarm;

    new PropertiesAccessor(alarmProperties)
      .getLong("dataTagId").ifPresent(alarmCacheObject::setTagId)
      .getString("faultFamily").ifPresent(alarmCacheObject::setFaultFamily)
      .getString("faultMember").ifPresent(alarmCacheObject::setFaultMember)
      .getInteger("faultCode").ifPresent(alarmCacheObject::setFaultCode)
      .getAs("alarmCondition", AlarmCondition::fromConfigXML).ifPresent(alarmCacheObject::setCondition);

    // ALARM metadata
    Metadata newMetadata = MetadataUtils.parseMetadataConfiguration(alarmProperties, alarmCacheObject.getMetadata());
    alarmCacheObject.setMetadata(newMetadata);

    return null;
  }

  @Override
  public void validateConfig(Alarm alarm) throws ConfigurationException {

    new MicroValidator<>(alarm)
      .notNull(Alarm::getId, "id")
      .notNull(Alarm::getTagId, "dataTagId")
      .notNull(Alarm::getFaultFamily, "faultFamily")
      .between(alarmObj -> alarmObj.getFaultFamily().length(), 0, MAX_FAULT_FAMILY_LENGTH,
        "Parameter \"faultFamily\" must be 1 to " + MAX_FAULT_FAMILY_LENGTH + " characters long")
      .notNull(Alarm::getFaultMember, "faultMember")
      .between(Alarm::getFaultCode, 0, Integer.MAX_VALUE, "Parameter \"faultCode\" must be >= 0")
      .notNull(Alarm::getCondition, "alarmCondition");
  }
}
