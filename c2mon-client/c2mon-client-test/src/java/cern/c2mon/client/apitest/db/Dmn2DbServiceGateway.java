package cern.c2mon.client.apitest.db;

import org.apache.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
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
                          
            final ClassPathXmlApplicationContext xmlContext = 
                new ClassPathXmlApplicationContext("classpath:application-context.xml"); 
                    
            theInstance = new Dmn2DbServiceGateway(xmlContext.getBean(C2MonClientApiTestService.class));
        }
                
    }
    
    public static void init(final String dbPropertiesURI) {
        if (theInstance == null) {

            LOGGER.info("getDbAccessService(), loading properties from " + dbPropertiesURI);

            GenericBeanDefinition propertiesFactoryBean = new GenericBeanDefinition();
            propertiesFactoryBean.setBeanClass(PropertiesFactoryBean.class);
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            propertyValues.addPropertyValue("location", dbPropertiesURI);
            propertiesFactoryBean.setPropertyValues(propertyValues);

            // start an initial Spring application context and register properties bean
            GenericApplicationContext ctx = new GenericApplicationContext();
            ctx.registerBeanDefinition("c2monProperties", propertiesFactoryBean);
            ctx.refresh();

            ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(
                    new String[] { "classpath:application-context.xml" }, ctx);

            theInstance = new Dmn2DbServiceGateway(xmlContext.getBean(C2MonClientApiTestService.class));
        }        
    }

}
