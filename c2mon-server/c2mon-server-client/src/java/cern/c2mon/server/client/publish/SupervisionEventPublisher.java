package cern.c2mon.server.client.publish;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.tim.server.supervision.SupervisionListener;
import cern.tim.server.supervision.SupervisionNotifier;
import cern.tim.util.jms.JmsSender;
import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;

@Service
public class SupervisionEventPublisher implements SupervisionListener {

  /** Class logger */
  private static final Logger LOGGER = Logger.getLogger(SupervisionEventPublisher.class);
  
  /** Bean providing for sending JMS messages and waiting for a response; default destination set */
  private final JmsSender jmsSender;
  
  /** Reference to the <code>SupervisionNotifier</code> for registering this listener */
  private final SupervisionNotifier supervisionNotifier;
  
  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();


  /**
   * Default Constructor
   * @param pJmsSender Used for sending JMS messages and waiting for a response
   * @param pSupervisionNotifier Used for registering this class as listener
   */
  @Autowired
  public SupervisionEventPublisher(@Qualifier("supervisionTopicPublisher") final JmsSender pJmsSender,
                                   final SupervisionNotifier pSupervisionNotifier) {
    jmsSender = pJmsSender;
    supervisionNotifier = pSupervisionNotifier;
  }
  
  
  /**
   * Init method registering this listener to the <code>TimCache</code>.
   */
  @PostConstruct
  public void init() {
    supervisionNotifier.registerAsListener(this);
  }


  @Override
  public void notifySupervisionEvent(@Valid final SupervisionEvent supervisionEvent) { 
    LOGGER.debug("Publishing supervision event: " + GSON.toJson(supervisionEvent));
    jmsSender.send(GSON.toJson(supervisionEvent));
  }
}
