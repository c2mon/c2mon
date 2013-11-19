package cern.c2mon.shared.common.type;


/**
 * Helper class for converting numeric representations of TIM data types
 * to String representations and vice versa.
 * @author J. Stowisek
 * @version $Revision: 1.5 $ ($Date: 2005/02/01 17:05:16 $ - $State: Exp $)
 */
public class TagDataType {

  // ----------------------------------------------------------------------------
  // String representation of the TIM data types
  // ----------------------------------------------------------------------------
  private static final String[] TIM_DATA_TYPES = new String[] {
    "Boolean", "Integer", "Float", "Double", "String", "Long"
  };

  // ----------------------------------------------------------------------------
  // Numeric representation of the TIM data types
  // ----------------------------------------------------------------------------
  public static final int TYPE_BOOLEAN = 0;
  public static final int TYPE_INTEGER = 1;
  public static final int TYPE_FLOAT = 2;
  public static final int TYPE_DOUBLE = 3;
  public static final int TYPE_STRING = 4;    
  public static final int TYPE_LONG = 5;    

  /**
   * Check whether a certain data type is a valid TIM data type.
   */
  public static boolean isValidDataType(String dataTypeString) {
    // Check if some dumb user hasn't passed a null String as a parameter
    if (dataTypeString == null) {
      return false;
    }
  
    for (int i = 0; i < TIM_DATA_TYPES.length; i++) {
      if (TIM_DATA_TYPES[i].equalsIgnoreCase(dataTypeString)) {
        return true;
      }
    }

    // String wasn't found --> the String is not a valid data type name
    return false;
  }

  /**
   * Check whether a certain data type is a valid TIM data type.
   */
  public static boolean isValidDataType(int dataTypeNumeric) {
    return (dataTypeNumeric > -1 && dataTypeNumeric < TIM_DATA_TYPES.length);
  }

  /**
   * Get the numeric representation of a TIM data type for its String
   * representation. 
   * If the specified String is not a valid TIM data type,
   * the method will return -1.
   */
  public static int getDataTypeNumeric(String dataTypeString) {
    // Check if some dumb user hasn't passed a null String as a parameter
    if (dataTypeString == null) {
      return -1;
    }
  
    for (int i = 0; i < TIM_DATA_TYPES.length; i++) {
      if (TIM_DATA_TYPES[i].equalsIgnoreCase(dataTypeString)) {
        return i;
      }
    }

    // String wasn't found --> the String is not a valid data type name
    return -1;
  }

  /**
   * Get the String representation of a TIM data type for its numeric
   * representation. 
   * If the specified number is not a valid TIM data type, the method will 
   * return "UNKNOWN"
   */
  public static String getDataTypeString(int dataTypeNumeric) {
    if (!isValidDataType(dataTypeNumeric)) {
      return "UNKNOWN";
    } else {
      return TIM_DATA_TYPES[dataTypeNumeric];
    }
  }

