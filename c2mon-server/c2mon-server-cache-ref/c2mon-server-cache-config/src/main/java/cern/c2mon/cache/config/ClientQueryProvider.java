package cern.c2mon.cache.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.common.Cacheable;

import java.util.Collection;
import java.util.function.Function;

public class ClientQueryProvider {

  private ClientQueryProvider() {
  }

  public static <T extends Cacheable> Collection<T> queryByClientInput(C2monCache<T> cache, Function<T, String> accessor, String clientInput) {
    return cache.query(cacheable -> accessor.apply(cacheable).matches(wildCardReplacer(clientInput)));
  }

  private static String wildCardReplacer(String clientInput) {
    return clientInput
      .replace("*", "\\*") // TODO (Alex) This probably should instead replace with valid regex? Because we are using matches
      .replace("?", "\\?");
  }
}
