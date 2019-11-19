package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.server.common.supervision.Supervised;

public interface SupervisionEventHandler<T extends Supervised> {

  void onUp(T supervised);

  void onDown(T supervised);
}
