package cern.c2mon.server.elasticsearch.structure.types;

/**
 * @author Justin Lewis Salmon
 */
public class Types {

  public static String of(String dataType) {
    String type = dataType.toLowerCase();

    if (dataType.contains(".")) {
      type = type.substring(type.lastIndexOf('.') + 1);
    }

    return "type_" + type;
  }
}
