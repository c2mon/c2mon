package cern.c2mon.shared.client.configuration.api.util;

/**
 * All assisted data types which are set in the datatype fields of instances of  {@link ConfigurationObject}.
 */
public enum DataType {

  STRING("String"),
  BOOLEAN("Boolean"),
  FLOAT("Float"),
  DOUBLE("Double"),
  SHORT("Short"),
  INTEGER("Integer"),
  LONG("Long");

  private final String string;

  DataType(String str){
    this.string = str;
  }

  @Override
  public String toString(){
    return string;
  }

}
