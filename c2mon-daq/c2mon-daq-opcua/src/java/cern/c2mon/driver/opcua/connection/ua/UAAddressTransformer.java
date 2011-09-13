package cern.c2mon.driver.opcua.connection.ua;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to transform classic addresses to UA addresses if necessary.
 * 
 * @author Andreas Lang
 *
 */
public final class UAAddressTransformer {
    
    /**
     * Number of the matching group for the bit number in the Siemens regular
     * expression.
     */
    private static final int SIEMENS_BIT_NUMBER_GROUP = 5;

    /**
     * Number of the matching group for the datatype in the Siemens regular
     * expression.
     */
    private static final int SIEMENS_DATATYPE_GROUP = 3;

    /**
     * Number of the matching group for the byte number in the Siemens regular
     * expression.
     */
    private static final int SIEMENS_BYTE_NUMBER_GROUP = 4;

    /**
     * Number of the matching group for the in the Siemens regular expression.
     */
    private static final int SIEMENS_DATABASE_GROUP = 2;

    /**
     * Number of the matching group for the host in the Siemens regular
     * expression.
     */
    private static final int SIEMENS_CONNECTION_GROUP = 1;
    
    /**
     * Minimum number of groups in the Siemens regular expression.
     */
    private static final int SIEMENS_GROUP_COUNT = 4;

    /**
     * Siemens classic address regular expression.
     * Address looks like: S7:[@LOCALSERVER]DB1,INT1.8
     * With round brackets for matching groups: S7:[(@LOCALSERVER)](DB1),(INT)(1).(8)
     */
    private static final String SIEMENS_REGEX =
        "S7:\\[(@.+)\\](\\w+),([a-zA-Z]+)([\\d]+)[\\.]?(\\d)*";
    
    /**
     * Compiled pattern for the regular expression.
     */
    private static final Pattern SIEMENS_PATTERN =
        Pattern.compile(SIEMENS_REGEX);

    /**
     * The localhost string used in classic OPC DA.
     */
    private static final String LOCALHOST = "@LOCALSERVER";

    /**
     * Private constructor. There should be no instances of this class.
     */
    private UAAddressTransformer() { }

    /**
     * Transforms the provided OPC address name if a transformation is required.
     * 
     * @param address The address to transform.
     * @return The transformed address.
     */
    public static String transform(final String address) {
        String resultingItemName;
        if (matchesSiemensClassic(address))
            resultingItemName = transformSiemensClassic(address);
        else
            resultingItemName = address;
        return resultingItemName;
    }

    /**
     * Transforms a classic OPC address to an OPC UA address. If the provided
     * address is not a classic OPC address the provided address will be
     * returned.
     * 
     * @param address The address to transform.
     * @return The transformed address.
     */
    public static String transformSiemensClassic(
            final String address) {
        Matcher matcher = SIEMENS_PATTERN.matcher(address);
        StringBuffer transformedAddress = new StringBuffer();
        if (matcher.find()) {
            transformedAddress.append("s=");
            String connection = matcher.group(SIEMENS_CONNECTION_GROUP);
            if (connection.equals(LOCALHOST))
                transformedAddress.append(connection.toLowerCase() + ".");
            else
                transformedAddress.append(connection + ".");
            String database = matcher.group(SIEMENS_DATABASE_GROUP);
            transformedAddress.append(database.toLowerCase() + ".");
            String byteNumber = matcher.group(SIEMENS_BYTE_NUMBER_GROUP);
            transformedAddress.append(byteNumber);
            String dataType = matcher.group(SIEMENS_DATATYPE_GROUP);
            transformedAddress.append("," + transformSiemensDataType(dataType));
            if (matcher.groupCount() > SIEMENS_GROUP_COUNT) {
                String bitNumber = matcher.group(SIEMENS_BIT_NUMBER_GROUP);
                transformedAddress.append(bitNumber);
            }
        }
        else {
            transformedAddress.append(address);
        }
        return transformedAddress.toString();
    }

    /**
     * Transforms a Siemens classic data type string to an Siemens UA data type
     * String.
     * 
     * @param dataTypeClassic The data type extension to convert.
     * @return The transformed data type.
     */
    public static String transformSiemensDataType(
            final String dataTypeClassic) {
        String dataTypeUA;
        if ("INT".equals(dataTypeClassic)) {
            dataTypeUA = "i";
        }
        else {
            dataTypeUA = dataTypeClassic.toLowerCase();
        }
        return dataTypeUA;
    }

    /**
     * Checks if a provided address matches a classic Siemens OPC address.
     * 
     * @param address The address to match.
     * @return True if the address is a classic Siemens OPC UA address else false.
     */
    public static boolean matchesSiemensClassic(final String address) {
        return matches(address, SIEMENS_REGEX);
    }
    
    /**
     * Returns true if the provided address matches a regular expression.
     * 
     * @param address The address to match.
     * @param regex The regular expression to use.
     * @return True if the address matches the regular expression else false.
     */
    public static boolean matches(
            final String address, final String regex) {
        return address.matches(regex);
    }
    
    

}
