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
package cern.c2mon.client.core.device.property;

import java.util.Collection;
import java.util.Set;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.client.core.tag.ClientRuleTag;
import cern.c2mon.client.core.tag.TagImpl;

/**
 * This class implements the {@link BaseProperty} interface and provides the
 * base functionality shared by both properties and fields.
 *
 * @author Justin Lewis Salmon
 */
public class BasePropertyImpl implements BaseProperty {

  /**
   * The name of the property. A property is unique within a device class.
   */
  private String name;

  /**
   * The category of this property, e.g. tag id, constant, etc.
   */
  private Category category;

  /**
   * The ID of the {@link Tag} (if applicable).
   */
  private Long tagId;

  /**
   * The actual value (may be null if the property is a {@link Tag}
   * and has not yet been lazily loaded).
   */
  private Tag tag;

  /**
   * Reference to the {@link TagService}, used to lazy-load data tags.
   */
  protected TagService tagService;

  /**
   * Constructor used to create an instance containing only a tag ID, to be
   * lazily loaded later.
   *
   * @param name the name of the property
   * @param category the category of this property
   * @param tagId the ID of the {@link Tag} corresponding to this
   *          property
   */
  public BasePropertyImpl(final String name, final Category category, final Long tagId) {
    this.name = name;
    this.category = category;
    this.tagId = tagId;
  }

  /**
   * Constructor used to create an instance containing a device property that
   * will not be lazily loaded ({@link ClientRuleTag} or
   * {@link ClientConstantValue}).
   *
   * @param name the name of the property
   * @param category the category of this property
   * @param Tag the client device property to set
   */
  public BasePropertyImpl(final String name, final Category category, final Tag Tag) {
    this.name = name;
    this.category = category;
    this.tag = Tag;
    if (isDataTag()) {
      this.tagId = Tag.getId();
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Category getCategory() {
    return category;
  }

  @Override
  public Long getTagId() {
    return tagId;
  }

  @Override
  public Tag getTag() {
    // Load the value (either lazy-load the data tag or evaluate the rule)
    setTag(loadTag());
    return this.tag;
  }

  /**
   * Retrieve the internal value of this property (may be null).
   *
   * @return the internal {@link Tag} instance, or null if it has
   *         not yet been loaded
   */
  public Tag getValue() {
    return tag;
  }

  /**
   * Check if this property points to a {@link Tag}.
   *
   * @return true if this property is a data tag, false otherwise
   */
  public boolean isDataTag() {
    return tag instanceof TagImpl || tagId != null;
  }

  /**
   * Check if this property points to a {@link ClientRuleTag}.
   *
   * @return true if this property is a rule tag, false otherwise.
   */
  public boolean isRuleTag() {
    return tag instanceof ClientRuleTag;
  }

  /**
   * Check if the data tag corresponding to this property has been subscribed to
   * (not applicable for tags other than {@link Tag}.
   *
   * @return true if the property tag has been subscribed to, false otherwise
   */
  public boolean isValueLoaded() {
    return isDataTag() && tag != null;
  }

  /**
   * Set the internal {@link Tag}.
   *
   * @param value the {@link Tag} to set
   */
  protected void setTag(Tag value) {
    this.tag = value;
  }

  /**
   * Perform the necessary tasks to fully instantiate the value of the property.
   * In the case of a data tag, this means lazy-loading its
   * {@link Tag} from the server (if it hasn't already been
   * subscribed to). In the case of a {@link ClientRuleTag}, the dependent tags
   * will be retrieved from the server and the rule will be evaluated.
   *
   * @return the new value
   */
  private Tag loadTag() {
    Tag value = this.tag;

    if (this.tagService == null) {
      this.tagService = C2monServiceGateway.getTagService();
    }

    // If the internal value is a Long, then we lazy load the data tag
    if (isDataTag() && !isValueLoaded()) {
      value = tagService.get(getTagId());
    }

    // If it is a rule tag, we evaluate the rule (if it isn't subscribed)
    else if (isRuleTag() && !tagService.isSubscribed((BaseTagListener) this.tag)) {

      // Get the data tag values from inside the rule
      Set<Long> tagIds = value.getRuleExpression().getInputTagIds();
      Collection<Tag> dataTagValues = tagService.get(tagIds);

      // Update the rule tag
      for (Tag tagValue : dataTagValues) {
        ((ClientRuleTag) value).onUpdate(tagValue);
      }
    }

    return value;
  }

  /**
   * Manually set the reference to the {@link TagService} on the property.
   * Used for testing purposes.
   *
   * @param tagManager the tag manager to use
   */
  public void setTagManager(TagService tagManager) {
    this.tagService = tagManager;
  }
}
