package cern.c2mon.server.client.publish;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.server.client.util.TransferObjectFactory;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.tim.server.alarm.AlarmAggregator;
import cern.tim.server.alarm.AlarmAggregatorListener;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.alarm.TagWithAlarms;
import cern.tim.server.common.alarm.TagWithAlarmsImpl;
import cern.tim.server.common.tag.Tag;
import cern.tim.util.jms.JmsSender;

/**
 * This class implements the <code>AlarmAggregatorListener</code>
 * interface for sending tag value updates to the tag JMS destination
 * topics. The update information is transmitted as GSON message
 * with the <code>TransferTagValue</code> class.
 *
 * @author Matthias Braeger
 * 
 * @see AlarmAggregatorListener
 * @see TagValueUpdate
 */
@Service
public class TagValuePublisher implements AlarmAggregatorListener {

  /** Bean providing for sending JMS messages and waiting for a response */
  private final JmsSender jmsSender;
  
  /** Listens for Tag updates, evaluates all associated alarms and passes the result */
  private final AlarmAggregator alarmAggregator;
  
  /**
   * Default Constructor
   * @param pJmsSender Used for sending JMS messages and waiting for a response
   * @param pAlarmAggregator Used to register this <code>AlarmAggregatorListener</code>
   */
  @Autowired
  public TagValuePublisher(@Qualifier("clientTopicPublisher") final JmsSender pJmsSender, final AlarmAggregator pAlarmAggregator) {
    jmsSender = pJmsSender;
    alarmAggregator = pAlarmAggregator;
  }
  
  /**
   * Init method registering this listener to the <code>AlarmAggregator</code>.
   */
  @PostConstruct
  public void init() {
    alarmAggregator.registerForTagUpdates(this);
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
    TransferTagValueImpl tagValue = 
      TransferObjectFactory.createTransferTagValue(tagWithAlarms);
    
    jmsSender.sendToTopic(tagValue.toJson(), tag.getTopic());
  }

}
