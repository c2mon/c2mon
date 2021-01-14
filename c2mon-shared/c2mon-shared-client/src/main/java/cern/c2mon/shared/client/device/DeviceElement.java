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
     * Set the unique name of the Device element.
     *
     * @return the name of the element
     */
    String getName();

    /**
     * Get the unique name of the Device element.
     *
     * @param the name of the element
     */
    void setName(String name);
}
