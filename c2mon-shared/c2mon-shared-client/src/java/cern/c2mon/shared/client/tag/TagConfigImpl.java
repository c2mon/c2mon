package cern.c2mon.shared.client.tag;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.shared.client.request.ClientRequestReport;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.util.json.GsonFactory;

@Root(name = "TagConfig")
public class TagConfigImpl extends ClientRequestReport implements TagConfig {

  /** The unique tag id */
  @NotNull @Min(1) @Attribute
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

  /** Is this Tag logged to the short-term-log */
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
   * @param valueDeadbandType the valueDeadbandType to set
   */
  public void setValueDeadbandType(final short valueDeadbandType) {
    this.valueDeadbandType = valueDeadbandType;
  }

  /**
   * @param valueDeadband the valueDeadband to set
   */
  public void setValueDeadband(final float valueDeadband) {
    this.valueDeadband = valueDeadband;
  }

  /**
   * @param timeDeadband the timeDeadband to set
   */
  public void setTimeDeadband(final int timeDeadband) {
    this.timeDeadband = timeDeadband;
  }

  /**
   * @param priority the priority to set
   */
  public void setPriority(final int priority) {
    this.priority = priority;
  }

  /**
   * @param guaranteedDelivery the guaranteedDelivery to set
   */
  public void setGuaranteedDelivery(final boolean guaranteedDelivery) {
    this.guaranteedDelivery = guaranteedDelivery;
  }

  /**
   * @param hardwareAddress the hardwareAddress to set
   */
  public void setHardwareAddress(final String hardwareAddress) {
    this.hardwareAddress = hardwareAddress;
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

  /**
   * @param ruleExpressionStr the ruleExpressionStr to set
   */
  public void setRuleExpressionStr(final String ruleExpressionStr) {
    this.ruleExpressionStr = ruleExpressionStr;
  }

  /**
   * @param topicName the topicName to set
   */
  public void setTopicName(final String topicName) {
    this.topicName = topicName;
  }

  /**
   * @return the valueDeadbandType
   */
  @Override
  public short getValueDeadbandType() {
    return valueDeadbandType;
  }

  /**
   * @return the valueDeadband
   */
  @Override
  public float getValueDeadband() {
    return valueDeadband;
  }

  /**
   * @return the timeDeadband
   */
  @Override
  public int getTimeDeadband() {
    return timeDeadband;
  }

  /**
   * @return the priority
   */
  @Override
  public int getPriority() {
    return priority;
  }

  /**
   * @return the guaranteedDelivery
   */
  @Override
  public boolean isGuaranteedDelivery() {
    return guaranteedDelivery;
  }

  /**
   * @return the hardwareAddress
   */
  @Override
  public String getHardwareAddress() {
    return hardwareAddress;
  }

  @Override
  public Boolean isControlTag() {
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
  public String getMaxValue() {
    return maxValue;
  }

  @Override
  public String getMinValue() {
    return minValue;
  }

  /**
   * @param minValue the minValue to set
   */  
  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }

  /**
   * @param maxValue the maxValue to set
   */  
  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
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
  public Long getId() {
    return id;
  }

  @Override
  public String getJapcPublication() {
    return publications.get(Publisher.JAPC);
  }

  @Override
  public String getRuleExpression() {
    return ruleExpressionStr;
  }

  @Override
  public String getTopicName() {
    return topicName;
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

    valueDeadbandLabel = DataTagDeadband.toString(valueDeadbandType);
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
  public Boolean isLogged() {
    return logged;
  }

  @Override
  public void setLogged(Boolean logged) {
    this.logged = logged;
  }

  @Override
  public List<String> getProcessNames() {
    return processNames;
  }

  @Override
  public void setProcessNames(List<String> processNames) {
    this.processNames = processNames;
  }
}
