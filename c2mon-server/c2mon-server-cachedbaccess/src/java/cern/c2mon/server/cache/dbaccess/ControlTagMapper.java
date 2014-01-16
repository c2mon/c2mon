package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;

public interface ControlTagMapper extends PersistenceMapper<ControlTag>, LoaderMapper<ControlTag> {
  void insertControlTag(ControlTagCacheObject controlTag);
  void deleteControlTag(Long id);
}
