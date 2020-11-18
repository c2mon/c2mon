package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.shared.common.datatag.address.OPCCommandHardwareAddress;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Utility class to convert a an argument into different classes using custom conversion methods. As this class is
 * called using query argument values, in effect it must only convert Strings.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ObjectConverter {

    private static final Map<String, Function<String, ?>> CONVERTERS = new ConcurrentHashMap<>();

    static {
        CONVERTERS.put(int.class.getName(), Integer::parseInt);
        CONVERTERS.put(Integer.class.getName(), Integer::valueOf);
        CONVERTERS.put(long.class.getName(), Long::parseLong);
        CONVERTERS.put(Long.class.getName(), Long::valueOf);
        CONVERTERS.put(boolean.class.getName(), Boolean::parseBoolean);
        CONVERTERS.put(Boolean.class.getName(), Boolean::valueOf);
        CONVERTERS.put(short.class.getName(), Short::parseShort);
        CONVERTERS.put(Short.class.getName(), Short::valueOf);
        CONVERTERS.put(Number.class.getName(), Integer::valueOf);
        CONVERTERS.put(OPCCommandHardwareAddress.COMMAND_TYPE.class.getName(), ObjectConverter::toCommandType);
        CONVERTERS.put(Class.class.getName(), ObjectConverter::toDataType);
    }

    /**
     * Utility class to convert an Object o into a type of class c. If that is not possible, throw an unchecked exception.
     * @param c the class to convert the Object into
     * @param o the object to convert
     * @param <T> the generic type of class c
     * @return the object converted to class c.
     */
    public static <T> T convert(Class<T> c, Object o) {
        if (o == null) {
            throw new UnsupportedOperationException("Cannot convert null.");
        }
        if (c.isAssignableFrom(o.getClass())) {
            return c.cast(o);
        }
        if (CONVERTERS.containsKey(c.getName()) && o instanceof String) {
            return (T) CONVERTERS.get(c.getName()).apply((String) o);
        }
        throw new UnsupportedOperationException("Conversion from " + o.getClass() +" to " + c + " is not supported.");
    }

    private static OPCCommandHardwareAddress.COMMAND_TYPE toCommandType(String s) {
        if ("method".equalsIgnoreCase(s)) {
            return OPCCommandHardwareAddress.COMMAND_TYPE.METHOD;
        } else if ("classic".equalsIgnoreCase(s)){
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
