package cern.c2mon.shared.client.device;

/**
 * Interface defining a DeviceProperty, PropertyField or DeviceCommand.
 */
public interface DeviceClassElement {

    /**
     * Get the unique ID of the Device Class element.
     *
     * @return the name of the element
     */
    Long getId();

    /**
     * Sets the unique ID of the Device Class element.
     *
     * @param id the name of the element
     */
    void setId(Long id);

    /**
     * Get the unique name of the Device Class element.
     *
     * @return the name of the element
     */
    String getName();

    /**
     * Get the description of the Device Class element.
     *
     * @return the description of the element
     */
    String getDescription();
}
