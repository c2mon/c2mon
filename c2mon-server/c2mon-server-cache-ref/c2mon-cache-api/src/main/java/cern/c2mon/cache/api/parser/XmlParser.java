package cern.c2mon.cache.api.parser;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.server.common.device.AbstractList;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * <P> - Property class
 * <C> - Command class
 *
 * @author Szymon Halastra
 */
public abstract class XmlParser {

  /**
   * Parse the XML representation of the properties of a class (which
   * comes from configuration) and return it as a list of properties.
   *
   * @param xmlString the XML representation string of the device class
   *                  properties
   *
   * @return the list of properties
   * @throws Exception if the XML could not be parsed
   */
  public static <P, T extends AbstractList> List<P> parseXmlProperties(String xmlString, Class<T> clazz) {
    try {
      List<P> properties = new ArrayList<>();

      Serializer serializer = new Persister();
      T list = serializer.read(clazz, xmlString);

      properties.addAll(list.getList());

      return properties;
    }
    catch (Exception e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Exception: Unable to create "
              + clazz.getSimpleName() + " from parameter \"deviceProperties\": " + e + "\n" + xmlString);
    }
  }

  /**
   * Parse the XML representation of the commands of a class (which comes
   * from configuration) and return it as a list of commands.
   *
   * @param xmlString the XML representation string of the device class commands
   *
   * @return the list of commands
   * @throws Exception if the XML could not be parsed
   */
  public static <C, T extends AbstractList> List<C> parseXmlCommands(String xmlString, Class<T> clazz) {
    try {
      List<C> commands = new ArrayList<>();

      Serializer serializer = new Persister();

      T list = serializer.read(clazz, xmlString);

      commands.addAll(list.getList());

      return commands;
    }
    catch (Exception e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
              "Exception: Unable to create " + clazz.getSimpleName() + " from parameter \"commands\": " + e + ":\n" + xmlString);
    }
  }
}
