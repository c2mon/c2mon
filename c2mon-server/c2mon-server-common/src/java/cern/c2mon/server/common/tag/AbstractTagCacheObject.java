/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.tag;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.log4j.Logger;

import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.DataTagValueDictionary;

/**
 * Abstract tag used as basis for all tag objects in the server: 
 * DataTag, ControlTag and RuleTag.
 * 
 * @author Mark Brightwell
 *
 */
public abstract class AbstractTagCacheObject implements DataTagConstants, Cloneable, Serializable { //removed cacheable for the time being... may not be needed

    /**
     * Private class logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractTagCacheObject.class);
  
    // TODO remove UID if not needed
   /**
    * UID since serializable
    */
   private static final long serialVersionUID = 8210576749860621700L;

    /**
     * Maximum length of the value description. If the user tries to send a
     * longer value description, it will be truncated.
     */
   private static final int MAX_DESC_LENGTH = 500;
  
   /**
    * Unique datatag identifier (unique across all types of tags: control, datatag and rules).
    */
   private Long id;
  
   /**
    * Unique tag name.
    */
   private String name;
  
   /**
    * Free-text description of the tag
    */
   private String description;
  
   /**
    * Expected data type for the tag's value
    */
   private String dataType;
   
   /**
    * Static dictionary for looking up preconfigured descriptions for tag values
    */
   private DataTagValueDictionary valueDictionary;
  
   /**
    * Indicates whether a tag is "in operation", "in maintenance" or "in test".
    */
   private short mode;
   
   /**
    * Indicates whether a tag has been reconfigured and is awaiting a DAQ restart, 
    * or cannot be trusted due to a reconfiguration error.
    */
   private DataTagConstants.Status status;
  
   /**
    * Name of the JMS topic on which the tag will be distributed to clients.
    * Set at start up to "c2mon.tag.default.publication" but should be overwritten
    * at runtime (no client listens to this topic).
    */
   private String topic = "c2mon.tag.default.publication";
  
   /**
    * Indicates whether this tag's value changes shall be logged to the
    * short-term log.
    */
   private boolean logged;
   
   /**
    * Unit of the tag's value. This parameter is defined at configuration time
    * and doesn't change during run-time. It is mainly used for analogue values
    * that may represent e.g. a flow in "m3", a voltage in "kV" etc.
    */
   private String unit;
   
   
   
   // -----------------------
   // PUBLISHING ADDRESSES
   // -----------------------
   /**
    * DIP address for tags published on DIP
    */
   private String dipAddress;
   
   /**
    * JAPC address for tags published on JAPC
    */
   private String japcAddress;
   
   
   // -------------------------------------------------------------------------------
   // RUN-TIME PROPERTIES (i.e. those updated when new values arrive from DAQ layer)
   // -------------------------------------------------------------------------------
  
   /**
    * Current value of the datatag (if any, null before first value reception). The value is of type
    * Boolean, Float, String etc. as indicated in the {@link dataType} field (and is cloneable with a 
    * shallow copy).
    */
   private Object value;
  
   /**
    * Description of the tag's current value (if any)
    */
   private String valueDescription;
  
   /**
    * Timestamp set when the current value was saved in the cache
    * (if at all). It should be set when the cache object is
    * added to the cache.
    */
   private Timestamp cacheTimestamp;
  
   /**
    * Quality of the tag's current value.
    * Assumed non-null within server.
    */
   private DataTagQuality dataTagQuality;
   
   /**
    * Flag indicating that the current value of this DataTag is the result of a
    * simulation. In that case, the value will neither be logged nor persisted to
    * the entity bean.
    */
   //TODO replace with new OVERRIDDEN mode
   private boolean simulated;
  
   // --------------------------
   // REFERENCES TO OTHER CACHE ELEMENTS
   // --------------------------
   
   /**
    * Identifiers of all alarms attached to the datatag
    */
   private Collection<Long> alarmIds;
  
   /**
    * Identifiers of all rules attached to the datatag
    * (tests will fail if not initialized as this field
    *  is set during cache loading from DB).
    */
   private Collection<Long> ruleIds;
   
