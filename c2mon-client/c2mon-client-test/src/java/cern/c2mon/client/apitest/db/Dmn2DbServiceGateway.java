package cern.c2mon.client.apitest.db;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import cern.c2mon.client.apitest.service.C2MonClientApiTestService;


public class Dmn2DbServiceGateway {

    /** Class logger */
    private static final Logger LOGGER = Logger.getLogger(Dmn2DbServiceGateway.class);

    private static Dmn2DbServiceGateway theInstance = null;

    private C2MonClientApiTestService dbAccessService;

    /**
     * Hidden constructor
     */
    private Dmn2DbServiceGateway(C2MonClientApiTestService dbService) {
        this.dbAccessService = dbService;
    }

    public static C2MonClientApiTestService getDbAccessService() {
        if (theInstance == null) {
            throw new IllegalStateException(
                    "Dmn2DbServiceGateway is not initialized. Call init() first");
        }

        return theInstance.dbAccessService;
    }

    
    public static void init() {
        if (theInstance == null) {
            LOGGER.info("loading application context from classpath:application-context.xml");
            final ClassPathXmlApplicationContext xmlContext = 
                new ClassPathXmlApplicationContext("classpath:application-context.xml"); 
                    
            theInstance = new Dmn2DbServiceGateway(xmlContext.getBean(C2MonClientApiTestService.class));
        }
                
    }
    
}
