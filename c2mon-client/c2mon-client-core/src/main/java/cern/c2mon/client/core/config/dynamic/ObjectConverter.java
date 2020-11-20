package cern.c2mon.client.core.config.dynamic;

import cern.c2mon.shared.common.datatag.address.OPCCommandHardwareAddress;
import cern.c2mon.shared.common.type.TypeConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utility class to convert a an argument into different classes using custom conversion methods. As this class is
 * called using query argument values, in effect it must only convert Strings. The class extends {@link TypeConverter}
 * by Tag-specific data formats.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ObjectConverter {

    private static final Map<String, Function<String, ?>> CONVERTERS = new ConcurrentHashMap<>();

    static {
        CONVERTERS.put(OPCCommandHardwareAddress.COMMAND_TYPE.class.getName(), ObjectConverter::toCommandType);
        CONVERTERS.put(Class.class.getName(), ObjectConverter::toDataType);
        CONVERTERS.put(Number.class.getName(), Integer::valueOf);
    }

    /**
     * Utility class to convert an Object to convert into a type of class clazz. If that is not possible, throw an unchecked exception.
     *
     * @param clazz     the class to convert the Object into
     * @param toConvert the object to convert
     * @param <T>       the generic type of class clazz
     * @return the object converted to class clazz.
     */
    public static <T> T convert(Class<T> clazz, Object toConvert) {
        if (toConvert == null) {
            throw new ClassCastException("Cannot convert null.");
        }
        if (clazz.isAssignableFrom(toConvert.getClass())) {
            return clazz.cast(toConvert);
        }
        // classes requiring special treatment
        if (CONVERTERS.containsKey(clazz.getName()) && toConvert instanceof String) {
            return (T) CONVERTERS.get(clazz.getName()).apply((String) toConvert);
        }
        return TypeConverter.castToType(toConvert, clazz);
    }

    private static OPCCommandHardwareAddress.COMMAND_TYPE toCommandType(String s) {
        if ("method".equalsIgnoreCase(s)) {
            return OPCCommandHardwareAddress.COMMAND_TYPE.METHOD;
        } else if ("classic".equalsIgnoreCase(s)) {
            return OPCCommandHardwareAddress.COMMAND_TYPE.CLASSIC;
        }
        throw new UnsupportedOperationException("Cannot convert " + s + " to command type. Please use METHOD or CLASSIC.");
    }

    private static Class<?> toDataType(String s) {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            log.error("Cannot infer dataType {} due. Resorting to java.lang.Object.", s, e);
            return Object.class;
        }
    }
}
