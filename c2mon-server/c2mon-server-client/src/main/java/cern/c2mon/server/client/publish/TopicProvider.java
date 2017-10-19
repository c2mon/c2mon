package cern.c2mon.server.client.publish;

import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.expression.ExpressionCacheObject;
import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Justin Lewis Salmon
 */
public class TopicProvider {

  public static String topicFor(Tag tag, ClientProperties properties) {
    String trunk = properties.getJms().getTagTopicPrefix();

    if (tag instanceof ControlTag) {
      return properties.getJms().getControlTagTopic();
    } else if (tag instanceof RuleTagCacheObject) {
      return trunk + "." + ((RuleTagCacheObject) tag).getLowestProcessId();
    } else if (tag instanceof ExpressionCacheObject) {
      return trunk + "." + ((ExpressionCacheObject) tag).getId();
    } else {
      return trunk + "." + ((DataTag) tag).getProcessId();
    }
  }
}
