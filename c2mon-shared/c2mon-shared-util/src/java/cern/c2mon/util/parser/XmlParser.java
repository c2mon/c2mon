package cern.c2mon.util.parser;

import org.w3c.dom.Document;

/**
 * Interface to the TIM XML parser.
 * @author Mark Brightwell
 *
 */
public interface XmlParser {

  /**
   * Parse XML string to document. Is thread safe (internally synchronized).
   * 
   * @param xmlString the xml String to parse
   * @return the parsed XML document
   * @throws ParserException if the parsing fails for some reason
   */
  Document parse(String xmlString) throws ParserException;

}
