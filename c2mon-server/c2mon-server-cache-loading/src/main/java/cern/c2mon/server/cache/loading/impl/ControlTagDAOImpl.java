package cern.c2mon.server.cache.loading.impl;

import cern.c2mon.server.cache.dbaccess.LoaderMapper;
import cern.c2mon.server.cache.loading.ConfigurableDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.control.ControlTag;

public abstract class ControlTagDAOImpl<CONTROL extends ControlTag> extends AbstractDefaultLoaderDAO<CONTROL> implements ConfigurableDAO<CONTROL> {

  /**
   * Constructor.
   *
   * @param initialBufferSize size of buffer for storing the objects
   * @param loaderMapper      required mapper for loading from the DB
   */
  public ControlTagDAOImpl(int initialBufferSize, LoaderMapper<CONTROL> loaderMapper) {
    super(initialBufferSize, loaderMapper);
  }

  @Override
  protected CONTROL doPostDbLoading(CONTROL item) {
    // TODO (Alex) Figure out defaults for this, but probably do nothing?
    return item;
  }

  @Override
  public void deleteItem(Long id) {
    // TODO (Alex) Figure out defaults for this
  }

  @Override
  public void updateConfig(CONTROL cacheable) {
    // TODO (Alex) Figure out defaults for this
  }

  @Override
  public void insert(CONTROL cacheable) {
    // TODO (Alex) Figure out defaults for this
  }
}
