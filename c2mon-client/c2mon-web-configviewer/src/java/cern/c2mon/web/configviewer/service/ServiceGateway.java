package cern.c2mon.web.configviewer.service;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;

/**
 * Service gateway wrapper for C2monServiceGateway
 * */
@Service
public class ServiceGateway {

    /**
     * ServiceGateway logger
     * */
    private static Logger logger = Logger.getLogger(TagService.class);
    
    /**
     * Tag manager
     * */
    private C2monTagManager tagManager;
    
    /**
     * Command manager
     * */
    private C2monCommandManager commandManager;

    /**
     * Initializes the tagManager and commandManager
     * */
    @PostConstruct
    public void init() {
        startC2monServiceGateway();
        tagManager = C2monServiceGateway.getTagManager();
        logger.info("TagManager instance: " + (tagManager == null ? "NULL" : "OK"));
        commandManager = C2monServiceGateway.getCommandManager();
        logger.info("CommandManager instance: " + (commandManager == null ? "NULL" : "OK"));
    }
    
    /**
     * Starts the C2monServiceGateway. Sleeps for 1 second to let the gateway start. Sleeping is a workaround, as the C2monServiceGateway.startC2monClient() method does not inform
     * if the start was completed.
     * */
    private void startC2monServiceGateway() {
        logger.info("Starting C2MON Service Gateway...");
        C2monServiceGateway.startC2monClient();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            logger.error("Wait for start of C2MON Service Gateway interrupted.", e);
        }
    }
    
    /**
     * Getter for tagManager
     * @return tag manager instance
     * */
    public C2monTagManager getTagManager(){
        return this.tagManager;
    }
    
    /**
     * Getter for commandManager
     * @return command manager instance
     * */
    public C2monCommandManager getCommandManager() {
        return this.commandManager;
    }
    
}
