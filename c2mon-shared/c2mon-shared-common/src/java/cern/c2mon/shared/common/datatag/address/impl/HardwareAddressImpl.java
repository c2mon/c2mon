package cern.c2mon.shared.common.datatag.address.impl;

import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.util.parser.SimpleXMLParser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of the HardwareAddress interface and of the abstract HardwareAddressFactory class.
 * 
 * @author J. Stowisek
 * @version $Revision: 1.4 $ ($Date: 2009/04/02 16:54:50 $ - $State: Exp $)
 */

public class HardwareAddressImpl extends HardwareAddressFactory implements HardwareAddress {

    /** Serial UID */
    private static final long serialVersionUID = -7336624461787666236L;

    private static final Logger LOG = Logger.getLogger(HardwareAddressImpl.class);

    private static SimpleXMLParser xmlParser = null;

    // ----------------------------------------------------------------------------
    // Private utility methods
    // ----------------------------------------------------------------------------

    /**
     * Decodes a field name from XML notation (e.g. my-field-name) to a valid Java field name (e.g. myFieldName)
     */
    private static final String decodeFieldName(final String pXmlFieldName) {
        // StringBuilder for constructing the resulting field name
        StringBuilder str = new StringBuilder();
        // Number of characters in the XML-encoded field name
        int fieldNameLength = pXmlFieldName.length();

        char currentChar;
        for (int i = 0; i < fieldNameLength; i++) {
            currentChar = pXmlFieldName.charAt(i);
            if (currentChar == '-') {
                str.append(Character.toUpperCase(pXmlFieldName.charAt(++i)));
            } else {
                str.append(currentChar);
            }
        }
        return str.toString();
    }

