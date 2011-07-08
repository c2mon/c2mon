package cern.c2mon.client.jms;

import java.util.Collection;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TransferTag;
import cern.tim.shared.client.command.CommandTagHandle;

public interface RequestHandler {

  Collection<TransferTag> requestTags(Collection<Long> tagIds);
  
  Collection<SupervisionEvent> getCurrentSupervisionStatus();
  
  Collection<CommandTagHandle> getCommandTagHandles(Collection<Long> commandIds);
  
}
