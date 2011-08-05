package cern.c2mon.client.apitest;

import static java.lang.System.out;
import static java.lang.String.format;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class C2MonClientApiTest {

	private static Logger log = Logger.getLogger(C2MonClientApiTest.class);
		
	static C2MonClientApiTestService service;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		try {

			ClassPathXmlApplicationContext xmlContext = new ClassPathXmlApplicationContext(
					new String[] { "classpath:application-context.xml" });

			service = xmlContext.getBean(C2MonClientApiTestService.class);
			
			
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			System.exit(-1);
		}

		
		List<MetricDef> metrics = service.getAllDeviceRuleMetrics();
		
		for (MetricDef md : metrics ) {
			out.println(format("%d %s",md.getRuleTagId(), md.getMetricName()));
		}
	
	}

}
