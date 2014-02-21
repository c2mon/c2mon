package cern.c2mon.server.client.publish;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import cern.c2mon.server.alarm.AlarmAggregator;
import cern.c2mon.server.alarm.AlarmAggregatorListener;
import cern.c2mon.server.cache.TagFacadeGateway;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.alarm.TagWithAlarmsImpl;
import cern.c2mon.server.common.republisher.Publisher;
import cern.c2mon.server.common.republisher.Republisher;
import cern.c2mon.server.common.republisher.RepublisherFactory;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.ConfigurationUpdate;
import cern.c2mon.server.configuration.ConfigurationUpdateListener;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.util.jms.JmsSender;

/**
 * This class implements the <code>AlarmAggregatorListener</code>
 * interface for sending tag value updates to the tag JMS destination
 * topics. The update information is transmitted as GSON message
 * with the <code>TransferTagValue</code> class.
 * 
 * This class implements the <code>ConfigurationUpdateListener</code>
 * interface for sending configuration updates to the tag JMS destination
 * topics. The update information is transmitted as GSON message
 * with the <code>TransferTag</code> class.
 *
 * @author Matthias Braeger, Mark Brightwell, Ignacio Vilches
 * 
 * @see AlarmAggregatorListener
 * @see ConfigurationUpdateListener
 * @see TagValueUpdate
 */
@Service
@ManagedResource(description = "Bean publishing tag updates to the clients")
public class TagValuePublisher implements AlarmAggregatorListener, ConfigurationUpdateListener, Publisher<TagWithAlarms> {

  private static final Logger LOGGER = Logger.getLogger(TagValuePublisher.class); 
  
  /** Bean providing for sending JMS messages and waiting for a response */
  private final JmsSender jmsSender;
  
  /** Listens for Configuration changes */
  private final ConfigurationUpdate configurationUpdate;
  
  /** Listens for Tag updates, evaluates all associated alarms and passes the result */
  private final AlarmAggregator alarmAggregator;
  
  /** Contains re-publication logic */
  private Republisher<TagWithAlarms> republisher;
  
  /** Time between republicaton attempts */
  private int republicationDelay;
  
  /**
   * Reference to the tag facade gateway to retrieve a tag copies with the
   * associated alarms
   */
  private final TagFacadeGateway tagFacadeGateway;

  /** Reference to the tag location service */
  private TagLocationService tagLocationService;
  
  /**
   * Default Constructor
   * @param jmsSender Used for sending JMS messages and waiting for a response
   * @param alarmAggregator Used to register this <code>AlarmAggregatorListener</code>
   * @param configurationUpdate Used to register this <code>ConfigurationUpdateListener</code>
   * @param pTagFacadeGateway Reference to the tag facade gateway singleton
   * @param tagLocationService Reference to the tag location service
   */
  @Autowired
  public TagValuePublisher(@Qualifier("clientTopicPublisher") final JmsSender jmsSender, 
                           final AlarmAggregator alarmAggregator, 
                           final ConfigurationUpdate configurationUpdate,
                           final TagFacadeGateway pTagFacadeGateway,
                           final TagLocationService tagLocationService) {
    this.jmsSender = jmsSender;
    this.alarmAggregator = alarmAggregator;   
    this.configurationUpdate = configurationUpdate;
    this.tagFacadeGateway = pTagFacadeGateway;
    this.tagLocationService = tagLocationService;
    this.republisher = RepublisherFactory.createRepublisher(this, "Tag");       
  }
  
  /**
   * Init method registering this listener to the <code>AlarmAggregator</code>.
   */
  @PostConstruct
  public void init() {
    LOGGER.info("init - Starting Tag publisher.");
    if (LOGGER.isTraceEnabled()) {
    	LOGGER.trace("init - Registering for Tag Updates.");
    }
    this.alarmAggregator.registerForTagUpdates(this);
    
    if (LOGGER.isTraceEnabled()) {
    	LOGGER.trace("init - Registering for Configuration Updates.");
    }
    this.configurationUpdate.registerForConfigurationUpdates(this);
    
    if (republicationDelay != 0)
      republisher.setRepublicationDelay(republicationDelay);
    republisher.start();
  }
  
  /**
   * Before shutdown, stop republisher thread.
   */
  @PreDestroy  
  public void shutdown() {
    LOGGER.info("shutdown - Stopping tag publisher.");
    republisher.stop();
  }
   
  /**
   * Generates for every notification a <code>TransferTagValue</code>
   * object which is then sent as serialized GSON message trough the 
   * dedicated JMS client tag topic.
   * @param tag the updated Tag
   * @param alarms the new values of the associated alarms; this list
   *               is null if no alarms are associated to the tag
   */
  @Override
  public void notifyOnUpdate(final Tag tag, final List<Alarm> alarms) {
    TagWithAlarms tagWithAlarms = new TagWithAlarmsImpl(tag, alarms);    
    try {
      publish(tagWithAlarms);
    } catch (JmsException e) {
      LOGGER.error("notifyOnUpdate - Error publishing tag update to topic for tag " + tagWithAlarms.getTag().getId() + " - submitting for republication", e); 
      republisher.publicationFailed(tagWithAlarms);      
    }
  }

  @Override
  public void publish(final TagWithAlarms tagWithAlarms) {    
    TransferTagValueImpl tagValue = TransferObjectFactory.createTransferTagValue(tagWithAlarms);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("publish - Publishing tag update to client: " + tagValue.toJson());
    }
    jmsSender.sendToTopic(tagValue.toJson(), tagWithAlarms.getTag().getTopic());
  }
  
  @Override
  public void notifyOnConfigurationUpdate(Long tagId) {
    tagLocationService.acquireReadLockOnKey(tagId); 
    try {
      TagWithAlarms tagWithAlarms = this.tagFacadeGateway.getTagWithAlarms(tagId);
      try {
        TransferTagImpl tag = TransferObjectFactory.createTransferTag(tagWithAlarms);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("notifyOnConfigurationUpdate - Publishing configuration update to client: " + tag.toJson());
        }
        jmsSender.sendToTopic(tag.toJson(), tagWithAlarms.getTag().getTopic());
      } catch (JmsException e) {
        LOGGER.error("notifyOnConfigurationUpdate - Error publishing configuration update to topic for tag " + tagWithAlarms.getTag().getId()
            + " - submitting for republication", e);
        republisher.publicationFailed(tagWithAlarms);
      }
    } finally {
      tagLocationService.releaseReadLockOnKey(tagId);
    }
  }

  /**
   * @return the republicationDelay
   */
  public int getRepublicationDelay() {
    return republicationDelay;
  }

  /**
   * @param republicationDelay the republicationDelay to set
   */
  public void setRepublicationDelay(final int republicationDelay) {
    this.republicationDelay = republicationDelay;
  }
  
  /**
   * @return the total number of failed publications since the publisher start
   */
  @ManagedOperation(description = "Returns the total number of failed publication attempts since the application started")  
  public long getNumberFailedPublications() {
    return republisher.getNumberFailedPublications();
  }

  /**
   * @return the number of current tag updates awaiting publication to the clients
   */
  @ManagedOperation(description = "Returns the current number of events awaiting re-publication (should be 0 in normal operation)")  
  public int getSizeUnpublishedList() {    
    return republisher.getSizeUnpublishedList();
  }
}
