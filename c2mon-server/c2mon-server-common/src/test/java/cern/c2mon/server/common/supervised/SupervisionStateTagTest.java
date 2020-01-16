package cern.c2mon.server.common.supervised;

import cern.c2mon.server.common.supervision.SupervisionStateTag;
import cern.c2mon.shared.common.supervision.SupervisionStatus;
import org.junit.Test;

import java.sql.Timestamp;

public class SupervisionStateTagTest {

  private static SupervisionStateTag sample = new SupervisionStateTag(0L, 1L, "EQ", null, null);

  @Test(expected = NullPointerException.class)
  public void setNullStatus() {
    sample.setSupervision(null,"", new Timestamp(1L));
  }

  @Test(expected = NullPointerException.class)
  public void setNullDescription() {
    sample.setSupervision(SupervisionStatus.RUNNING,null, new Timestamp(1L));
  }

  @Test(expected = NullPointerException.class)
  public void setNullTimestamp() {
    sample.setSupervision(SupervisionStatus.RUNNING,"", null);
  }
}
