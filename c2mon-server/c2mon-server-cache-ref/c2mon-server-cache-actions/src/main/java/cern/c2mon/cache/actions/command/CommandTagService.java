package cern.c2mon.cache.actions.command;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.cache.api.flow.DefaultC2monCacheFlow;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.command.SourceCommandTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class CommandTagService extends AbstractCacheServiceImpl<CommandTag> {

  @Inject
  public CommandTagService(C2monCache<CommandTag> commandTagCacheRef) {
    super(commandTagCacheRef, new DefaultC2monCacheFlow<>());
  }

  /**
   * Generates the XML needed to send to the DAQ at start-up.
   */
  public String getConfigXML(Long id) {
    String returnValue = "";
    try {
      CommandTag commandTag = cache.get(id);
      returnValue = generateSourceXML(commandTag); //old version: SourceDataTag.toConfigXML(tag);
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Failed to locate command tag with id " + id + " in the cache (returning empty String config).");
    }
    return returnValue;
  }

  public SourceCommandTag generateSourceCommandTag(CommandTag commandTag) {
    return new SourceCommandTag(commandTag.getId(),
      commandTag.getName(),
      commandTag.getSourceTimeout(),
      commandTag.getSourceRetries(),
      commandTag.getHardwareAddress());
  }

  private String generateSourceXML(CommandTag cmd) {
    return "    <CommandTag id=\"" + cmd.getId() + "\" name=\"" + cmd.getName() + "\">\n"
      + "      <source-timeout>" + cmd.getSourceTimeout() + "</source-timeout>\n"
      + "      <source-retries>" + cmd.getSourceRetries() + "</source-retries>\n"
      + (cmd.getHardwareAddress() != null ? cmd.getHardwareAddress().toConfigXML() : "")
      + "    </CommandTag>\n";
  }
}
