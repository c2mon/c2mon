package cern.c2mon.server.client.publish;

import cern.c2mon.server.client.config.ClientProperties;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class TopicProvider {

  public static String topicFor(Tag tag, ClientProperties properties) {
    String trunk = properties.getJms().getTagTopicPrefix();

    if (tag instanceof ControlTag) {
      return properties.getJms().getControlTagTopic();
    } else if (tag instanceof RuleTag) {
      return trunk + "." + ((RuleTag) tag).getLowestProcessId();
    } else {
      return trunk + "." + ((DataTag) tag).getProcessId();
    }
  }
}
