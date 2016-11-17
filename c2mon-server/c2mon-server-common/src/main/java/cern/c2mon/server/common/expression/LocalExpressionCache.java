package cern.c2mon.server.common.expression;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.expression.Expression;
import groovy.lang.GroovyObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Stores all relevant information of a compiled expression.
 * Because the time to compile an expression is to high we need to
 * store a compiled expression on a local node.
 *
 * @author Franz Ritter
 */
@Slf4j
public class LocalExpressionCache {

  /**
   * Holds the information to which time the expression got compiled the last time.
   */
  private static Map<Long, Map<String, Long>> tagIdToLocalScriptCounter = new ConcurrentHashMap<>();

  /**
   * Holds the compiled expressions.
   */
  private static Map<Long, Map<String, GroovyObject>> tagIdToLocalScripts = new ConcurrentHashMap<>();


  private LocalExpressionCache() {
  }

  /**
   * Get all tag ids with locally stored expressions.
   *
   * @return the tag ids of all locally stored expressions
   */
  public static Set<Long> getAllIds() {
    return tagIdToLocalScripts.keySet();
  }

  /**
   * Removes all local expression information of the tag to the given id.
   *
   * @param id The id of the tag which expression information needs to be deleted
   */
  public static void removeTagInformation(Long id) {
    tagIdToLocalScripts.remove(id);
    tagIdToLocalScriptCounter.remove(id);
  }

  /**
   * Removes all local expression information of the tags to the given ids.
   *
   * @param tagIds The ids of the tags which expression information needs to be deleted
   */
  public static void removeStaleTags(List<Long> tagIds) {
    tagIds.stream().forEach(id -> removeTagInformation(id));
  }

  protected static GroovyObject getScript(Long tagId, String expressionName) {
    return tagIdToLocalScripts.get(tagId).get(expressionName);
  }

  protected static <T extends Tag> void initializeLocalCache(Long tagId) {
    if (tagIdToLocalScriptCounter.get(tagId) == null) {
      tagIdToLocalScriptCounter.put(tagId, new ConcurrentHashMap<>());
    }
    if (tagIdToLocalScripts.get(tagId) == null) {
      tagIdToLocalScripts.put(tagId, new ConcurrentHashMap<>());
    }
  }

  protected static <T extends Tag> void checkLocalCounter(T tag, String expressionName) {
    if (tagIdToLocalScriptCounter.get(tag.getId()).get(expressionName) == null) {
      tagIdToLocalScriptCounter.get(tag.getId()).put(expressionName, -1L);
    }
  }

  protected static <T extends Tag> void checkLocalCacheConsistency(T updateTag) {
    Long tagId = updateTag.getId();
    if (updateTag.getExpressions().size() < tagIdToLocalScripts.size()) {
      tagIdToLocalScripts.get(tagId).keySet().stream().filter(localExpressionName -> !updateTag.getExpressions()
          .stream()
          .filter(cacheExpression -> cacheExpression.getName().equals(localExpressionName))
          .findFirst()
          .isPresent()).forEach(expressionName -> {
        log.debug("Remove the script {} of the tag {} from the local cache", expressionName, tagId);
        tagIdToLocalScripts.get(tagId).remove(expressionName);
        tagIdToLocalScriptCounter.get(tagId).remove(expressionName);
      });
    }
  }

  protected static <T extends Tag> void checkScriptState(T tag, Expression localExpression) {

    if (tagIdToLocalScriptCounter.get(tag.getId()).get(localExpression.getName()) < localExpression
        .getLastCompileCounter()) {
      GroovyObject currentScript = ExpressionFactory.createScript(localExpression.getExpression());
      tagIdToLocalScripts.get(tag.getId()).put(localExpression.getName(), currentScript);

      if (localExpression.getLastCompileCounter() == 0l) {
        localExpression.setLastCompileCounter(System.currentTimeMillis());
      }
      tagIdToLocalScriptCounter.get(tag.getId()).put(localExpression.getName(), localExpression.getLastCompileCounter
          ());
    }
  }

}
