package cern.c2mon.cache.actions.command;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
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
public class CommandTagService {

  private C2monCache<CommandTag> commandTagCacheRef;

  @Inject
  public CommandTagService(C2monCache<CommandTag> commandTagCacheRef) {
    this.commandTagCacheRef = commandTagCacheRef;
  }

  /**
   * Generates the XML needed to send to the DAQ at start-up.
   *
   * @param id
   *
   * @return
   */
  public String getConfigXML(Long id) {
    String returnValue = "";
    try {
      CommandTag commandTag = commandTagCacheRef.get(id);
      returnValue = generateSourceXML(commandTag); //old version: SourceDataTag.toConfigXML(tag);
    }
    catch (CacheElementNotFoundException cacheEx) {
      log.error("Failed to locate command tag with id " + id + " in the cache (returning empty String config).");
    }
    return returnValue;
  }

  public SourceCommandTag generateSourceCommandTag(CommandTag commandTag) {
    SourceCommandTag sourceCommandTag = new SourceCommandTag(commandTag.getId(),
            commandTag.getName(),
            commandTag.getSourceTimeout(),
            commandTag.getSourceRetries(),
            commandTag.getHardwareAddress());
    return sourceCommandTag;
  }

  /**
   *
   * @param cmd
   *
   * @return
   */
  private String generateSourceXML(CommandTag cmd) {
    StringBuffer str = new StringBuffer("    <CommandTag id=\"");

    str.append(cmd.getId());
    str.append("\" name=\"");
    str.append(cmd.getName());
    str.append("\">\n");

    str.append("      <source-timeout>");
    str.append(cmd.getSourceTimeout());
    str.append("</source-timeout>\n");

    str.append("      <source-retries>");
    str.append(cmd.getSourceRetries());
    str.append("</source-retries>\n");

    if (cmd.getHardwareAddress() != null) {
      str.append(cmd.getHardwareAddress().toConfigXML());
    }

    str.append("    </CommandTag>\n");
    return str.toString();
  }
}
