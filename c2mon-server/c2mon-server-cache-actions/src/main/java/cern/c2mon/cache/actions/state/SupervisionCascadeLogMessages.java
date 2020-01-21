package cern.c2mon.cache.actions.state;

import cern.c2mon.server.common.supervision.SupervisionStateTag;

final class SupervisionCascadeLogMessages {

  private SupervisionCascadeLogMessages() {

  }

  static String autoAction(String action, SupervisionStateTag trigger) {
    return "Automatically " + action +" because of parent Equipment "
      + "(#" + trigger.getSupervisedId() + ") "
      + " switching state to "
      + trigger.getSupervisionStatus() + " on " + trigger.getStatusTime();
  }
}
