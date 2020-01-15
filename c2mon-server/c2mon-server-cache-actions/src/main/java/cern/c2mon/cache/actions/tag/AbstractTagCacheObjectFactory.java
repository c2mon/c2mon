package cern.c2mon.cache.actions.tag;

import cern.c2mon.cache.api.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.common.util.MetadataUtils;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.PropertiesAccessor;
import cern.c2mon.shared.common.validation.MicroValidator;
import cern.c2mon.shared.daq.config.Change;

import java.util.Properties;

import static cern.c2mon.shared.common.datatag.DataTagConstants.MODE_OPERATIONAL;
import static cern.c2mon.shared.common.datatag.DataTagConstants.MODE_TEST;

public abstract class AbstractTagCacheObjectFactory<TAG extends Tag> extends AbstractCacheObjectFactory<TAG> {

  @Override
  public Change configureCacheObject(TAG cacheable, Properties properties) {
    AbstractTagCacheObject tagCacheObject = (AbstractTagCacheObject) cacheable;
    new PropertiesAccessor(properties)
      .getString("name").ifPresent(tagCacheObject::setName)
      .getString("description").ifPresent(tagCacheObject::setDescription)
      .getString("dataType").ifPresent(tagCacheObject::setDataType)
      .getShort("mode").ifPresent(tagCacheObject::setMode)
      .getString("isLogged").ifPresent(isLogged -> tagCacheObject.setLogged(isLogged.equalsIgnoreCase("true")))
      .getString("unit").ifPresent(unit -> tagCacheObject.setUnit(checkAndSetNull(unit)))
      .getString("dipAddress").ifPresent(dipAddress -> tagCacheObject.setDipAddress(checkAndSetNull(dipAddress)))
      .getString("japcAddress").ifPresent(japcAddress -> tagCacheObject.setJapcAddress(checkAndSetNull(japcAddress)));

    cern.c2mon.server.common.metadata.Metadata newMetadata = MetadataUtils.parseMetadataConfiguration(properties, tagCacheObject.getMetadata());
    tagCacheObject.setMetadata(newMetadata);

    return null;
  }

  @Override
  public void validateConfig(TAG cacheable) throws ConfigurationException {
    new MicroValidator<>(cacheable)
      .notNull(Tag::getId, "id")
      .notNull(Tag::getName, "name")
      .not(tagObj -> tagObj.getName().isEmpty(), "Parameter \"name\" cannot be empty") // This had a commented out max check as well, do we want that?
      .notNull(Tag::getDataType, "dataType")
      .between(Tag::getMode, MODE_OPERATIONAL, MODE_TEST, "mode")
      .not(tagObj -> tagObj.getUnit() != null && tagObj.getUnit().length() > 20, "Parameter \"unit\" must be 0 to 20 characters long");
  }
}
