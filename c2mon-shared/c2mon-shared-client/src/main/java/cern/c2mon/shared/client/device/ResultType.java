package cern.c2mon.shared.client.device;


import lombok.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The possible result types of the property. This is relevant for rules and constant values. See V20_REF_CODES in
 * the TIM reference DB.
 */
@AllArgsConstructor
public enum ResultType {
    BOOLEAN("Boolean", Boolean.class),
    DOUBLE("Double", Double.class),
    FLOAT("Float", Float.class),
    INTEGER("Integer", Integer.class),
    LONG("Long", Long.class),
    NUMERIC("Numeric", Number.class),
    STRING("String", String.class);

    private static final Map<String, ResultType> BY_LABEL = new ConcurrentHashMap<>();

    static {
        for (ResultType e: values()) {
            BY_LABEL.put(e.label, e);
        }
    }

    /**
     *  The enum values as stored in the database.
     */
    @Getter
    private final String label;

    /**
     * The java class corresponding to the type.
     */
    Class<?> resultClass;

    /**
     * Fetch the result type by its label
     * @param label the label of the result type
     * @return the result type corresponding to the label it it exists, or null if no match could be found.
     */
    public static ResultType getByLabel(String label) {
        return label == null ?  null : BY_LABEL.get(label);
    }

}
