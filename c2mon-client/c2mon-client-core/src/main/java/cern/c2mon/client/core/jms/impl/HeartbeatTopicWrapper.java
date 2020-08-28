package cern.c2mon.client.core.jms.impl;

import java.util.concurrent.ExecutorService;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;

public class HeartbeatTopicWrapper extends AbstractTopicWrapper<HeartbeatListener, Heartbeat> {

  public HeartbeatTopicWrapper(final SlowConsumerListener slowConsumerListener,
                              final ExecutorService topicPollingExecutor,
                              final C2monClientProperties properties) {
    super(slowConsumerListener, topicPollingExecutor, properties.getJms().getHeartbeatTopic());
  }
  
  @Override
  protected AbstractListenerWrapper<HeartbeatListener, Heartbeat> createListenerWrapper(SlowConsumerListener slowConsumerListener, final ExecutorService topicPollingExecutor) {
    return new HeartbeatListenerWrapper(DEFAULT_LISTENER_QUEUE_SIZE, slowConsumerListener, topicPollingExecutor);
  }
}
