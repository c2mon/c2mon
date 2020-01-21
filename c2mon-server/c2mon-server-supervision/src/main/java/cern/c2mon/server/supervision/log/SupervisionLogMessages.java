package cern.c2mon.server.supervision.log;

import cern.c2mon.server.common.commfault.CommFaultTag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.supervision.SupervisionEntity;

public final class SupervisionLogMessages {

  private SupervisionLogMessages() {

  }

  public static String eqCommFault(SourceDataTagValue sourceValue, SupervisionEntity type, CommFaultTag commFault, boolean isUp) {
    return eqCommFault(sourceValue.getValueDescription(), type.toString(), commFault.getName(), isUp);
  }

  public static String eqCommFault(String reason, String type, String eqInfo, boolean isUp) {
    StringBuilder builder = new StringBuilder()
      .append("Communication fault tag indicates that ")
      .append(type)
      .append(" ")
      .append(eqInfo)
      .append(" is")
      .append(isUp ? " up" : " down");
    if (!isUp && reason != null)
      builder.append(" Reason: ").append(reason);
    return builder.toString();
  }
}