  /**
   * Unit TEST for this class
   */
  public static void main(String[] args) {
    System.out.println("Is the String \"Boolean\" a valid data type?");
    System.out.println(TagDataType.isValidDataType("Boolean"));
    System.out.println("Is the numeric Boolean type a valid data type?");
    System.out.println(TagDataType.isValidDataType(TagDataType.TYPE_BOOLEAN));
    System.out.println(
        "Are the numeric and the String representation of Boolean equivalent?");
    System.out.println(
        TagDataType.getDataTypeNumeric(
            TagDataType.getDataTypeString(TagDataType.TYPE_BOOLEAN))
                == TagDataType.TYPE_BOOLEAN);

    System.out.println("Is the String \"Integer\" a valid data type?");
    System.out.println(TagDataType.isValidDataType("Integer"));
    System.out.println("Is the numeric Integer type a valid data type?");
    System.out.println(TagDataType.isValidDataType(TagDataType.TYPE_INTEGER));
    System.out.println(
        "Are the numeric and the String representation of Integer equivalent?");
    System.out.println(
        TagDataType.getDataTypeNumeric(
            TagDataType.getDataTypeString(TagDataType.TYPE_INTEGER))
                == TagDataType.TYPE_INTEGER);

    System.out.println("Is the String \"Float\" a valid data type?");
    System.out.println(TagDataType.isValidDataType("Float"));
    System.out.println("Is the numeric Float type a valid data type?");
    System.out.println(TagDataType.isValidDataType(TagDataType.TYPE_FLOAT));
    System.out.println(
        "Are the numeric and the String representation of Float equivalent?");
    System.out.println(
        TagDataType.getDataTypeNumeric(
            TagDataType.getDataTypeString(TagDataType.TYPE_FLOAT))
                == TagDataType.TYPE_FLOAT);

    System.out.println("Is the String \"Double\" a valid data type?");
    System.out.println(TagDataType.isValidDataType("Double"));
    System.out.println("Is the numeric Double type a valid data type?");
    System.out.println(TagDataType.isValidDataType(TagDataType.TYPE_DOUBLE));
    System.out.println(
        "Are the numeric and the String representation of Double equivalent?");
    System.out.println(
        TagDataType.getDataTypeNumeric(
            TagDataType.getDataTypeString(TagDataType.TYPE_DOUBLE))
                == TagDataType.TYPE_DOUBLE);

    System.out.println("Is the String \"String\" a valid data type?");
    System.out.println(TagDataType.isValidDataType("String"));
    System.out.println("Is the numeric String type a valid data type?");
    System.out.println(TagDataType.isValidDataType(TagDataType.TYPE_STRING));
    System.out.println(
        "Are the numeric and the String representation of String equivalent?");
    System.out.println(
        TagDataType.getDataTypeNumeric(
            TagDataType.getDataTypeString(TagDataType.TYPE_STRING))
                == TagDataType.TYPE_STRING);

    System.out.println("Is the String \"Long\" a valid data type?");
    System.out.println(TagDataType.isValidDataType("Long"));
    System.out.println("Is the numeric Long type a valid data type?");
    System.out.println(TagDataType.isValidDataType(TagDataType.TYPE_LONG));
    System.out.println(
        "Are the numeric and the Long representation of String equivalent?");
    System.out.println(
        TagDataType.getDataTypeNumeric(
            TagDataType.getDataTypeString(TagDataType.TYPE_LONG))
                == TagDataType.TYPE_LONG);

    String pattern = "\\*|([*a-zA-Z0-9_]+(\\.[*a-zA-Z0-9_]+)*)(\\,([*a-zA-Z0-9_]+(\\.[*a-zA-Z0-9_]+)*))*";

    System.out.println(new String("*").matches(pattern));
    System.out.println(new String("tcrpl1").matches(pattern));
    System.out.println(new String("tcrpl1.cern.ch").matches(pattern));
    System.out.println(new String("tcrpl1.cern.*").matches(pattern));
    System.out.println(new String("tcrpl1.*.ch").matches(pattern));
    System.out.println(new String("*.cern.ch").matches(pattern));
    System.out.println(new String("*.*.ch").matches(pattern));
    System.out.println(new String("tcrpl1,tcrpl2").matches(pattern));
    System.out.println(
        new String("tcrpl1.cern.ch,tcrpl2.cern.ch").matches(pattern));
    System.out.println(
        new String("tcrpl1.cern.ch,tcrpl2.cern.ch,tcrpl3.cern.ch").matches(
            pattern));
    System.out.println(new String("tcrpl*.cern.ch,pcst*").matches(pattern));
    System.out.println("---");
    System.out.println(new String(".tcrpl1.cern.ch").matches(pattern));
    System.out.println(new String("tcrpl1.cern.").matches(pattern));
    System.out.println(new String("tcrpl1..cern.ch").matches(pattern));
    System.out.println(
        new String("tcrpl1.cern.ch,tcrpl2.cern.ch,").matches(pattern));
    System.out.println(
        new String(",tcrpl1.cern.ch,tcrpl2.cern.ch").matches(pattern));
    System.out.println(
        new String("tcrpl1.cern.ch,,tcrpl2.cern.ch").matches(pattern));
  }
}