    /**
     * Encodes a field name in Java notation (e.g. myFieldName) to an XML field name (e.g. my-field-name).
     */
    private final String encodeFieldName(final String pFieldName) {
        // StringBuilder for constructing the resulting XML-encoded field name
        StringBuilder str = new StringBuilder();
        // Number of characters in the field name
        int fieldNameLength = pFieldName.length();

        char currentChar;
        for (int i = 0; i != fieldNameLength; i++) {
            currentChar = pFieldName.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                str.append('-');
                str.append(Character.toLowerCase(currentChar));
            } else {
                str.append(currentChar);
            }
        }
        return str.toString();
    }

    // ----------------------------------------------------------------------------
    // Implementation of the HardwareAddressFactory abstract methods
    // ----------------------------------------------------------------------------

    /**
     * Create a HardwareAddress object from its XML representation.
     * 
     * @param pElement DOM element containing the XML representation of a HardwareAddress object, as created by the
     *            toConfigXML() method.
     * @see cern.c2mon.shared.common.datatag.address.HardwareAddress#toConfigXML(Element)
     * @throws RuntimeException if unable to instantiate the Hardware address
     */
    public final synchronized HardwareAddress fromConfigXML(Element pElement) {
        Class hwAddressClass = null;
        HardwareAddressImpl hwAddress = null;

        try {
            hwAddressClass = Class.forName(pElement.getAttribute("class"));
            hwAddress = (HardwareAddressImpl) hwAddressClass.newInstance();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            throw new RuntimeException("Exception caught when instantiating a hardware address from XML", cnfe);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            throw new RuntimeException("Exception caught when instantiating a hardware address from XML", iae);
        } catch (InstantiationException ie) {
            ie.printStackTrace();
            throw new RuntimeException("Exception caught when instantiating a hardware address from XML", ie);
        }

        NodeList fields = pElement.getChildNodes();
        Node fieldNode = null;
        int fieldsCount = fields.getLength();
        String fieldName;
        String fieldValueString;
        String fieldTypeName = "";

        for (int i = 0; i < fieldsCount; i++) {
            fieldNode = fields.item(i);
            if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
                fieldName = fieldNode.getNodeName();
                if (fieldNode.getFirstChild() != null)
                    fieldValueString = fieldNode.getFirstChild().getNodeValue();
                else
                    fieldValueString = "";

                try {
                    Field field = hwAddressClass.getDeclaredField(decodeFieldName(fieldName));
                    fieldTypeName = field.getType().getName();

                    if (fieldTypeName.equals("short")) {
                        field.setShort(hwAddress, Short.parseShort(fieldValueString));
                    } else if (fieldTypeName.equals("java.lang.Short")) {
                        field.set(hwAddress, new Integer(Integer.parseInt(fieldValueString)));
                    } else if (fieldTypeName.equals("int")) {
                        field.setInt(hwAddress, Integer.parseInt(fieldValueString));
                    } else if (fieldTypeName.equals("java.lang.Integer")) {
                        field.set(hwAddress, new Integer(Integer.parseInt(fieldValueString)));
                    } else if (fieldTypeName.equals("float")) {
                        field.setFloat(hwAddress, Float.parseFloat(fieldValueString));
                    } else if (fieldTypeName.equals("java.lang.Float")) {
                        field.set(hwAddress, new Float(Float.parseFloat(fieldValueString)));
                    } else if (fieldTypeName.equals("double")) {
                        field.setDouble(hwAddress, Double.parseDouble(fieldValueString));
                    } else if (fieldTypeName.equals("java.lang.Double")) {
                        field.set(hwAddress, new Double(Double.parseDouble(fieldValueString)));
                    } else if (fieldTypeName.equals("long")) {
                        field.setLong(hwAddress, Long.parseLong(fieldValueString));
                    } else if (fieldTypeName.equals("java.lang.Long")) {
                        field.set(hwAddress, new Long(Long.parseLong(fieldValueString)));
                    } else if (fieldTypeName.equals("byte")) {
                        field.setByte(hwAddress, Byte.parseByte(fieldValueString));
                    } else if (fieldTypeName.equals("java.lang.Byte")) {
                        field.set(hwAddress, new Byte(Byte.parseByte(fieldValueString)));
                    } else if (fieldTypeName.equals("char")) {
                        field.setChar(hwAddress, fieldValueString.charAt(0));
                    } else if (fieldTypeName.equals("java.lang.Character")) {
                        field.set(hwAddress, new Character(fieldValueString.charAt(0)));
                    } else if (fieldTypeName.equals("boolean")) {
                        field.setBoolean(hwAddress, Boolean.getBoolean(fieldValueString));
                    } else if (fieldTypeName.equals("java.lang.Boolean")) {
                        field.set(hwAddress, new Boolean(Boolean.getBoolean(fieldValueString)));
                    } else if (field.getType().isEnum()) {
                        Object[] enumConstants = field.getType().getEnumConstants();
                        for (Object enumConstant : enumConstants) {
                            if (enumConstant.toString().equals(fieldValueString)) {
                                field.set(hwAddress, enumConstant);
                            }
                        }
                    } else {
                        field.set(hwAddress, fieldValueString);
                    }
                } catch (NoSuchFieldException nsfe) {
                    LOG.error("fromConfigXML(...) - Error occured while parsing XML <HardwareAddress> tag. "
                            + "The following variable does not exist in " + hwAddressClass.toString() + ": \""
                            + decodeFieldName(fieldName) + "\"");
                } catch (IllegalAccessException iae) {
                    iae.printStackTrace();
                } catch (NumberFormatException npe) {
                    LOG.error("fromConfigXML(...) - Error occured while parsing XML <HardwareAddress> tag. Field \""
                            + fieldName + "\" shall not be empty since we expect a \"" + fieldTypeName
                            + "\" value. Please correct the XML configuration for " + hwAddressClass.toString());
                } catch (Exception e) {
                    LOG.error("fromConfigXML(...) - Error occured while parsing XML <HardwareAddress> tag. Field \""
                            + fieldName + "\" could not be parsed, should be \"" + fieldTypeName
                            + "\" value. Please correct the XML configuration for " + hwAddressClass.toString());
                }
            }
        }
        // Return the fully configured HardwareAddress object
        return hwAddress;
    }

    public final synchronized HardwareAddress fromConfigXML(final String pXML) {
        try {
            if (xmlParser == null) {
                xmlParser = new SimpleXMLParser();
            }
            return fromConfigXML(xmlParser.parse(pXML).getDocumentElement());
        } catch (Exception e) {
            return null;
        }
    }

    public final synchronized HardwareAddress fromConfigXML(final Document pDocument) {
        return fromConfigXML(pDocument.getDocumentElement());
    }

    /**
     * Returns an XML representation of the HardwareAddress object.
     * 
     * @throws RuntimeException if Illegal access to fields
     */
    public final synchronized String toConfigXML() {
        Class handlerClass = this.getClass();
        Field[] fields = handlerClass.getDeclaredFields();

        StringBuilder str = new StringBuilder();

        /* Open the <HardwareAddress> tag */
        str.append("        <HardwareAddress class=\"");
        str.append(getClass().getName());
        str.append("\">\n");

        for (int i = 0; i < fields.length; i++) {
            if (Modifier.isProtected(fields[i].getModifiers()) && !Modifier.isFinal(fields[i].getModifiers())) {
                try {
                    if (fields[i].get(this) != null) {
                        String fieldXMLName = encodeFieldName(fields[i].getName());

                        str.append("          <");
                        str.append(fieldXMLName);
                        str.append(">");
                        try {
                            str.append(fields[i].get(this));
                        } catch (IllegalAccessException iae) {
                            iae.printStackTrace();
                        }
                        str.append("</");
                        str.append(fieldXMLName);
                        str.append(">\n");
                    }
                } catch (IllegalAccessException iae) {
                    iae.printStackTrace();
                    throw new RuntimeException("Exception caught while converting HardwareAddress to XML.", iae);
                }
            }
        }

        /* Close the </HardwareAddress> tag */
        str.append("        </HardwareAddress>\n");
        return str.toString();
    }

    @Override
    public HardwareAddressImpl clone() {
        try {
            return (HardwareAddressImpl) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception caught while cloning a HardwareAddress", e);
        }
    }

    public final String toConfigXML(HardwareAddress address) {
        return address.toConfigXML();
    }

    public void validate() throws ConfigurationException {
        //
    }

    /**
     * The two addresses are considered equals if they're of the same type and all their non-static attributes are equal
     */
    @Override
    public final boolean equals(final Object copy) {

        boolean result = copy != null && copy instanceof HardwareAddress && this.getClass().equals(copy.getClass());

        if (result) {

            Field[] fields = this.getClass().getDeclaredFields();

            for (Field field : fields) {
                // compare non-final, non-static and non-transient fields only
                if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())
                        && !Modifier.isTransient(field.getModifiers())) {
                    try {

                        if ((field.get(this) != null && field.get(copy) == null)
                                || (field.get(this) == null && field.get(copy) != null)) {
                            result = false;
                        } else if (field.get(this) != null && field.get(copy) != null) {

                            if (field.getType().isArray()) {

                                if (Object[].class.isAssignableFrom(field.getType())) {
                                    result = Arrays.equals((Object[]) field.get(this), (Object[]) field.get(copy));
                                } else {
                                    result = ArrayUtils.isEquals(field.get(this), field.get(copy));
                                }

                            } else {
                                result = field.get(this).equals(field.get(copy));
                            }
                        }
                    } catch (Exception e) {
                        result = false;
                    }
                }

                if (!result)
                    break;
            }// for
        }

        return result;
    }

    @Override
    public final int hashCode() {

        int result = 0;

        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            // compare non-final, non-static and non-transient fields only
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())
                    && !Modifier.isTransient(field.getModifiers())) {
                try {

                    // skip arrays
                    if (!field.getType().isArray() && field.get(this) != null)
                        // for string take its length
                        if (field.getType().equals(String.class)) {
                            result ^= ((String) field.get(this)).length();
                        } else if (field.getType().equals(short.class) || field.getType().equals(Short.class)) {
                            result ^= field.getShort(this);
                        } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                            result ^= field.getInt(this);
                        } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                            result ^= (int) field.getFloat(this);
                        } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                            result ^= (int) field.getDouble(this);
                        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                            result ^= (int) field.getLong(this);
                        } else if (field.getType().equals(byte.class) || field.getType().equals(Byte.class)) {
                            result ^= field.getByte(this);
                        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                            result ^= field.getBoolean(this) == Boolean.TRUE ? 1 : 0;
                        }

                } catch (Exception e) {
                    LOG.error(e);
                    throw new RuntimeException("Exception caught while calculating HardwareAddress hashcode.", e);
                }
            }

        }// for

        return result;
    }

}