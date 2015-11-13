package cern.c2mon.server.configuration.parser;

import java.util.List;

import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;

/**
 * This class holds all information about a Configoration object to translate
 * this object in List of ConfigurationElement.
 * 
 * @author Franz Ritter
 *
 */
public interface ConfigurationParser {
  /**
   * parsing the given Configuration object of this class and collects all Data
   * to Create the list of ConfigurationElement. Since all taks need do handle
   * in a specific order the list compareable depending to this order.
   */
  List<ConfigurationElement> parse(Configuration configuration);
}
