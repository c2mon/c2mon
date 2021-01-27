package cern.c2mon.shared.client.device;

import lombok.*;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

import java.io.Serializable;

/**
 * Interface defining a DeviceProperty, PropertyField or DeviceCommand. Each DeviceElement must correspond to a
 * {@link DeviceClassElement} by ID and name.
 */
@Getter
public abstract class DeviceElement implements Cloneable, Serializable {

    private static final long serialVersionUID = -6865580729113685166L;

    /**
     * The possible categories of device properties, device commands or property fields
     */
    @AllArgsConstructor
    protected enum Category {
        TAG_ID("tagId"),
        COMMAND_TAG_ID("commandTagId"),
        CLIENT_RULE("clientRule"),
        CONSTANT_VALUE("constantValue"),
        MAPPED_PROPERTY("mappedProperty");

        /**
         * The category name in the nomenclature of the database.
         */
        protected String label;
    }

    /**
     * The unique ID of the parent command, property or field.
     */
    @Attribute
    @Setter
    protected Long id;

    /**
     * The unique name of the parent command, property or field to whom the device element gives values.
     */
    @Attribute
    protected String name;

    /**
     * The real value of this device command, device property or property field.
     */
    @Element(required = false, name = "value")
    @Setter
    protected String value;

    /**
     * The category of this device command, device property or property field.
     */
    @Element(required = false, name = "category")
    @Setter
    protected String category;

    /**
     * The resulting value type of this device command, device property or property field. Defaults to String.
     */
    @Element(required = false, name = "result-type")
    @Convert(ResultTypeConverter.class)
    @Setter
    protected ResultType resultType = ResultType.STRING;

}
