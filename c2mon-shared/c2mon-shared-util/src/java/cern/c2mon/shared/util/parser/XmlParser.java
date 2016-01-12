/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.shared.util.parser;

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
