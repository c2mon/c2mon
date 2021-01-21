package cern.c2mon.shared.client.device;

/**
 * Interface defining a DeviceProperty, PropertyField or DeviceCommand. Each DeviceElement must correspond to a
 * {@link DeviceClassElement} by ID and name.
 */
public interface DeviceElement {

    /**
     * Get the unique ID of the Device element.
     *
     * @return the name of the element
     */
    Long getId();

    /**
     * Sets the unique ID of the Device element.
     *
     * @param id the name of the element
     */
    void setId(Long id);

    /**
     * Fet the unique name of the Device element.
     *
     * @return the name of the element
     */
    String getName();

    /**
     * Set the unique name of the Device element.
     *
     * @param name the name of the element
     */
    void setName(String name);

    /**
     * Get the actual value of this Device element
     * @return the actual value of this Device element
     */
    String getValue();

    /**
     * Get the category of this Device element (e.g "tagId", "clientRule", "constantValue", "commandTagId")
     * @return the category of the Device element
     */
    String getCategory();

    /**
     * Get the result type of this Device element
     * @return the result type of the Device element
     */
    String getResultType();
}
