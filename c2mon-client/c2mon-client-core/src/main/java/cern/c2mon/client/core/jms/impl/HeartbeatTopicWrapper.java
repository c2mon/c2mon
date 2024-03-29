package cern.c2mon.client.core.jms.impl;

import java.util.concurrent.ExecutorService;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.EnqueuingEventListener;
import cern.c2mon.client.core.jms.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;

public class HeartbeatTopicWrapper extends AbstractTopicWrapper<HeartbeatListener, Heartbeat> {

  public HeartbeatTopicWrapper(final SlowConsumerListener slowConsumerListener,
                               final EnqueuingEventListener enqueuingEventListener,
                               final ExecutorService topicPollingExecutor,
                               final C2monClientProperties properties) {
    super(slowConsumerListener, enqueuingEventListener, topicPollingExecutor, properties.getJms().getHeartbeatTopic(), properties);
  }

  @Override
  protected AbstractListenerWrapper<HeartbeatListener, Heartbeat> createListenerWrapper(C2monClientProperties properties,
                                                                                        SlowConsumerListener slowConsumerListener,
                                                                                        EnqueuingEventListener enqueuingEventListener,
                                                                                        final ExecutorService topicPollingExecutor) {
    return new HeartbeatListenerWrapper(properties.getDefaultListenerQueueSize(), slowConsumerListener, enqueuingEventListener, topicPollingExecutor);
  }
}
