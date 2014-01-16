package cern.c2mon.server.cache;

import java.sql.Timestamp;
import cern.c2mon.server.common.rule.RuleTag;

/**
 * Bean that should be used to access and update rule cache objects.
 * 
 * <p>This methods are all thread-safe. They perform the required synchronization,
 * including for a Terracotta distributed cache.
 * 
 * @author Mark Brightwell
 *
 */
public interface RuleTagFacade extends CommonTagFacade<RuleTag> {

  
  /**
   * Sets the RuleTag quality to good and updates the rule with the new
   * value, value description and timestamp.
   * 
   * <p>If the update causes no changes, the cache object is not updated (see filterout method in AbstracTagFacade).
   * 
   * <p>Notifiers listeners if updated and logs the rule to the rule log file.
   * 
   * @param id of the rule to update
   * @param value new value
   * @param valueDescription new value description
   * @param timestamp new timestamp
   */
  void updateAndValidate(Long id, Object value, String valueDescription, Timestamp timestamp);
  
  /**
   * Creates a cache object from the provided properties.
   * The cache object is not put into the cache: this should
   * be done once it is created, preferably by putting it in
   * the database and performing a <code>get</code> on the 
   * cache (which calls the DB loader).
   * 
   * <p>This method is used when configuring a new cache object
   * from properties retrieved from the database.
   * 
   * <p>The properties object must contain all required fields with
   * valid entries. If this is not the case, the method will throw a
   * configuration exception.
   * 
   * <p>The cache timestamp is set to the time the object was created;
   * the DAQ and source timestamps are set as null.
   * 
   * @param ruleId the id of the rule to create
   * @param properties the Properties object from which the object must be created
   * @return the create cache object
   * @throws ConfigurationException if the resulting cache object fails the validation checks
   */
  //RuleTagCacheObject createCacheObject(Long ruleId, Properties properties) throws ConfigurationException;

  /**
   * Sets the parent process and equipment fields for RuleTags.
   * 
   * @param ruleTag the RuleTag for which the fields should be set
   */
  void setParentSupervisionIds(RuleTag ruleTag);
  
  /**
   * Sets the parent process and equipment fields for RuleTags.
   * 
   * @param ruleTagId the id of the RuleTag for which the fields should be set
   */
  void setParentSupervisionIds(Long ruleTagId);
  
  /**
   * Updates configuration fields of the cache object with those contained
   * in the properties object. By "configuration fields" we are referring
   * to fields open to reconfiguration during runtime. In particular, values 
   * and timestamps are not modified by this method: updates due to incoming
   * data should be processed using the other update and invalidate methods.
   * 
   * <p>This method is thread-safe (it performs the required synchronization
   * on the cache object residing in the cache.
   * 1
   * <p>This method should preferably be performed on an object outside the cache
   * before being applied to the object residing in the cache, since changes cannot
   * be rolled back if the validation fails.
   * 
   * 
   * @param ruleTag the cache object to reconfigure (the object is modified by this method)
   * @param properties the properties that need reconfiguring
   * @throws ConfigurationException if the reconfigured cache object fails validation checks
   */
  //void configureCacheObject(RuleTag ruleTag, Properties properties) throws ConfigurationException;

  
}
