package cern.c2mon.server.jcacheref.various.general;

import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */
public interface CommonCache<K, V extends Tag> {

  Long getIdByName(String name);

  String getCacheName();

  void setCacheInitializedKey();
}