   /**
    * String of rules ids obtained from the database;
    * this string is then parsed and saved in the ruleIds
    * field
    */
   private String ruleIdsString;
   
   /**
    * This private member is set when a DataTagCacheObject is updated or
    * invalidated. It is used when the changes are persisted to the DataTag
    * entity bean in order to avoid updating fields that have not changed.
    * 
    * TODO remove these if not used
    */
   protected short tagChange = CHANGE_NONE;
  
   public static final short CHANGE_NONE = 0;
   public static final short CHANGE_UPDATE = 1;
   public static final short CHANGE_INVALIDATE = 2;
   public static final short CHANGE_CONFIGURATION = 3;
   
   
   /**
    * Synchronization locks
    */
   private ReadLock readLock;
   private WriteLock writeLock;
   
   // --------------------------
   // CONSTRUCTORS
   // --------------------------
   
   /**
    * Default public constructor. TODO remove unused constructors
    * 
    * Sets the default value of the quality to UNINITIALISED with no
    * description; the description should be added at a later stage
    * with information about this tag creation.
    */
   protected AbstractTagCacheObject() {
     //TODO check this - done by config loader
     //setDataTagQuality(new DataTagQuality(DataTagQuality.UNINITIALISED, "No value received for this data tag so far."));
     ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
     readLock = lock.readLock();
     writeLock = lock.writeLock();
     
     dataTagQuality = new DataTagQualityImpl();
     valueDictionary = new DataTagValueDictionary();
     //status = Status.OK;
     alarmIds = new ArrayList<Long>();
     ruleIds = new ArrayList<Long>();
     cacheTimestamp = new Timestamp(System.currentTimeMillis());
   }
   
   /**
    * Constructor used in implementations of the class.
    * @param pId
    */
   protected AbstractTagCacheObject(final Long id) {
     this();
     this.id = id;    
   }
   
   // ------------------------
   // CLONING AND EQUALITY
   // ------------------------
   
   /**
    * The clone is provided with <b>new</b> locks: these do not lock access
    * to the object residing in the cache (the clone is no longer in the
    * cache).
    */
   @Override
   public Object clone() throws CloneNotSupportedException {        
     AbstractTagCacheObject cacheObject = (AbstractTagCacheObject) super.clone();
     ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
     cacheObject.readLock = lock.readLock();
     cacheObject.writeLock = lock.writeLock();
     if (valueDictionary != null) {
       cacheObject.valueDictionary = (DataTagValueDictionary) valueDictionary.clone();        
     }
     if (dataTagQuality != null) {
       cacheObject.dataTagQuality = (DataTagQuality) dataTagQuality.clone();
     }       
     cacheObject.alarmIds = (ArrayList<Long>) ((ArrayList<Long>) alarmIds).clone();                    
     cacheObject.ruleIds = (ArrayList<Long>) ((ArrayList<Long>) ruleIds).clone();
     if (cacheTimestamp != null) {
       cacheObject.cacheTimestamp = (Timestamp) cacheTimestamp.clone();  
     }       
     return cacheObject;    
   }
   
   /**
    * only compare ids so far
    */
   @Override
   public boolean equals(Object dataTag) {
     if (dataTag instanceof AbstractTagCacheObject) {
       return this.id.equals(((AbstractTagCacheObject) dataTag).getId());
     } else {
       return false;
     }    
   }
   
   /**
    * Hashcode method - keep consistent with equals()!
    */
   @Override
   public int hashCode() {
     return this.id.hashCode();
   }
   
   // ----------------------------------------------------------------
   // METHODS MANAGING RULE IDS (i.e. rules in which this tag is used)
   // ----------------------------------------------------------------
   /**
    * Returns a collection containing the identifiers of all rules that need to
    * be evaluated when THIS tags changes. If this tag isn't used by any rule,
    * an empty collection is returned.
    * 
    * @return returns the collection of Tag ids that need evaluating; never returns null
    */
   public final Collection<Long> getRuleIds() {
     return this.ruleIds;
   }

