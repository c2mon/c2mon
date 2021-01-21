package cern.c2mon.shared.client.device;


import lombok.*;

import java.util.Arrays;

/**
 * The possible result types of the property. This is relevant for rules and constant values. See V20_REF_CODES in
 * the TIM reference DB. The enum values are spelt as they are stored in the database.
 */
@AllArgsConstructor
public enum ResultType {
    Boolean(Boolean.class),
    Double(Double.class),
    Float(Float.class),
    Integer(Integer.class),
    Long(Long.class),
    Numeric(Number.class),
    String(String.class);

    Class<?> resultClass;

    /**
     * Fetch the result type by its name, or fall back to the default result type 'String', if the name does not
     * correspond to a result type.
     * @param name the name of the result type
     * @return the result type corresponding to the name, or the default result type 'String' if none could be found.
     */
    public static ResultType getOrDefault(String name) {
        return Arrays.stream(ResultType.values())
                .filter(p -> p.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(String);
    }

}
