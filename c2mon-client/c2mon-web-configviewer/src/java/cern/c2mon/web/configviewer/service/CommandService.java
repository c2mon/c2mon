package cern.c2mon.web.configviewer.service;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;
import cern.c2mon.web.configviewer.util.XsltTransformUtility;

/**
 * Command service providing the XML representation of a given tag
 * */
@Service
public class CommandService {

  /**
   * CommandService logger
   * */
  private static Logger logger = Logger.getLogger(CommandService.class);

  /** the path to the xslt document */
  private static final String XSLT_PATH = "/generic_tag.xsl";

  /**
   * Gateway to C2monService 
   * */
  @Autowired
  private ServiceGateway gateway;
  
  /**
   * Performs xslt transformations. 
   * */
  @Autowired
  private XsltTransformUtility xsltTransformer;

  /**
   * Gets the XML representation of the configuration of a command
   * @param commandId id of the command
   * @return XML representation of command configuration 
   * @throws TagIdException if command was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getCommandTagXml(final String commandId) throws TagIdException {
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

  public String generateHtmlResponse(final String commandId) 
    throws TagIdException, TransformerException {

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

    String html = null;

    try {
      html = xsltTransformer.performXsltTransformation(xml);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation.");
      throw new TransformerException("Error while performing xslt transformation.");
    }

    return html;
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
