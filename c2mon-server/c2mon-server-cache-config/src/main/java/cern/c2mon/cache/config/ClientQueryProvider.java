package cern.c2mon.cache.config;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.SerializableFunction;

import java.util.Collection;

import static cern.c2mon.server.common.util.KotlinAPIs.letNotNull;
import static cern.c2mon.server.common.util.KotlinAPIs.orElse;

public final class ClientQueryProvider {

  private ClientQueryProvider() {
  }

  public static <T extends Cacheable> Collection<T> queryByClientInput(C2monCache<T> cache, SerializableFunction<T, String> accessor, String clientInput) {
    return cache.query(cacheable ->
      orElse(
        letNotNull(
          accessor.apply(cacheable),
          res -> res.matches(wildCardReplacer(clientInput))
        ),
        false));
  }

  private static String wildCardReplacer(String clientInput) {
    return clientInput
      .replace("*", "\\*") // TODO (Alex) This probably should instead replace with valid regex? Because we are using matches
      .replace("?", "\\?");
  }
}
