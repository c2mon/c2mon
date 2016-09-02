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
package cern.c2mon.shared.client.configuration.api.tag;


import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import cern.c2mon.shared.client.configuration.api.util.ConfigurationEntity;
import cern.c2mon.shared.client.configuration.api.util.DefaultValue;
import cern.c2mon.shared.client.configuration.api.util.IgnoreProperty;
import cern.c2mon.shared.client.expression.Expression;
import cern.c2mon.shared.client.metadata.Metadata;
import cern.c2mon.shared.client.tag.TagMode;

/**
 * Tag class which holds the information to create a {@link cern.c2mon.shared.client.configuration.ConfigurationElement}
 * related to all Tags.
 *
 * @author Franz Ritter
 */
@Data
public abstract class Tag implements ConfigurationEntity {

  @IgnoreProperty
  private boolean updated = false;

  @IgnoreProperty
  private boolean created = false;

  /**
   * determine if the instance of this class defines a DELETE command
   */
  @IgnoreProperty
  private boolean deleted;

  /**
   * Unique datatag identifier (unique across all types of tags: control,
   * datatag and rules).
   */
  @IgnoreProperty
  private Long id;

  /**
   * Unique tag name.
   */
  private String name;

  /**
   * Free-text description of the tag
   */
  @DefaultValue("<no description provided>")
  private String description;

  /**
   * Indicates whether a tag is "in operation", "in maintenance" or "in test".
   */
  @DefaultValue("OPERATIONAL")
  private TagMode mode;

  /**
   * Meta data of the tag object. Holds arbitrary data which are related to the given Tag.
   */
  private Metadata metadata;

  /**
   * All expressions which are attached to the tag.
   */
  private List<Expression> expressions;

  public Tag() {
  }

  public void addExpression(Expression expression){
    this.expressions = expression == null ? new ArrayList<>() : this.expressions;
    this.expressions.add(expression);
  }

}
