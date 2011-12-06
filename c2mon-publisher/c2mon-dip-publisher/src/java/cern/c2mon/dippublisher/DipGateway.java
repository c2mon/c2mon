/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.dippublisher;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.dippublisher.tools.TIMDriverHashtable;
import cern.dip.Dip;
import cern.dip.DipData;
import cern.dip.DipException;
import cern.dip.DipFactory;
import cern.dip.DipPublication;
import cern.dip.DipPublicationErrorHandler;
import cern.dip.DipTimestamp;

/**
 * This class represents the gateway between TIM system and DIP
 * The class responsibility is : 
 * - opening TIM.client's data tag connections
 * - listening for data tag updates and forwarding them via DIP
 */
public class DipGateway implements DataTagUpdateListener {

  /** The Log4j's logger */
  private static Logger logger = Logger.getLogger(DipGateway.class);

  public static final String FIELD_NAME = "value";
  public static final String TIMESTAMP_NAME = "timeNano";

  /**
   A list of registered TIMDipPublisherDatatag objects. Information
   gathered in those objects will be used for opening TIM client subscriptions as well
   as DIP publications
   */
  private TIMDriverHashtable registeredTags = new TIMDriverHashtable();

  /**
   A list of registered DIP publications
   */
  private TIMDriverHashtable dipPublications = new TIMDriverHashtable();

  private Collection tagCollection = null;

  private DipFactory dip = null;

  private C2monTagManager tagManager = null;


  /**
   * The default constructor
   * @param publisherName the publiser's name
   * @roseuid 414DA63700E4
   */
  public DipGateway(final String publisherName) {    
    
    dip = Dip.create(publisherName);
    
    C2monServiceGateway.startC2monClient();
    
    tagManager = C2monServiceGateway.getTagManager();
  }


  /**
   This method is responsible for opening and parsing the 
   file containing the list of all data-tag identifiers, that 
   TIMDIPPublisher should subscribe to and publish update via DIP
   @param dataTagsDoc 
   @roseuid 414DC44D0112
   */
  public void parseTags(final Document dataTagsDoc) {

    logger.debug("entering parseTags..");
    Element tagBlocs = (Element)dataTagsDoc.getElementsByTagName("DataTags").item(0);
    NodeList pdataTagUnits = tagBlocs.getElementsByTagName("DataTag");
    logger.debug("number of tags to register: " + pdataTagUnits.getLength());

    TIMDipPublisherDataTag pdataTag  = null;
    int tagsToRegister = pdataTagUnits.getLength();

    // for each equipment unit defined in the ProcessConfiguration XML
    for (int i = 0; i < pdataTagUnits.getLength(); i++) {
      pdataTag = TIMDipPublisherDataTag.createFromXML((Element) pdataTagUnits.item(i));
      if (pdataTag == null) {
        continue;      
      }
      else {
        // put created instance of TIMDipPublisherDataTag
        // into the hashtable (under unique eq identifier index)
        registeredTags.put(Long.valueOf(pdataTag.getTagID()), pdataTag);
      } 
    } // for


    logger.debug("number of tags properly registerd: " + registeredTags.size());
    if (tagsToRegister != registeredTags.size()) {
      logger.warn("Warning! : some tags could not be properly registered!");
    }


    logger.debug("leaving parseTags");
  }

  public void startPublications() throws DipException {
    
    logger.debug("entering startPublications..");
    // iterate through all table
    Enumeration e = this.registeredTags.elements();
    while (e.hasMoreElements()) {
      TIMDipPublisherDataTag ptag = (TIMDipPublisherDataTag) e.nextElement();
      try {
        logger.debug("creating DIP publication for topic: " + ptag.getDipPublicationTopic());
        DipPublication dpub = 
          dip.createDipPublication(ptag.getDipPublicationTopic(), new DIPErrHandler());
        this.dipPublications.put(new Long(ptag.getTagID()), dpub);  
      }
      catch (DipException ex) {
        logger.error("A problem with creating publication for tag id: " + ptag.getTagID() 
            + " occured!");
        logger.error(ex);
        throw ex;
      }
    }//while  

    logger.debug("leaving startPublications");     
  }

  public void createClientTags() throws Exception {

    subscribeDataTags(TIMDipPublisherDataTag.getRegisteredTagsIDs());
  }
  
  /**
   * Used to subscribe all data tags IDs that are specified in the
   * file.
   *  
   * @param dataTagSet a file that contains a list of data tag IDs
   * @return true, if subscription was successful
   */
  public final boolean subscribeDataTags(final Set dataTagSet) {
    
    logger.debug("subscribing DataTags..");
    boolean retval = false;
    
    if (dataTagSet != null) {    
      tagManager.subscribeDataTags(dataTagSet, this);
      retval = true;
    }
    
    logger.debug("subscribed DataTags");
    return retval;
  }



