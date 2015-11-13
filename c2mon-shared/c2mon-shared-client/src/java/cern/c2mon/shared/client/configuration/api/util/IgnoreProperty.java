package cern.c2mon.shared.client.configuration.api.util;

import java.lang.annotation.*;

/**
 * Annotation which tags a field of a class to be ignored by the parser of the {@link SequenceTaskFactory}.
 * This annotation is used by classes which extends {@link ConfigurationObject}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface IgnoreProperty {

}
