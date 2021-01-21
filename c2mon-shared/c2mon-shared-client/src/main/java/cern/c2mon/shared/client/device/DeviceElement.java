package cern.c2mon.shared.client.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

/**
 * Interface defining a DeviceProperty, PropertyField or DeviceCommand. Each DeviceElement must correspond to a
 * {@link DeviceClassElement} by ID and name.
 */
public abstract class DeviceElement implements Cloneable, Serializable {

    private static final long serialVersionUID = -6865580729113685166L;

    /**
     * The category of the command, property or field. The constants are spelt as they are stored in the database.
     */
    enum Category { tagId, commandTagId, clientRule, constantValue, mappedProperty }

    /**
     * The unique ID of the parent command, property or field.
     */
    @Attribute
    @Setter
    @Getter
    protected Long id;

    /**
     * The unique name of the parent command, property or field to whom the device element gives values.
     */
    @Attribute
    @Getter
    protected String name;

    /**
     * The real value of this device command, device property or property field.
     */
    @Element(required = false, name = "value")
    @Getter
    @Setter
    protected String value;

    /**
     * The category of this device command, device property or property field.
     */
    @Element(required = false, name = "category")
    protected Category category = Category.tagId;


    /**
     * The resulting value type of this device command, device property or property field. Defaults to String.
     */
    @Element(required = false, name = "result-type")
    protected ResultType resultType = ResultType.String;

    @JsonProperty("result-type")
    public String getResultType() {
        return resultType.name();
    }

    @JsonProperty("result-type")
    public void setResultType(String label) {
        resultType = ResultType.getOrDefault(label);
    }

    /**
     * Get the String name of the category instead of the enum for reasons of serialization.
     * @return the category name as it shall be saved in the database.
     */
    public String getCategory() {
        return category.toString();
    }

    public void setCategory(String category) {
        Optional<Category> matchingCategory = Arrays.stream(Category.values())
                .filter(c -> c.toString().equalsIgnoreCase(category))
                .findFirst();
        if (matchingCategory.isPresent()) {
            this.category = matchingCategory.get();
        } else {
            throw new IllegalArgumentException("No category found for name " + category + ".");
        }
    }
}
