package cern.c2mon.client.ext.dynconfig.query;

import cern.c2mon.client.ext.dynconfig.config.Protocols;

/**
 * An interface for protocol-dependant query objects.
 */
public interface IQueryObj {

    /**
     * Returns the tag description stored in the query object. By default, the tag name
     * is equal to the item name returned by getItemName().
     * @return the tag name stored in the query object
     */
    String getTagName();

    /**
     * Returns the tag description stored in the query object. By default, this is an empty String.
     * @return the tag description stored in the query object
     */
    String getTagDescription();

    /**
     * Returns the item name stored in the query object. This is a mandatory non-empty field that must be
     * set in the uri that the query object was parsed from.
     * @return the item name stored in the query object
     */
    String getItemName();

    /**
     * The data type of the values that the C2MON data tag would after creation.
     * @return the data type stored in the query object
     */
    Class<?> getDataType();

    /**
     * A query object defined a hardware address that is addressed by a specific protocol
     * @return the protocol that can be used to target the hardware address
     */
    Protocols getProtocol();

    /**
     * Checks whether a regular expression matches any of relevant fields of the IQueryObj. The specific
     * fields to match the regex to are specific to the implementation of the query objects.
     * @param regex the regular to match with the implementation-specific relevant fields
     * @return whether the regex matches any of the implementation-specific relevant fields.
     */
    boolean matches(String regex);

}