   /**
    * Returns an own copy of the list of rules that need evaluating when
    * this tag changes.
    * @return list of rule ids
    */
   public final Collection<Long> getCopyRuleIds() {
     readLock.lock();
     try{
       return new ArrayList<Long>(ruleIds);       
     } finally {
       readLock.unlock();
     }    
   }
   
   /**
    * Add a new rule to the collection of rules that need to be evaluated when
    * THIS tags changes.
    */   
   public final boolean addRuleId(final Long pId) {
     if (!this.ruleIds.contains(pId)) {
       this.ruleIds.add(pId);
       this.tagChange = CHANGE_CONFIGURATION;
       return true;
     }
     else {
       return false;
     }
   }

   /**
    * Remove a rule from the collection of rules that need to be evaluated when
    * THIS tags changes.
    */
   public final boolean removeRuleId(final Long pId) {
     if (this.ruleIds.contains(pId)) {
       this.ruleIds.remove(pId);
       this.tagChange = CHANGE_CONFIGURATION;
       return true;
     }
     else {
       return false;
     }
   }
   
   // --------------------------
   // GETTERS AND SETTERS
   // --------------------------
   public final Object getValue() {
     return this.value;
   }
  
   public final String getValueDescription() {
     return this.valueDescription;
   }
   
   /**
    * Setter method.
    * @param valueDescription the valueDescription to set
    */
   public void setValueDescription(String valueDescription) {
     this.valueDescription = valueDescription;
   }
  
   public final Timestamp getCacheTimestamp() {
     return this.cacheTimestamp;
   }
   
   public final DataTagQuality getDataTagQuality() {
     return this.dataTagQuality;
   }

   public final boolean isValid() {
     return dataTagQuality.isValid();
   }

   public final boolean isExistingTag() {
     return dataTagQuality.isExistingTag();
   }

   public final boolean isSimulated() {
     return this.simulated;
   }

   public final Collection<Long> getAlarmIds() {
     return this.alarmIds;
   }
   
   public final Collection<Long> getCopyAlarmIds() {
     readLock.lock();
     try {
       return new ArrayList<Long>(this.alarmIds);
     } finally {
       readLock.unlock();
     }     
   }
   
   public final String getDipAddress() {
     return this.dipAddress;
   }
   
   /**
    * @return the JAPC address on which the data tag is published,
    *         or <code>null</code> if not.
    */
   public final String getJapcAddress() {
     return japcAddress;
   }  
   
   /**
    * @param id the id to set
    */
   public final void setId(Long id) {
       this.id = id;
   }

   /**
    * Sets the tag name (no longer the topic, which depends on Process)
    * @param name the name to set
    */
   public final void setName(final String name) {
     if (name != null) {
       this.name = name;
//       StringBuffer str = new StringBuffer("tim.datatag.");
//       char[] topicName = null;
//       
//       topicName = name.toCharArray();
//       for (int i=0; i != topicName.length; i++) {
//         if (topicName[i] == '$' || topicName[i]=='*' || topicName[i] =='#') {
//           topicName[i]='X';
//         }
//       }
//       str.append(topicName);
//       this.topic = str.toString();
     } else {
       throw new IllegalArgumentException("Attempt to set Tag name to null!");
     }      
   }

   /**
    * @param dataType the dataType to set
    */
   public final void setDataType(String dataType) {
       this.dataType = dataType;
   }

   /**
    * @param mode the mode to set
    */
   public final void setMode(short mode) {
       this.mode = mode;
   }

   /**
    * @param value the value to set
    */
   public final void setValue(Object value) {
       this.value = value;
   }

   /**
    * @param dataQuality the dataQuality to set
    */
   public final void setDataTagQuality(DataTagQuality dataTagQuality) {
       this.dataTagQuality = dataTagQuality;
   }
   
   /**
    * @param timestamp the timestamp to set
    */
   public final void setCacheTimestamp(Timestamp timestamp) {
     if (this.cacheTimestamp == null || timestamp == null){
       this.cacheTimestamp = timestamp;
     } else {
       this.cacheTimestamp.setTime(timestamp.getTime());
     }     
   }
   
