package cern.c2mon.server.configuration.handler;

import cern.tim.server.cache.CommonTagFacade;
import cern.tim.server.cache.DataTagFacade;
import cern.tim.server.cache.TagLocationService;
import cern.tim.server.cache.TimCache;
import cern.tim.server.cache.loading.ConfigurableDAO;
import cern.tim.shared.common.ConfigurationException;
import cern.tim.shared.common.tag.Tag;

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
 * @author Mark Brightwell
 *
 */
abstract class TagConfigHandlerImpl<T extends Tag> implements TagConfigHandler<T> {

  protected TimCache<T> tagCache;
  
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
  
  public TagConfigHandlerImpl(ConfigurableDAO<T> configurableDAO, CommonTagFacade<T> configurableTagFacade, TimCache<T> tagCache, TagLocationService tagLocationService) {
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
  public void addRuleToTag(final Long tagId, final Long ruleId) {
    T tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      if (!tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.addDependentRuleToTag(tag, ruleId);   
        configurableDAO.updateConfig(tag);      
      }      
    } finally {
      tag.getWriteLock().unlock();
    }  
  }
  
  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    T tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      if (!tag.getRuleIds().contains(ruleId)) {
        commonTagFacade.removeDependentRuleFromTag(tag, ruleId);
        configurableDAO.updateConfig(tag);      
      }      
    } finally {
      tag.getWriteLock().unlock();
    }
  }
  
  @Override
  public void addAlarmToTag(final Long tagId, final Long alarmId) {
    T tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      commonTagFacade.addAlarm(tag, alarmId);      
    } finally {
      tag.getWriteLock().unlock();
    }  
  }
  
  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    Tag tag = tagCache.get(tagId);
    try {
      tag.getWriteLock().lock();
      tag.getAlarmIds().remove(alarmId); 
    } finally {
      tag.getWriteLock().unlock();
    }
  }
  
}
