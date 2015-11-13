package cern.c2mon.server.configuration.parser.tasks.util;

import cern.c2mon.shared.client.configuration.api.Configuration;

/**
 * Defines the Order of all {@link cern.c2mon.server.configuration.parser.tasks.SequenceTask}.
 * Changing the values in the ENUM changes also the order of the result of the
 * {@link cern.c2mon.server.configuration.parser.ConfigurationParser#parse(Configuration)} method.
 *
 * @author Franz Ritter
 */
public enum TaskOrder {
  COMMANDTAG_DELETE(0),
  ALARM_DELETE(1),
  RULETAG_DELETE(10),
  DATATAG_DELETE(20),
  SUBEQUIPMENT_DELETE(30),
  EQUIPMENT_DELETE(40),
  PROCESS_DELETE(50),
  CONTROLTAG_DELETE(60),

  CONTROLTAG_CREATE(100),
  PROCESS_CREATE(200),
  EQUIPMENT_CREATE(300),
  SUBEQUIPMENT_CREATE(400),
  DATATAG_CREATE(500),
  RULETAG_CREATE(600),
  ALARM_CREATE(700),
  COMMANDTAG_CREATE(800),

  CONTROLTAG_UPDATE(1000),
  PROCESS_UPDATE(2000),
  EQUIPMENT_UPDATE(3000),
  SUBEQUIPMENT_UPDATE(4000),
  DATATAG_UPDATE(5000),
  RULETAG_UPDATE(6000),
  ALARM_UPDATE(7000),
  COMMANDTAG_UPDATE(8000);

  private final int order;

  TaskOrder(int ord) {
    this.order = ord;
  }

  public int getOrder() {
    return order;
  }

}
