package cern.c2mon.shared.client.device;

import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Interface defining a DeviceProperty, PropertyField or DeviceCommand.
 */
public abstract class DeviceClassElement {

    /**
     * The unique ID of the property, command or field.
     */
    @Attribute
    @Getter
    @Setter
    protected Long id;

    /**
     * The name of the command, property or field. It must be unique within the DeviceClass.
     */
    @Attribute
    @Getter
    protected String name;

    /**
     * An optional description of the command, property or field.
     */
    @Element(required = false)
    @Getter
    protected String description;
}
