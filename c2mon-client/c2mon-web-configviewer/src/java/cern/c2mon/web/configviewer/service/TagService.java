package cern.c2mon.web.configviewer.service;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagConfigImpl;
import cern.c2mon.web.configviewer.service.ServiceGateway;
import cern.c2mon.web.configviewer.util.XsltTransformUtility;
import cern.tim.shared.common.datatag.TagQualityStatus;

/**
 * Datatag service providing the XML representation of a given datatag
 * */
@Service
public class TagService {

  /**
   * TagService logger
   * */
  private static Logger logger = Logger.getLogger(TagService.class);
  
  /** the path to the xslt document */
  private static final String XSLT_PATH = "/datatag_xslt.xsl";

  /**
   * Gateway to C2monService 
   * */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Gets the XML representation of the current value of datatag
   * @param dataTagId id of the datatag
   * @return XML datatag value representation 
   * @throws TagIdException if the datatag was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getDataTagValueXml(final String dataTagId) throws TagIdException {
    try {
      ClientDataTagImpl value = (ClientDataTagImpl) getDataTagValue(Long.parseLong(dataTagId));
      if (value != null)
        return value.getXml();
      else
        throw new TagIdException("No datatag found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid datatag id");
    }
  }

  /**
   * Gets the XML representation of the datatag configuration
   * @param tagId id of the datatag
   * @return XML datatag config representation 
   * @throws TagIdException if the datatag was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */

  public String getDataTagConfigXml(final String tagId) throws TagIdException {
    try {
      TagConfigImpl config = (TagConfigImpl) getTagConfig(Long.parseLong(tagId));
      if (config != null)
        return config.getXml();
      else
        throw new TagIdException("No datatag found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid datatag id");
    }
  }
  
  public String generateDataTagConfigHtmlResponse(final String tagId) throws TransformerException, TagIdException {

    String html = null;
    String tagConfigXml = getDataTagConfigXml(tagId);

    try {
      html = XsltTransformUtility.performXsltTransformation(tagConfigXml, XSLT_PATH);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation.");
      throw new TransformerException("Error while performing xslt transformation.");
    }

    return html;
  }
  
  public String generateDataTagValueHtmlResponse(final String tagId) throws TransformerException, TagIdException {

    String html = null;
    String tagConfigXml = getDataTagValueXml(tagId);

    try {
      html = XsltTransformUtility.performXsltTransformation(tagConfigXml, XSLT_PATH);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation.");
      throw new TransformerException("Error while performing xslt transformation.");
    }

    return html;
  }

  /**
   * Retrieves a tagConfig object from the service gateway tagManager
   * @param tagId id of the datatag
   * @return tag configuration 
   * */
  private TagConfig getTagConfig(final long tagId) {
    TagConfig tc = null;
    List<Long> tagIds = new ArrayList<Long>();
    tagIds.add(tagId);
    Collection<TagConfig> tagConfigs = gateway.getTagManager().getTagConfigurations(tagIds);
    Iterator<TagConfig> it = tagConfigs.iterator();
    if (it.hasNext()) {
      tc = it.next();
    }
    logger.debug("Tag config fetch for tag " + tagId + ": " + (tc == null ? "NULL" : "SUCCESS"));
    return tc;
  }

  /**
   * Retrieves a tagValue object from the service gateway tagManager
   * @param dataTagId id of the datatag
   * @return tag value 
   * */
  private ClientDataTagValue getDataTagValue(final long dataTagId) {
    ClientDataTagValue dt = null;
    List<Long> tagIds = new ArrayList<Long>();
    tagIds.add(dataTagId);
    Collection<ClientDataTagValue> dataTags = gateway.getTagManager().getDataTags(tagIds); 
    Iterator<ClientDataTagValue> it = dataTags.iterator();
    if (it.hasNext()) {
      dt = it.next();
    } 
    logger.debug("Datatag value fetch for tag " + dataTagId + ": " + (dt == null ? "NULL" : "SUCCESS"));
    return dt;
  }
}


