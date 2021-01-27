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
     * Fetch the result type by its label, or fall back to the default result type 'String', if the label does not
     * correspond to a result type.
     * @param label the label of the result type
     * @return the result type corresponding to the label, or the default result type 'String' if none could be found.
     */
    public static ResultType getOrDefault(String label) {
        if (label == null) {
            return STRING;
        }
        return BY_LABEL.getOrDefault(label, STRING);
    }

}
