package cern.c2mon.server.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;

/**
 * Node-local cache of compiled expressions. This cache is kept to avoid
 * the overhead of recompiling. Versioning is used to know when to recompile
 * in case expressions are changed.
 *
 * @author Martin Flamm
 */
@Slf4j
public class ExpressionCache {

  /**
   * Holds the compiled expressions.
   */
  private static Map<Long, Script> compiledExpressions = new ConcurrentHashMap<>();
  private static List<Long> cachedExpressions = new ArrayList<>();

  private ExpressionCache() {
  }

  /**
   * Removes local expression from the cache.
   *
   * @param id the id of the expression to be deleted
   */
  public static void removeTag(Long id) {
    compiledExpressions.remove(id);
    cachedExpressions.remove(id);
  }

  /**
   * Removes all local expression information of the tags to the given ids.
   *
   * @param tagIds the ids of the tags which expression information needs to be deleted
   */
  static void removeTags(List<Long> tagIds) {
    tagIds.stream().forEach(ExpressionCache::removeTag);
  }

  public static void cacheExpressionId(Long key) {
    cachedExpressions.add(key);
  }

  public static List<Long> getExpressionIds() {
    return cachedExpressions;
  }

  public static void cacheCompiledExpression(Long key, Script expression) {
    compiledExpressions.put(key, expression);
  }

  static Map<Long, Script> getCompiledExpressions() {
    return compiledExpressions;
  }

  static boolean isEmpty() {
    return compiledExpressions.isEmpty();
  }

}