  /**
   * Called by ClientDataTag changes, prints out a line with 
   * the tag information in the format
   * <code>[<i>timestamp</i>]: <i>tagId</i> : <i>tagName</i> : <i>tagValue</i>
   * : valid=<i>tagValidity</i> : <i>qualityDescriptio</i>
   * @param tag the changed ClientDataTag
   */
  @Override
  public void onUpdate(final ClientDataTagValue tag) {
    logger.debug("entering onUpdate..");

    if (logger.isDebugEnabled()) {
      StringBuffer str = new StringBuffer("received ClientDataTag update [\n");
      str.append(" id:" + tag.getId() + "\n");
      str.append(" name: " + tag.getName() + "\n");
      str.append(" type: " + tag.getType() + "\n");
      str.append(" value: " + tag.getValue() + "\n");
      str.append(" valueDescr: " + tag.getDescription() + "\n");    
      str.append(" isValid: " + tag.isValid() + "\n");
      str.append(" qdescription: " + tag.getDataTagQuality().getDescription() + "\n");
      if (tag.getDataTagQuality() != null)
        str.append(" qcode: " + tag.getDataTagQuality().toString() + "\n]");

      logger.debug(str.toString());        
    }

    DipPublication pub = (DipPublication) this.dipPublications.get(tag.getId());

    logger.debug("quality : " + tag.getDataTagQuality().getDescription()
        + " isValid: "
        + tag.getDataTagQuality().isValid());

    DipTimestamp ts = new DipTimestamp();
    ts.setMillis(tag.getTimestamp().getTime());
    DipData data = dip.createDipData();

    if (tag.getDataTagQuality().isValid()) {
      try {
        if (tag.getType().equals(Boolean.class)) {
          logger.debug("\ttag's type: java.lang.Boolean");        
          data.insert(FIELD_NAME, ((Boolean) tag.getValue()).booleanValue());            
        }
        else if (tag.getType().equals(Integer.class)) {
          logger.debug("\ttag's type: java.lang.Integer");        
          data.insert(FIELD_NAME, ((Integer) tag.getValue()).intValue());             
        }
        else if (tag.getType().equals(Long.class)) {
          logger.debug("\ttag's type: java.lang.Long");        
          data.insert(FIELD_NAME, ((Long) tag.getValue()).longValue());             
        }
        else if (tag.getType().equals(Float.class)) {
          logger.debug("\ttag's type: java.lang.Float");        
          data.insert(FIELD_NAME, ((Float) tag.getValue()).floatValue());  
        }
        else if (tag.getType().equals(Double.class)) {
          logger.debug("\ttag's type: java.lang.Double");        
          data.insert(FIELD_NAME, ((Double) tag.getValue()).doubleValue());             
        }
        else if (tag.getType().equals(String.class)) {
          logger.debug("\ttag's type: java.lang.String");        
          data.insert(FIELD_NAME, (String) tag.getValue());             
        }            
        else {
          logger.error("\ttag's type is UNKNOWN!");
          logger.info("\tthe tag update will not be forwarded to DIP"); 
        }                     

        // putting the source timestamp in ns
        //data.insert(TIMESTAMP_NAME,tag.getTimestamp().getTime()*1000000);

        logger.debug("\tsending the value via DIP");
        pub.send(data, ts);

      }
      catch (cern.dip.TypeMismatch ex) {
        logger.error("TypeMismatch exception was caught");
        logger.error(ex);
      }
      catch (DipException ex) {
        logger.error("Failed do send data on DIP");
      } 
      catch (Exception ex) {
        logger.error("An exception was caught!");
        logger.error(ex);
      }
    }
    else { //the quality <> OK
      try {
        logger.debug("setting publication QUALITY to BAD");        
        if (!tag.isValid()) {
          pub.setQualityBad();
        }
        else 
          if (tag.getDataTagQuality().getDescription() != null) {
            logger.debug("quality description : " + tag.getDataTagQuality().getDescription());        
            pub.setQualityBad(tag.getDataTagQuality().getDescription());
          }
          else {
            pub.setQualityBad();
          }

      }
      catch (DipException ex) {
        logger.error("DipException was caught");
        logger.error(ex);
      }
      catch (Exception ex) {
        logger.error("An exception was caught");
        logger.error(ex);
      }
    }

    logger.debug("leaving onUpdate");      
  }



  /**
   * This class implements DIP error handler
   */
  private static class DIPErrHandler implements DipPublicationErrorHandler {
    /**
     * This method implements the error handler
     */
    public void handleException(final DipPublication dp, final DipException de) {
      logger.error("Publication source "+dp.getTopicName() +" has error " + de.getMessage());
    }
  } 
}



