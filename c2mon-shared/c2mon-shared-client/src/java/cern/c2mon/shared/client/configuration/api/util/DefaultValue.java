package cern.c2mon.shared.client.configuration.api.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which gives default value to a field of an  class.
 * The default will be received due the parsing by the {@link SequenceTaskFactory}.
 * This annotation is used by classes which extends {@link ConfigurationObject}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefaultValue {

  /**
   * The actual default value expression.
   */
  String value();
}
