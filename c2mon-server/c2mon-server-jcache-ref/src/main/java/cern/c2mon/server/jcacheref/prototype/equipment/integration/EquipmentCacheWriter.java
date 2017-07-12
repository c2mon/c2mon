package cern.c2mon.server.jcacheref.prototype.equipment.integration;

import java.io.Serializable;
import java.util.Collection;

import javax.cache.Cache;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.server.cache.loading.EquipmentDAO;
import cern.c2mon.server.common.equipment.Equipment;

/**
 * @author Szymon Halastra
 */
public class EquipmentCacheWriter implements CacheWriter<Long, Equipment>, Serializable {

  @Autowired
  private EquipmentDAO equipmentDAO;

  @Override
  public void write(Cache.Entry<? extends Long, ? extends Equipment> entry) throws CacheWriterException {
    equipmentDAO.insert(entry.getValue());
  }

  @Override
  public void writeAll(Collection<Cache.Entry<? extends Long, ? extends Equipment>> entries) throws CacheWriterException {

  }

  @Override
  public void delete(Object key) throws CacheWriterException {

  }

  @Override
  public void deleteAll(Collection<?> keys) throws CacheWriterException {

  }
}
