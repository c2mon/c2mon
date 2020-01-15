package cern.c2mon.cache.api.spi;

import cern.c2mon.shared.common.Cacheable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Comparator;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Use this class to add extra functionality to search queries. If you only add here and don't edit the Ctor,
 * you will not be introducing breaking API changes.
 *
 * Different cache implementations may use these arguments in various ways, but they will always honor them.
 * For example, Ignite cache may fetch more than {@code maxResults} results, but it will, in the end, limit
 * the list of results to maxResults size.
 *
 * @param <V> the type of CacheObject searched for
 */
@Getter
@Setter
@Accessors(fluent = true)
public class CacheQuery<V extends Cacheable> implements Function<V, Boolean> {
  public static final int DEFAULT_MAX_RESULTS = 100_000;

  private final Function<V, Boolean> filter;

  /**
   * Kept to maintain compatibility with previous default, but this could definitely be reduced
   */
  private int maxResults = DEFAULT_MAX_RESULTS;

  /**
   * Sorts results. Combine with {@code maxResults} to create complex queries, e.g last oscillating alarm:
   * <pre>
   * cache.query(new CacheQuery<>(Alarm::isOscillating)
   *   .sortBy(Comparator.comparing(Alarm::getTimestamp).reversed())
   *   .maxResults(1))
   * </pre>
   *
   * Make sure you're sorting in the direction you expect! Notice the above example is reversing, default
   * {@code Comparator} order is ascending!
   */
  private Comparator<V> sortBy;

  public CacheQuery(Function<V, Boolean> filter) {
    requireNonNull(filter);
    this.filter = filter;
  }

  @Override
  public Boolean apply(V cacheObject) {
    return filter.apply(cacheObject);
  }
}
