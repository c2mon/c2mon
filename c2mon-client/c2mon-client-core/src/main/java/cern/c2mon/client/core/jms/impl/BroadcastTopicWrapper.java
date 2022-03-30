package cern.c2mon.client.core.jms.impl;

import java.util.concurrent.ExecutorService;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.BroadcastMessageListener;
import cern.c2mon.client.core.jms.EnqueuingEventListener;

public class BroadcastTopicWrapper extends AbstractTopicWrapper<BroadcastMessageListener, BroadcastMessage> {

  public BroadcastTopicWrapper(final SlowConsumerListener slowConsumerListener,
                               final EnqueuingEventListener enqueuingEventListener,
                               final ExecutorService topicPollingExecutor,
                               final C2monClientProperties properties) {
    super(slowConsumerListener, enqueuingEventListener, topicPollingExecutor, properties.getJms().getBroadcastTopic(), properties);
  }

  @Override
  protected AbstractListenerWrapper<BroadcastMessageListener, BroadcastMessage> createListenerWrapper(C2monClientProperties properties,
                                                                                                      SlowConsumerListener slowConsumerListener,
                                                                                                      EnqueuingEventListener enqueuingEventListener,
                                                                                                      final ExecutorService topicPollingExecutor) {
    return new BroadcastMessageListenerWrapper(properties.getDefaultListenerQueueSize(), slowConsumerListener, enqueuingEventListener, topicPollingExecutor);
  }
}
