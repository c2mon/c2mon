package cern.c2mon.client.jms.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import cern.c2mon.client.jms.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;
import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;

/**
 * Implementation of an AbstractListenerWrapper for subscribing
 * to the heartbeat topic and distributing events to listeners.
 * 
 * @author Mark Brightwell
 *
 */
public class HeartbeatListenerWrapper extends AbstractListenerWrapper<HeartbeatListener, Heartbeat> {
  
  /**
   * Get C2MON Gson instance for decoding Json message.
   */
  private Gson gson = GsonFactory.createGson();

  @Override
  protected Heartbeat convertMessage(final Message message) throws JMSException {
    return gson.fromJson(((TextMessage) message).getText(), Heartbeat.class);
  }

  @Override
  protected void invokeListener(final HeartbeatListener listener, final Heartbeat event) {
    listener.onHeartbeat(event);
  }

  
 
}
