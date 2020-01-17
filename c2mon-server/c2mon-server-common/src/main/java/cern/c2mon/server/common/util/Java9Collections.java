package cern.c2mon.server.common.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides some of the APIs that become available in Java > 8 for quality of life
 *
 * When C2MON updates to such a Java version, these APIs can be hopefully dropped
 *
 * @author Alexandros Papageorgiou
 */
public final class Java9Collections {

  private Java9Collections(){}

  /**
   * When using this, careful not to pass different types in runtime, as you could
   * blow up with {@link ClassCastException}
   *
   * @see <a href=https://stackoverflow.com/questions/12462079/possible-heap-pollution-via-varargs-parameter></a>
   */
  public static <T> Set<T> setOf(T... args) {
    return new HashSet<>(listOf(args));
  }

  /**
   * Ignores nulls in the input and creates a set of the remaining elements
   */
  public static <T> Set<T> setOfNonNulls(T... args) {
    return Stream.of(args).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  public static <T> List<T> listOf(T... args) {
    return Arrays.asList(args);
  }

  /**
   * Ignores nulls in the input and creates a list of the remaining elements
   */
  public static <T> List<T> listOfNonNulls(T... args) {
    return Stream.of(args).filter(Objects::nonNull).collect(Collectors.toList());
  }
}
