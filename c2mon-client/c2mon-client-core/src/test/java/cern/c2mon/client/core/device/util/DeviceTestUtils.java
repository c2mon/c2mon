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
package cern.c2mon.client.core.device.util;

import java.sql.Timestamp;

import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

/**
 * @author Justin Lewis Salmon
 */
public class DeviceTestUtils {

  public static TagUpdate createValidTransferTag(final Long tagId, final String tagName) {
    return createValidTransferTag(tagId, tagName, Float.valueOf(1.234f));
  }

  public static TagUpdate createValidTransferTag(final Long tagId, final String tagName, Object value) {
    DataTagQuality tagQuality = new DataTagQualityImpl();
    tagQuality.validate();
    TransferTagImpl tagUpdate =
      new TransferTagImpl(
          tagId,
          value,
          "test value desc",
          (DataTagQualityImpl) tagQuality,
          TagMode.TEST,
          new Timestamp(System.currentTimeMillis() - 10000L),
          new Timestamp(System.currentTimeMillis() - 5000L),
          new Timestamp(System.currentTimeMillis()),
          "Test description",
          tagName,
          "My.jms.topic");
    tagUpdate.setValueClassName(value.getClass().toString());
    return tagUpdate;
  }
}
