
// TIM DIPPublisher. CERN. All rights reserved.
//  
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D wbuczak    19/Sep/2004       Class generation from model
// P wbuczak    20/Sep/2004       First implementation
// -------------------------------------------------------------------------

package cern.c2mon.dippublisher;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

/**
This class represents a tag-definition read from the TIMDipPublisher's xml
conf. file
*/
public class TIMDipPublisherDataTag {
      
  /**
  Unique data-tag identifier
  */
  private long tagID;
      
      
  /**
  data-tag comment (may not be set)
  */
  private String comment;


  /**
  the topic name under which this tag will be accessible on DIP
   */
  private String dipPublicationTopic;

  private static Set<Long> registeredTagsIDs = null;

  /**
  The constructor (from XML block)
  @param tagnode the data-tag xml node
  */
  public static final TIMDipPublisherDataTag createFromXML(final Element tagNode) {

     if (registeredTagsIDs == null) {
       registeredTagsIDs = new HashSet<Long>();
     }
     
     TIMDipPublisherDataTag dt = null;
     long id;
     String topic = "";
     String comment = "";
     
     id = Long.parseLong(tagNode.getElementsByTagName("id").item(0).getFirstChild().getNodeValue());
     topic = tagNode.getElementsByTagName("dip-topic").item(0).getFirstChild().getNodeValue();
     if (tagNode.getElementsByTagName("comment").getLength() > 0)
       comment = tagNode.getElementsByTagName("comment").item(0).getFirstChild().getNodeValue();

     if (comment.length() > 0) {
       dt = new TIMDipPublisherDataTag(id, topic, comment);
     }
     else {
       dt = new TIMDipPublisherDataTag(id, topic);
     }
     
     return dt;
  }

  /**
   * 
   * @return a set of all registered tag ids
   */
  public static Set<Long> getRegisteredTagsIDs() {
    return registeredTagsIDs;
  }

  /**
  The constructor
  @param id
  @param name
  @roseuid 414DC58D03BC
  */
  public TIMDipPublisherDataTag(final long id, final String name) {
    
     this(id, name, null);       
  }
      


  /**
  The constructor
  @param id
  @param name
  @param comment
  @roseuid 414DC58C0389
  */
  public TIMDipPublisherDataTag(long id, String topic, String comment) {
     this.tagID = id;
     this.dipPublicationTopic = topic;
     this.comment = comment;       
     TIMDipPublisherDataTag.registeredTagsIDs.add(new Long(id));
  }
      

  /**
  This method returns tag identifier
  @return long
  @roseuid 414DC58E022D
  */
  public long getTagID() {
     return this.tagID;       
  }
      

  /**
  This method returns tag's comment
  @return java.lang.String
  @roseuid 414DC58E02EB
  */
  public String getTagComment() {
     return this.comment;       
   }


  /**
  This method sets the DIP publication topic name for the tag
  @param topicName the topic name
   */
  public void setDipPublicationTopic(final String topicName) {
    this.dipPublicationTopic = topicName;
  }


  /**
  This method return the DIP publication topic name for the tag
  @return java.lang.String the topic name
   */
  public String getDipPublicationTopic() {
    return this.dipPublicationTopic;
  }
  
}
