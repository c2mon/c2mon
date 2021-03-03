package cern.c2mon.server.common.util;

import cern.c2mon.shared.common.SerializableFunction;

import java.util.function.Consumer;

/**
 * This class provides some of the APIs of Kotlin for quality of life in development
 * <p>
 * When C2MON migrates to Kotlin, email me: papageorgiouAlexander@gmail.com
 *
 * @author Alexandros Papageorgiou
 */
public final class KotlinAPIs {

  private KotlinAPIs() {

  }

  /**
   * Takes the object as argument, applies the function on it, then returns the original object
   * <p>
   * example: list.add( apply(obj, System.out::println) )
   */
  public static <T> T apply(T object, Consumer<T> applyFunction) {
    applyFunction.accept(object);
    return object;
  }

  /**
   * Takes the (potentially null) object as argument,
   * if it is non null, applies the function on it,
   * then returns the original object (even if null!)
   * <p>
   * example: list.add( apply(obj, System.out::println) )
   */
  public static <T> T applyNotNull(T object, Consumer<T> applyFunction) {
    if (object != null)
      applyFunction.accept(object);
    return object;
  }

  /**
   * Takes the object as argument, then returns whatever the transformer function returns
   */
  public static <OUT, IN> OUT let(IN object, SerializableFunction<IN, OUT> transformer) {
    return transformer.apply(object);
  }

  /**
   * Takes the object as argument,
   * if it is non null, applies the function on it,
   * then returns whatever the transformer function returns
   */
  public static <OUT, IN> OUT letNotNull(IN object, SerializableFunction<IN, OUT> transformer) {
    return object == null ? null : transformer.apply(object);
  }

  /**
   * Returns the object if not null, or the default value otherwise
   */
  public static <T> T orElse(T object, T defaultValue) {
    return object == null ? defaultValue : object;
  }
}
