/**
 * 
 */
package cern.c2mon.notification.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.notification.Mailer;
import cern.c2mon.notification.SubscriptionRegistry;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.TagCacheUpdateListener;
import cern.c2mon.notification.shared.Subscriber;
import cern.c2mon.notification.shared.Subscription;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.rule.RuleEvaluationException;
import cern.c2mon.shared.rule.RuleExpression;
import cern.c2mon.shared.rule.RuleFormatException;
import cern.c2mon.shared.rule.RuleValidationReport;
import cern.dmn2.core.Status;

/**
 * @author felixehm
 *
 */
public class NotifierImplTest {

    /**
     * a factor to create values for example metrics.
     */
    static Double DEFAULT_METRIC_VALUE_FACTOR = 1.0d;
    
	SubscriptionRegistry reg = null;
	IMocksControl mockControl = EasyMock.createControl();
	NotifierImpl notifier = null;
	MyCache m = null;
	
	/**
	 * 
	 */
	@BeforeClass
	public static void initLog4J() {
	    System.setProperty("log4j.configuration", NotifierImplTest.class.getResource("log4j.properties").toExternalForm());
	    System.out.println(System.getProperty("log4j.configuration"));
	}
	
	
	/**
	 * 
	 * @throws Exception in case of an error.
	 */
	@Before
	public void reset() throws Exception {
	    reg = new SubscriptionRegistryTest.RegWithoutDb();
	    notifier = new NotifierImpl();
	    notifier.setSubscriptionRegistry(reg);
	    
	    m = new MyCache();
	    
        notifier.setTagCache(m);
        m.setRegistry(reg);
        m.setNotifier(notifier);
        reg.setTagCache(m);
        
        System.out.println(Status.UNKNOWN + " " + Status.UNKNOWN.toInt());
        System.out.println(Status.OK + " " + Status.OK.toInt());
        System.out.println(Status.WARNING + " " + Status.WARNING.toInt());
        System.out.println(Status.ERROR + " " + Status.ERROR.toInt());
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
	    /**
	     * 
	     * @param listener a {@link TagCacheUpdateListener}.
	     * @throws RuleFormatException in case of an error
	     */
	    public MyCache() throws RuleFormatException {
	        resolveSubTags(1L);
	    }
	    
	    @Override
	    public Tag resolveSubTags(Long l) {
	        Tag parent = super.resolveSubTags(l);
	        
	        // init with Fake CDTV
            System.out.println("Initializing cache with initial ClientDataTagValues");
	        for (Tag t : cache.values()) {
	            if (cache.get(t.getId()).getLatestUpdate() != null) continue;
	            
                if (t.isRule()) {
                    try {
                        t.update(getClientDataTagFakeUpdateFor(t, new Double(Status.OK.toInt())));
                    } catch (RuleFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        t.update(getClientDataTagFakeUpdateFor(t, DEFAULT_METRIC_VALUE_FACTOR * t.getId()));
                    } catch (RuleFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
	        return parent;
	    }
	    
	    	    
	    @Override
	    public Collection<ClientDataTagValue> getLatestFromServer(HashSet<Long> toSubscribeTo) {
	        ArrayList<ClientDataTagValue> result = new ArrayList<ClientDataTagValue>();
	        for (Long t : toSubscribeTo) {
	            result.add(cache.get(t).getLatestUpdate());
	        }
	        return result;
	    }
	    @Override
	    public void cancelSubscriptionFor(HashSet<Long> toCancel) {
	        // IGNORE
	    }
	    
	    @Override
	    protected void startSubscriptionFor(HashSet<Long> l) {
	        // IGNORE
	    }
	    @Override
	    protected DataTagUpdateListener startSubscriptionWithoutNotification(HashSet<Long> list) {
	        return null;
        }
	    
	    @Override
	    void unsubscribeFirstUpdateListener(DataTagUpdateListener initialReportTagListener) {
	        
	    }
	    
	    @Override
        public void cancelSubscription(Subscription subscription) {
            if (cache.containsKey(subscription.getTagId())) {
                cache.get(subscription.getTagId()).getSubscribers().remove(subscription);
            }
            if (cache.get(subscription.getTagId()).getSubscribers().size() == 0) {
                cache.remove(subscription.getTagId());
            }
        }
	    
	    @Override
	    protected Tag metricTagResolver2(Long tagID, Map<Long, Tag> overallList) {

	        /*
	         * complex rule with two sub rule and their children.
	         *   root rule 1
	         *      child rule 2 
             *         one metric 3
	         */
	        Tag rule1_0 = new Tag(1L, true);
	        Tag rule1_2 = new Tag(2L, true);
	        Tag metric1_1 = new Tag(3L, false);
	        Tag rule1_3 = new Tag(10L, true);
	        Tag metric1_2 = new Tag(11L, false);
	        
	        rule1_0.addChildTag(rule1_2);
	        rule1_0.addChildTag(rule1_3);
	        rule1_2.addChildTag(metric1_1);
	        rule1_3.addChildTag(metric1_2);
	        
	        overallList.put(rule1_0.getId(), rule1_0);
	        overallList.put(rule1_2.getId(), rule1_2);
	        overallList.put(rule1_3.getId(), rule1_3);
	        overallList.put(metric1_1.getId(), metric1_1);
	        overallList.put(metric1_2.getId(), metric1_2);
	        
	        
	        
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
	        
	        return overallList.get(tagID);
	    }
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	@Test
	public void test() throws IOException {
		NotifierImpl notifier = new NotifierImpl();
		notifier.setSubscriptionRegistry(reg);
	}
	
	/**
	 * helper to create a RuleExpression quickly.
	 * @param inRule the tags belonging to the rule.\
	 * @return a RuleExpression object
	 */
	private RuleExpression getRuleExpression(final Long [] inRule) {
	    /*
         * tagid=1 has one sub-rule 2 
         */
	    StringBuilder b = new StringBuilder();
	    for (Long l : inRule) {
	        b.append("#" + l + " & ");
	    }
	    b.append("true[0]");
	    
      RuleExpression rExpression = new RuleExpression(b.toString()) {
            private static final long serialVersionUID = 7424951105237067129L;

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

            @Override
            public RuleValidationReport validate(Map<Long, Object> pInputParams) {
                return null;
            }

            @Override
            public Object forceEvaluate(Map<Long, Object> pInputParams) {
              // TODO Auto-generated method stub
              return null;
            }
        };
        return rExpression;
	}
	
	
	
	/**
	 * 
	 * @throws Exception in case of an error
	 */
	@Test
	public void testRuleRuleMetric() throws Exception {

	    startTest("testRuleRuleMetric");

        /*
         * we expect to have a mail notification send for our test at the end of the last update() call.
         */
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(2);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
	    
	    
		/*
		 * create a subscription to tagid=1
		 */
	    Tag toBeNotifiedFor = new Tag(1L, true);
		Subscriber s = new Subscriber("test", "test@cern.ch", "");
		s.addSubscription(new Subscription(s.getUserName(), toBeNotifiedFor.getId()));
		reg.setSubscriber(s);
		
		printToSubmitNicely(toBeNotifiedFor.getId());
		
		
	    /*
	     * Metric update 3
	     */
        sendUpdateMetricTag(3L, 3d);
	    
	    /*
	     * Rule update 2
	     */
	    sendUpdateRuleTag(2L, Status.WARNING.toInt());
	    sendUpdateRuleTag(1L, Status.WARNING.toInt());
	    notifier.checkCacheForChanges();
        
	    Subscriber fromReg = reg.getSubscriber(s.getUserName());
	    Status rs = fromReg.getSubscriptions().get(1L).getLastStatusForResolvedSubTag(10L);
	    assertTrue("Status of rule is not OK, but " + rs , rs.equals(Status.OK));
	    
	    rs = fromReg.getSubscriptions().get(1L).getLastStatusForResolvedSubTag(2L);
        assertTrue("Status of rule is not WARNING, but " + rs , rs.equals(Status.WARNING));

        /*
         * Tag 1 => WARNING
         */
        rs = fromReg.getSubscriptions().get(1L).getLastNotifiedStatus();
        assertTrue("Status of rule is not WARNING, but " + rs , rs.equals(Status.WARNING));

        
        /*
         * Rule update 10
         */
        sendUpdateRuleTag(10L, Status.WARNING.toInt());
        notifier.checkCacheForChanges();
        
        fromReg = reg.getSubscriber(s.getUserName());
        
        rs = fromReg.getSubscriptions().get(1L).getLastStatusForResolvedSubTag(2L);
        assertTrue("Status for Tag is " + rs, rs.equals(Status.WARNING));
        
        rs = fromReg.getSubscriptions().get(1L).getLastStatusForResolvedSubTag(10L);
        assertTrue("Status for Tag is " + rs, rs.equals(Status.WARNING));

        rs = fromReg.getSubscriptions().get(1L).getLastNotifiedStatus();
        assertTrue("Status for Tag is " + rs, rs.equals(Status.WARNING));   

        
        EasyMock.verify(mailer);       
	}

	/**
	 * 
     * @throws Exception in case of an error
	 */
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
        
        printToSubmitNicely(toBeNotifiedFor.getId());  
        
        /*
         * we expect to have a mail notification send for our test here.
         */
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
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
        sendUpdateRuleTag(7L, Status.WARNING.toInt());
        notifier.checkCacheForChanges();
        
        EasyMock.verify(mailer);    
	}
	
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTwoSubscriptionsOneEnabled() throws Exception {
	    startTest("testTwoSubscriptionsOneRuleOneMetric");
        
	    /*
         * we expect to have a mail notification send for our test here.
         */
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
	    
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
        
        printToSubmitNicely(toBeNotifiedFor.getId());  
        
        /*
         * here, we send off 
         */
        sendUpdateMetricTag(8L, 5.5);
        sendUpdateRuleTag(7L, Status.WARNING.toInt());
        notifier.checkCacheForChanges();
        
        
        EasyMock.verify(mailer);    
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
        
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
        
        reg.setSubscriber(s);
        
        printToSubmitNicely(toBeNotifiedFor.getId());
        
        /*
         * let's do testing
         */
        sendUpdateRuleTag(200L, Status.WARNING.toInt());
        sendUpdateRuleTag(2000L, Status.WARNING.toInt());
        notifier.checkCacheForChanges();
        
        EasyMock.verify(mailer);
	}
	
	
	
	@Test
	public void testMetricChangeNotification() throws Exception {
	    startTest("testMetricChangeNotification");
	    
	    
	    Tag toBeNotifiedFor = new Tag(2L, true);
	    Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        sub.setEnabled(true);
        sub.setNotifyOnMetricChange(true);
        s.addSubscription(sub);
        
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(3);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
        
        reg.setSubscriber(s);
        
        // we put the rule in warning and then submit changed values and expect notifications
        sendUpdateRuleTag(2L, Status.ERROR.toInt());
        notifier.checkCacheForChanges();
        
        sendUpdateMetricTag(3L, 5.5d);
        notifier.checkCacheForChanges();
        
        sendUpdateMetricTag(3L, 10.5d);
        notifier.checkCacheForChanges();
        
        EasyMock.verify(mailer);        
	}
	
	
	
	@Test
	public void testSendingIntialReport() throws Exception {
	    startTest("testSendingIntialReport");
	    
	    Tag toBeNotifiedFor = new Tag(2L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        s.addSubscription(sub);
        
        
        // we need a second subscriber to the same as we cannot submit otherwise an ERROR
        Subscriber s2 = new Subscriber("test2", "test@cern.ch", "");
        Subscription sub2 = new Subscription(s2.getUserName(), toBeNotifiedFor.getId());
        s2.addSubscription(sub2);
        
        reg.setSubscriber(s2.getCopy());
        sendUpdateRuleTag(toBeNotifiedFor.getId(), Status.ERROR.toInt());
        notifier.checkCacheForChanges();

        /*
         * setup done, now we should have the Tag 2 = ERROR.
         * let's start a new subscription and see if we get a mail.
         */
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
        
        reg.setSubscriber(s.getCopy());
        /*
         * mail ? 
         */
        EasyMock.verify(mailer);
        assertTrue(reg.getSubscriber(s.getUserName()).getSubscription(sub.getTagId()).getLastNotifiedStatus().equals(Status.ERROR));
	}
	
	
	
	@Test
	public void testAddSecondSubscriberAndCheckInitialReport() throws Exception {
	    Tag toBeNotifiedFor = new Tag(1L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        s.addSubscription(sub);
        
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(2);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
        
        reg.setSubscriber(s.getCopy());
        
        sendUpdateRuleTag(2L, Status.ERROR.toInt());
        sendUpdateRuleTag(1L, Status.ERROR.toInt());
        sendUpdateRuleTag(10L, Status.WARNING.toInt());
        notifier.checkCacheForChanges();
        
        
        Subscriber s2 = new Subscriber("test2", "test@cern.ch", "");
        s2.addSubscription(new Subscription(s2.getUserName(), toBeNotifiedFor.getId()));
        reg.setSubscriber(s2.getCopy());
        
        s = reg.getSubscriber(s2.getUserName());
        System.out.println(s);
        assertTrue(s.getSubscription(toBeNotifiedFor.getId()).getLastStatusForResolvedSubTag(2L).equals(Status.ERROR));
        assertTrue(s.getSubscription(toBeNotifiedFor.getId()).getLastStatusForResolvedSubTag(1L).equals(Status.ERROR));
        assertTrue(s.getSubscription(toBeNotifiedFor.getId()).getLastStatusForResolvedSubTag(10L).equals(Status.WARNING));
        
        EasyMock.verify(mailer);        
	}
	
	
	
	@Test
	public void testNotificationOnRuleRecovery() throws Exception {
	    startTest("testNotificationOnRuleRecovery");
        
        /*
         * we expect to have a mail notification send for our test here.
         */
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(2);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
        
        /*
         * enabled
         */
        Tag toBeNotifiedFor = new Tag(7L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        s.addSubscription(sub);
        reg.setSubscriber(s.getCopy());
        
        sendUpdateRuleTag(toBeNotifiedFor.getId(), Status.WARNING.toInt());
        notifier.checkCacheForChanges();
        s = reg.getSubscriber(s.getUserName());
        System.out.println(s);
        assertTrue(s.getSubscription(toBeNotifiedFor.getId()).getLastNotifiedStatus().equals(Status.WARNING));
        
        sendUpdateRuleTag(toBeNotifiedFor.getId(), Status.OK.toInt());
        notifier.checkCacheForChanges();
        
        s = reg.getSubscriber(s.getUserName());
        System.out.println(s);
        assertTrue(s.getSubscription(toBeNotifiedFor.getId()).getLastNotifiedStatus().equals(Status.OK));
        EasyMock.verify(mailer);
	}
	
	@Test
	public void testNotificationOnChildRuleRecovery() throws Exception {
	    startTest("testNotificationOnChildRuleRecovery");
	    
	    Tag toBeNotifiedFor = new Tag(1L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        s.addSubscription(sub);
        
        Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(2);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
        
        reg.setSubscriber(s.getCopy());
        sendUpdateRuleTag(2L, Status.ERROR.toInt());
        sendUpdateRuleTag(1L, Status.ERROR.toInt());
        sendUpdateRuleTag(10L, Status.ERROR.toInt());
        notifier.checkCacheForChanges();
        
        sendUpdateRuleTag(2L, Status.OK.toInt());
        notifier.checkCacheForChanges();
        
        s = reg.getSubscriber(s.getUserName());
        System.out.println(s);
        assertTrue(s.getSubscription(toBeNotifiedFor.getId()).getLastStatusForResolvedSubTag(2L).equals(Status.OK));
        EasyMock.verify(mailer);    
	}
	
	
	@Test
	public void testNotificationWithNonAccessibleTag() throws Exception {
	    startTest("testNotificationWithNonAccessibleTag");
	    
	    Tag toBeNotifiedFor = new Tag(1L, true);
        Subscriber s = new Subscriber("test", "test@cern.ch", "");
        Subscription sub = new Subscription(s.getUserName(), toBeNotifiedFor.getId());
        s.addSubscription(sub);
	    
	    Mailer mailer = mockControl.createMock(Mailer.class);
        mailer.sendEmail(EasyMock.isA(String.class), EasyMock.isA(String.class), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);
        notifier.setMailer(mailer);
        EasyMock.replay(mailer);
	    
        reg.setSubscriber(s.getCopy());
	    
	    ClientDataTagValue c = getClientDataTagFakeUpdateFor(new Tag(3L, false), null);
	    c.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE);
	    m.onUpdate(c);
	    c = getClientDataTagFakeUpdateFor(new Tag(10L, true), null);
        c.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE);
        m.onUpdate(c);
        c = getClientDataTagFakeUpdateFor(new Tag(11L, false), null);
        c.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE);
        m.onUpdate(c);
        c = getClientDataTagFakeUpdateFor(new Tag(2L, true), null);
        c.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE);
        m.onUpdate(c);
        c = getClientDataTagFakeUpdateFor(new Tag(1L, true), null);
        c.getDataTagQuality().addInvalidStatus(TagQualityStatus.INACCESSIBLE);
        m.onUpdate(c);
        
        notifier.checkCacheForChanges();
        
        EasyMock.verify(mailer);    
	}
	
	
	// --- HELPERS
	
	
	
	/**
     * sends a rule update
     * @param id the id of the metric
     * @param ruleInputTag the input tags of the pseudo rule
     * @param status the new status value
     * @throws Exception in case the passed id is not a metric
     */
	private void sendUpdateRuleTag(long id, int status) throws Exception {
	    ClientDataTagValue ruleUpdate = getClientDataTagFakeUpdateFor(m.get(id), new Double(status));
        m.onUpdate(ruleUpdate);
	}
	
	/**
	 * sends a metric update
	 * @param id the id of the metric
	 * @param value the value of the metric
	 * @throws Exception in case the passed id is not a metric
	 */
	private void sendUpdateMetricTag(long id, double value) throws Exception {
	    ClientDataTagValue metricUpdate = getClientDataTagFakeUpdateFor(m.get(id), value);
        m.onUpdate(metricUpdate);
	}
	
	
	ClientDataTagValue getClientDataTagFakeUpdateFor(Tag tag, Double value) throws RuleFormatException {
	    ClientDataTagImpl result = null;
	    Long id = tag.getId();
	    if (tag.isRule()) {
	        result = new ClientDataTagImpl(id);
	        DataTagQualityImpl q = new DataTagQualityImpl();
	        q.removeInvalidStatus(TagQualityStatus.UNINITIALISED);
	        TransferTagImpl t = new TransferTagImpl(id, value , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "rule tag description", "RuleTag-" + id, null);
	        HashSet<Long> inputTags = new HashSet<Long>();
	        for (Tag c : tag.getChildTags()) {
	            inputTags.add(c.getId());
	        }
	        
	        t.setRuleExpression(getRuleExpression(inputTags.toArray(new Long [] {})));
	        result.update(t);
	    } else {
	        result = new ClientDataTagImpl(id);
	        DataTagQualityImpl q = new DataTagQualityImpl();
	        q.removeInvalidStatus(TagQualityStatus.UNINITIALISED);
	        TransferTagImpl t = new TransferTagImpl(id, value , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "metric tag description", "MetricTag-" + id, null);
	        result.update(t);  
	    }
	    return result;
	}
	
	
	/**
	 * Helper for nicer printout.
	 * 
	 * @param toBeNotified
	 * @throws Exception 
	 */
	private void printToSubmitNicely(Long toBeNotified) throws Exception {
	    /*
         * for friendly output
         */
        Tag t = m.get(toBeNotified);
        if (t == null) { 
            throw new Exception("Tag " + t + " was not resolved correctly. Please check the MyCache in the Test");
        }
        System.out.println("I trying to submit a notification for  : " + t);
        
        
        
	}
	
	/**
	 * 
	 * @param name the test name
	 */
	private void startTest(String name) {
	    System.out.println("------------------------------------------------------");
	    System.out.println("|             " + name);
	    System.out.println("------------------------------------------------------");
	}
	
	
}
