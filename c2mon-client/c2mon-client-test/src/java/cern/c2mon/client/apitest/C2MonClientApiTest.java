package cern.c2mon.client.apitest;

import static java.lang.System.out;
import static java.lang.String.format;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cern.c2mon.client.apitest.service.C2MonClientApiTestService;
import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;

public class C2MonClientApiTest {

    private static Logger log = Logger.getLogger(C2MonClientApiTest.class);

    /**
     * Log4j Logger for logging DataTag values.
     */
    protected static final Logger TAG_LOG = Logger.getLogger("ClientDataTagValueLogger");

    static C2MonClientApiTestService service;

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {

            ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(
                    new String[] { "classpath:application-context.xml" });
                        
            service = xmlContext.getBean(C2MonClientApiTestService.class);            
            
            log.debug("jms.properties: "+System.getProperty("jms.properties"));
          
            //C2monServiceGateway.startC2monClient(System.getProperty("jms.properties"));
            
            Properties jmsProperties = (Properties) xmlContext.getBean("jmsProperties");
            for (Map.Entry<Object, Object> entry : jmsProperties.entrySet()) {
              System.setProperty((String) entry.getKey(), (String) entry.getValue());
            }   
            
            C2monServiceGateway.startC2monClient();

        } catch (Exception e) {
            log.error(e);
            System.exit(-1);
        }

        List<MetricDef> metrics = service.getProcessMetrics("P_CLIC_TEST");
        
        Set<Long> tagIds = new HashSet<Long>();
        for (MetricDef md : metrics) {
        	
        	out.println(format("Metric %d %s", md.getMetricTagId(), md.getName()));
            tagIds.add(md.getMetricTagId());
        }

        C2monTagManager tagManager = C2monServiceGateway.getTagManager();
        log.info(format("trying to subscribe to %s metrics",tagIds.size()));
        tagManager.subscribeDataTags(tagIds, new DataTagUpdateListener() {
            @Override
            public void onUpdate(ClientDataTagValue tagUpdate) {               
                if (TAG_LOG.isInfoEnabled()) {
                    TAG_LOG.log(Level.INFO, tagUpdate);
                }              
            }
        });

    }
}
