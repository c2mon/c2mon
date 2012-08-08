package cern.c2mon.server.client.publish;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.TimCacheListener;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.component.Lifecycle;
import cern.tim.server.common.config.ServerConstants;
import cern.tim.server.common.republisher.Publisher;
import cern.tim.server.common.republisher.Republisher;
import cern.tim.server.common.republisher.RepublisherFactory;
import cern.tim.server.common.tag.Tag;
import cern.tim.util.jms.JmsSender;
import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Publishes active alarms to the C2MON client applications on the
 * alarm publication topic, specified using the property
 * jms.client.alarm.topic
 * 
 * <p>Will attempt re-publication of alarms if JMS connection fails.
 * 
 * 
 * @author Manos, Mark Brightwell
 *
 */
@Service
public class AlarmPublisher implements TimCacheListener<Alarm>, SmartLifecycle, Publisher<AlarmValue>  {
  
  /** Class logger */
  private static final Logger LOGGER = Logger.getLogger(AlarmPublisher.class);
  
  /** Bean providing for sending JMS messages and waiting for a response */
  private final JmsSender jmsSender;
  
  /** Used to register to Alarm updates */
  private CacheRegistrationService cacheRegistrationService;
  
  /** Reference to the tag location service to check whether a tag exists */
  private final TagLocationService tagLocationService;
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();
  
  /** Listener container lifecycle hook */
  private Lifecycle listenerContainer;
  
  /** Contains re-publication logic */
  private Republisher<AlarmValue> republisher;
  
  /** Lifecycle flag */
  private volatile boolean running = false;
  
  /**
   * Default Constructor
   * @param pJmsSender Used for sending JMS messages and waiting for a response.
   * @param pCacheRegistrationService Used to register to Alarm updates.
   * @param pTagLocationService Reference to the tag location service singleton.
   * Used to add tag information to the AlarmValue object.
   */
  @Autowired
  public AlarmPublisher(@Qualifier("alarmTopicPublisher") final JmsSender pJmsSender
      , final CacheRegistrationService pCacheRegistrationService
      , final TagLocationService pTagLocationService) {
    
    jmsSender = pJmsSender;
    cacheRegistrationService = pCacheRegistrationService;
    tagLocationService = pTagLocationService;
    republisher = RepublisherFactory.createRepublisher(this, "Alarm");
  }
  
  /**
   * Registering this listener to alarms.
   */
  @PostConstruct
  void init() {
    listenerContainer = cacheRegistrationService.registerToAlarms(this);
  }

  @Override
  public void confirmStatus(final Alarm alarm) {
    notifyElementUpdated(alarm);
  }

  /**
   * Generates for every alarm update an <code>AlarmValue</code>
   * object which is then sent as serialized GSON message trough the 
   * JMS client topic.
   * @param alarm the updated alarm
   */
  @Override
  public void notifyElementUpdated(final Alarm alarm) {
    
    Long tagId = alarm.getTagId();
    AlarmValue alarmValue = null;
    
    if (tagLocationService.isInTagCache(tagId)) {
      Tag tag = tagLocationService.getCopy(tagId);
      alarmValue = (TransferObjectFactory.createAlarmValue(alarm, tag));
    }
    else {
      LOGGER.warn("notifyElementUpdated() - unrecognized Tag with id " + tagId);
      alarmValue = (TransferObjectFactory.createAlarmValue(alarm));
    }
    try {
      publish(alarmValue);
    } catch (JmsException e) {
      LOGGER.error("Error publishing alarm to clients - submitting for republication. Alarm id is " + alarmValue.getId(), e); 
      republisher.publicationFailed(alarmValue);      
    }    
  }
  
  @Override
  public boolean isAutoStartup() {   
    return false;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    LOGGER.debug("Starting Alarm publisher");
    running = true;
    republisher.start();
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping Alarm publisher");
    listenerContainer.stop();
    republisher.stop();    
    running = false;    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;    
  }

  @Override
  public void publish(final AlarmValue alarmValue) {    
    String jsonAlarm = GSON.toJson(alarmValue);
    LOGGER.debug("Publishing alarm: " + jsonAlarm);
    jmsSender.send(jsonAlarm);
  }
 
}
