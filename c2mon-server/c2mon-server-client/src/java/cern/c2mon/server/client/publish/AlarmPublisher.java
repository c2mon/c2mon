package cern.c2mon.server.client.publish;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cache.TimCacheListener;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.util.jms.JmsSender;
import cern.tim.util.json.GsonFactory;

/**
 */
@Service
public class AlarmPublisher implements TimCacheListener<Alarm>  {
  
  /** Class logger */
  private static final Logger LOGGER = Logger.getLogger(AlarmPublisher.class);
  
  /** Bean providing for sending JMS messages and waiting for a response */
  private final JmsSender jmsSender;
  
  /** Used to register to Alarm updates */
  private CacheRegistrationService cacheRegistrationService;
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();
  
  /**
   * Default Constructor
   * @param pJmsSender Used for sending JMS messages and waiting for a response
   * @param pCacheRegistrationService Used to register to Alarm updates
   */
  @Autowired
  public AlarmPublisher(@Qualifier("alarmTopicPublisher") final JmsSender pJmsSender
      , final CacheRegistrationService pCacheRegistrationService) {
    
    jmsSender = pJmsSender;
    cacheRegistrationService = pCacheRegistrationService;
  }
  
  /**
   * Registering this listener to alarms.
   */
  @PostConstruct
  void init() {
    cacheRegistrationService.registerToAlarms(this);
  }

  @Override
  public void confirmStatus(final Alarm alarm) {
    // TODO
  }

  /**
   * Generates for every alarm update an <code>AlarmValue</code>
   * object which is then sent as serialized GSON message trough the 
   * JMS client topic.
   * @param alarm the updated alarm
   */
  @Override
  public void notifyElementUpdated(final Alarm alarm) {

    AlarmValue alarmValue = TransferObjectFactory.createAlarmValue(alarm);
    String jsonAlarm = GSON.toJson(alarmValue);
    LOGGER.debug("Publishing alarm: " + jsonAlarm);
    jmsSender.send(jsonAlarm);
  }
}
