/**
 * 
 */
package cern.c2mon.notification.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.notification.Mailer;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.impl.NotifierImpl;
import cern.c2mon.notification.impl.SubscriptionRegistryImpl;
import cern.c2mon.notification.impl.TagCache;
import cern.c2mon.notification.shared.Status;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.tim.shared.common.datatag.DataTagQualityImpl;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.rule.RuleEvaluationException;
import cern.tim.shared.rule.RuleExpression;
import cern.tim.shared.rule.RuleFormatException;

/**
 * @author felixehm
 *
 */
public class NotifierImplTest {

	SubscriptionRegistry reg = null;
	IMocksControl mockControl = EasyMock.createControl();
	NotifierImpl notifier = null;
	MyCache m = new MyCache();
	
	
	@BeforeClass
	public static void initLog4J() {
	    System.setProperty("log4j.configuration", NotifierImplTest.class.getResource("log4j.properties").toExternalForm());
	    System.out.println(System.getProperty("log4j.configuration"));
	}
	
	@Before
	public void reset() throws IOException {
	    reg = new SubscriptionRegistryImpl();
	    notifier = new NotifierImpl();
	    notifier.setSubscriptionRegistry(reg);
        HashSet<Long> list = new HashSet<Long>();
        list.add(1L);
        m.initAndResolve(list);
        notifier.setTagCache(m);
	}
	
	/**
	 * a helper class which provides us some rules and metrics.
	 * 
	 * Don't change this. The following tests depend on the structure.
	 * 
	 * @author ${user} 
	 * @version $Revision$, $Date$, $Author$
	 */
	private class MyCache extends TagCache {
	    @Override
	    public Tag metricTagResolver(Tag tagID, HashMap<Long, Tag> overallList) {

	        /*
	         * complex rule with sub rule and child.
	         *   root rule 1
	         *      child rule 2 
             *         one metric 3
	         */
	        Tag rule = new Tag(1L, true);
	        Tag rule2 = new Tag(2L, true);
	        Tag metric1 = new Tag(3L, false);
	        rule.addChildTag(rule2);
	        rule2.addChildTag(metric1);
	        
	        overallList.put(rule.getId(), rule);
	        overallList.put(rule2.getId(), rule2);
	        overallList.put(metric1.getId(), metric1);
	        
	        
	        
	        /*
	         * complex rule : two metrics assigned to one rule
	         *   - root rule 4
	         *      - metric 5
	         *      - metric 6 
	         *   
	         */
	        Tag rule3 = new Tag(4L, true);
	        Tag metric2 = new Tag(5L, false);
	        Tag metric3 = new Tag(6L, false);
	        
	        rule3.addChildTag(metric2);
            rule3.addChildTag(metric3);
            
            overallList.put(rule3.getId(), rule3);
            overallList.put(metric2.getId(), metric2);
            overallList.put(metric3.getId(), metric3);
	        
	        /*
	         * simple rule-metric relationship
	         * one (root-)rule 7
	         *    one metric 8
	         */
	        Tag rule4 = new Tag(7L, true);
	        Tag metric4 = new Tag(8L, false);
	        rule4.addChildTag(metric4);
	        overallList.put(rule4.getId(), rule4);
	        overallList.put(metric4.getId(), metric4);
	        
	        /*
	         * complex rule :
	         * 
	         * - rule 2000
	         *    - rule 200
	         *       - rule 20
	         *          - metric 21
	         *          - metric 22
	         *          - metric 23
	         *       - metric 210
	         *       - metric 220
	         *       - metric 230
	         *    - metric 2100
	         *    - metric 2200   
	         */
	        Tag rule5 = new Tag(2000L, true);
	        Tag rule5_1 = new Tag(200L, true);
	        Tag rule5_1_1 = new Tag(20L, true);
	        
	        Tag metric5_1 = new Tag(2100L, false);
	        Tag metric5_2 = new Tag(2200L, false);
	        Tag metric5_3 = new Tag(2300L, false);
	        
	        Tag metric5_1_1 = new Tag(210L, false);
	        Tag metric5_1_2 = new Tag(220L, false);
	        Tag metric5_1_3 = new Tag(230L, false);
	        
	        Tag metric5_1_1_1 = new Tag(21L, false);
	        Tag metric5_1_1_2 = new Tag(22L, false);
	        Tag metric5_1_1_3 = new Tag(23L, false);
	        
	        rule5_1_1.addChildTag(metric5_1_1_1);
	        rule5_1_1.addChildTag(metric5_1_1_2);
	        rule5_1_1.addChildTag(metric5_1_1_3);
	        
	        rule5_1.addChildTag(rule5_1_1);
	        rule5_1.addChildTag(metric5_1_1);
	        rule5_1.addChildTag(metric5_1_2);
	        rule5_1.addChildTag(metric5_1_3);
	        
	        rule5.addChildTag(rule5_1);
	        rule5.addChildTag(metric5_1);
	        rule5.addChildTag(metric5_2);
	        rule5.addChildTag(metric5_3);
	        
	        overallList.put(metric5_1_1_3.getId(), metric5_1_1_3);
	        overallList.put(metric5_1_1_2.getId(), metric5_1_1_2);
	        overallList.put(metric5_1_1_1.getId(), metric5_1_1_1);
	        overallList.put(rule5_1_1.getId(), rule5_1_1);
	        
	        overallList.put(metric5_1_3.getId(), metric5_1_3);
            overallList.put(metric5_1_2.getId(), metric5_1_2);
            overallList.put(metric5_1_1.getId(), metric5_1_1);
            overallList.put(rule5_1.getId(), rule5_1);
            
            overallList.put(metric5_3.getId(), metric5_3);
            overallList.put(metric5_2.getId(), metric5_2);
            overallList.put(metric5_1.getId(), metric5_1);
            overallList.put(rule5.getId(), rule5);
	        // pfff...
	        
	        
	        
	        return overallList.get(tagID.getId());
	    }
	}
	
