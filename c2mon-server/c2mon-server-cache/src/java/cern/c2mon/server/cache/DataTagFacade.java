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
package cern.c2mon.server.cache;

import java.sql.Timestamp;
import java.util.Properties;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.DataTagUpdate;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;


/**
 * Facade bean used to access DataTag objects in the cache (including ControlTag objects, which extend
 * DataTag).
 * 
 * Notice that the methods that take a DataTag object as parameter assume
 * this is a reference to the object in the cache (although they will also work
 * with objects that do not reside in the cache). On the other hand, methods that take
 * the cache object id as a parameter will retrieve the reference in the cache. 
 * 
 * <p>All Synchronize on the DataTag object lock.
 * 
 * <p>Calls to most invalidate and update methods set the cache timestamp to the current
 * time, with both src and DAQ timestamps set to null. Only the <java>updateFromSource</java> 
 * method sets the src and DAQ timestamp as those received from the DAQ and the cache timestamp 
 * to the current time. 
 * 
 * Methods in this class are also responsible for notifying the listeners that an update has
 * occurred.
 * 
 * @author Mark Brightwell
 *
 */
public interface DataTagFacade extends CommonTagFacade<DataTag> {

//  void updateAndEvaluateAlarms(DataTag dataTag, SourceDataTagValue sourceDataTagValue);
  
  /**
   * Updates the DataTag in the cache from the passed SourceDataTagValue. The method notifies
   * any cache listeners if an update is made. 
   * 
   * <p>The cache timestamp is set to the current time. The DAQ and source timestamps are
   * set to the values received in the SourceDataTagValue.
   * 
   * @param dataTagId id of DataTag
   * @param sourceDataTagValue the value received from the data acquisition layer
   * @return true if the tag was indeed updated (that is, the cache was modified, i.e. the update was not
   * filtered out for some reason), together with the cache timestamp of this update
   * @throws CacheElementNotFoundException if the Tag cannot be found in the cache
   */
  Event<Boolean> updateFromSource(Long dataTagId, SourceDataTagValue sourceDataTagValue);
  
  /**
   * Validates the DataTag and updates it with the provided value. Should be used
   * to update values within the server (only cache timestamp is set).
   * 
   * <p>The cache timestamp is set to the passed time and the DAQ and source timestamps are
   * reset to null.
   * 
   * <p>If the update causes no changes, the cache object is not updated (see filterout method in AbstracTagFacade).
   * 
   * <p>Notifies registered listeners if an update takes place.
   * 
   * @param dataTag usually a reference to the DataTag object in the cache
   * @param value new DataTag value
   * @param valueDescription description of the new value
   * @param timestamp time of the update
   */
  void updateAndValidate(DataTag dataTag, Object value, String valueDescription, Timestamp timestamp);
  
  /**
   * Same as other updateAndValidate method but takes a tag id as parameter and does the cache lookup
   * in the method.
   * 
   * <p>The cache timestamp is set to the current time and the DAQ and source timestamps are
   * reset to null.
   * 
   * <p>If the update causes no changes, the cache object is not updated (see filterout method in AbstracTagFacade).
   * 
   * <p>Notifies registered listeners if an update takes place.
   * 
   * @param dataTagId the id of the tag to update
   * @param value the new tag value
   * @param valueDescription the description of the new value (if any)
   * @param timestamp the time of the update
   */
  void updateAndValidate(Long dataTagId, Object value, String valueDescription, Timestamp timestamp);

  /**
   * Generates the SourceDataTag XML String for a DataTag
   * with the given id.
   * @param tagId the id of the Tag in the cache
   * @return the XML as String
   */
  String getConfigXML(Long tagId);

  /**
   * Public method returning the configuration XML string for a given {@link DataTag}.
   * 
   * @param dataTag the cache object
   * @return the XML string
   */
  String generateSourceXML(DataTag dataTag);

  /**
   * Generates a SourceDataTag from the server DataTag object.
   * Used when adding a new DataTag to the driver at runtime.
   * @param dataTag the cache object to forward to the DAQ
   * @return the SourceDataTag used by the DAQ
   */
  SourceDataTag generateSourceDataTag(DataTag dataTag);
  
  /**
   * Logs the object in a log file (used when a change is made to the cache object).
   * @param dataTagCacheObject the cache object to log to the file 
   */
  void log(DataTagCacheObject dataTagCacheObject);
  
  /**
   * Determines whether the DataTag is in UNCONFIGURED mode (which corresponds
   * to a DataTag whose configuration was not loaded from the database at
   * startup but has been added to the cache since then.
   * @param dataTag the DataTag to check the status of
   * @return true if the DataTag is in UNCONFIGURED mode
   */
  boolean isUnconfiguredTag(DataTag dataTag);
  
  /**
   * Allows modifications to the DataTagQuality of the DataTag: the desciption can be changed
   * and quality flags can be removed an added at will.
   * @param dataTag
   * @param qualityDescription
   */
  
  //void setQualityDescription(DataTag dataTag, String message);
  
  
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
   * 
   * @param id of the element to create
   * @param properties the Properties object from which the object must be created
   * @return the create cache object
   * @throws ConfigurationException if the resulting cache object fails the validation checks
   */
  //DataTag createCacheObject(Long dataTagId, Properties properties) throws ConfigurationException;
  
  /**
   * Updates configuration fields of the cache object with those contained
   * in the properties object. By "configuration fields" we are referring
   * to fields open to reconfiguration during runtime. In particular, values 
   * and timestamps are not modified by this method: updates due to incoming
   * data should be processed using the other update and invalidate methods.
   * 
   * <p>This method is thread-safe (it performs the required synchronization
   * on the cache object residing in the cache.
   * 
   * <p>This method should preferably be performed on an object outside the cache
   * before being applied to the object residing in the cache, since changes cannot
   * be rolled back if the validation fails.
   * 
   * <p>The returned change object can be used to inform the data when an update is
   * performed (not used during DataTag creation).
   * 
   * throws ConfigurationException if the reconfigured cache object fails validation checks (unchecked)
   * throws IllegalArgumentException thrown when creating DAQ change event for HardwareAddress (unchecked)
   * 
   * @param dataTag the cache object to reconfigure (the object is modified by this method)
   * @param properties the properties that need reconfiguring
   * @return a DataTagUpdate object with the changes needed to be passed to the DAQ
   * @throws IllegalAccessException thrown when creating DAQ change event for HardwareAddress 
   */
  DataTagUpdate configureCacheObject(DataTag dataTag, Properties properties) 
            throws IllegalAccessException;

  /**
   * Checks the cache object is properly configured (field format checks).
   * 
   * throws {@link ConfigurationException} if validation fails (unchecked)
   * @param dataTag the cache object to validate
   */
  void validateConfig(DataTag dataTag);

  /**
   * Notifies listeners.
   * @param dataTag
   * @param ruleTagId
   */
  void addDependentRule(DataTag dataTag, Long ruleTagId);
}
