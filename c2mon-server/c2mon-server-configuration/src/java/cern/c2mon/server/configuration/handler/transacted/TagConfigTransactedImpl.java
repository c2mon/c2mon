package cern.c2mon.server.configuration.handler.transacted;

import org.apache.log4j.Logger;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.CommonTagFacade;
import cern.c2mon.server.cache.DataTagFacade;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Public methods in this class should perform the complete
 * configuration process for the given tag (i.e. cache update
 * and database persistence).
 * 
 * <p>The methods contain the common reconfiguration logic for
 * all Tag objects (Control, Data and Rule tags).
 * 
 * <p>The appropriate Facade and DAO objects must be passed
 * to the constructor to provide the common configuration
 * functionality. 
 * 
 * <p>Notice that these methods will always be called within
 * a transaction initiated at the ConfigurationLoader level
 * and passed through the handler via a "create", "update"
 * or "remove" method, with rollback of DB changes if a 
 * RuntimeException is thrown.
 * 
 * @author Mark Brightwell
 * 
 * @param <T> the type of Tag
 *
 */
abstract class TagConfigTransactedImpl<T extends Tag> implements TagConfigTransacted<T> {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(TagConfigTransactedImpl.class);
  
  protected C2monCache<Long, T> tagCache;
  
  /**
   * The Facade bean for which this TagConfigHandler
   * provides the common functionality.
   * 
   * <p>(e.g. to provide helper functions for DataTag
   * configuration, this should be the {@link DataTagFacade}
   * bean).
   */
  protected CommonTagFacade<T> commonTagFacade;
  
  /**
   * The corresponding DAO.
   * 
   */
  protected ConfigurableDAO<T> configurableDAO;
  
  protected TagLocationService tagLocationService;
  
  public TagConfigTransactedImpl(ConfigurableDAO<T> configurableDAO, CommonTagFacade<T> configurableTagFacade, C2monCache<Long, T> tagCache, TagLocationService tagLocationService) {
    super();
    this.commonTagFacade = configurableTagFacade;   
    this.configurableDAO = configurableDAO;
    this.tagCache = tagCache;
    this.tagLocationService = tagLocationService;
  }

  /**
   * Throw a {@link ConfigurationException} if the Tag id already exists in one of the Tag
   * caches.
   * @param id the id to check 
   */
  protected void checkId(final Long id) {
    if (tagLocationService.isInTagCache(id)) {      
        throw new ConfigurationException(ConfigurationException.ENTITY_EXISTS, 
            "Attempting to create a Tag with an already existing id: " + id);      
    }
  }
  
  /**
   * If necessary, updates the list of rules that need evaluating for this tag,
   * persisting the change to the database also.
   * 
   * @param tag the tag object in the cache
   * @param ruleId the rule id
   */
  @Override
  @Transactional("cacheTransactionManager")
  public void addRuleToTag(final Long tagId, final Long ruleId) {
    LOGGER.trace("Adding rule " + ruleId + " reference from Tag " + tagId);
    tagCache.acquireWriteLockOnKey(tagId);    
    try {
      T tag = tagCache.get(tagId);
      if (!tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.addDependentRuleToTag(tag, ruleId);  
        configurableDAO.updateConfig(tag);
      }
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);    
    }  
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public void removeRuleFromTag(final Long tagId, final Long ruleId) {
    LOGGER.trace("Removing rule " + ruleId + " reference from Tag " + tagId);    
    tagCache.acquireWriteLockOnKey(tagId);
    try {      
      T tag = tagCache.get(tagId);
      if (tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.removeDependentRuleFromTag(tag, ruleId);
        configurableDAO.updateConfig(tag);
      }
    } finally {    
      tagCache.releaseWriteLockOnKey(tagId);
    }
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public void addAlarmToTag(final Long tagId, final Long alarmId) {
    LOGGER.trace("Adding Alarm " + alarmId + " reference from Tag " + tagId);
    tagCache.acquireWriteLockOnKey(tagId);
    try {
      T tag = tagCache.get(tagId);
      commonTagFacade.addAlarm(tag, alarmId);
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);
    }  
  }
  
  @Override
  @Transactional("cacheTransactionManager")
  public void removeAlarmFromTag(final Long tagId, final Long alarmId) {
    LOGGER.trace("Removing Alarm " + alarmId + " reference from Tag " + tagId);        
    tagCache.acquireWriteLockOnKey(tagId);
    try {      
      T tag = tagCache.get(tagId);
      tag.getAlarmIds().remove(alarmId);
    } finally {
      tagCache.releaseWriteLockOnKey(tagId);
    }
  }
  
}