   public final Long getId() {
     return this.id;
   }

   public final String getName() {
     return this.name;
   }

   public final String getDescription() {
     return this.description;
   }

   public final String getDataType() {
     return this.dataType;
   }

   public final DataTagValueDictionary getValueDictionary() {
     return this.valueDictionary;
   }

   public final short getMode() {
     return this.mode;
   }

   public final boolean isInOperation() {
     return (mode == MODE_OPERATIONAL);
   }

   public final boolean isInTest() {
     return (mode == MODE_TEST);
   }

   public final boolean isInMaintenance() {
     return (mode == MODE_MAINTENANCE);
   }
   
   public boolean isInUnconfigured() {
    return (mode == MODE_NOTCONFIGURED); 
   }

   public final String getTopic() {
     return this.topic;
   }

   public final boolean isLogged() {
     return this.logged;
   }
   
   public final String getUnit() {
     return this.unit;
   }
   
   /**
    * @param description the description to set
    */
   public void setDescription(String description) {
     this.description = description;
   }
   
   /**
    * @param logged the logged to set
    */
   public void setLogged(boolean logged) {
     this.logged = logged;
   }
 
   /**
    * @param simulated the simulated to set
    */
   public void setSimulated(boolean simulated) {
     this.simulated = simulated;
   }
      
   public void setTopic(final String topic) {
     this.topic = topic;
   }
   
   // ----------------------------------------------------------------------------
   // FIELDS RELEVANT FOR PERSISTENCE
   // ----------------------------------------------------------------------------

   public final short getDataTagChange() {
     return this.tagChange;
   }

  /**
   * @param unit the unit to set
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }

  /**
   * @param dipAddress the dipAddress to set
   */
  public void setDipAddress(String dipAddress) {
    this.dipAddress = dipAddress;
  }

  /**
   * @param japcAddress the japcAddress to set
   */
  public void setJapcAddress(String japcAddress) {
    this.japcAddress = japcAddress;
  }

  /**
   * @param valueDictionary the valueDictionary to set
   */
  public void setValueDictionary(DataTagValueDictionary valueDictionary) {
    this.valueDictionary = valueDictionary;
  }

  /**
   * @param alarmIds the alarmIds to set
   */
  public void setAlarmIds(Collection<Long> alarmIds) {
    this.alarmIds = alarmIds;
  }

  public String getRuleIdsString() {
    return ruleIdsString;
  }

  /**
   * See the comment in setRuleIdsString method, which
   * should preferably be used.
   * 
   * @param ruleIds the ruleIds to set
   */
  public void setRuleIds(Collection<Long> ruleIds) {
    this.ruleIds = ruleIds;
  }

  /**
   * Sets both the ruleIdsString and ruleIds fields.
   * 
   * <p>In this way, the two are always kept consistent
   * with each other. If there are no rules to evaluate,
   * the String field is always null (never empty String)
   * while the Array is an empty array.
   *
   * @param rulesIdsString the ids of rules in which this tag is used
   */
  public void setRuleIdsString(String ruleIdsString) {
    this.ruleIdsString = ruleIdsString;
    try {
      if (ruleIdsString != null && !ruleIdsString.isEmpty() ) {
        String[] ruleIdArray = ruleIdsString.split(",");
        setRuleIds(new ArrayList<Long>(ruleIdArray.length));
        for (int i = 0; i != ruleIdArray.length; i++) {
          if (!ruleIdArray[i].equals("")) {
            addRuleId(Long.valueOf(ruleIdArray[i].trim()));
          }
        }
      } else {
        setRuleIds(new ArrayList<Long>(0));
        this.ruleIdsString = null;
      }
    } catch (Exception e) {
      LOGGER.error("Exception caught while parsing the rule String field for tag " 
          + id + "; setting collection to empty collection.", e); 
      setRuleIds(new ArrayList<Long>(0));
      this.ruleIdsString = null;
    }
    
  }

  public void setStatus(DataTagConstants.Status status) {
    this.status = status;
  }

  public DataTagConstants.Status getStatus() {
    return status;
  }
   
  }
