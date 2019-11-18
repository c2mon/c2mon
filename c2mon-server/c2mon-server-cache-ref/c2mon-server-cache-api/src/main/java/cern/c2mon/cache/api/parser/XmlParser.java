package cern.c2mon.cache.api.parser;

import cern.c2mon.server.common.device.AbstractList;
import cern.c2mon.shared.common.ConfigurationException;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Szymon Halastra, Alexandros Papageorgiou
 */
public abstract class XmlParser {

  /**
   * Parse the XML representation of the T contents of a class (which comes
   * from configuration) and return it as a list of T objects.
   *
   * @param xml the XML representation string of the device class commands
   * @return the list of commands
   * @throws ConfigurationException if the XML could not be parsed
   */
  public static <T, LIST_T extends AbstractList<T>> List<T> parse(String xml, Class<LIST_T> clazz) {
    try {
      Serializer serializer = new Persister();
      LIST_T list = serializer.read(clazz, xml);

      return new ArrayList<>(list.getList());
    } catch (Exception e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
        "Exception: Unable to create " + clazz.getSimpleName()
          + " from parameter \"deviceProperties\": " + e + "\n" + xml);
    }
  }
}
