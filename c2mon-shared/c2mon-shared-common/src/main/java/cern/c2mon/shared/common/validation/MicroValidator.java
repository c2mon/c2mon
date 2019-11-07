package cern.c2mon.shared.common.validation;

import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.ConfigurationException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @param <T> T has been limited to Cacheable but can be extended to Object,
 *            I just wanted to have nicer error messages in the default case
 */
@Slf4j
public class MicroValidator<T extends Cacheable> {
  private final T value;
  private final ValidationMode mode;

  public MicroValidator(T value, ValidationMode mode) {
    this.value = value;
    this.mode = mode;
    if (value == null) {
      internalThrow("Argument for validation must be non null itself!");
    }
  }

  public MicroValidator(T value) {
    this(value, ValidationMode.NULL_POINTER_EXCEPTION);
  }

  public <R> MicroValidator<T> notNull(Function<T, R> memberAccessor) {
    return notNull(memberAccessor, "Expected member of " + value.getClass() + " with id " + value.getId() + "  to be non null");
  }

  public <R> MicroValidator<T> notNull(Function<T, R> memberAccessor, String messageIfFailed) {
    if (memberAccessor.apply(value) == null) {
      internalThrow(messageIfFailed);
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

  private void internalThrow(String message) {
    log.debug(message);
    if (mode == ValidationMode.CONFIGURATION_EXCEPTION)
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, message);
    else if (mode == ValidationMode.NULL_POINTER_EXCEPTION)
      throw new NullPointerException(message);
  }
}
