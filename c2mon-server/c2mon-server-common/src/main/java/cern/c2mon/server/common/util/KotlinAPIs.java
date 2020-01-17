package cern.c2mon.server.common.util;

import java.util.function.Consumer;

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

  public static <T> T apply(T object, Consumer<T> applyFunction) {
    applyFunction.accept(object);
    return object;
  }
}
