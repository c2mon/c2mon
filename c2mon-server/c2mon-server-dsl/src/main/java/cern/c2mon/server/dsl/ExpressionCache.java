package cern.c2mon.server.dsl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.common.expression.ExpressionCacheObject;

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
  private static Map<Long, Object> idToExpressionMap = new ConcurrentHashMap<>();

  private ExpressionCache() {
  }

  /**
   * Removes local expression from the cache.
   *
   * @param id the id of the expression to be deleted
   */
  public static void removeTag(Long id) {
    compiledExpressions.remove(id);
    idToExpressionMap.remove(id);
  }

  /**
   * Removes all local expression information of the tags to the given ids.
   *
   * @param tagIds the ids of the tags which expression information needs to be deleted
   */
  static void removeTags(List<Long> tagIds) {
    tagIds.stream().forEach(ExpressionCache::removeTag);
  }

  static void cacheExpression(Long key, ExpressionCacheObject expression) {
    idToExpressionMap.put(key, expression);
  }

  static void cacheCompiledExpression(Long key, Script expression) {
    compiledExpressions.put(key, expression);
  }

  static Map<Long, Script> getCompiledExpressions() {
    return compiledExpressions;
  }

  static boolean isEmpty() {
    return compiledExpressions.isEmpty();
  }

  static ExpressionCacheObject getExpression(long key) {
    return (ExpressionCacheObject) idToExpressionMap.get(key);
  }
}