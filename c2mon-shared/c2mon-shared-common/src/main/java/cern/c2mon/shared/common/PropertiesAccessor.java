package cern.c2mon.shared.common;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

public class PropertiesAccessor {

  private final Properties properties;

  public PropertiesAccessor(Properties properties) {
    this.properties = properties;
    Objects.requireNonNull(properties);
  }

  public PropertyOptional<String> getString(String propertyName) {
    return getAs(propertyName, Function.identity());
  }

  public PropertyOptional<Integer> getInteger(String propertyName) {
    return getAs(propertyName, Integer::parseInt);
  }

  public PropertyOptional<Long> getLong(String propertyName) {
    return getAs(propertyName, Long::parseLong);
  }

  public <R> PropertyOptional<R> getAs(String propertyName, Function<String, R> transformer) {
    String propertyValue = properties.getProperty(propertyName);
    return new PropertyOptional<>(transform(transformer, propertyValue));
  }

  private <R> R transform(Function<String, R> transformer, String propertyValue) {
    if (propertyValue == null)
      return null;

    try {
      return transformer.apply(propertyValue);
    } catch (NumberFormatException e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
        "NumberFormatException: Unable to convert parameter \"" + propertyValue + "\" using " + transformer.toString());
    } catch (Exception e) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE,
        "Unexpected error encountered while converting parameter \"" + propertyValue + "\" using " + transformer.toString(),
        e);
    }
  }

  public class PropertyOptional<T> {
    private final T value;

    private PropertyOptional(T value) {
      this.value = value;
    }

    public PropertiesAccessor ifPresent(Consumer<T> ifPresent) {
      if (value != null) {
        ifPresent.accept(value);
      }
      return PropertiesAccessor.this;
    }
  }
}