	@Test
	public void test() throws IOException {
		NotifierImpl notifier = new NotifierImpl();
		notifier.setSubscriptionRegistry(reg);
	}
	
	/**
	 * helper to create a RuleExpression quickly.
	 * @param inRule
	 */
	private RuleExpression getRuleExpression(final Long [] inRule) {
	    /*
         * tagid=1 has one sub-rule 2 
         */
	    StringBuilder b = new StringBuilder();
	    for (Long l : inRule) {
	        b.append("#" + l + " & ");
	    }
	    b.append("true");
	    
        RuleExpression rExpression = new RuleExpression(b.toString()) {
            @Override
            public Set<Long> getInputTagIds() {
                HashSet<Long> result = new HashSet<Long>();
                for (Long l : inRule) {
                    result.add(l);
                }
                return result;
            }
            
            @Override
            public Object evaluate(Map<Long, Object> arg0) throws RuleEvaluationException {
                return null;
            }
        };
        return rExpression;
	}
	
	@Test
	public void testRuleRuleMetric() throws Exception {

	    startTest("testRuleRuleMetric");
	    
		/*
		 * create a subscription to tagid=1
		 */
	    Tag toBeNotifiedFor = new Tag(1L, true);
		Subscriber s = new Subscriber("test", "test@cern.ch", "");
		s.addSubscription(new Subscription(s.getUserName(), toBeNotifiedFor.getId()));
		reg.setSubscriber(s);
		
		printToSubmitNicely(toBeNotifiedFor);
		
		/*
         * we expect to have a mail notification send for our test at the end of the last update() call.
         */
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(s.getEmail(), "", "");
        EasyMock.expectLastCall().once();
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
		
	    /*
	     * Metric update 3
	     */
        sendUpdateMetricTag(3L, 3d);
	    
	    /*
	     * Rule update 2
	     */
	    sendUpdateRuleTag(2L, new Long[] {3L}, Status.WARNING.toInteger());
        
        /*
         * Rule update 1
         */
        sendUpdateRuleTag(1L, new Long[] {2L}, Status.WARNING.toInteger());
        EasyMock.verify(mailer);
        
	}

	
	@Test
	public void testSimpleRuleOnOneMatric() throws Exception {

	    startTest("testSimpleRuleOnOneMatric");
	    
	    /*
         * create a subscription to tagid=1
         */
        Tag toBeNotifiedFor = new Tag(7L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        s.addSubscription(new Subscription(s.getUserName(), toBeNotifiedFor.getId()));
        reg.setSubscriber(s);
        
        printToSubmitNicely(toBeNotifiedFor);  
        
        /*
         * we expect to have a mail notification send for our test here.
         */
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(s.getEmail(), "", "");
        EasyMock.expectLastCall().once();
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
        
        /*
         * Metric update 3
         */
        sendUpdateMetricTag(8L, 5d);
        
        /*
         * Rule update 2
         */
        sendUpdateRuleTag(7L, new Long [] {8L}, Status.WARNING.toInteger());
        EasyMock.verify(mailer);	    
	}
	
	
	
	@Test
	public void testTwoSubscriptionsOneRuleOneMetric() throws Exception {
	    startTest("testTwoSubscriptionsOneRuleOneMetric");
        
        /*
         * enabled
         */
        Tag toBeNotifiedFor = new Tag(7L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        sub.setEnabled(false);
        s.addSubscription(sub);
        reg.setSubscriber(s);
        
        /*
         * disabled
         */
        s = new Subscriber("test2", "test2@cern.ch", "");
        sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        sub.setEnabled(true);
        s.addSubscription(sub);
        reg.setSubscriber(s);
        
        printToSubmitNicely(toBeNotifiedFor);  
        
        /*
         * here, we send off 
         */
        sendUpdateMetricTag(8L, 5.5);
        sendUpdateRuleTag(7L, new Long [] {8L}, Status.WARNING.toInteger());
	}
	
	@Test
	public void testSendUnknownTagValues() throws Exception {
	    startTest("testTwoSubscriptionsOneRuleOneMetric");
        
        /*
         * enabled
         */
        Tag toBeNotifiedFor = new Tag(7L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        sub.setEnabled(false);
        s.addSubscription(sub);
        reg.setSubscriber(s);
        
        printToSubmitNicely(toBeNotifiedFor);  
        
        /*
         * here, we send off 
         */
        sendUpdateRuleTag(7L, new Long [] {8L}, Status.WARNING.toInteger());
	}
	
	@Test
	public void reSendNotificationForValueChange() {
	    // TODO
	 
	}
	
	
	
	@Test
    public void testRenotificationOnChildRuleChange() throws Exception {
        startTest("testRenotificationOnChildRuleChange");
        
        /*
         * enabled
         */
        Tag toBeNotifiedFor = new Tag(2000L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        sub.setEnabled(true);
        s.addSubscription(sub);
        
        reg.setSubscriber(s);
        
        printToSubmitNicely(toBeNotifiedFor);
        
        /*
         * let's have some metric's submitted
         */
        sendUpdateMetricTag(21L, 1.1);
        sendUpdateMetricTag(22L, 1.1);
        sendUpdateMetricTag(23L, 1.1);
        
        sendUpdateMetricTag(210L, 1.1);
        sendUpdateMetricTag(220L, 1.1);
        sendUpdateMetricTag(230L, 1.1);
        
        sendUpdateMetricTag(2100L, 1.1);
        sendUpdateMetricTag(2200L, 1.1);
        sendUpdateMetricTag(2300L, 1.1);
        
        /*
         * we update one child rule, then the root
         */
        sendUpdateRuleTag(20L, new Long [] {20L,21L,22L,23L}, 0);
        sendUpdateRuleTag(200L, new Long [] {20L,200L,210L,220L,230L}, 0);
        sendUpdateRuleTag(2000L, new Long [] {200L,2000L,2100L,2200L,2300L}, 0);
        
        /*
         * let's do testing
         */
        sendUpdateRuleTag(200L, new Long [] {20L,200L,210L,220L,230L}, Status.WARNING.toInteger());
        sendUpdateRuleTag(2000L, new Long [] {200L,2000L,2100L,2200L,2300L}, Status.WARNING.toInteger());
        
        
        sendUpdateMetricTag(2100L, 1.1);
        
        
	}
	
	
	
	
	@Test
	public void testMethodhasValueChanged() throws RuleFormatException {
	    ClientDataTagImpl before = new ClientDataTagImpl(1L);
        DataTagQualityImpl q = new DataTagQualityImpl();
        q.removeInvalidStatus(TagQualityStatus.UNINITIALISED);
        TransferTagImpl t = new TransferTagImpl(1L, 1.1d , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "metric tag description", "MetricTag-"+1, null);
        before.update(t);
        
        ClientDataTagImpl after = new ClientDataTagImpl(1L);
        t = new TransferTagImpl(1L, 2.1d , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "metric tag description", "MetricTag-"+1, null);
        after.update(t);
        
	    assertTrue(notifier.hasValueChanged(before, after));
	}
	
	
	
	
	// ------------------ HELPER METHODS -------------------------
	
	private void sendUpdateRuleTag(long id, Long [] ruleInputTag, double status) throws RuleFormatException {
	    ClientDataTagImpl ruleUpdate2 = new ClientDataTagImpl(id);
	    DataTagQualityImpl q = new DataTagQualityImpl();
        q.removeInvalidStatus(TagQualityStatus.UNINITIALISED);
        TransferTagImpl t = new TransferTagImpl(id, status , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "rule tag description", "RuleTag-"+id, null);
        if (m.get(id) == null || !m.get(id).isRule()) {
            throw new RuleFormatException("Tag " + id + " is not a rule!");
        }
        assert (ruleInputTag != null);
        t.setRuleExpression(getRuleExpression(ruleInputTag));
        ruleUpdate2.update(t);
        
        notifier.onUpdate(ruleUpdate2);
	}
	
	
	private void sendUpdateMetricTag(long id, double value) throws RuleFormatException {
	    ClientDataTagImpl ruleUpdate2 = new ClientDataTagImpl(id);
        DataTagQualityImpl q = new DataTagQualityImpl();
        q.removeInvalidStatus(TagQualityStatus.UNINITIALISED);
        TransferTagImpl t = new TransferTagImpl(id, value , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "metric tag description", "MetricTag-"+id, null);
        ruleUpdate2.update(t);  
        if (m.get(id) == null || m.get(id).isRule()) {
            throw new RuleFormatException("Tag " + id + " is not a metric!");
        }
        
        notifier.onUpdate(ruleUpdate2);
	}
	
	/**
	 * Helper for nicer printout.
	 * 
	 * @param toBeNotified
	 * @throws Exception 
	 */
	public void printToSubmitNicely(Tag toBeNotified) throws Exception {
	    /*
         * for friendly output
         */
        toBeNotified = m.metricTagResolver(toBeNotified, null);
        if (toBeNotified == null) { 
            throw new Exception("Tag " + toBeNotified + " was not resolved correctly. Please check the MyCache in the Test");
        }
        System.out.println("I trying to submit a notification for  : " + toBeNotified);
        
        
        
	}
	
	
	private void startTest(String name) {
	    System.out.println("------------------------------------------------------");
	    System.out.println("|             " + name);
	    System.out.println("------------------------------------------------------");
	}
	
	
}
