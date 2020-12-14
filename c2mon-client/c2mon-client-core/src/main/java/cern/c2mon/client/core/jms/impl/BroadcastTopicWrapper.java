package cern.c2mon.client.core.jms.impl;

import java.util.concurrent.ExecutorService;

import cern.c2mon.client.common.admin.BroadcastMessage;
import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.client.core.jms.BroadcastMessageListener;

public class BroadcastTopicWrapper extends AbstractTopicWrapper<BroadcastMessageListener, BroadcastMessage> {

  public BroadcastTopicWrapper(final SlowConsumerListener slowConsumerListener,
                              final ExecutorService topicPollingExecutor,
                              final C2monClientProperties properties) {
    super(slowConsumerListener, topicPollingExecutor, properties.getJms().getBroadcastTopic());
  }
  
  @Override
  protected AbstractListenerWrapper<BroadcastMessageListener, BroadcastMessage> createListenerWrapper(SlowConsumerListener slowConsumerListener, final ExecutorService topicPollingExecutor) {
    return new BroadcastMessageListenerWrapper(DEFAULT_LISTENER_QUEUE_SIZE, slowConsumerListener, topicPollingExecutor);
  }
}
