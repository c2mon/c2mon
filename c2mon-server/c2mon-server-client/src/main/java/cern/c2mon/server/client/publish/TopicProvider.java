package cern.c2mon.server.client.publish;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import org.springframework.core.env.Environment;

/**
 * @author Justin Lewis Salmon
 */
public class TopicProvider {

  public static String topicFor(Tag tag, Environment environment) {
    String trunk = environment.getRequiredProperty("c2mon.server.client.jms.topic.tag.trunk");

    if (tag instanceof ControlTag) {
      return environment.getRequiredProperty("c2mon.server.client.jms.topic.controltag");
    } else if (tag instanceof RuleTag) {
      return trunk + "." + ((RuleTag) tag).getLowestProcessId();
    } else {
      return trunk + "." + ((DataTag) tag).getProcessId();
    }
  }
}
