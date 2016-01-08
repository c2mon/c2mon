package cern.c2mon.shared.client.alarm;

import java.io.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.shared.client.request.ClientRequestReport;

/**
 * This bean class implements the <code>AlarmValue</code> interface
 * and is used to transport an alarm value update information
 * from the server to the client. The <code>AlarmValue</code> is embedded
 * into the <code>TransferTagValue</code> object.
 *
 * @author Matthias Braeger
 * 
 * @see cern.c2mon.shared.client.alarm.AlarmValue
 * @see cern.c2mon.shared.client.tag.TagValueUpdate
 */
@Root(name = "AlarmValue")
@Slf4j
public final class AlarmValueImpl extends ClientRequestReport implements AlarmValue, Cloneable {

  /** Alarm id */
  @NotNull @Min(1)
  @Attribute
  private Long id;
  
  /** LASER alarm fault code */
  @NotNull
  @Element
  private int faultCode;
  
  /** LASER alarm fault family */
  @NotNull
  @Element
  private String faultFamily;
  
  /** LASER alarm fault member */
  @NotNull
  @Element
  private String faultMemeber;
  
  /** Free text for additional information about the alarm */
  @Element(required = false)
  private String info;
  
  /** Unique identifier of the Tag to which the alarm is attached */
  @NotNull @Min(1)
  @Element
  private Long tagId;
  
  /** Description for the Tag to which the alarm is attached */
  @Element(required = false)
  private String tagDescription;  
  
  /** UTC timestamp of the alarm's last state change */
  @NotNull @Past
  @Element
  private Timestamp timestamp;
  
  /** <code>true</code>, if the alarm is active */
  @Element
  private boolean active;

  /**
   * Metadata according to the tag in this class.
   */
  @NotNull
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Hidden Constructor needed for JSON
   */
  private AlarmValueImpl() {
    this(null, -1, null, null, null, null, null, false);
  }
  
  /**
   * Default Constructor
   * @param pId Alarm id
   * @param pFaultCode LASER alarm fault code
   * @param pFaultMember LASER alarm fault memeber
   * @param pFaultFamily LASER alarm fault family
   * @param pInfo Free text for additional information about the alarm
   * @param pTagId Unique identifier of the Tag to which the alarm is attached
   * @param pTimestamp UTC timestamp of the alarm's last state change
   * @param isActive <code>true</code>, if the alarm is active
   */
  public AlarmValueImpl(final Long pId,
                        final int pFaultCode,
                        final String pFaultMember,
                        final String pFaultFamily,
                        final String pInfo,
                        final Long pTagId,
                        final Timestamp pTimestamp,
                        final boolean isActive) {
    id = pId;
    faultCode = pFaultCode;
    faultMemeber = pFaultMember;
    faultFamily = pFaultFamily;
    info = pInfo;
    tagId = pTagId;
    timestamp = pTimestamp;
    active = isActive;
  }
  
  /**
   * Default Constructor
   * @param pId Alarm id
   * @param pFaultCode LASER alarm fault code
   * @param pFaultMember LASER alarm fault memeber
   * @param pFaultFamily LASER alarm fault family
   * @param pInfo Free text for additional information about the alarm
   * @param pTagId Unique identifier of the Tag to which the alarm is attached
   * @param pTagDescription Description for the Tag to which the alarm is attached
   * @param pTimestamp UTC timestamp of the alarm's last state change
   * @param isActive <code>true</code>, if the alarm is active
   */
  public AlarmValueImpl(final Long pId,
                        final int pFaultCode,
                        final String pFaultMember,
                        final String pFaultFamily,
                        final String pInfo,
                        final Long pTagId,
                        final String pTagDescription,
                        final Timestamp pTimestamp,
                        final boolean isActive) {
    id = pId;
    faultCode = pFaultCode;
    faultMemeber = pFaultMember;
    faultFamily = pFaultFamily;
    info = pInfo;
    tagId = pTagId;
    tagDescription = pTagDescription;
    timestamp = pTimestamp;
    active = isActive;
  }
  
  @Override
  public int getFaultCode() {
    return faultCode;
  }

  @Override
  public String getFaultFamily() {
    return faultFamily;
  }

  @Override
  public String getFaultMember() {
    return faultMemeber;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public String getInfo() {
    return info;
  }

  @Override
  public Long getTagId() {
    return tagId;
  }

  @Override
  public Timestamp getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AlarmValueImpl other = (AlarmValueImpl) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public AlarmValue clone() throws CloneNotSupportedException {
    AlarmValueImpl clone = (AlarmValueImpl) super.clone();
    clone.timestamp = (Timestamp) timestamp.clone();
    clone.metadata = new HashMap<>();
    for(Map.Entry<String, Object> entry : metadata.entrySet()) {
      clone.metadata.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
    }

    return clone;
  }

  private <T> T deepClone(T o) {
    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream out = null;
      out = new ObjectOutputStream(byteOut);
      out.writeObject(o);
      out.flush();
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
      return (T) o.getClass().cast(in.readObject());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    log.error("Cloning of in metadata failed. the Object ist not serializable");
    throw  new RuntimeException("Cloning of in metadata failed. the Object ist not serializable");
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
  
  
  public static AlarmValueImpl fromXml(final String xml) throws Exception {

      AlarmValueImpl alarmVal = null;
      StringReader sr = null;
      Serializer serializer = new Persister(new AnnotationStrategy());

      try {
          sr = new StringReader(xml);
          alarmVal = serializer.read(AlarmValueImpl.class, new StringReader(xml), false);
      } finally {

          if (sr != null) {
              sr.close();
          }
      }

      return alarmVal;
  }
  
  
  @Override
  public String toString() {
      return this.getXml();
  }

  @Override
  public boolean isMoreRecentThan(final AlarmValue alarm) {
    
    return this.getTimestamp().after(alarm.getTimestamp());
  }

  @Deprecated
  public void setTagDescription(String tagDescription) {
    this.tagDescription = tagDescription;
  }

  @Deprecated
  @Override
  public String getTagDescription() {
    return tagDescription;
  }

  /**
   * Set the field metadata.
   * @param metadata the data to set.
   */
  public void setMetadata(Map<String, Object> metadata){
    this.metadata = metadata;
  }

  /**
   * Returns the metadata to the corresponding tag.
   * @return the metadata of the object.
   */
  public Map<String, Object> getMetadata(){
    return this.metadata;
  }
}
