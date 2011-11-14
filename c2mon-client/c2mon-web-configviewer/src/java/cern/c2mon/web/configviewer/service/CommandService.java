package cern.c2mon.web.configviewer.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.core.tag.ClientCommandTagImpl;

/**
 * Command service providing the XML representation of a given alarm
 * */
@Service
public class CommandService {

    /**
     * CommandService logger
     * */
    private static Logger logger = Logger.getLogger(CommandService.class);
    
    /**
     * Gateway to C2monService 
     * */
    @Autowired
    private ServiceGateway gateway;
    
    /**
     * Gets the XML representation of the configuration of a command
     * @param commandId id of the command
     * @return XML representation of command configuration 
     * @throws Exception if command was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
     * thrown by the underlying service gateway.
     * */
    public String getCommandTagXml(final String commandId) throws Exception {
        try {
            ClientCommandTagImpl<Object> command = (ClientCommandTagImpl<Object>) getCommandTag(Long.parseLong(commandId));
            if (command.isExistingCommand()) 
                //TODO: return command.getXml();
                return "commandXml";
            else
                throw new TagIdException("No command found");
        } catch (NumberFormatException e) {
            throw new TagIdException("Invalid command id");
        }
    }
    
    /**
     * Retrieves a command tag object from the service gateway tagManager
     * @param commandId id of the alarm
     * @return command tag
     * */
    private ClientCommandTag<Object> getCommandTag(final long commandId) {
        ClientCommandTag<Object> ct = gateway.getCommandManager().getCommandTag(commandId); 
            //commandManager.getCommandTag(commandId);
        logger.debug("Command fetch for command " + commandId + ": " + (ct == null ? "NULL" : "SUCCESS"));
        return ct;
    }
}
