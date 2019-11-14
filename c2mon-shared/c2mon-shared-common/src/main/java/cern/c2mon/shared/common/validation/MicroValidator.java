package cern.c2mon.shared.common.validation;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.type.TypeConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @param <T> T has been limited to Cacheable but can be extended to Object,
 *            I just wanted to have nicer error messages in the default case
 * @implNote This could have a simpler API using by reflective accesses, but we opted to avoid
 * it because of GraalVM potential and code simplicity.
 */
@Slf4j
public class MicroValidator<T extends Cacheable> {
  private final T value;

  public MicroValidator(T value) {
    this.value = value;
    if (value == null) {
      internalThrow("Argument for validation must be non null itself!");
    }
  }

  public <R extends Comparable<R>> MicroValidator<T> between(Function<T, R> memberAccessor, R minInclusive, R maxInclusive) {
    return between(memberAccessor, minInclusive, maxInclusive,
      "Condition evaluation failed for member of " + value.getClass() + " with id " + value.getId());
  }

  public <R extends Comparable<R>> MicroValidator<T> between(Function<T, R> memberAccessor, R minInclusive, R maxInclusive, String paramName) {
    if (memberAccessor.apply(value).compareTo(minInclusive) < 0 || memberAccessor.apply(value).compareTo(maxInclusive) > 0) {
      internalThrow("Invalid value for parameter " + paramName + " : " + memberAccessor.apply(value) +
        ". Expected value between " + minInclusive + " and " + maxInclusive);
    }
    return this;
  }

  public <R> MicroValidator<T> notNull(Function<T, R> memberAccessor, String paramName) {
    if (memberAccessor.apply(value) == null) {
      internalThrow("Expected parameter \"" + paramName + "\" of " + value.getClass() + " with id " + value.getId() + "  to be non null");
    }
    return this;
  }

  public MicroValidator<T> not(Function<T, Boolean> condition) {
    return not(condition, "Condition evaluation failed for member of " + value.getClass() + " with id " + value.getId());
  }

  public MicroValidator<T> not(Function<T, Boolean> condition, String messageIfFailed) {
    if (condition.apply(value)) {
      internalThrow(messageIfFailed);
    }
    return this;
  }

  public MicroValidator<T> must(Function<T, Boolean> condition, String messageIfFailed) {
    return not(condition.andThen(previousResult -> !previousResult), messageIfFailed);
  }

  public <R> MicroValidator<T> optType(Function<T, R> memberAccessor, String type, String paramName) {
    try {
      R member = memberAccessor.apply(value);
      if (member != null) {
        Class minValueClass = TypeConverter.getType(type);
        if (!minValueClass.isInstance(member)) {
          internalThrow("Parameter " + paramName + " not found as expected type " + type);
        }
      }
    } catch (Exception e) {
      internalThrow("Error validating parameter " + paramName + "\n" + e.getMessage());
    }
    return this;
  }

  private void internalThrow(String message) {
    log.debug(message);
    throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, message);
  }
}
