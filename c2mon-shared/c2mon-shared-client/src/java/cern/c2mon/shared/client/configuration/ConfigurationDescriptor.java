/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.client.configuration;

import java.sql.Date;

/**
 * Object representing a (re-)configuration task.
 * 
 * <p>Imported as-is from TIM1.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigurationDescriptor {

  /**
   * Unique identifier of the configuration
   */
  private long id = -1;

  /**
   * Name of the configuration
   */
  private String name = null;

  /**
   * Description of the configuration
   */
  private String description = null;
  
  /**
   * User name of the configuration creator
   */
  private String author = "nobody";
  
  /**
   * Creation date
   */
  private Date creationDate = null;
  
  /**
   * Constructor
   * @param pId config id
   * @param pName config name 
   * @param pDescription description
   * @param pAuthor user who generated configuration
   * @param pCreationDate the data the configuration was created
   */
  public ConfigurationDescriptor(
    final long   pId,
    final String pName,
    final String pDescription,
    final String pAuthor,
    final Date   pCreationDate
  ) {
    this.id           = pId;
    this.name         = pName;
    this.description  = pDescription;
    this.author       = pAuthor;
    this.creationDate = pCreationDate;
  }
  
  /**
   * Getter
   * @return the config id
   */
  public long getId() {
    return this.id;
  }
  
  /**
   * Setter.
   * @param pId the id
   */
  public void setId(final long pId) {
    this.id = pId;
  }
  
  /**
   * Getter
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * Setter
   * @param pName the name to set
   */
  public void setName(final String pName) {
    this.name = pName;
  }
  
  /**
   * Getter.
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }
  
  /**
   * Setter.
   * @param pDescription description to set
   */
  public void setDescription(final String pDescription) {
    this.description = pDescription;
  }
  
  /**
   * Getter
   * @return the author
   */
  public String getAuthor() {
    return this.author;
  }
  
  /**
   * Setter.
   * @param pAuthor the author to set
   */
  public void setAuthor(final String pAuthor) {
    this.author = pAuthor;
  }
  
  /**
   * Getter.
   * @return the creation date
   */
  public Date getCreationDate() {
    return this.creationDate;
  }
  
  /**
   * Setter.
   * @param pCreationDate the date
   */
  protected void setCreationDate(final Date pCreationDate) {
    this.creationDate = pCreationDate;
  }
  
  /**
   * Returns an XML description of the Configuration.
   * @return XML as String
   */
  public String toXML() {
    StringBuffer result = new StringBuffer(200);

    result.append("  <Configuration>\n");
    
    result.append("  <id>");
    result.append(this.id);
    result.append("</id>\n");

    result.append("  <name>");
    result.append(this.name);
    result.append("</name>\n");

    result.append("  <description>");
    result.append(this.description);
    result.append("</description>\n");

    result.append("  <author>");
    result.append(this.author);
    result.append("</author>\n");
    
    result.append("  <creation-date>");
    result.append(this.creationDate.toString());
    result.append("</creation-date>\n");

    result.append("  </Configuration>\n");
    return result.toString();
  }
}
