package cern.c2mon.web.configviewer.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.web.configviewer.service.ServiceGateway;
import cern.c2mon.web.configviewer.service.TagIdException;

/**
 * Command service providing the XML representation of a given alarm
 * */
@Service
public class CommandService {

  /**
   * CommandService logger
   * */
  private static Logger logger = Logger.getLogger(CommandService.class);

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/optimised_tag.xsl";

  /**
   * Gateway to C2monService 
   * */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Gets the XML representation of the configuration of a command
   * @param commandId id of the command
   * @return XML representation of command configuration 
   * @throws Exception if command was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getCommandTagXml(final String commandId) throws Exception {
    try {
      ClientCommandTagImpl command = (ClientCommandTagImpl) getCommandTag(Long.parseLong(commandId));
      if (command.isExistingCommand()) 
        return command.getXml();
      else
        throw new TagIdException("No command found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid command id");
    }
  }

  public String generateHtmlResponse(final String commandId) throws TagIdException {

    String xml = null;
    
    try {
      ClientCommandTagImpl command = (ClientCommandTagImpl) getCommandTag(Long.parseLong(commandId));
      if (command.isExistingCommand()) 
        xml = command.getXml();
      else
        throw new TagIdException("No command found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid command id");
    }

    return transformToHtml(xml);
  }

  /**
   * Transforms the xml to Html using xslt.
   * @param xml the xml
   * @return the html
   */
  private String transformToHtml(final String xml) {

    OutputStream ostream = null;

    try {

      InputStream xsltResource = getClass().getResourceAsStream(XSLT_PATH);
      Source xsltSource = new StreamSource(xsltResource);
      TransformerFactory transFact;
      Transformer trans = null;

      transFact = TransformerFactory.newInstance();

      Source xmlSource = new StreamSource(new StringReader(xml));

      trans = transFact.newTransformer(xsltSource);

      ostream = new ByteArrayOutputStream();
      trans.transform(xmlSource, new StreamResult((ostream)));

    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String result = ostream.toString();

    // a little hack to make firefox happy!
    result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",""); 

    return result;
  }

  /**
   * Retrieves a command tag object from the service gateway tagManager
   * @param commandId id of the alarm
   * @return command tag
   * */
  private ClientCommandTag<Object> getCommandTag(final long commandId) {
    ClientCommandTag<Object> ct = gateway.getCommandManager().getCommandTag(commandId); 
    logger.debug("Command fetch for command " + commandId + ": " + (ct == null ? "NULL" : "SUCCESS"));
    return ct;
  }
}
