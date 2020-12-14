/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.shared.client.tag;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import lombok.Data;

import cern.c2mon.shared.client.request.ClientRequestReport;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;
import cern.c2mon.shared.util.json.GsonFactory;

@Root(name = "TagConfig")
@Data
public class TagConfigImpl extends ClientRequestReport implements TagConfig {

  /** The unique tag id */
  @Attribute
  private Long id;

  /**
   * @see DataTagAddress
   */
  private short valueDeadbandType;

  /**
   * Only used for xml serialization,
   * to display valueDeadbandType as a string.
   * @see http://issues/browse/TIMS-665
   */
  @SuppressWarnings("unused")
  @Element(required = false)
  private String valueDeadbandLabel;

  /**
   * @see DataTagAddress
   */
  @Element(required = false)
  private float valueDeadband;

  /**
   * @see DataTagAddress
   */
  @Element(required = false)
  private int timeDeadband;

  /**
   * @see DataTagAddress
   */
  @Element(required = false)
  private int priority;

  /**
   * @see DataTagAddress
   */
  @Element(required = false)
  private boolean guaranteedDelivery;

  /**
   * Hardware address is transferred as XML String.
   */
  @Element(required = false, data = true)
  private String hardwareAddress;

  /**
   * All address information of the given DataTag
   * This is an central element for this class which provides the information for the daq to create an DataTag.
   */
  @ElementMap(name = "address-parameters", key="key", attribute=true, required = false)
  private Map<String, String> addressParameters = new HashMap<>();

  /**
   * List of rule ids which are using this tag.
   */
  @ElementList
  private ArrayList<Long> ruleIds = new ArrayList<Long>();

  /**
   * List of alarm ids which are defined for this tag.
   */
  @ElementList
  private ArrayList<Long> alarmIds = new ArrayList<Long>();

  /** In case of a rule tag this field should not be null */
  @Element(required = false)
  private String ruleExpressionStr;

  /**
   * Flag indicating whether this tag is a control tag.
   */
  @Element
  private Boolean controlTag = Boolean.FALSE;

  /**
   * Min and max values this Tag can take. Passed as Strings
   * as only for displaying.
   */
  @Element(required = false)
  private String minValue;

  @Element(required = false)
  private String maxValue;

  /**
   * String representation of the JMS destination where
   * <code>TransferTagValue</code> is published on change.
   */
  @Element(required = false)
  private String topicName;

  /** Publication topics to which the Tag is republished. */
  @ElementMap
  private Map<Publisher, String> publications = new HashMap<Publisher, String>();

  /** Is this Tag logged to the history */
  @Element(required = false)
  private Boolean logged;

  @ElementList(required = false)
  /** List of Process names this Tag is attached to */
  private List<String> processNames = new ArrayList<String>();

  /**
   * Default Constructor
   * @param pTagId The tag id to which that configuration belongs
   */
  public TagConfigImpl(final long pTagId) {
    id = pTagId;
  }

  /**
   * Private default constructor needed for JSON
   */
  @SuppressWarnings("unused")
  private TagConfigImpl() {
    id = -1L;
  }

  /**
   * @param alarmIds the alarmIds to set
   */
  public void setAlarmIds(List<Long> alarmIds) {
    if (alarmIds != null)
      this.alarmIds = new ArrayList<Long>(alarmIds);
    else {
      this.alarmIds = new ArrayList<Long>();
    }
  }

  /**
   * @param ruleIds List of rule ids which are using this tag
   */
  public void setRuleIds(final List<Long> ruleIds) {
    if (ruleIds != null)
      this.ruleIds = new ArrayList<Long>(ruleIds);
    else {
      this.ruleIds = new ArrayList<Long>();
    }
  }

  @Override
  public boolean isControlTag() {
    return controlTag;
  }

  /**
   * @param controlTag the controlTag to set
   */
  public void setControlTag(final Boolean controlTag) {
    this.controlTag = controlTag;
  }

  /**
   * @return the ruleIds
   */
  @Override
  public Collection<Long> getRuleIds() {
    return ruleIds;
  }

  /**
   * Adds the passed ids to the list of rule ids for this Tag.
   * @param ruleIds list of rule ids
   */
  public void addRuleIds(final Collection<Long> ruleIds) {
    this.ruleIds.addAll(ruleIds);
  }

  /**
   * Adds a publication to the transfer object.
   * @param publisher the publisher
   * @param topic the topic the Tag is published on
   */
  public void addPublication(final Publisher publisher, final String topic) {
    this.publications.put(publisher, topic);
  }

  @Override
  public Collection<Long> getAlarmIds() {
    return alarmIds;
  }

  @Override
  public String getDipPublication() {
    return publications.get(Publisher.DIP);
  }

  @Override
  public String getJapcPublication() {
    return publications.get(Publisher.JAPC);
  }

  @Override
  public String getRuleExpression() {
    return ruleExpressionStr;
  }

  public String getXml() {
    Serializer serializer = new Persister(new AnnotationStrategy());
    StringWriter fw = null;
    String result = null;

    try {
      fw = new StringWriter();
      serializer.write(this, fw);
      result = fw.toString();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  @org.simpleframework.xml.core.Persist
  public void prepare() {
    valueDeadbandLabel = ValueDeadbandType.getValueDeadbandType((int) valueDeadbandType).toString();
  }

  public static TagConfigImpl fromXml(final String xml) throws Exception {

    TagConfigImpl conf = null;
    StringReader sr = null;
    Serializer serializer = new Persister(new AnnotationStrategy());

    try {
      sr = new StringReader(xml);
      conf = serializer.read(TagConfigImpl.class, new StringReader(xml), false);
    } finally {

      if (sr != null) {
        sr.close();
      }
    }

    return conf;
  }

  @Override
  public String toString() {
    return this.getXml();
  }

  /**
   * Deserialized the JSON string into a <code>TagConfig</code> object instance
   * @param json A JSON string representation of a <code>TagConfigImpl</code> class
   * @return The deserialized <code>TagConfig</code> instance of the JSON message
   */
  public static TagConfig fromJson(final String json) {
    return GsonFactory.createGson().fromJson(json, TagConfig.class);
  }

  @Override
  public boolean isLogged() {
    return logged;
  }

  @Override
  public void setLogged(Boolean logged) {
    this.logged = logged;
  }
}
