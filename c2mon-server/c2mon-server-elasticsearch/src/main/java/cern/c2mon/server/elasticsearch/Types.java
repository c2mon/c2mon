package cern.c2mon.server.elasticsearch;

/**
 * Static utility singleton for working with Elasticsearch types.
 *
 * @author Justin Lewis Salmon
 */
public class Types {

  /**
   * Retrieve the corresponding Elasticsearch type of the given Java type.
   *
   * @param dataType the Java type (fully-qualified or simple)
   *
   * @return the corresponding Elasticsearch type
   */
  public static String of(String dataType) {
    String type = dataType.toLowerCase();

    if (dataType.contains(".")) {
      type = type.substring(type.lastIndexOf('.') + 1);
    }

    return "type_" + type;
  }
}
