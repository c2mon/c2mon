/**
 *
 */
package cern.c2mon.client.ext.device.util;

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
    TagUpdate tagUpdate =
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

    return tagUpdate;
  }
}
