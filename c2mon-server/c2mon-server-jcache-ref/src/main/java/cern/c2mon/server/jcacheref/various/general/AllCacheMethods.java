package cern.c2mon.server.jcacheref.various.general;

import java.util.Collection;
import java.util.List;

/**
 * @author Szymon Halastra
 */
public interface AllCacheMethods<K, V> {

  Collection<V> findAlarm(/*AlarmQuery query*/); // used in AlarmCache

  long getTagId(String name); //Used in CommandTagCache

  boolean hasTagWithName(String name);

  V get(String name);

  Collection<V> findNameWildcard(String regex);

  List<V> getTagIdsByEquipmentId(long id);

  List<V> getTagIdsBySubequipmentId(long id);

  List<V> getByDeviceClassClassId(long id);

  long getDeviceClassIdByName(String name);

  void updateDeviceIds(long id);

  V getCopy();

  long getProcessId(String name);

  Integer getNumTags(long ProcessId);

  Integer getNumInvalidTags(long processId);

  void setParentSupervisionIds(V tag);
}
