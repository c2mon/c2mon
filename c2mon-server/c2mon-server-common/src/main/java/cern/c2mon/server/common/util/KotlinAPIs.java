package cern.c2mon.server.common.util;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides some of the APIs of Kotlin for quality of life in development
 *
 * When C2MON migrates to Kotlin, email me: papageorgiouAlexander@gmail.com
 *
 * @author Alexandros Papageorgiou
 */
public final class KotlinAPIs {

  private KotlinAPIs() {

  }

  /**
   * Takes the object as argument, applies the function on it, then returns the original object
   *
   * example: list.add( apply(obj, System.out::println) )
   */
  public static <T> T apply(T object, Consumer<T> applyFunction) {
    applyFunction.accept(object);
    return object;
  }

  /**
   * Takes the object as argument, then returns whatever the transformer function returns
   */
  public static <OUT, IN> OUT let(IN object, Function<IN,OUT> transformer) {
    return transformer.apply(object);
  }
}
