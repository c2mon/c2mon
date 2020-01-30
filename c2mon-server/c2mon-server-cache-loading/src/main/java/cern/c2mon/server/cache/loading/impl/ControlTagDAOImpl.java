package cern.c2mon.server.cache.loading.impl;

import cern.c2mon.server.cache.dbaccess.DataTagMapper;
import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import lombok.NonNull;

import static cern.c2mon.server.common.util.KotlinAPIs.apply;

public abstract class ControlTagDAOImpl<CONTROL extends ControlTag> extends AbstractDefaultLoaderDAO<CONTROL> implements ConfigurableDAO<CONTROL> {

  private final DataTagMapper dataTagMapper;

  /**
   * Constructor.
   *
   * @param initialBufferSize size of buffer for storing the objects
   * @param loaderMapper      required mapper for loading from the DB
   */
  public ControlTagDAOImpl(int initialBufferSize, LoaderMapper<CONTROL> loaderMapper, final DataTagMapper dataTagMapper) {
    super(initialBufferSize, loaderMapper);
    this.dataTagMapper = dataTagMapper;
  }

  @Override
  protected CONTROL doPostDbLoading(CONTROL item) {
    // TODO (Alex) Figure out defaults for this, but probably do nothing?
    return item;
  }

  @Override
  public void deleteItem(Long id) {
    dataTagMapper.deleteDataTag(id);
    // TODO (Alex) Should we be deleting from somewhere else too?
  }

  @Override
  public void updateConfig(CONTROL cacheable) {
    dataTagMapper.updateConfig(convertToDataTag(cacheable));
  }

  @Override
  public void insert(@NonNull CONTROL cacheable) {
    dataTagMapper.insertControlTag(convertToDataTag(cacheable));
  }

  protected DataTag convertToDataTag(CONTROL control) {
    return apply(new DataTagCacheObject(control.getId()), dataTag -> {

      dataTag.setSourceTimestamp(control.getSourceTimestamp());
      dataTag.setCacheTimestamp(control.getCacheTimestamp());
      dataTag.setName(control.getName());
      dataTag.setDescription(control.getDescription());
      dataTag.setDataType(control.getDataType());
      dataTag.setMode(control.getMode());
      dataTag.setValue(control.getValue());
      dataTag.setValueDescription(control.getValueDescription());
      dataTag.setDataTagQuality(control.getDataTagQuality());
      dataTag.setUnit(control.getUnit());
      dataTag.setMetadata(control.getMetadata());
      dataTag.setRuleIds(control.getRuleIds());
      dataTag.setAlarmIds(control.getAlarmIds());
      dataTag.setDipAddress(control.getDipAddress());
      dataTag.setAddress(control.getAddress());
    });
  }
}
