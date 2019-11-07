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

  public NullableProperty<String> getString(String propertyName) {
    return getAs(propertyName, Function.identity());
  }

  public NullableProperty<Integer> getInteger(String propertyName) {
    return getAs(propertyName, Integer::parseInt);
  }

  public NullableProperty<Short> getShort(String propertyName) {
    return getAs(propertyName, Short::parseShort);
  }

  public NullableProperty<Long> getLong(String propertyName) {
    return getAs(propertyName, Long::parseLong);
  }

  public <R> NullableProperty<R> getAs(String propertyName, Function<String, R> transformer) {
    String propertyValue = properties.getProperty(propertyName);
    return new NullableProperty<>(transform(transformer, propertyValue));
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

  public class NullableProperty<T> {
    private final T value;

    private NullableProperty(T value) {
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
