package cern.c2mon.server.configuration.parser.tasks;

import cern.c2mon.server.configuration.parser.tasks.util.TaskOrder;
import cern.c2mon.shared.client.configuration.ConfigurationElement;

/**
 * Wrapper class for all {@link ConfigurationElement}s which add a field for ordering.
 * SequenceTask provide the possibility of ordering all ConfigurationElements based on the
 * behavior of the ConfigurationElement.
 * @author Franz Ritter
 *
 */
public class SequenceTask implements Comparable<SequenceTask> {
  private TaskOrder order;
  private ConfigurationElement configurationElement;

  protected SequenceTask(ConfigurationElement configurationElement, TaskOrder order){
    this.configurationElement = configurationElement;
    this.order = order;
  }

  public int getOrder() {
    return order.getOrder();
  }

  @Override
  public int compareTo(SequenceTask o) {
    return (new Integer(o.getOrder()).compareTo(this.getOrder()) * -1);
  }

  public ConfigurationElement getConfigurationElement() {
    return configurationElement;
  }
}
