package cern.c2mon.server.cache.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.Cache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.command.SourceCommandTag;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Service
public class CommandTagService {

  private Cache<Long, CommandTag> commandTagCacheRef;

  @Autowired
  public CommandTagService(Cache<Long, CommandTag> commandTagCacheRef) {
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
